package br.com.crechesementinhas.dao;

import br.com.crechesementinhas.model.Aluno;
import br.com.crechesementinhas.model.Sexo;
import br.com.crechesementinhas.util.ConnectionFactory;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Acesso à tabela "aluno". Único lugar com SQL de aluno.
 * Os métodos que gravam recebem a Connection de uma Transacao (alteração + auditoria
 * acontecem juntas ou não acontecem).
 */
public class AlunoDAO {

    private static final String COLUNAS = """
            id_aluno, nome, data_nascimento, cpf, sexo,
            naturalidade, nacionalidade, observacao
            """;

    private static final String SQL_INSERIR = """
            INSERT INTO aluno (nome, data_nascimento, cpf, sexo,
                               naturalidade, nacionalidade, observacao)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            RETURNING id_aluno
            """;

    private static final String SQL_ATUALIZAR = """
            UPDATE aluno
               SET nome = ?, data_nascimento = ?, cpf = ?, sexo = ?,
                   naturalidade = ?, nacionalidade = ?, observacao = ?
             WHERE id_aluno = ?
            """;

    private static final String SQL_BUSCAR_POR_ID =
            "SELECT " + COLUNAS + " FROM aluno WHERE id_aluno = ?";

    // ILIKE ignora maiúsculas e minúsculas (a busca pelo nome é a mais usada pela secretaria).
    // LIMIT: a lista nunca cresce sem controle na tela.
    private static final String SQL_LISTAR = """
            SELECT id_aluno, nome, data_nascimento, cpf, sexo,
                   naturalidade, nacionalidade, observacao
              FROM aluno
             WHERE nome ILIKE ?
             ORDER BY nome
             LIMIT 500
            """;

    private static final String SQL_EXISTE_NOME_NASCIMENTO = """
            SELECT EXISTS (
                SELECT 1 FROM aluno
                 WHERE LOWER(nome) = LOWER(?) AND data_nascimento = ? AND id_aluno <> ?
            )
            """;

    private static final String SQL_EXISTE_CPF = """
            SELECT EXISTS (
                SELECT 1 FROM aluno
                 WHERE cpf = ? AND id_aluno <> ?
            )
            """;

    /**
     * Grava um aluno novo e devolve o id gerado pelo banco.
     * Usa a conexão RECEBIDA e não a fecha.
     */
    public int inserir(Connection conexao, Aluno aluno) throws SQLException {
        try (PreparedStatement comando = conexao.prepareStatement(SQL_INSERIR)) {
            preencher(comando, aluno);
            try (ResultSet resultado = comando.executeQuery()) {
                resultado.next();
                return resultado.getInt(1);
            }
        }
    }

    /** Atualiza todos os campos do aluno. Usa a conexão RECEBIDA e não a fecha. */
    public void atualizar(Connection conexao, Aluno aluno) throws SQLException {
        try (PreparedStatement comando = conexao.prepareStatement(SQL_ATUALIZAR)) {
            preencher(comando, aluno);
            comando.setInt(8, aluno.getIdAluno());
            comando.executeUpdate();
        }
    }

    /** Busca um aluno pelo id, abrindo a própria conexão. */
    public Optional<Aluno> buscarPorId(int idAluno) throws SQLException {
        try (Connection conexao = ConnectionFactory.getConnection()) {
            return buscarPorId(conexao, idAluno);
        }
    }

    /** Busca um aluno pelo id dentro de uma transação já aberta. */
    public Optional<Aluno> buscarPorId(Connection conexao, int idAluno) throws SQLException {
        try (PreparedStatement comando = conexao.prepareStatement(SQL_BUSCAR_POR_ID)) {
            comando.setInt(1, idAluno);
            try (ResultSet resultado = comando.executeQuery()) {
                return resultado.next() ? Optional.of(mapear(resultado)) : Optional.empty();
            }
        }
    }

    /**
     * Lista os alunos em ordem alfabética, filtrando por parte do nome.
     *
     * @param trechoNome parte do nome; vazio ou null lista todos
     */
    public List<Aluno> listar(String trechoNome) throws SQLException {
        List<Aluno> alunos = new ArrayList<>();
        String filtro = "%" + escaparCuringas(trechoNome == null ? "" : trechoNome.trim()) + "%";

        try (Connection conexao = ConnectionFactory.getConnection();
             PreparedStatement comando = conexao.prepareStatement(SQL_LISTAR)) {

            comando.setString(1, filtro);
            try (ResultSet resultado = comando.executeQuery()) {
                while (resultado.next()) {
                    alunos.add(mapear(resultado));
                }
            }
        }
        return alunos;
    }

    /**
     * Já existe aluno com este CPF? (único no banco)
     *
     * @param cpf          os 11 dígitos
     * @param idIgnorado   id do próprio aluno na edição (para não acusar ele mesmo); null no cadastro
     */
    public boolean existeCpf(Connection conexao, String cpf, Integer idIgnorado) throws SQLException {
        try (PreparedStatement comando = conexao.prepareStatement(SQL_EXISTE_CPF)) {
            comando.setString(1, cpf);
            // -1 nunca é um id real: no cadastro nenhum aluno é "ignorado".
            comando.setInt(2, idIgnorado == null ? -1 : idIgnorado);
            try (ResultSet resultado = comando.executeQuery()) {
                resultado.next();
                return resultado.getBoolean(1);
            }
        }
    }

    /** Já existe aluno com este nome (sem diferenciar maiúsculas) e esta data de nascimento? */
    public boolean existeNomeENascimento(Connection conexao, String nome, LocalDate nascimento,
                                         Integer idIgnorado) throws SQLException {
        try (PreparedStatement comando = conexao.prepareStatement(SQL_EXISTE_NOME_NASCIMENTO)) {
            comando.setString(1, nome);
            comando.setDate(2, Date.valueOf(nascimento));
            comando.setInt(3, idIgnorado == null ? -1 : idIgnorado);
            try (ResultSet resultado = comando.executeQuery()) {
                resultado.next();
                return resultado.getBoolean(1);
            }
        }
    }

    /** Preenche os 7 primeiros "?" (iguais no INSERT e no UPDATE). */
    private void preencher(PreparedStatement comando, Aluno aluno) throws SQLException {
        comando.setString(1, aluno.getNome());
        comando.setDate(2, Date.valueOf(aluno.getDataNascimento()));
        comando.setString(3, aluno.getCpf());
        comando.setString(4, aluno.getSexo().name());
        comando.setString(5, aluno.getNaturalidade());
        comando.setString(6, aluno.getNacionalidade());
        comando.setString(7, aluno.getObservacao());
    }

    /** O usuário digita "%" ou "_" no nome? Trata como letra comum, não como curinga. */
    private String escaparCuringas(String texto) {
        return texto.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }

    /** Converte a linha do resultado da consulta em um objeto Aluno. */
    private Aluno mapear(ResultSet resultado) throws SQLException {
        Aluno aluno = new Aluno();
        aluno.setIdAluno(resultado.getInt("id_aluno"));
        aluno.setNome(resultado.getString("nome"));
        aluno.setDataNascimento(resultado.getObject("data_nascimento", LocalDate.class));
        aluno.setCpf(resultado.getString("cpf"));
        aluno.setSexo(Sexo.valueOf(resultado.getString("sexo")));
        aluno.setNaturalidade(resultado.getString("naturalidade"));
        aluno.setNacionalidade(resultado.getString("nacionalidade"));
        aluno.setObservacao(resultado.getString("observacao"));
        return aluno;
    }
}
