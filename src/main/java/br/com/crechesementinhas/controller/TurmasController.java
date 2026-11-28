package br.com.crechesementinhas.controller;

import br.com.crechesementinhas.model.AlunoTurma;
import br.com.crechesementinhas.model.Permissao;
import br.com.crechesementinhas.model.SituacaoFaixaEtaria;
import br.com.crechesementinhas.model.Turma;
import br.com.crechesementinhas.service.ControleAcesso;
import br.com.crechesementinhas.service.HistoricoService;
import br.com.crechesementinhas.service.MatriculaException;
import br.com.crechesementinhas.service.TurmaService;
import br.com.crechesementinhas.util.CalculadoraIdade;
import br.com.crechesementinhas.util.Formatador;
import br.com.crechesementinhas.util.Navegacao;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.util.StringConverter;

/**
 * Tela "Consulta e Turmas" (turmas.fxml), Frente E.
 * Só cuida da tela: as regras ficam no TurmaService.
 */
public class TurmasController {

    // Filtros e tabela de alunos
    @FXML private TextField campoBusca;
    @FXML private ComboBox<Turma> comboFiltroTurma;
    @FXML private CheckBox checkSomenteAtivos;
    @FXML private CheckBox checkSomenteAlerta;
    @FXML private Label labelAlerta;
    @FXML private TableView<AlunoTurma> tabelaAlunos;
    @FXML private TableColumn<AlunoTurma, String> colunaNome;
    @FXML private TableColumn<AlunoTurma, String> colunaIdade;
    @FXML private TableColumn<AlunoTurma, String> colunaTurma;
    @FXML private TableColumn<AlunoTurma, String> colunaFaixa;
    @FXML private TableColumn<AlunoTurma, SituacaoFaixaEtaria> colunaSituacao;
    @FXML private TableColumn<AlunoTurma, String> colunaAndarilha;
    @FXML private Label labelTotal;

    // Painel da criança selecionada
    @FXML private Label labelAluno;
    @FXML private Label labelDetalhe;
    @FXML private Button botaoHistorico;
    @FXML private ComboBox<Turma> comboNovaTurma;
    @FXML private TextField campoMotivoTroca;
    @FXML private Label labelAvisoTroca;
    @FXML private Button botaoTrocar;
    @FXML private CheckBox checkAndarilha;
    @FXML private TextField campoMotivoAndarilha;
    @FXML private DatePicker campoAndarilhaAte;
    @FXML private Button botaoAndarilha;
    @FXML private Label labelMensagem;

    // Aba de ocupação
    @FXML private TableView<Turma> tabelaTurmas;
    @FXML private TableColumn<Turma, String> colunaTurmaNome;
    @FXML private TableColumn<Turma, String> colunaTurmaFaixa;
    @FXML private TableColumn<Turma, String> colunaTurmaPeriodo;
    @FXML private TableColumn<Turma, String> colunaTurmaSala;
    @FXML private TableColumn<Turma, String> colunaTurmaOcupacao;
    @FXML private TableColumn<Turma, String> colunaTurmaVagas;

    private final TurmaService turmaService = new TurmaService();
    private final HistoricoService historicoService = new HistoricoService();

    private AlunoTurma selecionado;

    /** Prepara a tela de turmas: tabelas, filtros, buscas e permissões. */
    @FXML
    private void initialize() {
        configurarTabelas();
        configurarData(campoAndarilhaAte);

        campoBusca.textProperty().addListener((obs, antigo, novo) -> carregarAlunos());
        comboFiltroTurma.valueProperty().addListener((obs, antigo, novo) -> carregarAlunos());
        checkSomenteAtivos.selectedProperty().addListener((obs, antigo, novo) -> carregarAlunos());
        checkSomenteAlerta.selectedProperty().addListener((obs, antigo, novo) -> carregarAlunos());
        comboNovaTurma.valueProperty().addListener((obs, antigo, novo) -> atualizarAvisoTroca());
        tabelaAlunos.getSelectionModel().selectedItemProperty()
                .addListener((obs, antigo, novo) -> selecionar(novo));

        carregarTurmas();
        carregarAlunos();
        selecionar(null);
    }

    // =================================================================
    //  CARGA DOS DADOS
    // =================================================================

    /** Carrega as turmas nas tabelas e caixas de seleção, mantendo o filtro atual. */
    private void carregarTurmas() {
        try {
            List<Turma> turmas = turmaService.listarTurmas();
            Turma filtro = comboFiltroTurma.getValue();
            comboFiltroTurma.setItems(FXCollections.observableArrayList(turmas));
            comboNovaTurma.setItems(FXCollections.observableArrayList(turmas));
            tabelaTurmas.setItems(FXCollections.observableArrayList(turmas));
            if (filtro != null) {
                turmas.stream().filter(t -> t.getIdTurma().equals(filtro.getIdTurma()))
                        .findFirst().ifPresent(comboFiltroTurma::setValue);
            }
        } catch (SQLException e) {
            erro("Não foi possível carregar as turmas: " + e.getMessage());
        }
    }

    /** Carrega a lista de crianças conforme os filtros e mantém a criança selecionada. */
    private void carregarAlunos() {
        Integer idSelecionado = selecionado == null ? null : selecionado.getIdAluno();
        try {
            List<AlunoTurma> alunos = turmaService.buscar(campoBusca.getText(), comboFiltroTurma.getValue(),
                    checkSomenteAtivos.isSelected(), checkSomenteAlerta.isSelected());
            tabelaAlunos.setItems(FXCollections.observableArrayList(alunos));
            labelTotal.setText(alunos.size() + (alunos.size() == 1 ? " criança" : " crianças"));
            atualizarFaixaDeAlerta();

            // Mantém a mesma criança selecionada depois de recarregar
            if (idSelecionado != null) {
                alunos.stream().filter(a -> a.getIdAluno() == idSelecionado).findFirst()
                        .ifPresent(a -> tabelaAlunos.getSelectionModel().select(a));
            }
        } catch (SQLException e) {
            erro("Não foi possível carregar as crianças: " + e.getMessage());
        }
    }

    /** Faixa laranja no topo: quantas crianças matriculadas estão fora da faixa da turma (todas as turmas). */
    private void atualizarFaixaDeAlerta() {
        try {
            long fora = turmaService.contarForaDaFaixa(turmaService.buscar("", null, true, true));
            if (fora == 0) {
                esconder(labelAlerta);
            } else {
                mostrar(labelAlerta, "Atenção: " + fora + (fora == 1 ? " criança está" : " crianças estão")
                        + " fora da faixa etária da própria turma. Marque \"Só com alerta de faixa etária\" para ver.");
            }
        } catch (SQLException e) {
            esconder(labelAlerta);
        }
    }

    /** Mostra os dados da criança escolhida e o que pode ser feito com ela. */
    private void selecionar(AlunoTurma aluno) {
        selecionado = aluno;
        esconder(labelAvisoTroca);
        comboNovaTurma.setValue(null);
        campoMotivoTroca.clear();

        boolean ativa = aluno != null && aluno.temMatriculaAtiva();
        boolean podeAlterar = ControleAcesso.temPermissao(Permissao.CONFIRMAR_MATRICULA); // RNF-03

        if (aluno == null) {
            labelAluno.setText("Selecione uma criança na tabela");
            labelDetalhe.setText("");
        } else {
            LocalDate hoje = LocalDate.now();
            labelAluno.setText(aluno.getNome());
            labelDetalhe.setText(CalculadoraIdade.descrever(aluno.getDataNascimento(), hoje)
                    + " (" + aluno.idadeEmMeses(hoje) + " meses)\n"
                    + (ativa
                       ? "Matrícula " + aluno.getNumeroMatricula() + " na turma " + aluno.getNomeTurma()
                         + " (" + aluno.getFaixaTurmaTexto() + " meses)\n"
                         + dica(aluno.situacaoFaixa(hoje))
                       : "Sem matrícula ativa (matricule pela tela Matrícula)."));
        }

        checkAndarilha.setSelected(ativa && aluno.isAndarilha());
        campoMotivoAndarilha.setText(ativa && aluno.getMotivoAndarilha() != null ? aluno.getMotivoAndarilha() : "");
        campoAndarilhaAte.setValue(ativa ? aluno.getAndarilhaAte() : null);
        campoMotivoAndarilha.setDisable(!checkAndarilha.isSelected() || !podeAlterar);
        campoAndarilhaAte.setDisable(!checkAndarilha.isSelected() || !podeAlterar);

        botaoHistorico.setDisable(aluno == null);
        comboNovaTurma.setDisable(!ativa || !podeAlterar);
        campoMotivoTroca.setDisable(!ativa || !podeAlterar);
        botaoTrocar.setDisable(!ativa || !podeAlterar);
        checkAndarilha.setDisable(!ativa || !podeAlterar);
        botaoAndarilha.setDisable(!ativa || !podeAlterar);
    }

    // =================================================================
    //  TROCA DE TURMA (só com confirmação)
    // =================================================================

    /**
     * Mostra o aviso de faixa etária quando a turma escolhida não combina com a idade da criança.
     */
    private void atualizarAvisoTroca() {
        esconder(labelAvisoTroca);
        turmaService.avisoFaixaNaNovaTurma(selecionado, comboNovaTurma.getValue())
                .ifPresent(aviso -> mostrar(labelAvisoTroca, "Atenção: " + aviso));
    }

    /** Troca a criança de turma, com motivo, registrando no histórico. */
    @FXML
    private void trocarTurma() {
        esconder(labelMensagem);
        Turma nova = comboNovaTurma.getValue();
        try {
            if (selecionado == null || nova == null) {
                throw new MatriculaException("Selecione a criança e a nova turma.");
            }
            Optional<String> aviso = turmaService.avisoFaixaNaNovaTurma(selecionado, nova);
            String texto = "Trocar " + selecionado.getNome() + " da turma " + selecionado.getNomeTurma()
                    + " para a turma " + nova.getNome() + "?"
                    + aviso.map(a -> "\n\nATENÇÃO: " + a + "\nEssa exceção ficará registrada no histórico.")
                           .orElse("")
                    + (selecionado.isAndarilha()
                       ? "\n\nA criança é andarilha: o registro de andarilha (motivo e prazo) será encerrado."
                       : "")
                    + "\n\nA troca fica registrada no histórico da criança.";
            if (!confirmar("Confirmar troca de turma", texto)) {
                return;
            }
            String nome = selecionado.getNome();
            turmaService.trocarTurma(selecionado, nova, campoMotivoTroca.getText(), aviso.isPresent());
            sucesso(nome + " agora está na turma " + nova.getNome() + ".");
            recarregarTudo();
        } catch (MatriculaException | SecurityException e) {
            erro(e.getMessage());
        } catch (SQLException e) {
            erro("Erro ao gravar no banco: " + e.getMessage());
        }
    }

    // =================================================================
    //  ANDARILHA
    // =================================================================

    /** Libera ou trava os campos do regime andarilha conforme a caixa marcada. */
    @FXML
    private void aoAlternarAndarilha() {
        campoMotivoAndarilha.setDisable(!checkAndarilha.isSelected());
        campoAndarilhaAte.setDisable(!checkAndarilha.isSelected());
        if (!checkAndarilha.isSelected()) {
            campoMotivoAndarilha.clear();
            campoAndarilhaAte.setValue(null);
        }
    }

    /** Grava o regime andarilha da matrícula (motivo e prazo). */
    @FXML
    private void salvarAndarilha() {
        esconder(labelMensagem);
        try {
            if (selecionado == null) {
                throw new MatriculaException("Selecione uma criança na tabela.");
            }
            boolean andarilha = checkAndarilha.isSelected();
            if (!andarilha && selecionado.isAndarilha()
                    && !confirmar("Remover andarilha", "Remover o registro de andarilha de "
                                  + selecionado.getNome() + "?")) {
                return;
            }
            String nome = selecionado.getNome();
            turmaService.registrarAndarilha(selecionado, andarilha, campoMotivoAndarilha.getText(),
                    campoAndarilhaAte.getValue());
            sucesso(andarilha ? "Andarilha registrada para " + nome + "."
                              : "Registro de andarilha removido de " + nome + ".");
            recarregarTudo();
        } catch (MatriculaException | SecurityException e) {
            erro(e.getMessage());
        } catch (SQLException e) {
            erro("Erro ao gravar no banco: " + e.getMessage());
        }
    }

    // =================================================================
    //  OUTROS
    // =================================================================

    /** Abre, em janela separada, o histórico da criança (inclui troca de turma e andarilha). */
    @FXML
    private void verHistorico() {
        if (selecionado == null) {
            return;
        }
        try {
            HistoricoController.abrir(botaoHistorico.getScene().getWindow(),
                    "Histórico de alterações: " + selecionado.getNome(),
                    historicoService.consultarDoAluno(selecionado.getIdAluno()));
        } catch (SQLException e) {
            erro("Não foi possível consultar o histórico: " + e.getMessage());
        }
    }

    /** Limpa a busca e os filtros da lista de crianças. */
    @FXML
    private void limparFiltros() {
        campoBusca.clear();
        comboFiltroTurma.setValue(null);
        checkSomenteAtivos.setSelected(true);
        checkSomenteAlerta.setSelected(false);
    }

    /** Volta ao menu principal. */
    @FXML
    private void voltar() {
        Navegacao.abrir("menu.fxml", "Menu");
    }

    /** Recarrega as turmas e a lista de crianças, mantendo a seleção. */
    private void recarregarTudo() {
        carregarTurmas();
        carregarAlunos();
        selecionar(tabelaAlunos.getSelectionModel().getSelectedItem());
    }

    /** Define o que cada coluna das tabelas da tela mostra. */
    private void configurarTabelas() {
        LocalDate hoje = LocalDate.now();
        colunaNome.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNome()));
        colunaIdade.setCellValueFactory(c -> new SimpleStringProperty(
                CalculadoraIdade.descrever(c.getValue().getDataNascimento(), hoje)));
        colunaTurma.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().temMatriculaAtiva() ? c.getValue().getNomeTurma() : "-"));
        colunaFaixa.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getFaixaTurmaTexto()));
        colunaAndarilha.setCellValueFactory(c -> new SimpleStringProperty(
                !c.getValue().isAndarilha() ? ""
                : c.getValue().getAndarilhaAte() == null ? "Sem prazo"
                : Formatador.data(c.getValue().getAndarilhaAte())));

        // Alerta de faixa etária: vermelho fora da faixa, laranja no último mês
        colunaSituacao.setCellValueFactory(c -> new SimpleObjectProperty<>(
                c.getValue().situacaoFaixa(hoje)));
        colunaSituacao.setCellFactory(coluna -> new TableCell<>() {
            @Override
            protected void updateItem(SituacaoFaixaEtaria situacao, boolean vazio) {
                super.updateItem(situacao, vazio);
                if (vazio || situacao == null) {
                    setText(null);
                    setStyle("");
                    return;
                }
                setText(situacao.getDescricao());
                if (situacao.isForaDaFaixa()) {
                    setStyle("-fx-text-fill: #c62828; -fx-font-weight: bold;");
                } else if (situacao == SituacaoFaixaEtaria.ANDARILHA) {
                    setStyle("-fx-text-fill: #1565c0;");
                } else if (situacao.isAlerta()) {
                    setStyle("-fx-text-fill: #e65100; -fx-font-weight: bold;");
                } else {
                    setStyle("-fx-text-fill: #607d8b;");
                }
            }
        });

        colunaTurmaNome.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNome()));
        colunaTurmaFaixa.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getIdadeMinimaMeses() + " a " + c.getValue().getIdadeMaximaMeses() + " meses"));
        colunaTurmaPeriodo.setCellValueFactory(c -> new SimpleStringProperty(periodo(c.getValue().getPeriodo())));
        colunaTurmaSala.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getSala() == null ? "" : c.getValue().getSala()));
        colunaTurmaOcupacao.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getMatriculasAtivas() + " de " + c.getValue().getCapacidade()));
        colunaTurmaVagas.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getVagasLivres() == 0 ? "Lotada" : String.valueOf(c.getValue().getVagasLivres())));
    }

    /** Texto do painel da criança: a situação da faixa e o que fazer. */
    private static String dica(SituacaoFaixaEtaria situacao) {
        return switch (situacao) {
            case ACIMA -> "Acima da faixa da turma: avalie a troca para a próxima turma.";
            case ABAIXO -> "Abaixo da faixa da turma: confira se a turma está certa.";
            case ULTIMO_MES -> "Último mês na faixa: no mês que vem passa da idade da turma.";
            case ANDARILHA -> "Andarilha: fica nesta turma como exceção até o prazo previsto.";
            case ANDARILHA_VENCIDA -> "Andarilha com prazo vencido: troque de turma ou renove o prazo.";
            default -> situacao.getDescricao();
        };
    }

    /** DatePicker no formato dd/MM/aaaa; texto inválido vira "sem data" em vez de erro. */
    private static void configurarData(DatePicker campo) {
        DateTimeFormatter formato = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        campo.setConverter(new StringConverter<>() {
            @Override
            public String toString(LocalDate data) {
                return data == null ? "" : data.format(formato);
            }

            @Override
            public LocalDate fromString(String texto) {
                if (texto == null || texto.isBlank()) {
                    return null;
                }
                try {
                    return LocalDate.parse(texto.trim(), formato);
                } catch (DateTimeParseException e) {
                    return null;
                }
            }
        });
    }

    /** Traduz o código do período da turma (MANHA, TARDE, INTEGRAL) para o texto da tela. */
    private static String periodo(String codigo) {
        if (codigo == null) {
            return "";
        }
        return switch (codigo) {
            case "MANHA" -> "Manhã";
            case "TARDE" -> "Tarde";
            case "INTEGRAL" -> "Integral";
            default -> codigo;
        };
    }

    /** Abre uma caixa de confirmação (Sim/Não); devolve verdadeiro se a pessoa confirmar. */
    private boolean confirmar(String titulo, String texto) {
        Alert alerta = new Alert(Alert.AlertType.CONFIRMATION, texto, ButtonType.YES, ButtonType.NO);
        alerta.setTitle(titulo);
        alerta.setHeaderText(titulo);
        return alerta.showAndWait().orElse(ButtonType.NO) == ButtonType.YES;
    }

    /** Mostra uma mensagem de sucesso, em verde. */
    private void sucesso(String texto) {
        labelMensagem.setStyle("-fx-text-fill: #2e7d32; -fx-font-weight: bold;");
        mostrar(labelMensagem, texto);
    }

    /** Mostra uma mensagem de erro, em vermelho. */
    private void erro(String texto) {
        labelMensagem.setStyle("-fx-text-fill: #c62828; -fx-font-weight: bold;");
        mostrar(labelMensagem, texto);
    }

    /** Torna visível o rótulo, com o texto informado. */
    private static void mostrar(Label label, String texto) {
        label.setText(texto);
        label.setVisible(true);
        label.setManaged(true);
    }

    /** Esconde o rótulo e libera o espaço que ele ocupava. */
    private static void esconder(Label label) {
        label.setVisible(false);
        label.setManaged(false);
    }
}
