package br.com.crechesementinhas.controller;

import br.com.crechesementinhas.model.Contrato;
import br.com.crechesementinhas.model.DadosContrato;
import br.com.crechesementinhas.model.Documento;
import br.com.crechesementinhas.model.MatriculaResumo;
import br.com.crechesementinhas.model.ResponsavelContrato;
import br.com.crechesementinhas.service.ContratoService;
import br.com.crechesementinhas.service.DocumentoService;
import br.com.crechesementinhas.util.Formatador;
import br.com.crechesementinhas.util.Navegacao;
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
    
    // =================================================================
    //  CONTRATO
    // =================================================================

    /** Lê os campos do formulário e monta um objeto Contrato. */
    private Contrato lerFormulario() {
        if (dadosAtuais == null) {
            throw new IllegalArgumentException("Selecione uma matrícula.");
        }
        Contrato c = new Contrato();
        ResponsavelContrato financeiro = cbFinanceiro.getValue();
        c.setIdResponsavelFinanceiro(financeiro == null ? 0 : financeiro.getIdResponsavel());
        c.setPlano(cbPlano.getValue());
        BigDecimal valor = Formatador.lerDinheiro(txtValor.getText());
        c.setValorMensalidade(valor);
        try {
            c.setDiaVencimento(Integer.parseInt(txtDiaVencimento.getText().trim()));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Dia de vencimento inválido.");
        }
        c.setFormaPagamento(cbFormaPagamento.getValue());
        c.setVigenciaAte(dpVigencia.getValue());
        return c;
    }

    @FXML
    private void onGerarNovaVersao() {
        try {
            Contrato novo = lerFormulario();

            // RN-12: vencimento fora do padrão (10 ou 25) só com confirmação
            if (contratoService.diaForaDoPadrao(novo.getDiaVencimento())
                    && !confirmar("O vencimento padrão da creche é dia 10 ou 25.\n"
                    + "Confirma o dia " + novo.getDiaVencimento() + " (exceção combinada em contrato)?")) {
                return;
            }

            // EX-04: avisa que a versão vigente será encerrada
            Contrato vigente = contratoService.buscarVigente(tabelaContratos.getItems());
            if (vigente != null && !confirmar("A versão " + vigente.getVersao()
                    + " (vigente) será ENCERRADA e uma nova versão será criada.\nDeseja continuar?")) {
                return;
            }

            int versao = contratoService.gerarNovaVersao(novo, dadosAtuais);
            recarregarContratos();
            tabelaContratos.getSelectionModel().selectFirst(); // a mais nova fica no topo
            mensagem("Contrato versão " + versao + " gerado. Imprima e colha a assinatura do responsável legal.");

        } catch (IllegalArgumentException e) {
            aviso(e.getMessage());
        } catch (SQLException e) {
            erro("Erro ao gravar o contrato", e);
        }
    }
    
    @FXML
    private void onRematricula() {
        if (dadosAtuais == null) {
            aviso("Selecione uma matrícula.");
            return;
        }
        preencherFormulario(true);
        aviso("FA-03 - Rematrícula: o formulário foi preenchido com o contrato atual e a vigência do próximo ano.\n"
                + "Confira o valor da mensalidade (ele muda com a idade da criança - RN-07) "
                + "e clique em \"Gerar nova versão\".");
    }    

    private Contrato contratoSelecionado() {
        Contrato c = tabelaContratos.getSelectionModel().getSelectedItem();
        if (c == null) {
            aviso("Selecione na tabela a versão do contrato.");
        }
        return c;
    }

    /** Abre um arquivo com o programa padrão do Windows (leitor de PDF, visualizador de fotos...). */
    private void abrirNoComputador(File arquivo) {
        if (!Desktop.isDesktopSupported()) {
            aviso("Não foi possível abrir automaticamente. O arquivo está em:\n" + arquivo.getAbsolutePath());
            return;
        }
        // Roda fora da thread da tela para ela não "congelar"
        new Thread(() -> {
            try {
                Desktop.getDesktop().open(arquivo);
            } catch (IOException e) {
                Platform.runLater(() -> erro("Erro ao abrir o arquivo", e));
            }
        }).start();
    }

    @FXML
    private void onSalvarPdf() {
        Contrato c = contratoSelecionado();
        if (c == null) {
            return;
        }
        FileChooser seletor = new FileChooser();
        seletor.setTitle("Salvar contrato em PDF");
        seletor.setInitialFileName(contratoService.nomeArquivoPdf(dadosAtuais, c));
        seletor.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF", "*.pdf"));
        File destino = seletor.showSaveDialog(janela());
        if (destino == null) {
            return; // a pessoa clicou em Cancelar
        }
        try {
            contratoService.gerarPdf(dadosAtuais, c, destino);
            mensagem("PDF salvo em: " + destino.getAbsolutePath());
        } catch (IOException e) {
            erro("Erro ao gerar o PDF", e);
        }
    }

    /** Gera o PDF numa pasta temporária e abre no leitor de PDF do Windows (de lá, é só imprimir). */
    @FXML
    private void onAbrirImprimir() {
        Contrato c = contratoSelecionado();
        if (c == null) {
            return;
        }
        try {
            File temporario = File.createTempFile("contrato_", ".pdf");
            temporario.deleteOnExit();
            contratoService.gerarPdf(dadosAtuais, c, temporario);
            abrirNoComputador(temporario);
        } catch (IOException e) {
            erro("Erro ao gerar o PDF", e);
        }
    }
    
    // =================================================================
    //  DOCUMENTOS (anexos)
    // =================================================================

    @FXML
    private void onAnexarDocumento() {
        if (dadosAtuais == null) {
            aviso("Selecione uma matrícula.");
            return;
        }
        if (cbTipoDocumento.getValue() == null) {
            aviso("Selecione o tipo do documento antes de escolher o arquivo.");
            return;
        }
        FileChooser seletor = new FileChooser();
        seletor.setTitle("Escolher documento (PDF, JPEG ou PNG até 5 MB)");
        seletor.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("PDF, JPEG ou PNG", "*.pdf", "*.jpg", "*.jpeg", "*.png"),
                new FileChooser.ExtensionFilter("Todos os arquivos", "*.*"));
        File arquivo = seletor.showOpenDialog(janela());
        if (arquivo == null) {
            return;
        }
        try {
            documentoService.anexar(dadosAtuais.getIdMatricula(), cbTipoDocumento.getValue(), arquivo);
            recarregarDocumentos();
            mensagem("Documento anexado: " + arquivo.getName());
        } catch (IllegalArgumentException e) {
            aviso(e.getMessage()); // EX-05: a pasta digital continua intacta
        } catch (IOException | SQLException e) {
            erro("Erro ao anexar o documento", e);
        }
    }

    @FXML
    private void onAbrirDocumento() {
        Documento d = tabelaDocumentos.getSelectionModel().getSelectedItem();
        if (d == null) {
            aviso("Selecione um documento na tabela.");
            return;
        }
        try {
            abrirNoComputador(documentoService.arquivoDe(d));
        } catch (IllegalArgumentException e) {
            aviso(e.getMessage());
        }
    }

    @FXML
    private void onValidarDocumento() {
        try {
            documentoService.validar(tabelaDocumentos.getSelectionModel().getSelectedItem());
            recarregarDocumentos();
            mensagem("Documento marcado como VALIDADO.");
        } catch (IllegalArgumentException e) {
            aviso(e.getMessage());
        } catch (SQLException e) {
            erro("Erro ao validar o documento", e);
        }
    }
    
    @FXML
    private void onRegistrarAssinatura() {
        try {
            Contrato selecionado = tabelaContratos.getSelectionModel().getSelectedItem();
            contratoService.registrarAssinatura(selecionado, cbSignatario.getValue(), dpAssinatura.getValue());
            recarregarContratos();
            mensagem("Assinatura registrada. O contrato agora é válido (RN-05).");
        } catch (IllegalArgumentException e) {
            aviso(e.getMessage());
        } catch (SQLException e) {
            erro("Erro ao registrar a assinatura", e);
        }
    }
    
    // =================================================================
    //  NAVEGAÇÃO
    // =================================================================

    @FXML
    private void voltar() {
        Navegacao.abrir("menu.fxml", "Menu");
    }
}