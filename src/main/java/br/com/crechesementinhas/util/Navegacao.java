package br.com.crechesementinhas.util;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Troca a tela exibida na janela principal do sistema.
 */
public final class Navegacao {

    // A "/" no começo indica caminho absoluto, a partir de src/main/resources.
    private static final String PASTA_TELAS = "/br/com/crechesementinhas/view/";

    private static Stage janelaPrincipal;

    private Navegacao() {
    }

    /** Chamado uma única vez, pelo App, ao iniciar o sistema. */
    public static void iniciar(Stage janela) {
        janelaPrincipal = Objects.requireNonNull(janela, "Janela não pode ser nula.");
    }

    /**
     * Abre uma tela na janela principal.
     *
     * @param arquivoFxml nome do arquivo dentro da pasta view (ex.: "menu.fxml")
     * @param titulo      texto que aparece na barra da janela
     */
    public static void abrir(String arquivoFxml, String titulo) {
        if (janelaPrincipal == null) {
            throw new IllegalStateException("Navegacao.iniciar() não foi chamado.");
        }

        URL local = Navegacao.class.getResource(PASTA_TELAS + arquivoFxml);
        if (local == null) {
            throw new IllegalStateException("Tela não encontrada: " + PASTA_TELAS + arquivoFxml);
        }

        try {
            Parent tela = new FXMLLoader(local).load();
            janelaPrincipal.setScene(new Scene(tela));
            janelaPrincipal.setTitle("Creche Sementinhas - " + titulo);
            janelaPrincipal.sizeToScene();     // ajusta a janela ao tamanho da nova tela
            janelaPrincipal.centerOnScreen();
        } catch (IOException e) {
            throw new IllegalStateException("Erro ao carregar a tela " + arquivoFxml, e);
        }
    }
}