package br.com.crechesementinhas.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Properties;

/**
 * Diz ONDE ficam guardados os arquivos anexados (fotos, certidões, laudos...).
 *
 * A pasta é lida da linha "docs.pasta" do db.properties. Exemplos:
 *   docs.pasta=C:/CrecheSementinhas/documentos          (um computador só)
 *   docs.pasta=//SERVIDOR/CrecheSementinhas/documentos   (pasta compartilhada na rede)
 * Se a linha não existir, usa a pasta "CrecheSementinhas/documentos" do usuário.
 */
public final class PastaDocumentos {

    /** Classe só com métodos estáticos: não é instanciada. */
    private PastaDocumentos() {
    }

    /** Devolve a pasta onde ficam guardados os arquivos anexados. */
    public static Path raiz() {
        Properties config = new Properties();
        try (InputStream entrada = PastaDocumentos.class.getResourceAsStream("/db.properties")) {
            if (entrada != null) {
                config.load(new InputStreamReader(entrada, StandardCharsets.UTF_8));
            }
        } catch (IOException e) {
            // se não conseguir ler, segue com a pasta padrão
        }

        String pasta = config.getProperty("docs.pasta");
        if (pasta == null || pasta.isBlank()) {
            return Path.of(System.getProperty("user.home"), "CrecheSementinhas", "documentos");
        }
        return Path.of(pasta.trim());
    }
}