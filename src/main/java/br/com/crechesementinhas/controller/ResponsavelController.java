package br.com.crechesementinhas.controller;

import br.com.crechesementinhas.model.Aluno;
import br.com.crechesementinhas.model.AlunoResponsavel;
import br.com.crechesementinhas.model.Permissao;
import br.com.crechesementinhas.model.Responsavel;
import br.com.crechesementinhas.service.AlunoService;
import br.com.crechesementinhas.service.CadastroResponsavelException;
import br.com.crechesementinhas.service.ControleAcesso;
import br.com.crechesementinhas.service.HistoricoService;
import br.com.crechesementinhas.service.ResponsavelService;
import br.com.crechesementinhas.util.Cpf;
import br.com.crechesementinhas.util.Dialogos;
import br.com.crechesementinhas.util.Mascara;
import br.com.crechesementinhas.util.Navegacao;
import br.com.crechesementinhas.util.Telefone;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.List;
import javafx.animation.PauseTransition;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import javafx.util.StringConverter;

/**
 * Controller da tela de responsáveis (responsavel.fxml). RF-02.
 * Esquerda: busca e lista. Centro: dados do responsável e, abaixo, os vínculos com as crianças.
 * O controller só cuida da tela: as regras ficam no ResponsavelService.
 */
public class ResponsavelController {

    private static final DateTimeFormatter FORMATO_DATA =
            DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT);

    private static final Color VERMELHO = Color.web("#c62828");
    private static final Color VERDE = Color.web("#2e7d32");
    private static final Color LARANJA = Color.web("#e65100");
    private static final Color CINZA = Color.web("#607d8b");

    @FXML private TextField campoBusca;

    /** Conteúdo do formulário no último carregamento/gravação: serve para saber se há algo não salvo. */
    private String estadoSalvo; // null até a tela abrir pela primeira vez
    private boolean restaurandoSelecao;
    @FXML private TableView<Responsavel> tabelaResponsaveis;
    @FXML private TableColumn<Responsavel, String> colunaNome;
    @FXML private TableColumn<Responsavel, String> colunaCpf;
    @FXML private TableColumn<Responsavel, String> colunaTelefone;
    @FXML private Label labelTotal;

    @FXML private Label labelTituloForm;
    @FXML private TextField campoNome;
    @FXML private TextField campoCpf;
    @FXML private TextField campoTelefone;
    @FXML private TextField campoEmail;
    @FXML private TextField campoCep;
    @FXML private TextField campoEndereco;
    @FXML private TextField campoProfissao;
    @FXML private Label rotuloJustificativa;
    @FXML private TextField campoJustificativa;
    @FXML private Label labelMensagem;
    @FXML private Button botaoSalvar;
    @FXML private Button botaoNovo;
    @FXML private Button botaoHistorico;

    @FXML private VBox painelVinculos;
    @FXML private Label labelAvisoVinculos;
    @FXML private TableView<AlunoResponsavel> tabelaVinculos;
    @FXML private TableColumn<AlunoResponsavel, String> colunaVinculoAluno;
    @FXML private TableColumn<AlunoResponsavel, String> colunaVinculoParentesco;
    @FXML private TableColumn<AlunoResponsavel, String> colunaVinculoLegal;
    @FXML private TableColumn<AlunoResponsavel, String> colunaVinculoFinanceiro;
    @FXML private TableColumn<AlunoResponsavel, String> colunaVinculoInicio;
    @FXML private TableColumn<AlunoResponsavel, String> colunaVinculoFim;
    @FXML private Button botaoEncerrar;
    @FXML private Button botaoSalvarPapeis;
    @FXML private ComboBox<Aluno> comboAluno;
    @FXML private ComboBox<String> comboParentesco;
    @FXML private DatePicker campoVinculoInicio;
    @FXML private CheckBox checkLegal;
    @FXML private CheckBox checkFinanceiro;
    @FXML private Button botaoVincular;

    private final ResponsavelService responsavelService = new ResponsavelService();
    private final AlunoService alunoService = new AlunoService();
    private final HistoricoService historicoService = new HistoricoService();

    /** Responsável que está sendo editado; null quando o formulário é de um novo. */
    private Responsavel responsavelEmEdicao;

    private final PauseTransition esperaBusca = new PauseTransition(Duration.millis(250));

    /** Prepara a tela de responsáveis: tabelas, formulário, vínculos, permissões e busca. */
    @FXML
    private void initialize() {
        configurarTabelas();
        configurarFormulario();

        // RNF-03: quem não pode cadastrar só consulta.
        boolean podeGravar = ControleAcesso.temPermissao(Permissao.CADASTRAR_MATRICULA);
        botaoSalvar.setDisable(!podeGravar);
        botaoNovo.setDisable(!podeGravar);
        botaoVincular.setDisable(!podeGravar);

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
        for (TextField campo : new TextField[] {campoNome, campoCpf, campoTelefone, campoEmail, campoCep,
                campoEndereco, campoProfissao, campoJustificativa}) {
            campo.addEventHandler(KeyEvent.KEY_PRESSED, evento -> {
                if (evento.getCode() == KeyCode.ESCAPE && !botaoNovo.isDisable()) {
                    novoResponsavel();
                }
            });
        }

        carregarAlunos();
        carregarLista();
        novoResponsavel();
    }

    /** Define o que cada coluna das tabelas de responsáveis e de vínculos mostra. */
    private void configurarTabelas() {
        colunaNome.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getNome()));
        colunaCpf.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getCpfFormatado()));
        colunaTelefone.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getTelefoneFormatado()));
        tabelaResponsaveis.setPlaceholder(new Label("Nenhum responsável encontrado."));
        tabelaResponsaveis.getSelectionModel().selectedItemProperty().addListener((obs, antigo, escolhido) -> {
            if (escolhido == null || restaurandoSelecao) {
                return;
            }
            if (temAlteracoes() && !Dialogos.confirmarDescarte()) {
                // Desistiu de trocar: volta a marcar o responsável que estava aberto.
                restaurandoSelecao = true;
                tabelaResponsaveis.getSelectionModel().select(antigo);
                restaurandoSelecao = false;
                return;
            }
            editar(escolhido);
        });

        colunaVinculoAluno.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getNomeAluno()));
        colunaVinculoParentesco.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getGrauParentesco()));
        colunaVinculoLegal.setCellValueFactory(c ->
                new ReadOnlyStringWrapper(c.getValue().isResponsavelLegal() ? "Sim" : "Não"));
        colunaVinculoFinanceiro.setCellValueFactory(c ->
                new ReadOnlyStringWrapper(c.getValue().isResponsavelFinanceiro() ? "Sim" : "Não"));
        colunaVinculoInicio.setCellValueFactory(c ->
                new ReadOnlyStringWrapper(FORMATO_DATA.format(c.getValue().getDataInicio())));
        colunaVinculoFim.setCellValueFactory(c -> new ReadOnlyStringWrapper(
                c.getValue().getDataFim() == null ? "Vigente" : FORMATO_DATA.format(c.getValue().getDataFim())));
        tabelaVinculos.setPlaceholder(new Label("Nenhuma criança vinculada."));

        // Só dá para encerrar ou alterar um vínculo ainda aberto; ao selecionar, as caixas mostram os papéis dele.
        tabelaVinculos.getSelectionModel().selectedItemProperty().addListener((obs, antigo, escolhido) -> {
            boolean aberto = escolhido != null && escolhido.getDataFim() == null;
            checkLegal.setSelected(aberto && escolhido.isResponsavelLegal());
            checkFinanceiro.setSelected(aberto && escolhido.isResponsavelFinanceiro());
            atualizarBotaoEncerrar();
        });
    }

    /**
     * Configura o formulário: parentescos, lista de crianças, máscaras de digitação e atalhos de
     * teclado.
     */
    private void configurarFormulario() {
        comboParentesco.getItems().setAll("Mãe", "Pai", "Avó", "Avô", "Tia", "Tio", "Madrasta", "Padrasto", "Tutor(a)");

        comboAluno.setConverter(new StringConverter<>() {
            @Override
            public String toString(Aluno aluno) {
                return aluno == null ? "" : aluno.getNome()
                        + " (nasc. " + FORMATO_DATA.format(aluno.getDataNascimento()) + ")";
            }

            @Override
            public Aluno fromString(String texto) {
                return null; // o combo não é editável
            }
        });

        campoVinculoInicio.setConverter(new StringConverter<>() {
            @Override
            public String toString(LocalDate data) {
                return data == null ? "" : FORMATO_DATA.format(data);
            }

            @Override
            public LocalDate fromString(String texto) {
                try {
                    return texto == null || texto.isBlank() ? null : LocalDate.parse(texto.trim(), FORMATO_DATA);
                } catch (DateTimeParseException e) {
                    return null;
                }
            }
        });

        Mascara.cpf(campoCpf);
        Mascara.data(campoVinculoInicio.getEditor());
        Mascara.telefone(campoTelefone);
        Mascara.cep(campoCep);
        // Ao sair do campo, mostra CPF, telefone e CEP com a máscara.
        campoCpf.focusedProperty().addListener((obs, tinha, tem) -> {
            if (!tem) {
                String digitos = Cpf.normalizar(campoCpf.getText());
                if (digitos != null && digitos.length() == 11) {
                    campoCpf.setText(Cpf.formatar(digitos));
                    avisarSeCpfRepetido();
                }
            }
        });
        campoTelefone.focusedProperty().addListener((obs, tinha, tem) -> {
            if (!tem) {
                campoTelefone.setText(Telefone.formatar(Telefone.normalizar(campoTelefone.getText())));
            }
        });
        campoCep.focusedProperty().addListener((obs, tinha, tem) -> {
            if (!tem) {
                campoCep.setText(formatarCep(campoCep.getText()));
            }
        });
    }

    /** Aviso na hora: o CPF digitado já é de outro responsável? (não impede nada; o salvar também confere) */
    private void avisarSeCpfRepetido() {
        try {
            Integer idAtual = responsavelEmEdicao == null ? null : responsavelEmEdicao.getIdResponsavel();
            if (responsavelService.cpfJaCadastrado(campoCpf.getText(), idAtual)) {
                mostrarMensagem("Já existe um responsável cadastrado com este CPF.", VERMELHO);
            }
        } catch (SQLException e) {
            // sem aviso antecipado; ao salvar o erro aparece normalmente
        }
    }

    // ---------------------------------------------------------------- lista

    /** Recarrega a lista de responsáveis conforme o texto da busca e atualiza o total. */
    private void carregarLista() {
        try {
            List<Responsavel> lista = responsavelService.listar(campoBusca.getText());
            tabelaResponsaveis.getItems().setAll(lista);
            labelTotal.setText(lista.size() == 1 ? "1 responsável" : lista.size() + " responsáveis");
        } catch (SQLException e) {
            erroDeBanco(e);
        }
    }

    /** Crianças que podem ser vinculadas (lista do cadastro de alunos). */
    private void carregarAlunos() {
        try {
            comboAluno.getItems().setAll(alunoService.listar(""));
        } catch (SQLException e) {
            erroDeBanco(e);
        }
    }

    // ----------------------------------------------------------- formulário

    /**
     * Limpa o formulário para cadastrar um responsável novo, perguntando antes se há alterações não
     * salvas.
     */
    @FXML
    private void novoResponsavel() {
        if (temAlteracoes() && !Dialogos.confirmarDescarte()) {
            return;
        }
        responsavelEmEdicao = null;
        tabelaResponsaveis.getSelectionModel().clearSelection();
        labelTituloForm.setText("Novo responsável");
        preencherCampos(null);
        mostrarJustificativa(false);
        esconderMensagem();
        atualizarPainelVinculos();
        guardarEstado();
        campoNome.requestFocus();
    }

    /** Foto do que está digitado, para comparar depois. */
    private String fotoDoFormulario() {
        return String.join("\u0001", campoNome.getText(), campoCpf.getText(), campoTelefone.getText(),
                campoEmail.getText(), campoCep.getText(), campoEndereco.getText(), campoProfissao.getText(),
                campoJustificativa.getText());
    }

    /** Guarda uma foto do formulário, para saber depois se algo foi alterado. */
    private void guardarEstado() {
        estadoSalvo = fotoDoFormulario();
    }

    /** Diz se o formulário tem dados digitados que ainda não foram salvos. */
    private boolean temAlteracoes() {
        return estadoSalvo != null && !fotoDoFormulario().equals(estadoSalvo);
    }

    /** Abre o responsável escolhido no formulário para edição. */
    private void editar(Responsavel responsavel) {
        responsavelEmEdicao = responsavel;
        labelTituloForm.setText("Editando: " + responsavel.getNome());
        preencherCampos(responsavel);
        mostrarJustificativa(true);
        esconderMensagem();
        atualizarPainelVinculos();
        guardarEstado();
    }

    /** Preenche o formulário com os dados do responsável (ou o esvazia quando ele é nulo). */
    private void preencherCampos(Responsavel r) {
        campoNome.setText(r == null ? "" : r.getNome());
        campoCpf.setText(r == null ? "" : r.getCpfFormatado());
        campoTelefone.setText(r == null ? "" : r.getTelefoneFormatado());
        campoEmail.setText(r == null ? "" : valor(r.getEmail()));
        campoCep.setText(r == null ? "" : formatarCep(r.getCep()));
        campoEndereco.setText(r == null ? "" : valor(r.getEndereco()));
        campoProfissao.setText(r == null ? "" : valor(r.getProfissao()));
        campoJustificativa.clear();
    }

    /** Mostra ou esconde o campo de motivo da alteração (só aparece ao editar). */
    private void mostrarJustificativa(boolean visivel) {
        rotuloJustificativa.setVisible(visivel);
        rotuloJustificativa.setManaged(visivel);
        campoJustificativa.setVisible(visivel);
        campoJustificativa.setManaged(visivel);
    }

    /** Cadastra o responsável novo ou grava a alteração. */
    @FXML
    private void salvar() {
        Responsavel dados = new Responsavel();
        dados.setIdResponsavel(responsavelEmEdicao == null ? null : responsavelEmEdicao.getIdResponsavel());
        dados.setNome(campoNome.getText());
        dados.setCpf(campoCpf.getText());
        dados.setTelefone(campoTelefone.getText());
        dados.setEmail(campoEmail.getText());
        dados.setCep(campoCep.getText());
        dados.setEndereco(campoEndereco.getText());
        dados.setProfissao(campoProfissao.getText());

        boolean novo = responsavelEmEdicao == null;
        try {
            Responsavel salvo = novo
                    ? responsavelService.cadastrar(dados)
                    : responsavelService.atualizar(dados, campoJustificativa.getText());

            guardarEstado(); // gravou: o que está na tela deixa de ser "não salvo"
            campoBusca.clear();
            carregarLista();
            selecionarNaLista(salvo.getIdResponsavel());
            mostrarMensagem(novo ? "Responsável cadastrado. Agora você pode vincular as crianças abaixo."
                                 : "Dados do responsável atualizados.", VERDE);

        } catch (CadastroResponsavelException | SecurityException e) {
            mostrarMensagem(e.getMessage(), VERMELHO);
        } catch (SQLException e) {
            erroDeBanco(e);
        }
    }

    /** Marca na tabela o responsável com o identificador informado. */
    private void selecionarNaLista(Integer id) {
        for (Responsavel r : tabelaResponsaveis.getItems()) {
            if (r.getIdResponsavel().equals(id)) {
                tabelaResponsaveis.getSelectionModel().select(r);
                tabelaResponsaveis.scrollTo(r);
                return;
            }
        }
    }

    // -------------------------------------------------------------- vínculos

    /** O painel só funciona com um responsável já gravado (o vínculo precisa do id dele). */
    private void atualizarPainelVinculos() {
        boolean salvo = responsavelEmEdicao != null;
        painelVinculos.setDisable(!salvo);
        botaoHistorico.setDisable(!salvo);
        tabelaVinculos.getItems().clear();
        comboAluno.setValue(null);
        comboParentesco.setValue(null);
        comboParentesco.getEditor().clear();
        checkLegal.setSelected(false);
        checkFinanceiro.setSelected(false);
        campoVinculoInicio.setValue(LocalDate.now());
        campoVinculoInicio.getEditor().setText(FORMATO_DATA.format(LocalDate.now()));
        mostrarAvisoVinculo(salvo ? "" : "Salve o responsável para poder vincular crianças.", CINZA);
        if (salvo) {
            carregarVinculos();
        }
        atualizarBotaoEncerrar();
    }

    /** Recarrega a tabela de crianças vinculadas ao responsável. */
    private void carregarVinculos() {
        try {
            tabelaVinculos.getItems().setAll(
                    responsavelService.listarVinculos(responsavelEmEdicao.getIdResponsavel()));
        } catch (SQLException e) {
            erroDeBanco(e);
        }
    }

    /**
     * Habilita os botões de encerrar e de salvar o vínculo só quando há um vínculo aberto
     * selecionado e o usuário pode gravar.
     */
    private void atualizarBotaoEncerrar() {
        AlunoResponsavel escolhido = tabelaVinculos.getSelectionModel().getSelectedItem();
        boolean podeGravar = ControleAcesso.temPermissao(Permissao.CADASTRAR_MATRICULA);
        boolean aberto = escolhido != null && escolhido.getDataFim() == null;
        botaoEncerrar.setDisable(!podeGravar || !aberto);
        botaoSalvarPapeis.setDisable(!podeGravar || !aberto);
    }

    /** Grava as alterações de responsável legal e financeiro do vínculo selecionado. */
    @FXML
    private void salvarPapeis() {
        AlunoResponsavel escolhido = tabelaVinculos.getSelectionModel().getSelectedItem();
        if (escolhido == null) {
            return;
        }
        try {
            responsavelService.alterarPapeis(escolhido.getIdAlunoResponsavel(), checkLegal.isSelected(),
                    checkFinanceiro.isSelected(), campoJustificativa.getText());
            carregarVinculos();
            avisarPendencias(escolhido.getIdAluno(), "Vínculo atualizado.");
        } catch (CadastroResponsavelException | SecurityException e) {
            mostrarAvisoVinculo(e.getMessage(), VERMELHO);
        } catch (SQLException e) {
            erroDeBanco(e);
        }
    }

    /** Vincula o responsável a uma criança, com parentesco, papéis e data de início. */
    @FXML
    private void vincular() {
        LocalDate inicio;
        try {
            String texto = campoVinculoInicio.getEditor().getText();
            inicio = texto == null || texto.isBlank() ? null : LocalDate.parse(texto.trim(), FORMATO_DATA);
        } catch (DateTimeParseException e) {
            mostrarAvisoVinculo("Data de início inválida. Use o formato dd/mm/aaaa.", VERMELHO);
            return;
        }

        Aluno aluno = comboAluno.getValue();
        AlunoResponsavel vinculo = new AlunoResponsavel();
        vinculo.setIdResponsavel(responsavelEmEdicao == null ? null : responsavelEmEdicao.getIdResponsavel());
        vinculo.setIdAluno(aluno == null ? null : aluno.getIdAluno());
        vinculo.setGrauParentesco(comboParentesco.getEditor().getText());
        vinculo.setResponsavelLegal(checkLegal.isSelected());
        vinculo.setResponsavelFinanceiro(checkFinanceiro.isSelected());
        vinculo.setDataInicio(inicio);

        try {
            responsavelService.vincular(vinculo);
            carregarVinculos();
            tabelaVinculos.getSelectionModel().clearSelection();
            comboAluno.setValue(null);
            checkLegal.setSelected(false);
            checkFinanceiro.setSelected(false);
            avisarPendencias(vinculo.getIdAluno(), "Vínculo criado.");
        } catch (CadastroResponsavelException | SecurityException e) {
            mostrarAvisoVinculo(e.getMessage(), VERMELHO);
        } catch (SQLException e) {
            erroDeBanco(e);
        }
    }

    /** Encerra o vínculo selecionado na data de hoje, mantendo o histórico. */
    @FXML
    private void encerrarVinculo() {
        AlunoResponsavel escolhido = tabelaVinculos.getSelectionModel().getSelectedItem();
        if (escolhido == null) {
            return;
        }
        Alert confirmacao = new Alert(Alert.AlertType.CONFIRMATION,
                "Encerrar hoje o vínculo com " + escolhido.getNomeAluno() + "?\n"
                + "O histórico é mantido; nada é apagado.", ButtonType.OK, ButtonType.CANCEL);
        confirmacao.setTitle("Encerrar vínculo");
        confirmacao.setHeaderText(null);
        if (confirmacao.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
            return;
        }

        try {
            responsavelService.encerrarVinculo(escolhido.getIdAlunoResponsavel(), LocalDate.now(),
                    campoJustificativa.getText());
            carregarVinculos();
            avisarPendencias(escolhido.getIdAluno(), "Vínculo encerrado.");
        } catch (CadastroResponsavelException | SecurityException e) {
            mostrarAvisoVinculo(e.getMessage(), VERMELHO);
        } catch (SQLException e) {
            erroDeBanco(e);
        }
    }

    /**
     * Depois de criar ou encerrar um vínculo, avisa o que ainda falta na criança:
     * responsável legal (RN-01) e responsável financeiro (quem paga a mensalidade).
     */
    private void avisarPendencias(Integer idAluno, String mensagemOk) throws SQLException {
        StringBuilder pendencias = new StringBuilder();
        if (idAluno != null && !responsavelService.alunoTemResponsavelLegal(idAluno)) {
            pendencias.append(" Atenção: esta criança ainda não tem nenhum responsável legal vigente;")
                      .append(" a matrícula só pode ser concluída com pelo menos um.");
        }
        if (idAluno != null && !responsavelService.alunoTemResponsavelFinanceiro(idAluno)) {
            pendencias.append(" Falta indicar o responsável financeiro (quem paga a mensalidade): marque a opção")
                      .append(" ao vincular o responsável legal ou cadastre outra pessoa que pague.");
        }
        if (pendencias.length() == 0) {
            mostrarAvisoVinculo(mensagemOk, VERDE);
        } else {
            mostrarAvisoVinculo(mensagemOk + pendencias, LARANJA);
        }
    }

    /** Abre, em janela separada, quem alterou o quê e quando neste responsável e nos vínculos dele (RNF-02). */
    @FXML
    private void verHistorico() {
        if (responsavelEmEdicao == null) {
            return;
        }
        try {
            HistoricoController.abrir(botaoHistorico.getScene().getWindow(),
                    "Histórico de alterações — " + responsavelEmEdicao.getNome(),
                    historicoService.consultarDoResponsavel(responsavelEmEdicao.getIdResponsavel()));
        } catch (SQLException e) {
            erroDeBanco(e);
        }
    }

    /** Volta ao menu principal, perguntando antes se há alterações não salvas. */
    @FXML
    private void voltar() {
        if (temAlteracoes() && !Dialogos.confirmarDescarte()) {
            return;
        }
        Navegacao.abrir("menu.fxml", "Menu");
    }

    // ------------------------------------------------------------- utilitários

    /**
     * Formata o CEP com o traço (00000-000); devolve o texto sem alteração se não tiver 8 números.
     */
    private String formatarCep(String texto) {
        String digitos = texto == null ? "" : texto.replaceAll("\\D", "");
        return digitos.length() == 8 ? digitos.substring(0, 5) + "-" + digitos.substring(5) : digitos;
    }

    /** Devolve o texto, ou vazio quando for nulo (para preencher campos). */
    private String valor(String texto) {
        return texto == null ? "" : texto;
    }

    /** Mostra na tela a mensagem de erro de acesso ao banco de dados. */
    private void erroDeBanco(SQLException e) {
        e.printStackTrace();
        mostrarMensagem("Não foi possível acessar o banco de dados. Tente novamente.", VERMELHO);
    }

    /** Mostra uma mensagem abaixo do formulário, na cor informada. */
    private void mostrarMensagem(String texto, Color cor) {
        labelMensagem.setText(texto);
        labelMensagem.setTextFill(cor);
        labelMensagem.setVisible(true);
        labelMensagem.setManaged(true);
    }

    /** Mensagem do painel de vínculos (fica junto da tabela, sem precisar rolar a tela). */
    private void mostrarAvisoVinculo(String texto, Color cor) {
        labelAvisoVinculos.setText(texto);
        labelAvisoVinculos.setTextFill(cor);
    }

    /** Esconde a mensagem exibida abaixo do formulário. */
    private void esconderMensagem() {
        labelMensagem.setVisible(false);
        labelMensagem.setManaged(false);
    }
}
