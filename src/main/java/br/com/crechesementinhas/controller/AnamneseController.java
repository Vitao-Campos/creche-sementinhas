package br.com.crechesementinhas.controller;

import br.com.crechesementinhas.model.AlunoResponsavel;
import br.com.crechesementinhas.model.CabecalhoFicha;
import br.com.crechesementinhas.model.DadosFicha;
import br.com.crechesementinhas.model.FichaAnamnese;
import br.com.crechesementinhas.model.MatriculaComFicha;
import br.com.crechesementinhas.model.Permissao;
import br.com.crechesementinhas.service.AnamneseService;
import br.com.crechesementinhas.service.CadastroAnamneseException;
import br.com.crechesementinhas.service.ControleAcesso;
import br.com.crechesementinhas.util.CalculadoraIdade;
import br.com.crechesementinhas.util.GeradorPdfAnamnese;
import br.com.crechesementinhas.util.Navegacao;
import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.util.Duration;

/**
 * Tela da ficha de anamnese (RF-05, RF-09, RN-10): escolhe a criança matriculada, preenche ou atualiza
 * as respostas, salva e gera o PDF para visualizar e imprimir. A regra fica no AnamneseService.
 */
public class AnamneseController {

    private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM/uuuu");

    @FXML private TextField campoBusca;
    @FXML private TableView<MatriculaComFicha> tabelaMatriculas;
    @FXML private TableColumn<MatriculaComFicha, String> colunaNome;
    @FXML private TableColumn<MatriculaComFicha, String> colunaMatricula;
    @FXML private TableColumn<MatriculaComFicha, String> colunaFicha;
    @FXML private Label labelTotal;

    @FXML private Label labelCrianca;
    @FXML private Label labelNascimento;
    @FXML private Label labelTurma;
    @FXML private Label labelResponsaveis;
    @FXML private Label labelSituacao;

    @FXML private ScrollPane painelFormulario;
    @FXML private CheckBox checkAlergia;
    @FXML private TextField campoAlergia;
    @FXML private CheckBox checkRestricao;
    @FXML private TextField campoRestricao;
    @FXML private CheckBox checkProblemas;
    @FXML private TextField campoProblemas;
    @FXML private CheckBox checkMedicacao;
    @FXML private TextField campoMedicacao;
    @FXML private CheckBox checkCirurgia;
    @FXML private TextField campoCirurgia;
    @FXML private CheckBox checkFratura;
    @FXML private TextField campoFratura;
    @FXML private TextField campoAntitermico;
    @FXML private CheckBox checkMamadeira;
    @FXML private CheckBox checkFormula;
    @FXML private TextField campoFormula;
    @FXML private CheckBox checkChupeta;
    @FXML private CheckBox checkFralda;
    @FXML private CheckBox checkImagem;
    @FXML private TextField campoJustificativa;
    @FXML private Label labelMensagem;
    @FXML private Button botaoSalvar;
    @FXML private Button botaoVisualizar;
    @FXML private Button botaoSalvarPdf;

    private final AnamneseService anamneseService = new AnamneseService();
    private final PauseTransition esperaBusca = new PauseTransition(Duration.millis(300));

    private DadosFicha dadosAtuais;
    private String estadoSalvo; // null até a primeira carga
    private boolean restaurandoSelecao;

    /** Prepara a tela da ficha de anamnese: lista de matrículas, campos de descrição e busca. */
    @FXML
    private void initialize() {
        colunaNome.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().nomeAluno()));
        colunaMatricula.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().numeroMatricula()));
        colunaFicha.setCellValueFactory(c -> new ReadOnlyStringWrapper(
                c.getValue().fichaPreenchida() ? "Preenchida" : "Pendente"));
        tabelaMatriculas.setPlaceholder(new Label("Nenhuma criança com matrícula ativa."));
        tabelaMatriculas.getSelectionModel().selectedItemProperty().addListener((obs, antigo, escolhido) -> {
            if (escolhido == null || restaurandoSelecao) {
                return;
            }
            if (temAlteracoes() && !confirmarDescarte()) {
                restaurandoSelecao = true;
                tabelaMatriculas.getSelectionModel().select(antigo);
                restaurandoSelecao = false;
                return;
            }
            abrir(escolhido.idMatricula());
        });

        // Cada "sim" libera o campo de descrição; "não" limpa e trava.
        ligar(checkAlergia, campoAlergia);
        ligar(checkRestricao, campoRestricao);
        ligar(checkProblemas, campoProblemas);
        ligar(checkMedicacao, campoMedicacao);
        ligar(checkCirurgia, campoCirurgia);
        ligar(checkFratura, campoFratura);
        ligar(checkFormula, campoFormula);

        esperaBusca.setOnFinished(e -> carregarLista(null));
        campoBusca.textProperty().addListener((obs, antigo, novo) -> esperaBusca.playFromStart());
        campoBusca.setOnKeyPressed(evento -> {
            if (evento.getCode() == KeyCode.ENTER) {
                esperaBusca.stop();
                carregarLista(null);
            } else if (evento.getCode() == KeyCode.ESCAPE) {
                campoBusca.clear();
            }
        });

        carregarLista(null);
        mostrarNenhuma();
    }

    // ------------------------------------------------------------------ lista

    /** Recarrega a lista; se informado, marca de novo a matrícula indicada. */
    private void carregarLista(Integer idParaMarcar) {
        try {
            List<MatriculaComFicha> lista = anamneseService.listarMatriculas(campoBusca.getText());
            restaurandoSelecao = true;
            tabelaMatriculas.getItems().setAll(lista);
            if (idParaMarcar != null) {
                lista.stream().filter(m -> m.idMatricula() == idParaMarcar).findFirst()
                        .ifPresent(m -> tabelaMatriculas.getSelectionModel().select(m));
            }
            restaurandoSelecao = false;
            long pendentes = lista.stream().filter(m -> !m.fichaPreenchida()).count();
            labelTotal.setText(lista.size() + (lista.size() == 1 ? " criança" : " crianças")
                    + " — " + pendentes + (pendentes == 1 ? " ficha pendente" : " fichas pendentes"));
        } catch (SQLException e) {
            restaurandoSelecao = false;
            erroDeBanco(e);
        }
    }

    // ------------------------------------------------------------------ formulário

    /**
     * Liga a caixa de marcação ao campo de descrição: marcar libera o campo; desmarcar limpa e
     * trava.
     */
    private void ligar(CheckBox caixa, TextField descricao) {
        descricao.setDisable(true);
        caixa.selectedProperty().addListener((obs, antigo, marcada) -> {
            descricao.setDisable(!marcada);
            if (!marcada) {
                descricao.clear();
            }
        });
    }

    /** Deixa a tela sem nenhuma criança selecionada, com o formulário vazio e travado. */
    private void mostrarNenhuma() {
        dadosAtuais = null;
        labelCrianca.setText("Nenhuma criança selecionada");
        labelNascimento.setText("-");
        labelTurma.setText("-");
        labelResponsaveis.setText("-");
        labelSituacao.setText("-");
        preencher(null);
        painelFormulario.setDisable(true);
        botaoSalvar.setDisable(true);
        botaoVisualizar.setDisable(true);
        botaoSalvarPdf.setDisable(true);
        guardarEstado();
    }

    /**
     * Abre a ficha da matrícula escolhida: mostra os dados da criança e preenche as respostas já
     * salvas.
     */
    private void abrir(int idMatricula) {
        try {
            dadosAtuais = anamneseService.carregar(idMatricula);
        } catch (SQLException e) {
            erroDeBanco(e);
            return;
        } catch (CadastroAnamneseException e) {
            mostrarMensagem(e.getMessage(), true);
            return;
        }
        CabecalhoFicha c = dadosAtuais.cabecalho();
        labelCrianca.setText(c.nomeAluno());
        labelNascimento.setText(FORMATO_DATA.format(c.dataNascimento()) + "  ("
                + CalculadoraIdade.descrever(c.dataNascimento(), LocalDate.now()) + ")");
        labelTurma.setText(c.numeroMatricula() + " — " + c.nomeTurma());
        labelResponsaveis.setText(dadosAtuais.responsaveis().isEmpty()
                ? "Nenhum responsável vinculado"
                : dadosAtuais.responsaveis().stream().map(this::descreverResponsavel)
                        .collect(Collectors.joining("; ")));
        labelSituacao.setText(dadosAtuais.ficha() == null
                ? "Ainda não preenchida" : "Preenchida (pode ser atualizada a qualquer tempo)");
        preencher(dadosAtuais.ficha());

        boolean podeGravar = ControleAcesso.temPermissao(Permissao.CADASTRAR_MATRICULA);
        painelFormulario.setDisable(false);
        botaoSalvar.setDisable(!podeGravar);
        botaoVisualizar.setDisable(false);
        botaoSalvarPdf.setDisable(false);
        esconderMensagem();
        guardarEstado();
    }

    /** Monta o texto de um responsável, com parentesco e papel (legal e/ou financeiro). */
    private String descreverResponsavel(AlunoResponsavel v) {
        String papel = (v.isResponsavelLegal() ? "legal" : "")
                + (v.isResponsavelLegal() && v.isResponsavelFinanceiro() ? " e " : "")
                + (v.isResponsavelFinanceiro() ? "financeiro" : "");
        return v.getNomeResponsavel() + " (" + v.getGrauParentesco() + (papel.isEmpty() ? "" : ", " + papel) + ")";
    }

    /**
     * Preenche o formulário com as respostas da ficha (ou o esvazia quando a ficha ainda não
     * existe).
     */
    private void preencher(FichaAnamnese f) {
        checkAlergia.setSelected(f != null && f.isPossuiAlergia());
        campoAlergia.setText(f == null ? "" : texto(f.getDescricaoAlergia()));
        checkRestricao.setSelected(f != null && f.isRestricoesAlimentares());
        campoRestricao.setText(f == null ? "" : texto(f.getDescricaoRestricao()));
        checkProblemas.setSelected(f != null && f.isPossuiProblemasSaude());
        campoProblemas.setText(f == null ? "" : texto(f.getDescricaoProblemasSaude()));
        checkMedicacao.setSelected(f != null && f.isUsaMedicacaoContinua());
        campoMedicacao.setText(f == null ? "" : texto(f.getDescricaoMedicacao()));
        checkCirurgia.setSelected(f != null && f.isPossuiCirurgia());
        campoCirurgia.setText(f == null ? "" : texto(f.getDescricaoCirurgia()));
        checkFratura.setSelected(f != null && f.isPossuiFraturas());
        campoFratura.setText(f == null ? "" : texto(f.getDescricaoFratura()));
        campoAntitermico.setText(f == null ? "" : texto(f.getReceitaAntitermico()));
        checkMamadeira.setSelected(f != null && f.isUsaMamadeira());
        checkFormula.setSelected(f != null && f.isUsaFormula());
        campoFormula.setText(f == null ? "" : texto(f.getDescricaoFormula()));
        checkChupeta.setSelected(f != null && f.isUsaChupeta());
        checkFralda.setSelected(f != null && f.isUsaFralda());
        checkImagem.setSelected(f != null && f.isAutorizacaoImagem());
        campoJustificativa.clear();
    }

    /** Lê o formulário e monta a ficha de anamnese da matrícula aberta. */
    private FichaAnamnese lerFormulario() {
        FichaAnamnese f = new FichaAnamnese();
        f.setIdMatricula(dadosAtuais == null ? null : dadosAtuais.cabecalho().idMatricula());
        f.setPossuiAlergia(checkAlergia.isSelected());
        f.setDescricaoAlergia(campoAlergia.getText());
        f.setRestricoesAlimentares(checkRestricao.isSelected());
        f.setDescricaoRestricao(campoRestricao.getText());
        f.setPossuiProblemasSaude(checkProblemas.isSelected());
        f.setDescricaoProblemasSaude(campoProblemas.getText());
        f.setUsaMedicacaoContinua(checkMedicacao.isSelected());
        f.setDescricaoMedicacao(campoMedicacao.getText());
        f.setPossuiCirurgia(checkCirurgia.isSelected());
        f.setDescricaoCirurgia(campoCirurgia.getText());
        f.setPossuiFraturas(checkFratura.isSelected());
        f.setDescricaoFratura(campoFratura.getText());
        f.setReceitaAntitermico(campoAntitermico.getText());
        f.setUsaMamadeira(checkMamadeira.isSelected());
        f.setUsaFormula(checkFormula.isSelected());
        f.setDescricaoFormula(campoFormula.getText());
        f.setUsaChupeta(checkChupeta.isSelected());
        f.setUsaFralda(checkFralda.isSelected());
        f.setAutorizacaoImagem(checkImagem.isSelected());
        return f;
    }

    // ------------------------------------------------------------------ alterações não salvas

    /** Junta tudo o que está digitado numa só linha de texto, para comparar com o estado salvo. */
    private String fotoDoFormulario() {
        return String.join("\u0001",
                String.valueOf(checkAlergia.isSelected()), campoAlergia.getText(),
                String.valueOf(checkRestricao.isSelected()), campoRestricao.getText(),
                String.valueOf(checkProblemas.isSelected()), campoProblemas.getText(),
                String.valueOf(checkMedicacao.isSelected()), campoMedicacao.getText(),
                String.valueOf(checkCirurgia.isSelected()), campoCirurgia.getText(),
                String.valueOf(checkFratura.isSelected()), campoFratura.getText(),
                campoAntitermico.getText(),
                String.valueOf(checkMamadeira.isSelected()),
                String.valueOf(checkFormula.isSelected()), campoFormula.getText(),
                String.valueOf(checkChupeta.isSelected()), String.valueOf(checkFralda.isSelected()),
                String.valueOf(checkImagem.isSelected()), campoJustificativa.getText());
    }

    /** Guarda uma foto do formulário, para saber depois se algo foi alterado. */
    private void guardarEstado() {
        estadoSalvo = fotoDoFormulario();
    }

    /** Diz se o formulário tem dados digitados que ainda não foram salvos. */
    private boolean temAlteracoes() {
        return estadoSalvo != null && !fotoDoFormulario().equals(estadoSalvo);
    }

    /** Pergunta se a pessoa quer descartar as alterações não salvas. */
    private boolean confirmarDescarte() {
        ButtonType descartar = new ButtonType("Descartar", ButtonBar.ButtonData.OK_DONE);
        ButtonType continuar = new ButtonType("Continuar editando", ButtonBar.ButtonData.CANCEL_CLOSE);
        Alert aviso = new Alert(Alert.AlertType.CONFIRMATION,
                "Há alterações que ainda não foram salvas. Descartar?", descartar, continuar);
        aviso.setTitle("Alterações não salvas");
        aviso.setHeaderText(null);
        return aviso.showAndWait().orElse(continuar) == descartar;
    }

    // ------------------------------------------------------------------ ações

    /** Salva a ficha (cria na primeira vez, atualiza nas seguintes) e atualiza a lista. */
    @FXML
    private void salvar() {
        if (dadosAtuais == null) {
            return;
        }
        try {
            int idMatricula = dadosAtuais.cabecalho().idMatricula();
            anamneseService.salvar(lerFormulario(), campoJustificativa.getText());
            guardarEstado();
            carregarLista(idMatricula);
            abrir(idMatricula);
            mostrarMensagem("Ficha de anamnese salva.", false);
        } catch (CadastroAnamneseException | SecurityException e) {
            mostrarMensagem(e.getMessage(), true);
        } catch (SQLException e) {
            erroDeBanco(e);
        }
    }

    /** Gera o PDF numa pasta temporária e abre no leitor de PDF (de lá, é só imprimir). */
    @FXML
    private void visualizarPdf() {
        File temporario = gerarPdfTemporario();
        if (temporario != null) {
            abrirNoComputador(temporario);
        }
    }

    /** Salva o PDF da ficha no local escolhido pela pessoa. */
    @FXML
    private void salvarPdf() {
        if (!podeGerarPdf()) {
            return;
        }
        FileChooser seletor = new FileChooser();
        seletor.setTitle("Salvar ficha de anamnese em PDF");
        seletor.setInitialFileName(nomeArquivo());
        seletor.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF", "*.pdf"));
        File destino = seletor.showSaveDialog(tabelaMatriculas.getScene().getWindow());
        if (destino == null) {
            return;
        }
        try {
            GeradorPdfAnamnese.gerar(dadosAtuais, destino);
            mostrarMensagem("PDF salvo em: " + destino.getAbsolutePath(), false);
        } catch (IOException e) {
            mostrarMensagem("Erro ao gerar o PDF: " + e.getMessage(), true);
        }
    }

    /** O PDF sai do que está salvo: com alteração pendente, pede para salvar antes. */
    private boolean podeGerarPdf() {
        if (dadosAtuais == null) {
            return false;
        }
        if (temAlteracoes()) {
            mostrarMensagem("Há alterações não salvas. Salve a ficha antes de gerar o PDF.", true);
            return false;
        }
        return true;
    }

    /** Gera o PDF da ficha numa pasta temporária, para visualizar ou imprimir. */
    private File gerarPdfTemporario() {
        if (!podeGerarPdf()) {
            return null;
        }
        try {
            File temporario = File.createTempFile("anamnese_", ".pdf");
            temporario.deleteOnExit();
            GeradorPdfAnamnese.gerar(dadosAtuais, temporario);
            return temporario;
        } catch (IOException e) {
            mostrarMensagem("Erro ao gerar o PDF: " + e.getMessage(), true);
            return null;
        }
    }

    /** Sugere o nome do arquivo PDF, com o número da matrícula. */
    private String nomeArquivo() {
        return "anamnese_" + dadosAtuais.cabecalho().numeroMatricula() + ".pdf";
    }

    /** Abre o arquivo no programa padrão do computador (leitor de PDF), sem travar a tela. */
    private void abrirNoComputador(File arquivo) {
        if (!Desktop.isDesktopSupported()) {
            mostrarMensagem("Não foi possível abrir automaticamente. O arquivo está em: "
                    + arquivo.getAbsolutePath(), true);
            return;
        }
        // Fora da thread da tela, para ela não "congelar".
        new Thread(() -> {
            try {
                Desktop.getDesktop().open(arquivo);
            } catch (IOException e) {
                Platform.runLater(() -> mostrarMensagem("Erro ao abrir o PDF: " + e.getMessage(), true));
            }
        }).start();
    }

    /** Volta ao menu principal, perguntando antes se há alterações não salvas. */
    @FXML
    private void voltar() {
        if (temAlteracoes() && !confirmarDescarte()) {
            return;
        }
        Navegacao.abrir("menu.fxml", "Menu");
    }

    // ------------------------------------------------------------------ utilitários

    /** Devolve o texto, ou vazio quando for nulo. */
    private String texto(String valor) {
        return valor == null ? "" : valor;
    }

    /** Mostra uma mensagem abaixo do formulário, em vermelho (erro) ou verde (sucesso). */
    private void mostrarMensagem(String texto, boolean erro) {
        labelMensagem.setText(texto);
        labelMensagem.setTextFill(erro ? Color.web("#c62828") : Color.web("#2e7d32"));
        labelMensagem.setVisible(true);
        labelMensagem.setManaged(true);
    }

    /** Esconde a mensagem exibida abaixo do formulário. */
    private void esconderMensagem() {
        labelMensagem.setVisible(false);
        labelMensagem.setManaged(false);
    }

    /** Mostra na tela a mensagem de erro de acesso ao banco de dados. */
    private void erroDeBanco(SQLException e) {
        mostrarMensagem("Erro ao acessar o banco de dados: " + e.getMessage(), true);
    }
}
