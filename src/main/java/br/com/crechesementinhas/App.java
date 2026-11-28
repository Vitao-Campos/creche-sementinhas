package br.com.crechesementinhas;

import br.com.crechesementinhas.util.Navegacao;
import javafx.application.Application;
import javafx.scene.image.Image;
import javafx.stage.Stage;

/**
 * Ponto de entrada do sistema da Creche Sementinhas.
 */
public class App extends Application {

    /** Abre a janela de login do sistema. */
    @Override
    public void start(Stage stage) {
        // Ícone da janela e da barra de tarefas: o emblema da creche
        stage.getIcons().add(new Image(
                App.class.getResourceAsStream("/br/com/crechesementinhas/imagens/emblema.png")));
        stage.setResizable(false); // o login é fixo; a Navegacao libera o tamanho depois do login
        Navegacao.iniciar(stage);
        Navegacao.abrir("login.fxml", "Login");
        stage.show();
    }

    /** Inicia o sistema. */
    public static void main(String[] args) {
        launch(args);
    }
}