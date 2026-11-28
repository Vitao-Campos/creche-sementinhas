package br.com.crechesementinhas.service;

import br.com.crechesementinhas.dao.HistoricoConsultaDAO;
import br.com.crechesementinhas.model.RegistroHistorico;
import java.sql.SQLException;
import java.util.List;

/** Consulta do histórico de alterações para as telas (RNF-02). */
public class HistoricoService {

    private final HistoricoConsultaDAO dao;

    /** Cria o serviço com o acesso ao banco padrão. */
    public HistoricoService() {
        this(new HistoricoConsultaDAO());
    }

    /** Cria o serviço com o acesso informado (usado em testes). */
    public HistoricoService(HistoricoConsultaDAO dao) {
        this.dao = dao;
    }

    /** Tudo que mudou na ficha da criança: dados, responsáveis, vínculos e autorizados. */
    public List<RegistroHistorico> consultarDoAluno(int idAluno) throws SQLException {
        return dao.listarPorAluno(idAluno);
    }

    /** Tudo que mudou no responsável e nos vínculos dele. */
    public List<RegistroHistorico> consultarDoResponsavel(int idResponsavel) throws SQLException {
        return dao.listarPorResponsavel(idResponsavel);
    }
}
