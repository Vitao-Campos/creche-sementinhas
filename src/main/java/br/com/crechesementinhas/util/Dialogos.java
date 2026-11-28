package br.com.crechesementinhas.util;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

/** Caixas de diálogo comuns às telas. */
public final class Dialogos {

    private Dialogos() {
    }

    /** Pergunta se pode descartar o que foi digitado e não foi salvo. true = pode descartar. */
    public static boolean confirmarDescarte() {
        ButtonType descartar = new ButtonType("Descartar", javafx.scene.control.ButtonBar.ButtonData.OK_DONE);
        ButtonType continuar = new ButtonType("Continuar editando", javafx.scene.control.ButtonBar.ButtonData.CANCEL_CLOSE);
        Alert aviso = new Alert(Alert.AlertType.CONFIRMATION,
                "Há alterações que ainda não foram salvas. Descartar?", descartar, continuar);
        aviso.setTitle("Alterações não salvas");
        aviso.setHeaderText(null);
        return aviso.showAndWait().orElse(continuar) == descartar;
    }
}
