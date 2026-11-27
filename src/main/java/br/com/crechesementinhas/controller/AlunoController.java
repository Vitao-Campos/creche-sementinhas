package br.com.crechesementinhas.controller;

import br.com.crechesementinhas.model.Aluno;
import br.com.crechesementinhas.model.AlunoPessoaAutorizada;
import br.com.crechesementinhas.model.AlunoResponsavel;
import br.com.crechesementinhas.model.Permissao;
import br.com.crechesementinhas.model.Sexo;
import br.com.crechesementinhas.model.TipoAutorizacao;
import br.com.crechesementinhas.service.AlunoService;
import br.com.crechesementinhas.service.AutorizadoService;
import br.com.crechesementinhas.service.CadastroAutorizadoException;
import br.com.crechesementinhas.service.HistoricoService;
import br.com.crechesementinhas.service.CadastroAlunoException;
import br.com.crechesementinhas.service.PossivelAlunoDuplicadoException;
import br.com.crechesementinhas.service.ControleAcesso;
import br.com.crechesementinhas.service.ResponsavelService;
import br.com.crechesementinhas.util.CalculadoraIdade;
import br.com.crechesementinhas.util.Dialogos;
import br.com.crechesementinhas.util.Cpf;
import br.com.crechesementinhas.util.Navegacao;
import br.com.crechesementinhas.util.Telefone;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javafx.animation.PauseTransition;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import javafx.util.StringConverter;

/**
 * Controller da tela de alunos (aluno.fxml). RF-02.
 * Lista e busca pelo nome à esquerda; cadastro e edição à direita.
 * O controller só cuida da tela: as regras ficam no AlunoService.
 */
public class AlunoController {

    // "uuuu" (e não "yyyy") + STRICT: 31/02/2024 é recusado em vez de virar 29/02.
    private static final DateTimeFormatter FORMATO_DATA =
            DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT);

    @FXML private TextField campoBusca;

    /** Conteúdo do formulário no último carregamento/gravação: serve para saber se há algo não salvo. */
    private String estadoSalvo; // null até a tela abrir pela primeira vez
    private boolean restaurandoSelecao;
    @FXML private TableView<Aluno> tabelaAlunos;
    @FXML private TableColumn<Aluno, String> colunaNome;
    @FXML private TableColumn<Aluno, String> colunaNascimento;
    @FXML private TableColumn<Aluno, String> colunaIdade;
    @FXML private TableColumn<Aluno, String> colunaCpf;
    @FXML private Label labelTotal;

    @FXML private Label labelTituloForm;
    @FXML private TextField campoNome;
    @FXML private DatePicker campoNascimento;
    @FXML private Label labelIdade;
    @FXML private ComboBox<Sexo> comboSexo;
    @FXML private TextField campoCpf;
    @FXML private TextField campoNaturalidade;
    @FXML private TextField campoNacionalidade;
    @FXML private TextArea campoObservacao;
    @FXML private Label rotuloJustificativa;
    @FXML private TextField campoJustificativa;
    @FXML private Label labelMensagem;
    @FXML private Button botaoSalvar;
    @FXML private Button botaoNovo;
    @FXML private Button botaoHistorico;

    private static final Color VERMELHO = Color.web("#c62828");
    private static final Color VERDE = Color.web("#2e7d32");
    private static final Color LARANJA = Color.web("#e65100");
    private static final Color CINZA = Color.web("#607d8b");

    // Ficha do aluno: responsáveis (somente leitura aqui) e pessoas autorizadas a buscar a criança.
    @FXML private VBox painelFicha;
    @FXML private Label labelResumoLegal;
    @FXML private Label labelResumoFinanceiro;
    @FXML private TableView<AlunoResponsavel> tabelaResponsaveisAluno;
    @FXML private TableColumn<AlunoResponsavel, String> colunaRespNome;
    @FXML private TableColumn<AlunoResponsavel, String> colunaRespParentesco;
    @FXML private TableColumn<AlunoResponsavel, String> colunaRespLegal;
    @FXML private TableColumn<AlunoResponsavel, String> colunaRespFinanceiro;
    @FXML private TableColumn<AlunoResponsavel, String> colunaRespTelefone;
    @FXML private TableColumn<AlunoResponsavel, String> colunaRespSituacao;
    @FXML private TableView<AlunoPessoaAutorizada> tabelaAutorizados;
    @FXML private TableColumn<AlunoPessoaAutorizada, String> colunaAutNome;
    @FXML private TableColumn<AlunoPessoaAutorizada, String> colunaAutRg;
    @FXML private TableColumn<AlunoPessoaAutorizada, String> colunaAutTelefone;
    @FXML private TableColumn<AlunoPessoaAutorizada, String> colunaAutParentesco;
    @FXML private TableColumn<AlunoPessoaAutorizada, String> colunaAutTipo;
    @FXML private TableColumn<AlunoPessoaAutorizada, String> colunaAutSituacao;
    @FXML private Button botaoRetirar;
    @FXML private TextField campoAutNome;
    @FXML private TextField campoAutRg;
    @FXML private TextField campoAutTelefone;
    @FXML private ComboBox<String> comboAutParentesco;
    @FXML private ComboBox<TipoAutorizacao> comboAutTipo;
    @FXML private Label labelAvisoFicha;
    @FXML private Button botaoAutorizar;

    private final AlunoService alunoService = new AlunoService();
    private final ResponsavelService responsavelService = new ResponsavelService();
    private final AutorizadoService autorizadoService = new AutorizadoService();
    private final HistoricoService historicoService = new HistoricoService();

    /** Aluno que está sendo editado; null quando o formulário é de um aluno novo. */
    private Aluno alunoEmEdicao;

    // Espera o usuário parar de digitar para buscar (evita uma consulta por tecla).
    private final PauseTransition esperaBusca = new PauseTransition(Duration.millis(250));

    @FXML
    private void initialize() {
        configurarTabela();
        configurarFormulario();
        configurarFicha();

        // RNF-03: quem não pode cadastrar só consulta.
        boolean podeGravar = ControleAcesso.temPermissao(Permissao.CADASTRAR_MATRICULA);
        botaoSalvar.setDisable(!podeGravar);
        botaoNovo.setDisable(!podeGravar);
        botaoAutorizar.setDisable(!podeGravar);

        esperaBusca.setOnFinished(e -> carregarLista());
        campoBusca.textProperty().addListener((obs, antigo, novo) -> esperaBusca.playFromStart());
        // Enter busca na hora (sem esperar a pausa da digitação); Esc limpa a busca.
        campoBusca.setOnKeyPressed(evento -> {
            if (evento.getCode() == KeyCode.ENTER) {
                esperaBusca.stop();
                carregarLista();
            } else if (evento.getCode() == KeyCode.ESCAPE) {
                campoBusca.clear();
            }
        });

        // Esc nos campos de texto do formulário = começar um cadastro novo (pergunta antes se houver algo não salvo).
        for (TextField campo : new TextField[] {campoNome, campoCpf, campoNaturalidade, campoNacionalidade,
                campoJustificativa}) {
            campo.addEventHandler(KeyEvent.KEY_PRESSED, evento -> {
                if (evento.getCode() == KeyCode.ESCAPE && !botaoNovo.isDisable()) {
                    novoAluno();
                }
            });
        }

        carregarLista();
        novoAluno();
    }

    private void configurarTabela() {
        colunaNome.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getNome()));
        colunaNascimento.setCellValueFactory(c -> new ReadOnlyStringWrapper(
                FORMATO_DATA.format(c.getValue().getDataNascimento())));
        colunaIdade.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getIdadeTexto()));
        colunaCpf.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getCpfFormatado()));

        tabelaAlunos.setPlaceholder(new Label("Nenhum aluno encontrado."));
        tabelaAlunos.getSelectionModel().selectedItemProperty().addListener((obs, antigo, escolhido) -> {
            if (escolhido == null || restaurandoSelecao) {
                return;
            }
            if (temAlteracoes() && !Dialogos.confirmarDescarte()) {
                // Desistiu de trocar: volta a marcar a criança que estava aberta.
                restaurandoSelecao = true;
                tabelaAlunos.getSelectionModel().select(antigo);
                restaurandoSelecao = false;
                return;
            }
            editar(escolhido);
        });
    }

    private void configurarFormulario() {
        comboSexo.getItems().setAll(Sexo.values());

        // O DatePicker mostra e lê datas como dd/MM/aaaa.
        campoNascimento.setConverter(new StringConverter<>() {
            @Override
            public String toString(LocalDate data) {
                return data == null ? "" : FORMATO_DATA.format(data);
            }

            @Override
            public LocalDate fromString(String texto) {
                try {
                    return texto == null || texto.isBlank() ? null
                            : LocalDate.parse(texto.trim(), FORMATO_DATA);
                } catch (DateTimeParseException e) {
                    return null;
                }
            }
        });
        // A idade aparece enquanto a data é digitada ou escolhida (RF-03).
        campoNascimento.getEditor().textProperty().addListener((obs, antigo, novo) -> atualizarIdade());

        // Ao sair do campo CPF, mostra com pontos e traço (se tiver 11 dígitos).
        campoCpf.focusedProperty().addListener((obs, tinha, tem) -> {
            if (!tem) {
                String digitos = Cpf.normalizar(campoCpf.getText());
                if (digitos != null && digitos.length() == 11) {
                    campoCpf.setText(Cpf.formatar(digitos));
                }
            }
        });
    }

    // ---------------------------------------------------------------- lista

    private void carregarLista() {
        try {
            List<Aluno> alunos = alunoService.listar(campoBusca.getText());
            tabelaAlunos.getItems().setAll(alunos);
            labelTotal.setText(alunos.size() == 1 ? "1 aluno" : alunos.size() + " alunos");
        } catch (SQLException e) {
            erroDeBanco(e);
        }
    }

    // ----------------------------------------------------------- formulário

    /** Limpa o formulário para cadastrar uma criança nova. */
    @FXML
    private void novoAluno() {
        if (temAlteracoes() && !Dialogos.confirmarDescarte()) {
            return;
        }
        alunoEmEdicao = null;
        tabelaAlunos.getSelectionModel().clearSelection();
        labelTituloForm.setText("Novo aluno");
        preencherCampos(null);
        mostrarJustificativa(false);
        esconderMensagem();
        atualizarFicha();
        guardarEstado();
        campoNome.requestFocus();
    }

    /** Foto do que está digitado, para comparar depois. */
    private String fotoDoFormulario() {
        return String.join("\u0001", campoNome.getText(), campoNascimento.getEditor().getText(),
                String.valueOf(comboSexo.getValue()), campoCpf.getText(), campoNaturalidade.getText(),
                campoNacionalidade.getText(), campoObservacao.getText(), campoJustificativa.getText());
    }

    private void guardarEstado() {
        estadoSalvo = fotoDoFormulario();
    }

    private boolean temAlteracoes() {
        return estadoSalvo != null && !fotoDoFormulario().equals(estadoSalvo);
    }

    private void editar(Aluno aluno) {
        alunoEmEdicao = aluno;
        labelTituloForm.setText("Editando: " + aluno.getNome());
        preencherCampos(aluno);
        mostrarJustificativa(true);
        esconderMensagem();
        atualizarFicha();
        guardarEstado();
    }

    private void preencherCampos(Aluno aluno) {
        campoNome.setText(aluno == null ? "" : aluno.getNome());
        campoNascimento.setValue(aluno == null ? null : aluno.getDataNascimento());
        campoNascimento.getEditor().setText(aluno == null ? ""
                : FORMATO_DATA.format(aluno.getDataNascimento()));
        comboSexo.setValue(aluno == null ? null : aluno.getSexo());
        campoCpf.setText(aluno == null ? "" : aluno.getCpfFormatado());
        campoNaturalidade.setText(aluno == null ? "" : valor(aluno.getNaturalidade()));
        campoNacionalidade.setText(aluno == null ? "" : valor(aluno.getNacionalidade()));
        campoObservacao.setText(aluno == null ? "" : valor(aluno.getObservacao()));
        campoJustificativa.clear();
        atualizarIdade();
    }

    private void mostrarJustificativa(boolean visivel) {
        rotuloJustificativa.setVisible(visivel);
        rotuloJustificativa.setManaged(visivel);
        campoJustificativa.setVisible(visivel);
        campoJustificativa.setManaged(visivel);
    }

    private void atualizarIdade() {
        LocalDate nascimento = lerDataOuNulo();
        labelIdade.setText(nascimento == null ? "—"
                : CalculadoraIdade.descrever(nascimento, LocalDate.now()));
    }

    // ---------------------------------------------------------------- salvar

    @FXML
    private void salvar() {
        LocalDate nascimento;
        try {
            nascimento = lerData();
        } catch (DateTimeParseException e) {
            mostrarMensagem("Data de nascimento inválida. Use o formato dd/mm/aaaa.", true);
            return;
        }

        Aluno dados = new Aluno();
        dados.setIdAluno(alunoEmEdicao == null ? null : alunoEmEdicao.getIdAluno());
        dados.setNome(campoNome.getText());
        dados.setDataNascimento(nascimento);
        dados.setSexo(comboSexo.getValue());
        dados.setCpf(campoCpf.getText());
        dados.setNaturalidade(campoNaturalidade.getText());
        dados.setNacionalidade(campoNacionalidade.getText());
        dados.setObservacao(campoObservacao.getText());

        boolean novo = alunoEmEdicao == null;
        try {
            Aluno salvo = novo
                    ? cadastrarComConfirmacao(dados)
                    : alunoService.atualizar(dados, campoJustificativa.getText());
            if (salvo == null) {
                mostrarMensagem("Cadastro cancelado.", false);
                return;
            }

            guardarEstado(); // gravou: o que está na tela deixa de ser "não salvo"
            campoBusca.clear();
            carregarLista();
            selecionarNaLista(salvo.getIdAluno());
            mostrarMensagem(novo ? "Aluno cadastrado com sucesso."
                                 : "Dados do aluno atualizados.", false);

        } catch (CadastroAlunoException e) {
            mostrarMensagem(e.getMessage(), true);
        } catch (SecurityException e) {
            mostrarMensagem(e.getMessage(), true);
        } catch (SQLException e) {
            erroDeBanco(e);
        }
    }

    /** Se já há criança com o mesmo nome e nascimento, pergunta antes de cadastrar; null = desistiu. */
    private Aluno cadastrarComConfirmacao(Aluno dados) throws SQLException {
        try {
            return alunoService.cadastrar(dados, false);
        } catch (PossivelAlunoDuplicadoException e) {
            Alert aviso = new Alert(Alert.AlertType.CONFIRMATION,
                    e.getMessage() + "\nPode ser a mesma criança. Cadastrar mesmo assim?",
                    ButtonType.YES, ButtonType.NO);
            aviso.setTitle("Possível cadastro duplicado");
            aviso.setHeaderText(null);
            if (aviso.showAndWait().orElse(ButtonType.NO) != ButtonType.YES) {
                return null;
            }
            return alunoService.cadastrar(dados, true);
        }
    }

    /** Marca o aluno na lista (o que também recarrega o formulário com os dados gravados). */
    private void selecionarNaLista(Integer idAluno) {
        for (Aluno aluno : tabelaAlunos.getItems()) {
            if (aluno.getIdAluno().equals(idAluno)) {
                tabelaAlunos.getSelectionModel().select(aluno);
                tabelaAlunos.scrollTo(aluno);
                return;
            }
        }
    }

    // ------------------------------------------------- ficha: responsáveis e autorizados

    private void configurarFicha() {
        colunaRespNome.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getNomeResponsavel()));
        colunaRespParentesco.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getGrauParentesco()));
        colunaRespLegal.setCellValueFactory(c ->
                new ReadOnlyStringWrapper(c.getValue().isResponsavelLegal() ? "Sim" : "Não"));
        colunaRespFinanceiro.setCellValueFactory(c ->
                new ReadOnlyStringWrapper(c.getValue().isResponsavelFinanceiro() ? "Sim" : "Não"));
        colunaRespTelefone.setCellValueFactory(c ->
                new ReadOnlyStringWrapper(Telefone.formatar(c.getValue().getTelefoneResponsavel())));
        colunaRespSituacao.setCellValueFactory(c -> new ReadOnlyStringWrapper(
                c.getValue().getDataFim() == null ? "Vigente"
                        : "Até " + FORMATO_DATA.format(c.getValue().getDataFim())));
        tabelaResponsaveisAluno.setPlaceholder(new Label("Nenhum responsável vinculado."));

        colunaAutNome.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getNome()));
        colunaAutRg.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getRg()));
        colunaAutTelefone.setCellValueFactory(c ->
                new ReadOnlyStringWrapper(c.getValue().getTelefoneFormatado()));
        colunaAutParentesco.setCellValueFactory(c ->
                new ReadOnlyStringWrapper(valor(c.getValue().getParentesco())));
        colunaAutTipo.setCellValueFactory(c ->
                new ReadOnlyStringWrapper(c.getValue().getTipoAutorizacao().getDescricao()));
        colunaAutSituacao.setCellValueFactory(c ->
                new ReadOnlyStringWrapper(c.getValue().isAtivo() ? "Ativa" : "Retirada"));
        tabelaAutorizados.setPlaceholder(new Label("Ninguém autorizado ainda."));
        tabelaAutorizados.getSelectionModel().selectedItemProperty().addListener(
                (obs, antigo, escolhido) -> atualizarBotaoRetirar());

        comboAutParentesco.getItems().setAll("Avó", "Avô", "Tia", "Tio", "Irmã", "Irmão",
                "Madrinha", "Padrinho", "Vizinho(a)", "Amigo(a) da família");
        comboAutTipo.getItems().setAll(TipoAutorizacao.values());

        campoAutTelefone.focusedProperty().addListener((obs, tinha, tem) -> {
            if (!tem) {
                campoAutTelefone.setText(Telefone.formatar(Telefone.normalizar(campoAutTelefone.getText())));
            }
        });
    }

    /** A ficha só funciona com a criança já gravada (os vínculos precisam do id dela). */
    private void atualizarFicha() {
        boolean salvo = alunoEmEdicao != null;
        painelFicha.setDisable(!salvo);
        botaoHistorico.setDisable(!salvo);
        tabelaResponsaveisAluno.getItems().clear();
        tabelaAutorizados.getItems().clear();
        limparFormularioAutorizado();
        labelResumoLegal.setText("");
        labelResumoFinanceiro.setText("");
        mostrarAvisoFicha(salvo ? "" : "Salve a criança para ver os responsáveis e autorizar pessoas.", CINZA);
        if (salvo) {
            carregarResponsaveisDoAluno();
            carregarAutorizados();
        }
        atualizarBotaoRetirar();
    }

    private void carregarResponsaveisDoAluno() {
        try {
            List<AlunoResponsavel> vinculos =
                    responsavelService.listarVinculosDoAluno(alunoEmEdicao.getIdAluno());
            tabelaResponsaveisAluno.getItems().setAll(vinculos);

            LocalDate hoje = LocalDate.now();
            List<AlunoResponsavel> vigentes = vinculos.stream().filter(v -> v.estaVigente(hoje)).toList();
            resumir(labelResumoLegal, "Responsável legal", "Nenhum responsável legal vigente (RN-01)",
                    vigentes.stream().filter(AlunoResponsavel::isResponsavelLegal).toList());
            resumir(labelResumoFinanceiro, "Responsável financeiro (paga a mensalidade)",
                    "Nenhum responsável financeiro indicado",
                    vigentes.stream().filter(AlunoResponsavel::isResponsavelFinanceiro).toList());
        } catch (SQLException e) {
            erroDeBanco(e);
        }
    }

    /** "Título: Nome (Parentesco), Nome (Parentesco)", ou o texto de falta em laranja. */
    private void resumir(Label rotulo, String titulo, String semNinguem, List<AlunoResponsavel> pessoas) {
        if (pessoas.isEmpty()) {
            rotulo.setText(semNinguem);
            rotulo.setTextFill(LARANJA);
        } else {
            rotulo.setText(titulo + ": " + pessoas.stream()
                    .map(v -> v.getNomeResponsavel() + " (" + v.getGrauParentesco() + ")")
                    .collect(Collectors.joining(", ")));
            rotulo.setTextFill(VERDE);
        }
    }

    private void carregarAutorizados() {
        try {
            tabelaAutorizados.getItems().setAll(
                    autorizadoService.listarPorAluno(alunoEmEdicao.getIdAluno()));
        } catch (SQLException e) {
            erroDeBanco(e);
        }
    }

    private void atualizarBotaoRetirar() {
        AlunoPessoaAutorizada escolhida = tabelaAutorizados.getSelectionModel().getSelectedItem();
        boolean podeGravar = ControleAcesso.temPermissao(Permissao.CADASTRAR_MATRICULA);
        botaoRetirar.setDisable(!podeGravar || escolhida == null || !escolhida.isAtivo());
    }

    @FXML
    private void autorizar() {
        AlunoPessoaAutorizada dados = new AlunoPessoaAutorizada();
        dados.setIdAluno(alunoEmEdicao == null ? null : alunoEmEdicao.getIdAluno());
        dados.setNome(campoAutNome.getText());
        dados.setRg(campoAutRg.getText());
        dados.setTelefone(campoAutTelefone.getText());
        dados.setParentesco(comboAutParentesco.getEditor().getText());
        dados.setTipoAutorizacao(comboAutTipo.getValue());

        try {
            autorizadoService.autorizar(dados);
            carregarAutorizados();
            limparFormularioAutorizado();
            mostrarAvisoFicha("Pessoa autorizada.", VERDE);
        } catch (CadastroAutorizadoException | SecurityException e) {
            mostrarAvisoFicha(e.getMessage(), VERMELHO);
        } catch (SQLException e) {
            erroDeBanco(e);
        }
    }

    @FXML
    private void retirarAutorizacao() {
        AlunoPessoaAutorizada escolhida = tabelaAutorizados.getSelectionModel().getSelectedItem();
        if (escolhida == null) {
            return;
        }
        TextInputDialog dialogo = new TextInputDialog();
        dialogo.setTitle("Retirar autorização");
        dialogo.setHeaderText(escolhida.getNome() + " deixará de poder buscar a criança.\n"
                + "O histórico é mantido; nada é apagado.");
        dialogo.setContentText("Motivo (opcional):");
        Optional<String> motivo = dialogo.showAndWait();
        if (motivo.isEmpty()) {
            return; // cancelou
        }

        try {
            autorizadoService.desativar(escolhida.getIdAlunoPessoaAutorizada(), motivo.get());
            carregarAutorizados();
            mostrarAvisoFicha("Autorização retirada.", VERDE);
        } catch (CadastroAutorizadoException | SecurityException e) {
            mostrarAvisoFicha(e.getMessage(), VERMELHO);
        } catch (SQLException e) {
            erroDeBanco(e);
        }
    }

    /** Abre, em janela separada, quem alterou o quê e quando na ficha desta criança (RNF-02). */
    @FXML
    private void verHistorico() {
        if (alunoEmEdicao == null) {
            return;
        }
        try {
            HistoricoController.abrir(botaoHistorico.getScene().getWindow(),
                    "Histórico de alterações — " + alunoEmEdicao.getNome(),
                    historicoService.consultarDoAluno(alunoEmEdicao.getIdAluno()));
        } catch (SQLException e) {
            erroDeBanco(e);
        }
    }

    private void limparFormularioAutorizado() {
        campoAutNome.clear();
        campoAutRg.clear();
        campoAutTelefone.clear();
        comboAutParentesco.setValue(null);
        comboAutParentesco.getEditor().clear();
        comboAutTipo.setValue(null);
    }

    /** Mensagem da ficha (fica junto das tabelas, sem precisar rolar a tela). */
    private void mostrarAvisoFicha(String texto, Color cor) {
        labelAvisoFicha.setText(texto);
        labelAvisoFicha.setTextFill(cor);
    }

    @FXML
    private void voltar() {
        if (temAlteracoes() && !Dialogos.confirmarDescarte()) {
            return;
        }
        Navegacao.abrir("menu.fxml", "Menu");
    }

    // ------------------------------------------------------------- utilitários

    /** Lê a data DIGITADA no campo (o texto, não o valor guardado pelo DatePicker). */
    private LocalDate lerData() {
        String texto = campoNascimento.getEditor().getText();
        return texto == null || texto.isBlank() ? null : LocalDate.parse(texto.trim(), FORMATO_DATA);
    }

    private LocalDate lerDataOuNulo() {
        try {
            return lerData();
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private String valor(String texto) {
        return texto == null ? "" : texto;
    }

    private void erroDeBanco(SQLException e) {
        e.printStackTrace();
        mostrarMensagem("Não foi possível acessar o banco de dados. Tente novamente.", true);
    }

    private void mostrarMensagem(String texto, boolean erro) {
        labelMensagem.setText(texto);
        labelMensagem.setTextFill(erro ? Color.web("#c62828") : Color.web("#2e7d32"));
        labelMensagem.setVisible(true);
        labelMensagem.setManaged(true);
    }

    private void esconderMensagem() {
        labelMensagem.setVisible(false);
        labelMensagem.setManaged(false);
    }
}
