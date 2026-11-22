package br.com.crechesementinhas;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * SÓ PARA TESTE da Frente F: abre a tela de Contrato e documentos direto,
 * sem passar pelo login/menu (que ainda estão sendo feitos pela Frente A).
 * Na etapa 4 (integração), o menu principal abrirá esta tela e este arquivo pode ser apagado.
 *
 * Como rodar no VS Code: abra este arquivo e clique em "Run" acima do método main.
 */
public class TesteFrenteF {

    public static void main(String[] args) {
        Application.launch(Tela.class, args);
    }

    public static class Tela extends Application {
        @Override
        public void start(Stage stage) throws Exception {
            Parent raiz = FXMLLoader.load(
                    TesteFrenteF.class.getResource("/br/com/crechesementinhas/view/ContratoDocumentos.fxml"));
            stage.setTitle("Creche Sementinhas - Contrato e documentos (TESTE)");
            stage.setScene(new Scene(raiz));
            stage.show();
        }
    }
}