package br.com.crechesementinhas.controller;

import br.com.crechesementinhas.model.Aluno;
import br.com.crechesementinhas.model.Permissao;
import br.com.crechesementinhas.model.Sexo;
import br.com.crechesementinhas.service.AlunoService;
import br.com.crechesementinhas.service.CadastroAlunoException;
import br.com.crechesementinhas.service.ControleAcesso;
import br.com.crechesementinhas.util.CalculadoraIdade;
import br.com.crechesementinhas.util.Cpf;
import br.com.crechesementinhas.util.Navegacao;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.List;
import javafx.animation.PauseTransition;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import javafx.util.StringConverter;

/**
 * Controller da tela de alunos (aluno.fxml). RF-03.
 * Lista e busca pelo nome à esquerda; cadastro e edição à direita.
 * O controller só cuida da tela: as regras ficam no AlunoService.
 */
public class AlunoController {

    // "uuuu" (e não "yyyy") + STRICT: 31/02/2024 é recusado em vez de virar 29/02.
    private static final DateTimeFormatter FORMATO_DATA =
            DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT);

    @FXML private TextField campoBusca;
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

    private final AlunoService alunoService = new AlunoService();

    /** Aluno que está sendo editado; null quando o formulário é de um aluno novo. */
    private Aluno alunoEmEdicao;

    // Espera o usuário parar de digitar para buscar (evita uma consulta por tecla).
    private final PauseTransition esperaBusca = new PauseTransition(Duration.millis(250));

    @FXML
    private void initialize() {
        configurarTabela();
        configurarFormulario();

        // RNF-03: quem não pode cadastrar só consulta.
        boolean podeGravar = ControleAcesso.temPermissao(Permissao.CADASTRAR_MATRICULA);
        botaoSalvar.setDisable(!podeGravar);
        botaoNovo.setDisable(!podeGravar);

        esperaBusca.setOnFinished(e -> carregarLista());
        campoBusca.textProperty().addListener((obs, antigo, novo) -> esperaBusca.playFromStart());

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
            if (escolhido != null) {
                editar(escolhido);
            }
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
        alunoEmEdicao = null;
        tabelaAlunos.getSelectionModel().clearSelection();
        labelTituloForm.setText("Novo aluno");
        preencherCampos(null);
        mostrarJustificativa(false);
        esconderMensagem();
        campoNome.requestFocus();
    }

    private void editar(Aluno aluno) {
        alunoEmEdicao = aluno;
        labelTituloForm.setText("Editando: " + aluno.getNome());
        preencherCampos(aluno);
        mostrarJustificativa(true);
        esconderMensagem();
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
                    ? alunoService.cadastrar(dados)
                    : alunoService.atualizar(dados, campoJustificativa.getText());

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

    @FXML
    private void voltar() {
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
