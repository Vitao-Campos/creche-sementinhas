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
    
    // =================================================================
    //  RECARGA DA TELA
    // =================================================================

    private void recarregarContratos() throws SQLException {
        tabelaContratos.setItems(FXCollections.observableArrayList(
                contratoService.listarContratos(dadosAtuais.getIdMatricula())));
        txtPreview.clear();
    }

    private void recarregarDocumentos() throws SQLException {
        tabelaDocumentos.setItems(FXCollections.observableArrayList(
                documentoService.listar(dadosAtuais.getIdMatricula())));
    }

    private void limparTela() {
        dadosAtuais = null;
        lblAluno.setText("");
        cbFinanceiro.getItems().clear();
        cbSignatario.getItems().clear();
        tabelaContratos.getItems().clear();
        tabelaDocumentos.getItems().clear();
        txtPreview.clear();
    }
    
    /**
     * Preenche o formulário: copia o contrato vigente (se existir) ou usa valores padrão.
     * @param rematricula true = sugere a vigência do ano seguinte (FA-03)
     */
    private void preencherFormulario(boolean rematricula) {
        Contrato vigente = contratoService.buscarVigente(tabelaContratos.getItems());

        if (vigente != null) {
            cbFinanceiro.setValue(dadosAtuais.buscarResponsavel(vigente.getIdResponsavelFinanceiro()));
            cbPlano.setValue(vigente.getPlano());
            txtValor.setText(vigente.getValorMensalidade() == null
                    ? "" : vigente.getValorMensalidade().toPlainString().replace('.', ','));
            txtDiaVencimento.setText(String.valueOf(vigente.getDiaVencimento()));
            cbFormaPagamento.setValue(vigente.getFormaPagamento());
            dpVigencia.setValue(rematricula
                    ? contratoService.vigenciaRematricula(vigente)
                    : vigente.getVigenciaAte());
        } else {
            // Primeiro contrato: sugere o 1º responsável legal como financeiro (RN-02)
            if (!dadosAtuais.getResponsaveisLegais().isEmpty()) {
                cbFinanceiro.setValue(dadosAtuais.getResponsaveisLegais().get(0));
            }
            cbPlano.setValue(contratoService.planoPadrao(dadosAtuais.getPeriodoTurma()));
            // INTEGRAÇÃO (etapa 4): trocar pela mensalidade calculada pela Frente B (RF-04 / RN-07)
            txtValor.clear();
            txtDiaVencimento.setText("10");
            cbFormaPagamento.setValue("BOLETO");
            dpVigencia.setValue(contratoService.vigenciaPadrao());
        }
    }
    
    // =================================================================
    //  MATRÍCULA
    // =================================================================

    @FXML
    private void onAtualizarLista() {
        carregarMatriculas();
    }

    private void carregarMatriculas() {
        try {
            List<MatriculaResumo> lista = contratoService.listarMatriculasAtivas();
            cbMatricula.setItems(FXCollections.observableArrayList(lista));
            mensagem(lista.size() + " matrícula(s) ativa(s) encontrada(s).");
        } catch (SQLException e) {
            erro("Erro ao buscar matrículas", e);
        }
    }

    private void carregarMatricula(MatriculaResumo selecionada) {
        limparTela();
        if (selecionada == null) {
            return;
        }
        try {
            dadosAtuais = contratoService.buscarDados(selecionada.getIdMatricula());
            if (dadosAtuais == null) {
                aviso("Matrícula não encontrada.");
                return;
            }
            lblAluno.setText(dadosAtuais.getNomeAluno() + "  |  Nascimento: "
                    + Formatador.data(dadosAtuais.getDataNascimentoAluno())
                    + "  |  Turma: " + dadosAtuais.getNomeTurma());

            // RF-05: preenche automaticamente com o que já está cadastrado
            cbFinanceiro.setItems(FXCollections.observableArrayList(dadosAtuais.getResponsaveis()));
            cbSignatario.setItems(FXCollections.observableArrayList(dadosAtuais.getResponsaveisLegais()));
            if (!dadosAtuais.getResponsaveisLegais().isEmpty()) {
                cbSignatario.getSelectionModel().selectFirst();
            }

            recarregarContratos();
            recarregarDocumentos();
            preencherFormulario(false);

            if (dadosAtuais.getResponsaveisLegais().isEmpty()) {
                aviso("RN-01: este aluno ainda não tem responsável legal cadastrado.\n"
                        + "Cadastre o responsável (tela de Responsáveis) antes de gerar o contrato.");
            }
        } catch (SQLException e) {
            erro("Erro ao carregar a matrícula", e);
        }
    }
    
    /**
     * O JavaFX chama este método sozinho, logo depois de abrir a tela.
     */
    @FXML
    private void initialize() {
        cbPlano.setItems(FXCollections.observableArrayList(
                "Integral", "Meio período - manhã", "Meio período - tarde"));
        cbFormaPagamento.setItems(FXCollections.observableArrayList(
                "BOLETO", "PIX", "CARTAO", "DINHEIRO"));
        cbTipoDocumento.setItems(FXCollections.observableArrayList(DocumentoService.TIPOS));

        // Liga cada coluna da tabela a um "getter" da classe Contrato
        // (ex.: "versao" -> getVersao()).
        colVersao.setCellValueFactory(new PropertyValueFactory<>("versao"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colPlano.setCellValueFactory(new PropertyValueFactory<>("plano"));
        colDia.setCellValueFactory(new PropertyValueFactory<>("diaVencimento"));
        colFinanceiro.setCellValueFactory(new PropertyValueFactory<>("nomeResponsavelFinanceiro"));
        // Colunas que precisam de formatação brasileira:
        colValor.setCellValueFactory(linha ->
                new SimpleStringProperty(Formatador.dinheiro(linha.getValue().getValorMensalidade())));
        colVigencia.setCellValueFactory(linha ->
                new SimpleStringProperty(Formatador.data(linha.getValue().getVigenciaAte())));
        colAssinatura.setCellValueFactory(linha -> {
            Contrato c = linha.getValue();
            String texto = c.getDataAssinatura() == null
                    ? c.getSituacaoAssinatura()
                    : "Assinado em " + Formatador.data(c.getDataAssinatura());
            return new SimpleStringProperty(texto);
        });

        colDocTipo.setCellValueFactory(new PropertyValueFactory<>("tipo"));
        colDocNome.setCellValueFactory(new PropertyValueFactory<>("nome"));
        colDocStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colDocData.setCellValueFactory(linha ->
                new SimpleStringProperty(Formatador.data(linha.getValue().getData())));

        // Ao clicar numa versão do contrato, mostra a pré-visualização dela
        tabelaContratos.getSelectionModel().selectedItemProperty()
                .addListener((obs, antigo, novo) -> mostrarPreview(novo));

        // Ao escolher outra matrícula, recarrega tudo
        cbMatricula.valueProperty().addListener((obs, antiga, nova) -> carregarMatricula(nova));

        dpAssinatura.setValue(LocalDate.now());
        carregarMatriculas();
    }

    private void mostrarPreview(Contrato c) {
        if (c == null || dadosAtuais == null) {
            txtPreview.clear();
            return;
        }
        try {
            txtPreview.setText(contratoService.montarTexto(dadosAtuais, c));
        } catch (IOException e) {
            erro("Erro ao montar o contrato", e);
        }
    }
}