package br.com.crechesementinhas;

import java.io.IOException;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Ponto de entrada do sistema da Creche Sementinhas.
 * Abre a tela de login.
 */
public class App extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        // Caminho relativo ao pacote desta classe:
        // br/com/crechesementinhas/ + view/login.fxml
        FXMLLoader carregador = new FXMLLoader(App.class.getResource("view/login.fxml"));
        Parent tela = carregador.load();

        stage.setTitle("Creche Sementinhas - Login");
        stage.setScene(new Scene(tela));
        stage.setResizable(false);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}