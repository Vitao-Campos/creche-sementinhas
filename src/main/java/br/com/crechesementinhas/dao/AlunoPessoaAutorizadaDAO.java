package br.com.crechesementinhas.dao;

import br.com.crechesementinhas.model.AlunoPessoaAutorizada;
import br.com.crechesementinhas.model.TipoAutorizacao;
import br.com.crechesementinhas.util.ConnectionFactory;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Acesso à tabela "aluno_pessoa_autorizada" (quem pode buscar cada criança). */
public class AlunoPessoaAutorizadaDAO {

    private static final String SQL_SELECT = """
            SELECT ap.id_aluno_pessoa_autorizada, ap.id_aluno, ap.id_pessoa_autorizada,
                   p.nome, p.rg, p.telefone,
                   ap.parentesco, ap.tipo_autorizacao, ap.ativo
              FROM aluno_pessoa_autorizada ap
              JOIN pessoa_autorizada p ON p.id_pessoa_autorizada = ap.id_pessoa_autorizada
            """;

    private static final String SQL_INSERIR = """
            INSERT INTO aluno_pessoa_autorizada
                   (id_aluno, id_pessoa_autorizada, parentesco, tipo_autorizacao, ativo)
            VALUES (?, ?, ?, ?, TRUE)
            RETURNING id_aluno_pessoa_autorizada
            """;

    private static final String SQL_BUSCAR_POR_ID = SQL_SELECT + " WHERE ap.id_aluno_pessoa_autorizada = ?";

    // Autorizações ativas primeiro, depois por nome.
    private static final String SQL_LISTAR_POR_ALUNO = SQL_SELECT + """
             WHERE ap.id_aluno = ?
             ORDER BY NOT ap.ativo, p.nome
            """;

    private static final String SQL_ATUALIZAR_ATIVO =
            "UPDATE aluno_pessoa_autorizada SET ativo = ? WHERE id_aluno_pessoa_autorizada = ?";

    private static final String SQL_EXISTE_ATIVA = """
            SELECT EXISTS (
                SELECT 1 FROM aluno_pessoa_autorizada
                 WHERE id_aluno = ? AND id_pessoa_autorizada = ? AND ativo
            )
            """;

    /** Grava a autorização de uma pessoa para a criança e devolve o identificador gerado. */
    public int inserir(Connection conexao, AlunoPessoaAutorizada a) throws SQLException {
        try (PreparedStatement comando = conexao.prepareStatement(SQL_INSERIR)) {
            comando.setInt(1, a.getIdAluno());
            comando.setInt(2, a.getIdPessoaAutorizada());
            comando.setString(3, a.getParentesco());
            comando.setString(4, a.getTipoAutorizacao().name());
            try (ResultSet resultado = comando.executeQuery()) {
                resultado.next();
                return resultado.getInt(1);
            }
        }
    }

    /** Busca uma autorização pelo identificador. */
    public Optional<AlunoPessoaAutorizada> buscarPorId(Connection conexao, int id) throws SQLException {
        try (PreparedStatement comando = conexao.prepareStatement(SQL_BUSCAR_POR_ID)) {
            comando.setInt(1, id);
            try (ResultSet resultado = comando.executeQuery()) {
                return resultado.next() ? Optional.of(mapear(resultado)) : Optional.empty();
            }
        }
    }

    /** Quem está (ou esteve) autorizado a buscar a criança. */
    public List<AlunoPessoaAutorizada> listarPorAluno(int idAluno) throws SQLException {
        List<AlunoPessoaAutorizada> lista = new ArrayList<>();
        try (Connection conexao = ConnectionFactory.getConnection();
             PreparedStatement comando = conexao.prepareStatement(SQL_LISTAR_POR_ALUNO)) {
            comando.setInt(1, idAluno);
            try (ResultSet resultado = comando.executeQuery()) {
                while (resultado.next()) {
                    lista.add(mapear(resultado));
                }
            }
        }
        return lista;
    }

    /** Ativa ou desativa a autorização (a retirada mantém o registro no histórico). */
    public void atualizarAtivo(Connection conexao, int id, boolean ativo) throws SQLException {
        try (PreparedStatement comando = conexao.prepareStatement(SQL_ATUALIZAR_ATIVO)) {
            comando.setBoolean(1, ativo);
            comando.setInt(2, id);
            comando.executeUpdate();
        }
    }

    /** Esta pessoa já está ATIVA para esta criança? */
    public boolean existeAtiva(Connection conexao, int idAluno, int idPessoa) throws SQLException {
        try (PreparedStatement comando = conexao.prepareStatement(SQL_EXISTE_ATIVA)) {
            comando.setInt(1, idAluno);
            comando.setInt(2, idPessoa);
            try (ResultSet resultado = comando.executeQuery()) {
                resultado.next();
                return resultado.getBoolean(1);
            }
        }
    }

    /** Converte a linha do resultado da consulta em um objeto AlunoPessoaAutorizada. */
    private AlunoPessoaAutorizada mapear(ResultSet rs) throws SQLException {
        AlunoPessoaAutorizada a = new AlunoPessoaAutorizada();
        a.setIdAlunoPessoaAutorizada(rs.getInt("id_aluno_pessoa_autorizada"));
        a.setIdAluno(rs.getInt("id_aluno"));
        a.setIdPessoaAutorizada(rs.getInt("id_pessoa_autorizada"));
        a.setNome(rs.getString("nome"));
        a.setRg(rs.getString("rg"));
        a.setTelefone(rs.getString("telefone"));
        a.setParentesco(rs.getString("parentesco"));
        a.setTipoAutorizacao(TipoAutorizacao.valueOf(rs.getString("tipo_autorizacao")));
        a.setAtivo(rs.getBoolean("ativo"));
        return a;
    }
}
