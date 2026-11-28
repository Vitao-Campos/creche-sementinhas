package br.com.crechesementinhas.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Fábrica de conexões com o PostgreSQL.
 *
 * Onde procura o db.properties, nesta ordem:
 *   1) Na pasta de onde o sistema foi aberto (ao lado do .jar, no executável).
 *      Permite trocar o IP do servidor sem gerar o jar de novo.
 *   2) Em src/main/resources (desenvolvimento com mvn javafx:run).
 */
public final class ConnectionFactory {

    private static final String ARQUIVO_EXTERNO = "db.properties";
    private static final String ARQUIVO_INTERNO = "/db.properties";

    // Carregado UMA única vez, quando a classe é usada pela primeira vez.
    // Desempenho: ler o arquivo do disco é lento; guardar em memória deixa
    // cada consulta posterior às propriedades em O(1).
    private static final Properties CONFIG = carregarConfiguracao();

    private ConnectionFactory() {
    }

    private static Properties carregarConfiguracao() {
        Properties propriedades = new Properties();

        // 1) Arquivo externo, ao lado do executável.
        Path externo = Path.of(ARQUIVO_EXTERNO);
        if (Files.isRegularFile(externo)) {
            try (Reader leitor = Files.newBufferedReader(externo, StandardCharsets.UTF_8)) {
                propriedades.load(leitor);
                return propriedades;
            } catch (IOException e) {
                throw new IllegalStateException(
                        "Erro ao ler o arquivo " + externo.toAbsolutePath(), e);
            }
        }

        // 2) Arquivo interno, em src/main/resources (desenvolvimento).
        try (InputStream entrada = ConnectionFactory.class.getResourceAsStream(ARQUIVO_INTERNO)) {
            if (entrada == null) {
                throw new IllegalStateException(
                        "Arquivo db.properties não encontrado. "
                        + "No executável, coloque-o na mesma pasta do sistema. "
                        + "No desenvolvimento, copie o db.properties.example em "
                        + "src/main/resources, renomeie e preencha.");
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