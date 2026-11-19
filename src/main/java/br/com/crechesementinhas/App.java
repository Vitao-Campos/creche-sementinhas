package br.com.crechesementinhas;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/**
 * Ponto de entrada do sistema da Creche Sementinhas.
 * Por enquanto, apenas abre uma janela de teste.
 */
public class App extends Application {

    @Override
    public void start(Stage stage) {
        Label mensagem = new Label("Sistema Creche Sementinhas - JavaFX funcionando!");

        StackPane layout = new StackPane(mensagem);

        Scene cena = new Scene(layout, 600, 400);

        stage.setTitle("Creche Sementinhas");
        stage.setScene(cena);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}