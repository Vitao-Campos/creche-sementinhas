package br.com.crechesementinhas.controller;

import br.com.crechesementinhas.dao.AlunoDAO;
import br.com.crechesementinhas.dao.MatriculaDAO;
import br.com.crechesementinhas.model.Aluno;
import br.com.crechesementinhas.model.Matricula;
import br.com.crechesementinhas.service.MatriculaService;
import br.com.crechesementinhas.util.ControleAcesso;
import br.com.crechesementinhas.util.Navegacao;
import br.com.crechesementinhas.util.Permissao;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class MatriculaController {

    @FXML private TextField campoBusca;
    @FXML private TableView<Aluno> tabelaAlunos;
    @FXML private TableColumn<Aluno, Integer> colunaId;
    @FXML private TableColumn<Aluno, String> colunaNome;
    @FXML private TableColumn<Aluno, String> colunaNascimento;
    @FXML private TableColumn<Aluno, String> colunaIdade;
    @FXML private Label labelStatusAluno;

    @FXML private Label labelAlunoSelecionado;
    @FXML private Label labelMensalidadeEstimada;
    @FXML private TextField campoIdTurma;
    @FXML private DatePicker campoDataMatricula;
    @FXML private CheckBox checkAndarilha;
    @FXML private TextField campoMotivoAndarilha;
    @FXML private Button botaoMatricular;

    @FXML private Label labelMatriculaAtiva;
    @FXML private DatePicker campoDataEncerramento;
    @FXML private TextField campoMensalidadeAtual;
    @FXML private Label labelValorProporcional;
    @FXML private TextField campoJustificativaDesligamento;
    @FXML private Button botaoDesligar;

    @FXML private Label labelMensagem;

    private final AlunoDAO alunoDAO = new AlunoDAO();
    private final MatriculaDAO matriculaDAO = new MatriculaDAO();
    private final MatriculaService matriculaService = new MatriculaService();
    private final ObservableList<Aluno> listaAlunos = FXCollections.observableArrayList();
    private final DateTimeFormatter formatoData = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private final NumberFormat moedaBR = NumberFormat.getCurrencyInstance(Locale.of("pt", "BR"));

    private Aluno alunoSelecionado;
    private Matricula matriculaAtivaSelecionada;

    @FXML
    public void initialize() {
        configurarTabela();
        carregarAlunos();

        campoDataMatricula.setValue(LocalDate.now());
        campoDataEncerramento.setValue(LocalDate.now());

        botaoMatricular.setDisable(!ControleAcesso.temPermissao(Permissao.CADASTRAR_MATRICULA));
        botaoDesligar.setDisable(!ControleAcesso.temPermissao(Permissao.DESLIGAR_ALUNO));
    }

    private void configurarTabela() {
        colunaId.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().getIdAluno()));
        colunaNome.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNome()));
        colunaNascimento.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getDataNascimento() != null ? c.getValue().getDataNascimento().format(formatoData) : ""));
        colunaIdade.setCellValueFactory(c -> new SimpleStringProperty(
                matriculaService.formatarIdade(c.getValue().getDataNascimento())));

        FilteredList<Aluno> filtrados = new FilteredList<>(listaAlunos, p -> true);
        campoBusca.textProperty().addListener((obs, antigo, novo) -> {
            String termo = novo == null ? "" : novo.toLowerCase().trim();
            filtrados.setPredicate(a -> termo.isEmpty() || a.getNome().toLowerCase().contains(termo));
        });
        tabelaAlunos.setItems(filtrados);

        tabelaAlunos.getSelectionModel().selectedItemProperty().addListener((obs, antigo, novo) -> {
            if (novo != null) {
                selecionarAluno(novo);
            }
        });
    }

    private void carregarAlunos() {
        try {
            listaAlunos.setAll(alunoDAO.listarTodos());
        } catch (Exception e) {
            mostrarMensagem("Erro ao carregar alunos: " + e.getMessage(), true);
        }
    }

    private void selecionarAluno(Aluno aluno) {
        this.alunoSelecionado = aluno;
        labelAlunoSelecionado.setText(aluno.getNome() + " (ID: " + aluno.getIdAluno() + ")");

        BigDecimal mensalidade = matriculaService.calcularMensalidadePorFaixaEtaria(aluno.getDataNascimento());
        labelMensalidadeEstimada.setText(moedaBR.format(mensalidade));
        campoMensalidadeAtual.setText(mensalidade.toString());

        try {
            this.matriculaAtivaSelecionada = matriculaDAO.buscarAtivaPorAluno(aluno.getIdAluno());
            if (matriculaAtivaSelecionada != null) {
                labelMatriculaAtiva.setText(matriculaAtivaSelecionada.getNumeroMatricula() + " (Turma " + matriculaAtivaSelecionada.getIdTurma() + ")");
                labelStatusAluno.setText("Aluno possui matrícula ATIVA.");
                simularProporcional();
            } else {
                labelMatriculaAtiva.setText("Nenhuma matrícula ativa");
                labelStatusAluno.setText("Aluno disponível para nova matrícula.");
                labelValorProporcional.setText("R$ 0,00");
            }
        } catch (Exception e) {
            mostrarMensagem("Erro ao consultar matrícula: " + e.getMessage(), true);
        }
    }

    @FXML
    private void aoAlternarAndarilha() {
        boolean marcada = checkAndarilha.isSelected();
        campoMotivoAndarilha.setDisable(!marcada);
        if (!marcada) campoMotivoAndarilha.clear();
    }

    @FXML
    private void efetivarMatricula() {
        try {
            if (alunoSelecionado == null) {
                throw new IllegalArgumentException("EX-06: Selecione um aluno na tabela à esquerda.");
            }
            if (matriculaAtivaSelecionada != null) {
                throw new IllegalStateException("EX-06: Este aluno já possui uma matrícula ATIVA.");
            }

            int idTurma;
            try {
                idTurma = Integer.parseInt(campoIdTurma.getText().trim());
            } catch (Exception e) {
                throw new IllegalArgumentException("EX-06: Informe um ID numérico válido para a turma.");
            }

            Matricula nova = matriculaService.efetivarMatriculaParaAlunoExistente(
                    alunoSelecionado,
                    idTurma,
                    campoDataMatricula.getValue(),
                    checkAndarilha.isSelected(),
                    campoMotivoAndarilha.getText()
            );

            mostrarMensagem("Matrícula " + nova.getNumeroMatricula() + " efetivada com sucesso!", false);
            selecionarAluno(alunoSelecionado);
        } catch (Exception e) {
            mostrarMensagem(e.getMessage(), true);
        }
    }

    @FXML
    private void simularProporcional() {
        try {
            String valorTxt = campoMensalidadeAtual.getText().replace(",", ".").trim();
            if (valorTxt.isEmpty() || campoDataEncerramento.getValue() == null) {
                labelValorProporcional.setText("R$ 0,00");
                return;
            }
            BigDecimal proporcional = matriculaService.calcularValorProporcionalDesligamento(
                    new BigDecimal(valorTxt), campoDataEncerramento.getValue());
            labelValorProporcional.setText(moedaBR.format(proporcional));
        } catch (Exception e) {
            labelValorProporcional.setText("R$ 0,00");
        }
    }

    @FXML
    private void desligarAluno() {
        try {
            if (matriculaAtivaSelecionada == null) {
                throw new IllegalStateException("EX-06: Selecione um aluno com matrícula ATIVA para desligar.");
            }
            BigDecimal mensalidade = new BigDecimal(campoMensalidadeAtual.getText().replace(",", ".").trim());
            BigDecimal proporcional = matriculaService.desligarAluno(
                    matriculaAtivaSelecionada,
                    campoDataEncerramento.getValue(),
                    mensalidade,
                    campoJustificativaDesligamento.getText()
            );

            mostrarMensagem("Matrícula encerrada! Valor proporcional: " + moedaBR.format(proporcional), false);
            campoJustificativaDesligamento.clear();
            selecionarAluno(alunoSelecionado);
        } catch (Exception e) {
            mostrarMensagem(e.getMessage(), true);
        }
    }

    @FXML
    private void voltar() {
        Navegacao.abrir("menu.fxml", "Creche Sementinhas - Menu");
    }

    private void mostrarMensagem(String texto, boolean erro) {
        labelMensagem.setText(texto);
        labelMensagem.setStyle(erro
                ? "-fx-text-fill: #c62828; -fx-font-weight: bold;"
                : "-fx-text-fill: #2e7d32; -fx-font-weight: bold;");
        labelMensagem.setVisible(true);
        labelMensagem.setManaged(true);
    }
}