package br.com.crechesementinhas.dao;

import br.com.crechesementinhas.model.Contrato;
import br.com.crechesementinhas.model.DadosContrato;
import br.com.crechesementinhas.model.MatriculaResumo;
import br.com.crechesementinhas.model.ResponsavelContrato;
import br.com.crechesementinhas.util.ConnectionFactory;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Acesso ao banco para CONTRATOS (único lugar da Frente F com SQL de contrato).
 * Sempre usamos PreparedStatement com "?" para evitar SQL Injection.
 */
public class ContratoDAO {

    /** Matrículas ATIVAS, para a ComboBox do topo da tela. */
    public List<MatriculaResumo> listarMatriculasAtivas() throws SQLException {
        String sql = """
                SELECT m.id_matricula, m.numero_matricula, a.nome
                FROM matricula m
                JOIN aluno a ON a.id_aluno = m.id_aluno
                WHERE m.status = 'ATIVA'
                ORDER BY a.nome
                """;
        List<MatriculaResumo> lista = new ArrayList<>();
        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(new MatriculaResumo(
                        rs.getInt("id_matricula"),
                        rs.getString("numero_matricula"),
                        rs.getString("nome")));
            }
        }
        return lista;
    }
    
    /**
     * RF-05: busca os dados JÁ cadastrados (aluno, turma e responsáveis)
     * para preencher o contrato automaticamente, sem digitar de novo.
     */
    public DadosContrato buscarDadosContrato(int idMatricula) throws SQLException {
        String sqlMatricula = """
                SELECT m.id_matricula, m.numero_matricula, m.status,
                       a.id_aluno, a.nome AS nome_aluno, a.data_nascimento,
                       t.nome AS nome_turma, t.periodo
                FROM matricula m
                JOIN aluno a ON a.id_aluno = m.id_aluno
                JOIN turma t ON t.id_turma = m.id_turma
                WHERE m.id_matricula = ?
                """;
        // Só vínculos que ainda valem (data_fim vazia ou no futuro).
        String sqlResponsaveis = """
                SELECT r.id_responsavel, r.nome, r.cpf, r.telefone, r.endereco,
                       ar.grau_parentesco, ar.responsavel_legal
                FROM aluno_responsavel ar
                JOIN responsavel r ON r.id_responsavel = ar.id_responsavel
                WHERE ar.id_aluno = ?
                  AND (ar.data_fim IS NULL OR ar.data_fim >= CURRENT_DATE)
                ORDER BY ar.responsavel_legal DESC, r.nome
                """;

        try (Connection con = ConnectionFactory.getConnection()) {
            DadosContrato dados = null;

            try (PreparedStatement ps = con.prepareStatement(sqlMatricula)) {
                ps.setInt(1, idMatricula);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        dados = new DadosContrato();
                        dados.setIdMatricula(rs.getInt("id_matricula"));
                        dados.setNumeroMatricula(rs.getString("numero_matricula"));
                        dados.setStatusMatricula(rs.getString("status"));
                        dados.setIdAluno(rs.getInt("id_aluno"));
                        dados.setNomeAluno(rs.getString("nome_aluno"));
                        dados.setDataNascimentoAluno(rs.getDate("data_nascimento").toLocalDate());
                        dados.setNomeTurma(rs.getString("nome_turma"));
                        dados.setPeriodoTurma(rs.getString("periodo"));
                    }
                }
            }

            if (dados == null) {
                return null; // matrícula não existe
            }

            try (PreparedStatement ps = con.prepareStatement(sqlResponsaveis)) {
                ps.setInt(1, dados.getIdAluno());
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        dados.getResponsaveis().add(new ResponsavelContrato(
                                rs.getInt("id_responsavel"),
                                rs.getString("nome"),
                                rs.getString("cpf"),
                                rs.getString("telefone"),
                                rs.getString("endereco"),
                                rs.getString("grau_parentesco"),
                                rs.getBoolean("responsavel_legal")));
                    }
                }
            }
            return dados;
        }
    }
    
    /** Todas as versões de contrato da matrícula (histórico - RF-08), da mais nova para a mais antiga. */
    public List<Contrato> listarPorMatricula(int idMatricula) throws SQLException {
        String sql = """
                SELECT c.*, r.nome AS nome_financeiro
                FROM contrato c
                JOIN responsavel r ON r.id_responsavel = c.id_responsavel_financeiro
                WHERE c.id_matricula = ?
                ORDER BY c.versao DESC
                """;
        List<Contrato> lista = new ArrayList<>();
        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idMatricula);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(montarContrato(rs));
                }
            }
        }
        return lista;
    }

    /** Transforma uma linha do banco (ResultSet) em um objeto Contrato. */
    private Contrato montarContrato(ResultSet rs) throws SQLException {
        Contrato c = new Contrato();
        c.setIdContrato(rs.getInt("id_contrato"));
        c.setIdMatricula(rs.getInt("id_matricula"));
        c.setIdResponsavelFinanceiro(rs.getInt("id_responsavel_financeiro"));
        c.setNomeResponsavelFinanceiro(rs.getString("nome_financeiro"));
        c.setPlano(rs.getString("plano"));
        c.setFormaPagamento(rs.getString("forma_pagamento"));
        c.setDiaVencimento(rs.getInt("data_vencimento_mensalidade"));
        c.setValorMensalidade(rs.getBigDecimal("valor_mensalidade"));
        c.setVigenciaAte(rs.getDate("vigencia_contrato").toLocalDate());
        Date assinatura = rs.getDate("data_assinatura");
        c.setDataAssinatura(assinatura == null ? null : assinatura.toLocalDate());
        Date saida = rs.getDate("data_saida");
        c.setDataSaida(saida == null ? null : saida.toLocalDate());
        c.setVersao(rs.getInt("versao"));
        c.setStatus(rs.getString("status"));
        return c;
    }
    
        /**
     * RN-06: trava a matrícula (FOR UPDATE) e devolve o id do contrato VIGENTE,
     * ou null se ainda não houver contrato.
     * Usa a conexão da Transacao: não abre, não fecha e não faz commit.
     */
    public Integer travarEBuscarVigente(Connection conexao, int idMatricula) throws SQLException {
        String sqlTravar = "SELECT id_matricula FROM matricula WHERE id_matricula = ? FOR UPDATE";
        String sqlVigente = """
                SELECT id_contrato FROM contrato
                WHERE id_matricula = ? AND status = 'VIGENTE'
                """;

        // Trava: se outro computador estiver gerando contrato desta matrícula, este espera.
        try (PreparedStatement ps = conexao.prepareStatement(sqlTravar)) {
            ps.setInt(1, idMatricula);
            ps.executeQuery().close();
        }

        try (PreparedStatement ps = conexao.prepareStatement(sqlVigente)) {
            ps.setInt(1, idMatricula);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt("id_contrato") : null;
            }
        }
    }

    /**
     * RF-08 / RN-06 / EX-04: cria uma NOVA versão do contrato.
     *   1. descobre o próximo número de versão;
     *   2. ENCERRA a versão vigente anterior (histórico não é apagado);
     *   3. grava a nova versão como VIGENTE.
     * Usa a conexão da Transacao: não abre, não fecha e não faz commit.
     * Preenche a versão e o id no objeto "novo" e devolve o id do contrato criado.
     */
    public int criarNovaVersao(Connection conexao, Contrato novo) throws SQLException {
        String sqlProxima = "SELECT COALESCE(MAX(versao), 0) + 1 FROM contrato WHERE id_matricula = ?";
        String sqlEncerrar = """
                UPDATE contrato SET status = 'ENCERRADO'
                WHERE id_matricula = ? AND status = 'VIGENTE'
                """;
        String sqlInserir = """
                INSERT INTO contrato (id_matricula, id_responsavel_financeiro, plano,
                    forma_pagamento, data_vencimento_mensalidade, vigencia_contrato,
                    data_assinatura, versao, status, valor_mensalidade)
                VALUES (?, ?, ?, ?, ?, ?, NULL, ?, 'VIGENTE', ?)
                RETURNING id_contrato
                """;

        int proximaVersao;
        try (PreparedStatement ps = conexao.prepareStatement(sqlProxima)) {
            ps.setInt(1, novo.getIdMatricula());
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                proximaVersao = rs.getInt(1);
            }
        }

        try (PreparedStatement ps = conexao.prepareStatement(sqlEncerrar)) {
            ps.setInt(1, novo.getIdMatricula());
            ps.executeUpdate();
        }

        try (PreparedStatement ps = conexao.prepareStatement(sqlInserir)) {
            ps.setInt(1, novo.getIdMatricula());
            ps.setInt(2, novo.getIdResponsavelFinanceiro());
            ps.setString(3, novo.getPlano());
            ps.setString(4, novo.getFormaPagamento());
            ps.setInt(5, novo.getDiaVencimento());
            ps.setDate(6, Date.valueOf(novo.getVigenciaAte()));
            ps.setInt(7, proximaVersao);
            ps.setBigDecimal(8, novo.getValorMensalidade());
            // RETURNING faz o INSERT devolver o id gerado, que a auditoria precisa
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                novo.setIdContrato(rs.getInt("id_contrato"));
            }
        }

        novo.setVersao(proximaVersao);
        return novo.getIdContrato();
    }
    
    /** RN-05: grava a data em que o responsável legal assinou. */
    public void registrarAssinatura(int idContrato, LocalDate data) throws SQLException {
        String sql = """
                UPDATE contrato SET data_assinatura = ?
                WHERE id_contrato = ? AND status = 'VIGENTE'
                """;
        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(data));
            ps.setInt(2, idContrato);
            int linhas = ps.executeUpdate();
            if (linhas == 0) {
                throw new SQLException("Só é possível assinar o contrato VIGENTE.");
            }
        }
    }
}