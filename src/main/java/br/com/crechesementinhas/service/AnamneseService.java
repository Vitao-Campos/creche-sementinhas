package br.com.crechesementinhas.service;

import br.com.crechesementinhas.dao.AlunoPessoaAutorizadaDAO;
import br.com.crechesementinhas.dao.AlunoResponsavelDAO;
import br.com.crechesementinhas.dao.FichaAnamneseDAO;
import br.com.crechesementinhas.model.AlunoPessoaAutorizada;
import br.com.crechesementinhas.model.AlunoResponsavel;
import br.com.crechesementinhas.model.CabecalhoFicha;
import br.com.crechesementinhas.model.DadosFicha;
import br.com.crechesementinhas.model.FichaAnamnese;
import br.com.crechesementinhas.model.MatriculaComFicha;
import br.com.crechesementinhas.model.Permissao;
import br.com.crechesementinhas.util.Transacao;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Ficha de anamnese (RF-05, RF-09, RN-10): uma ficha por matrícula, que pode ser atualizada
 * a qualquer tempo. Cada alteração fica no histórico (RNF-02), na mesma transação.
 */
public class AnamneseService {

    static final String ENTIDADE = "ficha_anamnese";

    private final FichaAnamneseDAO fichaDAO;
    private final AlunoResponsavelDAO vinculoDAO;
    private final AlunoPessoaAutorizadaDAO autorizadoDAO;
    private final AuditoriaService auditoria;

    /** Cria o serviço com os acessos ao banco padrão. */
    public AnamneseService() {
        this(new FichaAnamneseDAO(), new AlunoResponsavelDAO(), new AlunoPessoaAutorizadaDAO(),
                new AuditoriaService());
    }

    /** Cria o serviço com os acessos informados (usado em testes). */
    public AnamneseService(FichaAnamneseDAO fichaDAO, AlunoResponsavelDAO vinculoDAO,
                           AlunoPessoaAutorizadaDAO autorizadoDAO, AuditoriaService auditoria) {
        this.fichaDAO = fichaDAO;
        this.vinculoDAO = vinculoDAO;
        this.autorizadoDAO = autorizadoDAO;
        this.auditoria = auditoria;
    }

    /** Crianças com matrícula ativa, em ordem alfabética, com a situação da ficha. */
    public List<MatriculaComFicha> listarMatriculas(String busca) throws SQLException {
        return fichaDAO.listarMatriculasAtivas(busca);
    }

    /** Reúne o que a tela e o PDF mostram: dados da criança, respostas, responsáveis e autorizados (RF-05). */
    public DadosFicha carregar(int idMatricula) throws SQLException {
        return Transacao.executar(conexao -> {
            CabecalhoFicha cabecalho = fichaDAO.buscarCabecalho(conexao, idMatricula)
                    .orElseThrow(() -> new CadastroAnamneseException("Matrícula não encontrada."));
            FichaAnamnese ficha = fichaDAO.buscarPorMatricula(conexao, idMatricula).orElse(null);
            LocalDate hoje = LocalDate.now();
            List<AlunoResponsavel> responsaveis = vinculoDAO.listarPorAluno(cabecalho.idAluno()).stream()
                    .filter(v -> v.estaVigente(hoje)).toList();
            List<AlunoPessoaAutorizada> autorizados = autorizadoDAO.listarPorAluno(cabecalho.idAluno()).stream()
                    .filter(AlunoPessoaAutorizada::isAtivo).toList();
            return new DadosFicha(cabecalho, ficha, responsaveis, autorizados);
        });
    }

    /**
     * Grava a ficha da matrícula (cria na primeira vez, atualiza nas seguintes).
     *
     * @param justificativa motivo da atualização (opcional, vai para o histórico)
     * @throws CadastroAnamneseException dados inválidos ou matrícula que não está ativa
     */
    public FichaAnamnese salvar(FichaAnamnese ficha, String justificativa) throws SQLException {
        ControleAcesso.exigir(Permissao.CADASTRAR_MATRICULA);
        if (ficha.getIdMatricula() == null) {
            throw new CadastroAnamneseException("Escolha na lista a criança da ficha.");
        }
        AnamneseValidador.normalizar(ficha);
        List<String> erros = AnamneseValidador.validar(ficha);
        if (!erros.isEmpty()) {
            throw new CadastroAnamneseException(String.join("\n", erros));
        }
        String motivo = justificativa == null || justificativa.isBlank() ? null : justificativa.strip();

        Transacao.executar(conexao -> {
            CabecalhoFicha cabecalho = fichaDAO.buscarCabecalho(conexao, ficha.getIdMatricula())
                    .orElseThrow(() -> new CadastroAnamneseException("Matrícula não encontrada."));
            if (!"ATIVA".equals(cabecalho.status())) {
                throw new CadastroAnamneseException("A ficha só pode ser alterada em matrícula ativa.");
            }
            Optional<FichaAnamnese> existente = fichaDAO.buscarPorMatricula(conexao, ficha.getIdMatricula());
            if (existente.isEmpty()) {
                int id = fichaDAO.inserir(conexao, ficha);
                ficha.setIdFichaAnamnese(id);
                auditoria.registrarInsercao(conexao, ENTIDADE, id);
            } else {
                ficha.setIdFichaAnamnese(existente.get().getIdFichaAnamnese());
                fichaDAO.atualizar(conexao, ficha);
                registrarMudancas(conexao, existente.get(), ficha, motivo);
            }
            return null;
        });
        return ficha;
    }

    /** Registra no histórico cada campo da ficha que mudou (campo igual não grava nada). */
    private void registrarMudancas(java.sql.Connection c, FichaAnamnese a, FichaAnamnese d, String m)
            throws SQLException {
        int id = d.getIdFichaAnamnese();
        auditoria.registrarAlteracao(c, ENTIDADE, id, "autorizacao_imagem", a.isAutorizacaoImagem(), d.isAutorizacaoImagem(), m);
        auditoria.registrarAlteracao(c, ENTIDADE, id, "possui_alergia", a.isPossuiAlergia(), d.isPossuiAlergia(), m);
        auditoria.registrarAlteracao(c, ENTIDADE, id, "descricao_alergia", a.getDescricaoAlergia(), d.getDescricaoAlergia(), m);
        auditoria.registrarAlteracao(c, ENTIDADE, id, "restricoes_alimentares", a.isRestricoesAlimentares(), d.isRestricoesAlimentares(), m);
        auditoria.registrarAlteracao(c, ENTIDADE, id, "descricao_restricao", a.getDescricaoRestricao(), d.getDescricaoRestricao(), m);
        auditoria.registrarAlteracao(c, ENTIDADE, id, "receita_antitermico", a.getReceitaAntitermico(), d.getReceitaAntitermico(), m);
        auditoria.registrarAlteracao(c, ENTIDADE, id, "possui_cirurgia", a.isPossuiCirurgia(), d.isPossuiCirurgia(), m);
        auditoria.registrarAlteracao(c, ENTIDADE, id, "descricao_cirurgia", a.getDescricaoCirurgia(), d.getDescricaoCirurgia(), m);
        auditoria.registrarAlteracao(c, ENTIDADE, id, "possui_problemas_saude", a.isPossuiProblemasSaude(), d.isPossuiProblemasSaude(), m);
        auditoria.registrarAlteracao(c, ENTIDADE, id, "descricao_problemas_saude", a.getDescricaoProblemasSaude(), d.getDescricaoProblemasSaude(), m);
        auditoria.registrarAlteracao(c, ENTIDADE, id, "usa_medicacao_continua", a.isUsaMedicacaoContinua(), d.isUsaMedicacaoContinua(), m);
        auditoria.registrarAlteracao(c, ENTIDADE, id, "descricao_medicacao", a.getDescricaoMedicacao(), d.getDescricaoMedicacao(), m);
        auditoria.registrarAlteracao(c, ENTIDADE, id, "possui_fraturas", a.isPossuiFraturas(), d.isPossuiFraturas(), m);
        auditoria.registrarAlteracao(c, ENTIDADE, id, "descricao_fratura", a.getDescricaoFratura(), d.getDescricaoFratura(), m);
        auditoria.registrarAlteracao(c, ENTIDADE, id, "usa_mamadeira", a.isUsaMamadeira(), d.isUsaMamadeira(), m);
        auditoria.registrarAlteracao(c, ENTIDADE, id, "usa_formula", a.isUsaFormula(), d.isUsaFormula(), m);
        auditoria.registrarAlteracao(c, ENTIDADE, id, "descricao_formula", a.getDescricaoFormula(), d.getDescricaoFormula(), m);
        auditoria.registrarAlteracao(c, ENTIDADE, id, "usa_chupeta", a.isUsaChupeta(), d.isUsaChupeta(), m);
        auditoria.registrarAlteracao(c, ENTIDADE, id, "usa_fralda", a.isUsaFralda(), d.isUsaFralda(), m);
    }
}
