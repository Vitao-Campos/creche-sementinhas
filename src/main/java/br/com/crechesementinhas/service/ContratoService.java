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
}