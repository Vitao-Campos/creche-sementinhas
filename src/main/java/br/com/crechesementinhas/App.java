package br.com.crechesementinhas;

import br.com.crechesementinhas.util.ConnectionFactory;
import java.sql.Connection;
import java.sql.SQLException;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/**
 * Ponto de entrada do sistema da Creche Sementinhas.
 * Por enquanto, testa a conexão com o banco e mostra o resultado.
 */
public class App extends Application {

    @Override
    public void start(Stage stage) {
        Label mensagem = new Label(testarConexao());

        Scene cena = new Scene(new StackPane(mensagem), 600, 400);

        stage.setTitle("Creche Sementinhas");
        stage.setScene(cena);
        stage.show();
    }

    private String testarConexao() {
        // try-with-resources: a conexão é fechada automaticamente no final.
        try (Connection conexao = ConnectionFactory.getConnection()) {
            String banco = conexao.getCatalog();
            String versao = conexao.getMetaData().getDatabaseProductVersion();
            return "✔ Conectado ao banco \"" + banco + "\" (PostgreSQL " + versao + ")";
        } catch (SQLException e) {
            return "✘ Erro ao conectar: " + e.getMessage();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}