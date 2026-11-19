package br.com.crechesementinhas.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Fábrica de conexões com o PostgreSQL.
 * Lê os dados de acesso do arquivo db.properties (fora do Git).
 */
public final class ConnectionFactory {

    private static final String ARQUIVO_CONFIG = "/db.properties";

    // Carregado UMA única vez, quando a classe é usada pela primeira vez.
    // Desempenho: ler o arquivo do disco é lento; guardar em memória deixa
    // cada consulta posterior às propriedades em O(1).
    private static final Properties CONFIG = carregarConfiguracao();

    // Construtor privado: ninguém precisa criar objetos desta classe,
    // todos os métodos são estáticos (boa prática para classes utilitárias).
    private ConnectionFactory() {
    }

    private static Properties carregarConfiguracao() {
        Properties propriedades = new Properties();

        // try-with-resources: fecha o arquivo automaticamente, mesmo se der erro.
        try (InputStream entrada = ConnectionFactory.class.getResourceAsStream(ARQUIVO_CONFIG)) {

            if (entrada == null) {
                throw new IllegalStateException(
                        "Arquivo db.properties não encontrado em src/main/resources. "
                        + "Copie o db.properties.example, renomeie e preencha.");
            }

            // UTF-8 garante que senhas com acento ou ç sejam lidas corretamente.
            propriedades.load(new InputStreamReader(entrada, StandardCharsets.UTF_8));
            return propriedades;

        } catch (IOException e) {
            throw new IllegalStateException("Erro ao ler o arquivo db.properties.", e);
        }
    }

    /**
     * Abre uma nova conexão com o banco.
     * Quem chamar deve fechá-la (de preferência com try-with-resources).
     *
     * Desempenho: abrir uma conexão custa alguns milissegundos (rede + autenticação).
     * Para um sistema desktop com poucos usuários, isso é aceitável. Em sistemas
     * grandes, usa-se um "pool" de conexões reaproveitáveis (ex.: HikariCP).
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
                CONFIG.getProperty("db.url"),
                CONFIG.getProperty("db.user"),
                CONFIG.getProperty("db.password"));
    }
}