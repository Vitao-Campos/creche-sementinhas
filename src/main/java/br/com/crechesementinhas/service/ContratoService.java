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
}