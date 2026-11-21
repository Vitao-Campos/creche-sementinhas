package br.com.crechesementinhas.controller;

import br.com.crechesementinhas.model.Contrato;
import br.com.crechesementinhas.model.DadosContrato;
import br.com.crechesementinhas.model.Documento;
import br.com.crechesementinhas.model.MatriculaResumo;
import br.com.crechesementinhas.model.ResponsavelContrato;
import br.com.crechesementinhas.service.ContratoService;
import br.com.crechesementinhas.service.DocumentoService;
import br.com.crechesementinhas.util.Formatador;
import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.Window;

/**
 * Tela da Frente F: Contrato (RF-05, RF-08, RF-09, RN-05, RN-06, EX-04, FA-03)
 * e Documentos anexados (RF-06, EX-05).
 *
 * Cada campo com @FXML tem o MESMO nome do fx:id no arquivo ContratoDocumentos.fxml.
 */
public class ContratoDocumentosController {

    // ---------- topo ----------
    @FXML private ComboBox<MatriculaResumo> cbMatricula;
    @FXML private Label lblAluno;

    // ---------- aba Contrato: formulário ----------
    @FXML private ComboBox<ResponsavelContrato> cbFinanceiro;
    @FXML private ComboBox<String> cbPlano;
    @FXML private TextField txtValor;
    @FXML private TextField txtDiaVencimento;
    @FXML private ComboBox<String> cbFormaPagamento;
    @FXML private DatePicker dpVigencia;

    // ---------- aba Contrato: histórico de versões ----------
    @FXML private TableView<Contrato> tabelaContratos;
    @FXML private TableColumn<Contrato, Integer> colVersao;
    @FXML private TableColumn<Contrato, String> colStatus;
    @FXML private TableColumn<Contrato, String> colPlano;
    @FXML private TableColumn<Contrato, String> colValor;
    @FXML private TableColumn<Contrato, Integer> colDia;
    @FXML private TableColumn<Contrato, String> colFinanceiro;
    @FXML private TableColumn<Contrato, String> colVigencia;
    @FXML private TableColumn<Contrato, String> colAssinatura;

    // ---------- aba Contrato: assinatura e pré-visualização ----------
    @FXML private ComboBox<ResponsavelContrato> cbSignatario;
    @FXML private DatePicker dpAssinatura;
    @FXML private TextArea txtPreview;

    // ---------- aba Documentos ----------
    @FXML private ComboBox<String> cbTipoDocumento;
    @FXML private TableView<Documento> tabelaDocumentos;
    @FXML private TableColumn<Documento, String> colDocTipo;
    @FXML private TableColumn<Documento, String> colDocNome;
    @FXML private TableColumn<Documento, String> colDocData;
    @FXML private TableColumn<Documento, String> colDocStatus;

    // ---------- rodapé ----------
    @FXML private Label lblMensagem;

    private final ContratoService contratoService = new ContratoService();
    private final DocumentoService documentoService = new DocumentoService();

    /** Dados da matrícula selecionada (aluno, turma, responsáveis). */
    private DadosContrato dadosAtuais;

    // =================================================================
    //  MENSAGENS PARA O USUÁRIO
    // =================================================================

    private Window janela() {
        return cbMatricula.getScene().getWindow();
    }

    private void mensagem(String texto) {
        lblMensagem.setText(texto);
    }

    private void aviso(String texto) {
        Alert alerta = new Alert(Alert.AlertType.WARNING, texto, ButtonType.OK);
        alerta.setHeaderText(null);
        alerta.showAndWait();
    }

    private boolean confirmar(String pergunta) {
        Alert alerta = new Alert(Alert.AlertType.CONFIRMATION, pergunta, ButtonType.YES, ButtonType.NO);
        alerta.setHeaderText(null);
        Optional<ButtonType> resposta = alerta.showAndWait();
        return resposta.isPresent() && resposta.get() == ButtonType.YES;
    }

    private void erro(String titulo, Exception e) {
        Alert alerta = new Alert(Alert.AlertType.ERROR, e.getMessage(), ButtonType.OK);
        alerta.setHeaderText(titulo);
        alerta.showAndWait();
    }
}