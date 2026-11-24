package br.com.crechesementinhas.controller;

import br.com.crechesementinhas.model.RegistroHistorico;
import br.com.crechesementinhas.service.HistoricoFormatador;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.Window;

/**
 * Janela separada com o histórico de alterações (historico.fxml). RNF-02.
 * Mostra quando, quem alterou, o que e o valor antes e depois.
 * Quem abre usa o método estático abrir(...); a janela só exibe, não grava nada.
 */
public class HistoricoController {

    private static final String ARQUIVO_FXML = "/br/com/crechesementinhas/view/historico.fxml";

    @FXML private Label labelTitulo;
    @FXML private Label labelTotal;
    @FXML private TableView<RegistroHistorico> tabela;
    @FXML private TableColumn<RegistroHistorico, String> colunaDataHora;
    @FXML private TableColumn<RegistroHistorico, String> colunaUsuario;
    @FXML private TableColumn<RegistroHistorico, String> colunaSobre;
    @FXML private TableColumn<RegistroHistorico, String> colunaAcao;
    @FXML private TableColumn<RegistroHistorico, String> colunaCampo;
    @FXML private TableColumn<RegistroHistorico, String> colunaAntes;
    @FXML private TableColumn<RegistroHistorico, String> colunaDepois;
    @FXML private TableColumn<RegistroHistorico, String> colunaMotivo;

    /**
     * Abre a janela de histórico, sem bloquear a janela principal.
     *
     * @param dono      janela que pediu (a janela do histórico fica sempre na frente dela)
     * @param titulo    texto do cabeçalho, ex.: "Histórico de alterações — Ana Lima"
     * @param registros o que mostrar (mais recente primeiro)
     */
    public static void abrir(Window dono, String titulo, List<RegistroHistorico> registros) {
        URL local = HistoricoController.class.getResource(ARQUIVO_FXML);
        if (local == null) {
            throw new IllegalStateException("Tela não encontrada: " + ARQUIVO_FXML);
        }
        try {
            FXMLLoader carregador = new FXMLLoader(local);
            Parent raiz = carregador.load();
            HistoricoController controller = carregador.getController();
            controller.mostrar(titulo, registros);

            Stage janela = new Stage();
            janela.initOwner(dono);
            janela.setTitle("Creche Sementinhas - " + titulo);
            janela.setScene(new Scene(raiz));
            janela.setMinWidth(800);
            janela.setMinHeight(400);
            janela.show();
        } catch (IOException e) {
            throw new IllegalStateException("Erro ao carregar a tela do histórico", e);
        }
    }

    @FXML
    private void initialize() {
        colunaDataHora.setCellValueFactory(c ->
                new ReadOnlyStringWrapper(HistoricoFormatador.dataHora(c.getValue().dataHora())));
        colunaUsuario.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().usuario()));
        colunaSobre.setCellValueFactory(c -> new ReadOnlyStringWrapper(
                HistoricoFormatador.sobre(c.getValue().entidade(), c.getValue().referencia())));
        colunaAcao.setCellValueFactory(c ->
                new ReadOnlyStringWrapper(HistoricoFormatador.acao(c.getValue().tipoAlteracao())));
        colunaCampo.setCellValueFactory(c ->
                new ReadOnlyStringWrapper(HistoricoFormatador.campo(c.getValue().campoAlterado())));
        colunaAntes.setCellValueFactory(c -> new ReadOnlyStringWrapper(
                HistoricoFormatador.valor(c.getValue().campoAlterado(), c.getValue().valorAnterior())));
        colunaDepois.setCellValueFactory(c -> new ReadOnlyStringWrapper(
                HistoricoFormatador.valor(c.getValue().campoAlterado(), c.getValue().valorNovo())));
        colunaMotivo.setCellValueFactory(c -> new ReadOnlyStringWrapper(
                c.getValue().justificativa() == null ? "" : c.getValue().justificativa()));

        // Textos longos (observações, endereço, motivo) quebram em várias linhas em vez de ficarem cortados.
        for (TableColumn<RegistroHistorico, String> coluna :
                List.of(colunaSobre, colunaCampo, colunaAntes, colunaDepois, colunaMotivo)) {
            comQuebraDeLinha(coluna);
        }
        tabela.setPlaceholder(new Label("Nenhuma alteração registrada."));
    }

    private void mostrar(String titulo, List<RegistroHistorico> registros) {
        labelTitulo.setText(titulo);
        tabela.getItems().setAll(registros);
        labelTotal.setText(registros.size() == 1 ? "1 registro" : registros.size() + " registros");
    }

    @FXML
    private void fechar() {
        ((Stage) tabela.getScene().getWindow()).close();
    }

    private void comQuebraDeLinha(TableColumn<RegistroHistorico, String> coluna) {
        coluna.setCellFactory(c -> new TableCell<RegistroHistorico, String>() {
            private final Text texto = new Text();

            {
                // A largura do texto acompanha a da coluna: o que não cabe desce para a linha de baixo.
                texto.wrappingWidthProperty().bind(coluna.widthProperty().subtract(14));
            }

            @Override
            protected void updateItem(String item, boolean vazio) {
                super.updateItem(item, vazio);
                if (vazio || item == null) {
                    setGraphic(null);
                } else {
                    texto.setText(item);
                    setGraphic(texto);
                }
            }
        });
    }
}
