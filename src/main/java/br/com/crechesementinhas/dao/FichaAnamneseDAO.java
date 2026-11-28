package br.com.crechesementinhas.dao;

import br.com.crechesementinhas.model.CabecalhoFicha;
import br.com.crechesementinhas.model.FichaAnamnese;
import br.com.crechesementinhas.model.MatriculaComFicha;
import br.com.crechesementinhas.util.ConnectionFactory;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Acesso à tabela "ficha_anamnese" (uma por matrícula). Único lugar com esse SQL. */
public class FichaAnamneseDAO {

    private static final String SQL_LISTAR_MATRICULAS = """
            SELECT m.id_matricula, m.numero_matricula, a.nome,
                   EXISTS (SELECT 1 FROM ficha_anamnese f WHERE f.id_matricula = m.id_matricula) AS preenchida
              FROM matricula m
              JOIN aluno a ON a.id_aluno = m.id_aluno
             WHERE m.status = 'ATIVA' AND LOWER(a.nome) LIKE LOWER(?)
             ORDER BY a.nome
            """;

    private static final String SQL_CABECALHO = """
            SELECT m.id_matricula, m.numero_matricula, m.status, a.id_aluno, a.nome,
                   a.data_nascimento, t.nome AS nome_turma
              FROM matricula m
              JOIN aluno a ON a.id_aluno = m.id_aluno
              JOIN turma t ON t.id_turma = m.id_turma
             WHERE m.id_matricula = ?
            """;

    private static final String SQL_COLUNAS = """
            id_ficha_anamnese, id_matricula, autorizacao_imagem, possui_alergia, descricao_alergia,
            restricoes_alimentares, descricao_restricao, receita_antitermico, possui_cirurgia,
            descricao_cirurgia, possui_problemas_saude, descricao_problemas_saude, usa_medicacao_continua,
            descricao_medicacao, possui_fraturas, descricao_fratura, usa_mamadeira, usa_formula,
            descricao_formula, usa_chupeta, usa_fralda
            """;

    private static final String SQL_BUSCAR =
            "SELECT " + SQL_COLUNAS + " FROM ficha_anamnese WHERE id_matricula = ?";

    private static final String SQL_INSERIR = """
            INSERT INTO ficha_anamnese
                   (id_matricula, autorizacao_imagem, possui_alergia, descricao_alergia,
                    restricoes_alimentares, descricao_restricao, receita_antitermico, possui_cirurgia,
                    descricao_cirurgia, possui_problemas_saude, descricao_problemas_saude,
                    usa_medicacao_continua, descricao_medicacao, possui_fraturas, descricao_fratura,
                    usa_mamadeira, usa_formula, descricao_formula, usa_chupeta, usa_fralda)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            RETURNING id_ficha_anamnese
            """;

    private static final String SQL_ATUALIZAR = """
            UPDATE ficha_anamnese SET
                   autorizacao_imagem = ?, possui_alergia = ?, descricao_alergia = ?,
                   restricoes_alimentares = ?, descricao_restricao = ?, receita_antitermico = ?,
                   possui_cirurgia = ?, descricao_cirurgia = ?, possui_problemas_saude = ?,
                   descricao_problemas_saude = ?, usa_medicacao_continua = ?, descricao_medicacao = ?,
                   possui_fraturas = ?, descricao_fratura = ?, usa_mamadeira = ?, usa_formula = ?,
                   descricao_formula = ?, usa_chupeta = ?, usa_fralda = ?
             WHERE id_ficha_anamnese = ?
            """;

    /** Matrículas ativas (filtradas por parte do nome) e se já têm ficha. */
    public List<MatriculaComFicha> listarMatriculasAtivas(String trechoNome) throws SQLException {
        List<MatriculaComFicha> lista = new ArrayList<>();
        try (Connection conexao = ConnectionFactory.getConnection();
             PreparedStatement comando = conexao.prepareStatement(SQL_LISTAR_MATRICULAS)) {
            comando.setString(1, "%" + (trechoNome == null ? "" : trechoNome.trim()) + "%");
            try (ResultSet rs = comando.executeQuery()) {
                while (rs.next()) {
                    lista.add(new MatriculaComFicha(rs.getInt("id_matricula"), rs.getString("numero_matricula"),
                            rs.getString("nome"), rs.getBoolean("preenchida")));
                }
            }
        }
        return lista;
    }

    public Optional<CabecalhoFicha> buscarCabecalho(Connection conexao, int idMatricula) throws SQLException {
        try (PreparedStatement comando = conexao.prepareStatement(SQL_CABECALHO)) {
            comando.setInt(1, idMatricula);
            try (ResultSet rs = comando.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                return Optional.of(new CabecalhoFicha(rs.getInt("id_matricula"), rs.getString("numero_matricula"),
                        rs.getString("status"), rs.getInt("id_aluno"), rs.getString("nome"),
                        rs.getDate("data_nascimento").toLocalDate(), rs.getString("nome_turma")));
            }
        }
    }

    public Optional<FichaAnamnese> buscarPorMatricula(Connection conexao, int idMatricula) throws SQLException {
        try (PreparedStatement comando = conexao.prepareStatement(SQL_BUSCAR)) {
            comando.setInt(1, idMatricula);
            try (ResultSet rs = comando.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        }
    }

    public int inserir(Connection conexao, FichaAnamnese f) throws SQLException {
        try (PreparedStatement comando = conexao.prepareStatement(SQL_INSERIR)) {
            comando.setInt(1, f.getIdMatricula());
            preencher(comando, f, 2);
            try (ResultSet rs = comando.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    public void atualizar(Connection conexao, FichaAnamnese f) throws SQLException {
        try (PreparedStatement comando = conexao.prepareStatement(SQL_ATUALIZAR)) {
            int proximo = preencher(comando, f, 1);
            comando.setInt(proximo, f.getIdFichaAnamnese());
            comando.executeUpdate();
        }
    }

    /** Preenche os 19 campos da ficha a partir da posição indicada; devolve a próxima posição livre. */
    private int preencher(PreparedStatement c, FichaAnamnese f, int i) throws SQLException {
        c.setBoolean(i++, f.isAutorizacaoImagem());
        c.setBoolean(i++, f.isPossuiAlergia());
        texto(c, i++, f.getDescricaoAlergia());
        c.setBoolean(i++, f.isRestricoesAlimentares());
        texto(c, i++, f.getDescricaoRestricao());
        texto(c, i++, f.getReceitaAntitermico());
        c.setBoolean(i++, f.isPossuiCirurgia());
        texto(c, i++, f.getDescricaoCirurgia());
        c.setBoolean(i++, f.isPossuiProblemasSaude());
        texto(c, i++, f.getDescricaoProblemasSaude());
        c.setBoolean(i++, f.isUsaMedicacaoContinua());
        texto(c, i++, f.getDescricaoMedicacao());
        c.setBoolean(i++, f.isPossuiFraturas());
        texto(c, i++, f.getDescricaoFratura());
        c.setBoolean(i++, f.isUsaMamadeira());
        c.setBoolean(i++, f.isUsaFormula());
        texto(c, i++, f.getDescricaoFormula());
        c.setBoolean(i++, f.isUsaChupeta());
        c.setBoolean(i++, f.isUsaFralda());
        return i;
    }

    private void texto(PreparedStatement c, int posicao, String valor) throws SQLException {
        if (valor == null) {
            c.setNull(posicao, Types.VARCHAR);
        } else {
            c.setString(posicao, valor);
        }
    }

    private FichaAnamnese mapear(ResultSet rs) throws SQLException {
        FichaAnamnese f = new FichaAnamnese();
        f.setIdFichaAnamnese(rs.getInt("id_ficha_anamnese"));
        f.setIdMatricula(rs.getInt("id_matricula"));
        f.setAutorizacaoImagem(rs.getBoolean("autorizacao_imagem"));
        f.setPossuiAlergia(rs.getBoolean("possui_alergia"));
        f.setDescricaoAlergia(rs.getString("descricao_alergia"));
        f.setRestricoesAlimentares(rs.getBoolean("restricoes_alimentares"));
        f.setDescricaoRestricao(rs.getString("descricao_restricao"));
        f.setReceitaAntitermico(rs.getString("receita_antitermico"));
        f.setPossuiCirurgia(rs.getBoolean("possui_cirurgia"));
        f.setDescricaoCirurgia(rs.getString("descricao_cirurgia"));
        f.setPossuiProblemasSaude(rs.getBoolean("possui_problemas_saude"));
        f.setDescricaoProblemasSaude(rs.getString("descricao_problemas_saude"));
        f.setUsaMedicacaoContinua(rs.getBoolean("usa_medicacao_continua"));
        f.setDescricaoMedicacao(rs.getString("descricao_medicacao"));
        f.setPossuiFraturas(rs.getBoolean("possui_fraturas"));
        f.setDescricaoFratura(rs.getString("descricao_fratura"));
        f.setUsaMamadeira(rs.getBoolean("usa_mamadeira"));
        f.setUsaFormula(rs.getBoolean("usa_formula"));
        f.setDescricaoFormula(rs.getString("descricao_formula"));
        f.setUsaChupeta(rs.getBoolean("usa_chupeta"));
        f.setUsaFralda(rs.getBoolean("usa_fralda"));
        return f;
    }
}
