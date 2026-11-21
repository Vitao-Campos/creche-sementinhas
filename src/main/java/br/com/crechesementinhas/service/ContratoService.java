package br.com.crechesementinhas.service;

import br.com.crechesementinhas.dao.ContratoDAO;
import br.com.crechesementinhas.model.Contrato;
import br.com.crechesementinhas.model.DadosContrato;
import br.com.crechesementinhas.model.MatriculaResumo;
import br.com.crechesementinhas.model.ResponsavelContrato;
import br.com.crechesementinhas.util.Formatador;
import br.com.crechesementinhas.util.GeradorPdf;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/**
 * Regras de negócio do CONTRATO (Frente F).
 * O controller (tela) chama este service; o service chama o DAO.
 * Quando uma regra é desrespeitada, lançamos IllegalArgumentException
 * com a mensagem que a tela vai mostrar para a secretária.
 */
public class ContratoService {

    private static final String MODELO = "/br/com/crechesementinhas/modelos/contrato_modelo.txt";

    private final ContratoDAO dao = new ContratoDAO();

    public List<MatriculaResumo> listarMatriculasAtivas() throws SQLException {
        return dao.listarMatriculasAtivas();
    }

    public DadosContrato buscarDados(int idMatricula) throws SQLException {
        return dao.buscarDadosContrato(idMatricula);
    }

    public List<Contrato> listarContratos(int idMatricula) throws SQLException {
        return dao.listarPorMatricula(idMatricula);
    }
    
    /** RN-06: devolve a única versão VIGENTE (ou null se ainda não há contrato). */
    public Contrato buscarVigente(List<Contrato> contratos) {
        for (Contrato c : contratos) {
            if ("VIGENTE".equals(c.getStatus())) {
                return c;
            }
        }
        return null;
    }
    
    /** RF-05: sugere o plano a partir do período da turma já cadastrada. */
    public String planoPadrao(String periodoTurma) {
        if ("MANHA".equals(periodoTurma)) {
            return "Meio período - manhã";
        }
        if ("TARDE".equals(periodoTurma)) {
            return "Meio período - tarde";
        }
        return "Integral";
    }

    /** Vigência sugerida para o 1º contrato: 31/12 deste ano (ou do próximo, se já for dezembro). */
    public LocalDate vigenciaPadrao() {
        LocalDate hoje = LocalDate.now();
        int ano = hoje.getMonthValue() == 12 ? hoje.getYear() + 1 : hoje.getYear();
        return LocalDate.of(ano, 12, 31);
    }
    
    /** FA-03 (rematrícula): a nova versão vale até 31/12 do ano seguinte ao contrato atual. */
    public LocalDate vigenciaRematricula(Contrato vigente) {
        if (vigente == null) {
            return vigenciaPadrao();
        }
        return LocalDate.of(vigente.getVigenciaAte().getYear() + 1, 12, 31);
    }

    /** RN-12: o padrão da creche é vencimento dia 10 ou 25. Outro dia exige confirmação. */
    public boolean diaForaDoPadrao(int dia) {
        return dia != 10 && dia != 25;
    }
    
    /**
     * RF-08 / RN-06 / EX-04: valida e grava uma nova versão do contrato.
     * A versão anterior (se existir) passa automaticamente para ENCERRADO.
     */
    public int gerarNovaVersao(Contrato novo, DadosContrato dados) throws SQLException {
        if (dados == null) {
            throw new IllegalArgumentException("Selecione uma matrícula.");
        }
        if (!"ATIVA".equals(dados.getStatusMatricula())) {
            throw new IllegalArgumentException("Só é possível gerar contrato para matrícula ATIVA.");
        }
        // RN-01: precisa de pelo menos um responsável legal vinculado
        if (dados.getResponsaveisLegais().isEmpty()) {
            throw new IllegalArgumentException(
                    "RN-01: o aluno não tem responsável legal cadastrado. Cadastre-o antes de gerar o contrato.");
        }
        // RN-02 / RN-03: o financeiro precisa ser um responsável ligado ao aluno
        if (dados.buscarResponsavel(novo.getIdResponsavelFinanceiro()) == null) {
            throw new IllegalArgumentException("Selecione o responsável financeiro.");
        }
        if (novo.getPlano() == null || novo.getPlano().isBlank()) {
            throw new IllegalArgumentException("Selecione o plano.");
        }
        if (novo.getFormaPagamento() == null) {
            throw new IllegalArgumentException("Selecione a forma de pagamento.");
        }
        if (novo.getValorMensalidade() == null
                || novo.getValorMensalidade().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("O valor da mensalidade deve ser maior que zero.");
        }
        if (novo.getDiaVencimento() < 1 || novo.getDiaVencimento() > 31) {
            throw new IllegalArgumentException("O dia de vencimento deve estar entre 1 e 31.");
        }
        if (novo.getVigenciaAte() == null || !novo.getVigenciaAte().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("A vigência deve ser uma data futura.");
        }

        novo.setIdMatricula(dados.getIdMatricula());
        return dao.criarNovaVersao(novo);
    }
    
    /**
     * RN-05 / EX-02: registra a assinatura. Só um RESPONSÁVEL LEGAL pode assinar.
     */
    public void registrarAssinatura(Contrato contrato, ResponsavelContrato signatario,
                                    LocalDate data) throws SQLException {
        if (contrato == null) {
            throw new IllegalArgumentException("Selecione na tabela o contrato que foi assinado.");
        }
        if (!"VIGENTE".equals(contrato.getStatus())) {
            throw new IllegalArgumentException("Só o contrato VIGENTE pode ser assinado.");
        }
        if (contrato.getDataAssinatura() != null) {
            throw new IllegalArgumentException("Este contrato já foi assinado em "
                    + Formatador.data(contrato.getDataAssinatura()) + ".");
        }
        if (signatario == null) {
            throw new IllegalArgumentException("Selecione quem assinou.");
        }
        if (!signatario.isResponsavelLegal()) {
            throw new IllegalArgumentException(
                    "EX-02: " + signatario.getNome() + " não é responsável legal e não pode assinar o contrato.");
        }
        if (data == null || data.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Informe a data da assinatura (não pode ser no futuro).");
        }
        dao.registrarAssinatura(contrato.getIdContrato(), data);
        // RNF-02 (etapa 4): registrar no histórico quem assinou (signatario.getNome()).
    }
}