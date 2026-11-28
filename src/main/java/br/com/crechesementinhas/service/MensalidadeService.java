package br.com.crechesementinhas.service;

import br.com.crechesementinhas.dao.FaixaMensalidadeDAO;
import br.com.crechesementinhas.model.FaixaMensalidade;
import br.com.crechesementinhas.model.Permissao;
import br.com.crechesementinhas.util.Formatador;
import br.com.crechesementinhas.util.Transacao;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Mensalidade por faixa etária (RF-04 / RN-07).
 *
 * Uso pelas outras frentes (ex.: contrato):
 *   new MensalidadeService().calcular(dataNascimento, LocalDate.now())
 *       .ifPresent(faixa -> campo.setText(Formatador.dinheiro(faixa.getValor())));
 */
public class MensalidadeService {

    private final FaixaMensalidadeDAO faixaDAO = new FaixaMensalidadeDAO();
    private final AuditoriaService auditoria = new AuditoriaService();

    /** Lista as faixas de mensalidade por idade. */
    public List<FaixaMensalidade> listarFaixas() throws SQLException {
        return faixaDAO.listar();
    }

    /**
     * RN-07: faixa (e valor) da criança na data de referência.
     * Vazio se a tabela não tiver faixa para a idade.
     */
    public Optional<FaixaMensalidade> calcular(LocalDate nascimento, LocalDate referencia) throws SQLException {
        int anos = CalculadoraMensalidade.idadeEmAnosParaMensalidade(nascimento, referencia);
        return CalculadoraMensalidade.faixaPara(faixaDAO.listar(), anos);
    }

    /**
     * Reajusta o valor de uma faixa. Só Direção e Suporte (RNF-03),
     * com justificativa e auditoria (RNF-02: mensalidade é campo crítico).
     * Não altera contratos já gerados: eles guardam o próprio valor (RF-08).
     */
    public void reajustar(int idFaixa, String valorDigitado, String justificativa) throws SQLException {
        ControleAcesso.exigir(Permissao.VER_RELATORIOS_FINANCEIROS);
        BigDecimal novo;
        try {
            novo = Formatador.lerDinheiro(valorDigitado).setScale(2, RoundingMode.HALF_UP);
        } catch (IllegalArgumentException e) {
            throw new MatriculaException(e.getMessage());
        }
        if (novo.signum() <= 0) {
            throw new MatriculaException("O valor da mensalidade deve ser maior que zero.");
        }
        String motivo = MatriculaValidador.aparar(justificativa);
        if (motivo == null) {
            throw new MatriculaException("Informe a justificativa do reajuste.");
        }
        if (motivo.length() > 255) {
            throw new MatriculaException("A justificativa pode ter no máximo 255 caracteres.");
        }
        Transacao.executar(conexao -> {
            BigDecimal anterior = faixaDAO.buscarValorETravar(conexao, idFaixa);
            if (anterior == null) {
                throw new MatriculaException("Faixa de mensalidade não encontrada.");
            }
            if (anterior.compareTo(novo) == 0) {
                throw new MatriculaException("O valor informado é igual ao atual.");
            }
            faixaDAO.atualizarValor(conexao, idFaixa, novo);
            auditoria.registrarAlteracao(conexao, "faixa_mensalidade", idFaixa,
                    "valor", anterior.toPlainString(), novo.toPlainString(), motivo);
            return null;
        });
    }
}
