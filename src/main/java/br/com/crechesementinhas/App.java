package br.com.crechesementinhas;

import br.com.crechesementinhas.util.Navegacao;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Ponto de entrada do sistema da Creche Sementinhas.
 */
public class App extends Application {

    @Override
    public void start(Stage stage) {
        stage.setResizable(false); // o login é fixo; a Navegacao libera o tamanho depois do login
        Navegacao.iniciar(stage);
        Navegacao.abrir("login.fxml", "Login");
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}