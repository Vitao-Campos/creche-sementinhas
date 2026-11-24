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

    // Só o login é pequeno e de tamanho fixo; todas as outras telas abrem maximizadas.
    private static final String TELA_LOGIN = "login.fxml";

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
            // Reaproveita a cena: trocar só o conteúdo mantém a janela como está
            // (maximizada ou no tamanho que o usuário escolheu).
            Scene cena = janelaPrincipal.getScene();
            if (cena == null) {
                janelaPrincipal.setScene(new Scene(tela));
            } else {
                cena.setRoot(tela);
            }
            janelaPrincipal.setTitle("Creche Sementinhas - " + titulo);

            if (TELA_LOGIN.equals(arquivoFxml)) {
                // Login (também ao clicar em Sair): janela pequena, fixa e centralizada.
                janelaPrincipal.setMaximized(false);
                janelaPrincipal.setResizable(false);
                janelaPrincipal.setMinWidth(0);
                janelaPrincipal.setMinHeight(0);
                janelaPrincipal.sizeToScene();
                janelaPrincipal.centerOnScreen();
            } else if (!janelaPrincipal.isResizable()) {
                // Primeira tela depois do login: libera o tamanho e maximiza.
                // Nas trocas seguintes a janela já é redimensionável e fica como o usuário deixou.
                janelaPrincipal.setResizable(true);
                janelaPrincipal.setMinWidth(1000);
                janelaPrincipal.setMinHeight(650);
                janelaPrincipal.setMaximized(true);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Erro ao carregar a tela " + arquivoFxml, e);
        }
    }
}