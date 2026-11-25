package br.com.crechesementinhas.controller;

import br.com.crechesementinhas.model.Aluno;
import br.com.crechesementinhas.model.Desligamento;
import br.com.crechesementinhas.model.FaixaMensalidade;
import br.com.crechesementinhas.model.Matricula;
import br.com.crechesementinhas.model.Permissao;
import br.com.crechesementinhas.model.Turma;
import br.com.crechesementinhas.service.CalculadoraMensalidade;
import br.com.crechesementinhas.service.ControleAcesso;
import br.com.crechesementinhas.service.MatriculaException;
import br.com.crechesementinhas.service.MatriculaService;
import br.com.crechesementinhas.service.MensalidadeService;
import br.com.crechesementinhas.util.Formatador;
import br.com.crechesementinhas.util.Navegacao;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Optional;
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
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.util.StringConverter;

/**
 * Tela de matrícula e desligamento (matricula.fxml), Frente B.
 * Só cuida da tela: as regras ficam no MatriculaService e no MensalidadeService.
 */
public class MatriculaController {

    // Criança
    @FXML private TextField campoBusca;
    @FXML private TableView<Aluno> tabelaAlunos;
    @FXML private TableColumn<Aluno, String> colunaNome;
    @FXML private TableColumn<Aluno, String> colunaNascimento;
    @FXML private TableColumn<Aluno, String> colunaIdade;
    @FXML private Label labelAluno;
    @FXML private Label labelIdade;
    @FXML private Label labelMatriculaAtual;

    // Aba: efetivar matrícula
    @FXML private DatePicker campoDataMatricula;
    @FXML private ComboBox<Turma> comboTurma;
    @FXML private Label labelAvisoTurma;
    @FXML private Label labelMensalidade;
    @FXML private CheckBox checkAndarilha;
    @FXML private TextField campoMotivoAndarilha;
    @FXML private DatePicker campoAndarilhaAte;
    @FXML private Button botaoMatricular;
    @FXML private TableView<Matricula> tabelaHistorico;
    @FXML private TableColumn<Matricula, String> colunaHistNumero;
    @FXML private TableColumn<Matricula, String> colunaHistTurma;
    @FXML private TableColumn<Matricula, String> colunaHistStatus;
    @FXML private TableColumn<Matricula, String> colunaHistInicio;
    @FXML private TableColumn<Matricula, String> colunaHistFim;

    // Aba: desligamento
    @FXML private DatePicker campoDataDesligamento;
    @FXML private TextField campoJustificativa;
    @FXML private Label labelBaseCalculo;
    @FXML private Label labelValorProporcional;
    @FXML private Label labelAviso;
    @FXML private Button botaoDesligar;

    // Aba: tabela de mensalidades
    @FXML private TableView<FaixaMensalidade> tabelaFaixas;
    @FXML private TableColumn<FaixaMensalidade, String> colunaFaixaDescricao;
    @FXML private TableColumn<FaixaMensalidade, String> colunaFaixaIdade;
    @FXML private TableColumn<FaixaMensalidade, String> colunaFaixaValor;
    @FXML private TextField campoNovoValor;
    @FXML private TextField campoJustificativaReajuste;
    @FXML private Button botaoReajustar;

    @FXML private Label labelMensagem;

    private final MatriculaService matriculaService = new MatriculaService();
    private final MensalidadeService mensalidadeService = new MensalidadeService();

    private Aluno alunoSelecionado;
    private Matricula matriculaAtiva;
    private boolean temResponsavelLegal;

    @FXML
    private void initialize() {
        configurarDatas();
        configurarTabelas();

        campoDataMatricula.setValue(LocalDate.now());
        // RN-12: sugere a primeira data aceita (hoje + 30 dias corridos)
        campoDataDesligamento.setValue(CalculadoraMensalidade.dataMinimaDesligamento(LocalDate.now()));

        // Recalcula enquanto o usuário mexe nos campos
        campoBusca.textProperty().addListener((obs, antigo, novo) -> carregarAlunos(novo));
        campoDataMatricula.valueProperty().addListener((obs, antigo, novo) -> atualizarMatricula());
        comboTurma.valueProperty().addListener((obs, antigo, novo) -> atualizarMatricula());
        campoDataDesligamento.valueProperty().addListener((obs, antigo, novo) -> simularDesligamento());
        tabelaAlunos.getSelectionModel().selectedItemProperty()
                .addListener((obs, antigo, novo) -> selecionarAluno(novo));

        // RNF-03: a tela só esconde o que o service também barra
        botaoReajustar.setDisable(!ControleAcesso.temPermissao(Permissao.VER_RELATORIOS_FINANCEIROS));

        carregarAlunos("");
        carregarTurmas();
        carregarFaixas();
        selecionarAluno(null);
    }

    // =================================================================
    //  CARGA DOS DADOS
    // =================================================================

    private void carregarAlunos(String filtro) {
        try {
            tabelaAlunos.setItems(FXCollections.observableArrayList(matriculaService.buscarAlunos(filtro)));
        } catch (SQLException e) {
            erro("Não foi possível carregar as crianças: " + e.getMessage());
        }
    }

    private void carregarTurmas() {
        Turma escolhida = comboTurma.getValue();
        try {
            comboTurma.setItems(FXCollections.observableArrayList(matriculaService.listarTurmas()));
            if (escolhida != null) {
                comboTurma.getItems().stream()
                        .filter(t -> t.getIdTurma().equals(escolhida.getIdTurma()))
                        .findFirst().ifPresent(comboTurma::setValue);
            }
        } catch (SQLException e) {
            erro("Não foi possível carregar as turmas: " + e.getMessage());
        }
    }

    private void carregarFaixas() {
        try {
            tabelaFaixas.setItems(FXCollections.observableArrayList(mensalidadeService.listarFaixas()));
        } catch (SQLException e) {
            erro("Não foi possível carregar a tabela de mensalidades (o script V6 foi executado?): "
                    + e.getMessage());
        }
    }

    private void selecionarAluno(Aluno aluno) {
        alunoSelecionado = aluno;
        matriculaAtiva = null;
        temResponsavelLegal = false;
        tabelaHistorico.getItems().clear();

        if (aluno == null) {
            labelAluno.setText("Nenhuma criança selecionada");
            labelIdade.setText("-");
            labelMatriculaAtual.setText("-");
        } else {
            labelAluno.setText(aluno.getNome());
            labelIdade.setText(aluno.getIdadeTexto() + " (" + aluno.getIdadeEmMeses() + " meses)");
            try {
                matriculaAtiva = matriculaService.buscarAtiva(aluno).orElse(null);
                temResponsavelLegal = matriculaService.temResponsavelLegal(aluno);
                tabelaHistorico.setItems(FXCollections.observableArrayList(matriculaService.historico(aluno)));
            } catch (SQLException e) {
                erro("Não foi possível consultar as matrículas: " + e.getMessage());
            }
            labelMatriculaAtual.setText(matriculaAtiva == null
                    ? (temResponsavelLegal
                       ? "Sem matrícula ativa"
                       : "Sem matrícula ativa. Falta um responsável legal: cadastre na tela Responsáveis (RN-01).")
                    : matriculaAtiva.getNumeroMatricula() + " - " + matriculaAtiva.getNomeTurma()
                      + " (desde " + Formatador.data(matriculaAtiva.getDataMatricula()) + ")");
        }
        // RNF-03 + situação da criança: só habilita o que faz sentido agora
        botaoMatricular.setDisable(!ControleAcesso.temPermissao(Permissao.CADASTRAR_MATRICULA)
                || aluno == null || matriculaAtiva != null || !temResponsavelLegal);
        botaoDesligar.setDisable(!ControleAcesso.temPermissao(Permissao.DESLIGAR_ALUNO)
                || matriculaAtiva == null);
        atualizarMatricula();
        simularDesligamento();
    }

    // =================================================================
    //  EFETIVAR MATRÍCULA (UC-01 / RN-01 / RN-07)
    // =================================================================

    /** Atualiza a mensalidade e o aviso de faixa etária conforme a data e a turma escolhidas. */
    private void atualizarMatricula() {
        esconder(labelAvisoTurma);
        if (alunoSelecionado == null || campoDataMatricula.getValue() == null) {
            labelMensalidade.setText("-");
            return;
        }
        try {
            Optional<FaixaMensalidade> faixa = mensalidadeService.calcular(
                    alunoSelecionado.getDataNascimento(), campoDataMatricula.getValue());
            labelMensalidade.setText(faixa
                    .map(f -> Formatador.dinheiro(f.getValor()) + "  (" + f.getDescricao() + ")")
                    .orElse("Sem faixa cadastrada para esta idade"));
        } catch (SQLException e) {
            labelMensalidade.setText("Erro ao consultar a tabela de mensalidades");
        }
        matriculaService.avisoFaixaEtaria(alunoSelecionado, comboTurma.getValue(), campoDataMatricula.getValue())
                .ifPresent(aviso -> mostrar(labelAvisoTurma, "Atenção: " + aviso));
    }

    @FXML
    private void aoAlternarAndarilha() {
        boolean marcada = checkAndarilha.isSelected();
        campoMotivoAndarilha.setDisable(!marcada);
        campoAndarilhaAte.setDisable(!marcada);
        if (!marcada) {
            campoMotivoAndarilha.clear();
            campoAndarilhaAte.setValue(null);
        }
    }

    @FXML
    private void efetivarMatricula() {
        esconder(labelMensagem);
        Turma turma = comboTurma.getValue();
        LocalDate data = campoDataMatricula.getValue();
        try {
            boolean confirmou = false;
            Optional<String> aviso = matriculaService.avisoFaixaEtaria(alunoSelecionado, turma, data);
            if (aviso.isPresent()) {
                if (!confirmar("Faixa etária da turma", aviso.get() + "\n\nMatricular mesmo assim?")) {
                    return;
                }
                confirmou = true;
            }
            Matricula nova = matriculaService.efetivar(alunoSelecionado, turma, data,
                    checkAndarilha.isSelected(), campoMotivoAndarilha.getText(),
                    campoAndarilhaAte.getValue(), confirmou);

            sucesso("Matrícula " + nova.getNumeroMatricula() + " efetivada na turma " + nova.getNomeTurma()
                    + ". Próximos passos: responsáveis, anamnese e contrato.");
            checkAndarilha.setSelected(false);
            aoAlternarAndarilha();
            carregarTurmas();
            selecionarAluno(alunoSelecionado);
        } catch (MatriculaException | SecurityException e) {
            erro(e.getMessage());
        } catch (SQLException e) {
            erro("Erro ao gravar no banco: " + e.getMessage());
        }
    }

    // =================================================================
    //  DESLIGAMENTO (RN-12 / RN-030)
    // =================================================================

    private void simularDesligamento() {
        labelAviso.setText("");
        if (matriculaAtiva == null) {
            labelBaseCalculo.setText(alunoSelecionado == null
                    ? "Selecione uma criança."
                    : "Esta criança não tem matrícula ativa.");
            labelValorProporcional.setText("-");
            return;
        }
        try {
            Desligamento d = matriculaService.simularDesligamento(matriculaAtiva, alunoSelecionado,
                    campoDataDesligamento.getValue());
            labelBaseCalculo.setText("Mensalidade de " + Formatador.dinheiro(d.mensalidadeBase())
                    + " (" + d.origemMensalidade() + ") × " + d.diasCobrados() + " ÷ "
                    + d.diasNoMes() + " dias do mês");
            labelValorProporcional.setText("Valor do último mês: " + Formatador.dinheiro(d.valorProporcional()));
            if (CalculadoraMensalidade.avisoCurto(d.diasDeAviso())) { // RN-12
                labelAviso.setText("Atenção: aviso de " + d.diasDeAviso() + " dias. A RN-12 pede "
                        + CalculadoraMensalidade.AVISO_PREVIO_DIAS + " (saída a partir de "
                        + Formatador.data(CalculadoraMensalidade.dataMinimaDesligamento(LocalDate.now()))
                        + "). Para desligar antes, será pedida confirmação e isso fica no histórico.");
                labelAviso.setStyle("-fx-text-fill: #e65100; -fx-font-weight: bold;");
            } else {
                labelAviso.setText("Aviso prévio de " + d.diasDeAviso() + " dias: dentro da RN-12.");
                labelAviso.setStyle("-fx-text-fill: #607d8b;");
            }
        } catch (MatriculaException | IllegalArgumentException e) {
            labelBaseCalculo.setText(e.getMessage());
            labelValorProporcional.setText("-");
        } catch (SQLException e) {
            labelBaseCalculo.setText("Erro ao consultar o contrato: " + e.getMessage());
            labelValorProporcional.setText("-");
        }
    }

    @FXML
    private void desligarAluno() {
        esconder(labelMensagem);
        try {
            if (matriculaAtiva == null) {
                throw new MatriculaException("Selecione uma criança com matrícula ATIVA.");
            }
            Desligamento d = matriculaService.simularDesligamento(matriculaAtiva, alunoSelecionado,
                    campoDataDesligamento.getValue());

            boolean avisoCurto = CalculadoraMensalidade.avisoCurto(d.diasDeAviso());
            String texto = "Desligar " + alunoSelecionado.getNome() + " em "
                    + Formatador.data(d.dataDesligamento()) + "?\n\n"
                    + "Valor do último mês: " + Formatador.dinheiro(d.valorProporcional()) + "\n"
                    + (avisoCurto ? "\nATENÇÃO: aviso de apenas " + d.diasDeAviso() + " dias (a RN-12 pede "
                                    + CalculadoraMensalidade.AVISO_PREVIO_DIAS
                                    + "). Essa exceção ficará registrada no histórico.\n" : "")
                    + "\nA matrícula e o contrato vigente serão encerrados.";
            if (!confirmar("Confirmar desligamento", texto)) {
                return;
            }
            Matricula encerrada = matriculaAtiva;
            Desligamento feito = matriculaService.desligar(encerrada, alunoSelecionado,
                    campoDataDesligamento.getValue(), campoJustificativa.getText(), avisoCurto);

            sucesso("Matrícula " + encerrada.getNumeroMatricula() + " encerrada. Valor do último mês: "
                    + Formatador.dinheiro(feito.valorProporcional()) + ".");
            campoJustificativa.clear();
            carregarTurmas();
            selecionarAluno(alunoSelecionado);
        } catch (MatriculaException | SecurityException e) {
            erro(e.getMessage());
        } catch (SQLException e) {
            erro("Erro ao gravar no banco: " + e.getMessage());
        }
    }

    // =================================================================
    //  TABELA DE MENSALIDADES (RN-07)
    // =================================================================

    @FXML
    private void reajustarFaixa() {
        esconder(labelMensagem);
        FaixaMensalidade faixa = tabelaFaixas.getSelectionModel().getSelectedItem();
        if (faixa == null) {
            erro("Selecione na tabela a faixa que será reajustada.");
            return;
        }
        try {
            mensalidadeService.reajustar(faixa.getIdFaixaMensalidade(),
                    campoNovoValor.getText(), campoJustificativaReajuste.getText());
            sucesso("Mensalidade da faixa \"" + faixa.getDescricao() + "\" reajustada. "
                    + "Contratos já gerados mantêm o valor deles.");
            campoNovoValor.clear();
            campoJustificativaReajuste.clear();
            carregarFaixas();
            atualizarMatricula();
            simularDesligamento();
        } catch (MatriculaException | SecurityException e) {
            erro(e.getMessage());
        } catch (SQLException e) {
            erro("Erro ao gravar no banco: " + e.getMessage());
        }
    }

    @FXML
    private void voltar() {
        Navegacao.abrir("menu.fxml", "Menu");
    }

    // =================================================================
    //  APOIO
    // =================================================================

    private void configurarTabelas() {
        colunaNome.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNome()));
        colunaNascimento.setCellValueFactory(c -> new SimpleStringProperty(
                Formatador.data(c.getValue().getDataNascimento())));
        colunaIdade.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getIdadeTexto()));

        colunaHistNumero.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNumeroMatricula()));
        colunaHistTurma.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNomeTurma()));
        colunaHistStatus.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getStatus()));
        colunaHistInicio.setCellValueFactory(c -> new SimpleStringProperty(
                Formatador.data(c.getValue().getDataMatricula())));
        colunaHistFim.setCellValueFactory(c -> new SimpleStringProperty(
                Formatador.data(c.getValue().getDataEncerramento())));

        colunaFaixaDescricao.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDescricao()));
        colunaFaixaIdade.setCellValueFactory(c -> {
            FaixaMensalidade f = c.getValue();
            String texto = f.getIdadeMaximaAnos() == null
                    ? f.getIdadeMinimaAnos() + " anos ou mais"
                    : f.getIdadeMinimaAnos() == f.getIdadeMaximaAnos()
                      ? f.getIdadeMinimaAnos() + (f.getIdadeMinimaAnos() == 1 ? " ano" : " anos")
                      : f.getIdadeMinimaAnos() + " a " + f.getIdadeMaximaAnos() + " anos";
            return new SimpleStringProperty(texto);
        });
        colunaFaixaValor.setCellValueFactory(c -> new SimpleStringProperty(
                Formatador.dinheiro(c.getValue().getValor())));
    }

    /** DatePickers no formato dd/MM/aaaa; texto inválido vira "sem data" em vez de erro. */
    private void configurarDatas() {
        DateTimeFormatter formato = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        StringConverter<LocalDate> conversor = new StringConverter<>() {
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
        };
        for (DatePicker campo : new DatePicker[] {campoDataMatricula, campoDataDesligamento, campoAndarilhaAte}) {
            campo.setConverter(conversor);
        }
    }

    private boolean confirmar(String titulo, String texto) {
        Alert alerta = new Alert(Alert.AlertType.CONFIRMATION, texto, ButtonType.YES, ButtonType.NO);
        alerta.setTitle(titulo);
        alerta.setHeaderText(titulo);
        return alerta.showAndWait().orElse(ButtonType.NO) == ButtonType.YES;
    }

    private void sucesso(String texto) {
        labelMensagem.setStyle("-fx-text-fill: #2e7d32; -fx-font-weight: bold;");
        mostrar(labelMensagem, texto);
    }

    private void erro(String texto) {
        labelMensagem.setStyle("-fx-text-fill: #c62828; -fx-font-weight: bold;");
        mostrar(labelMensagem, texto);
    }

    private static void mostrar(Label label, String texto) {
        label.setText(texto);
        label.setVisible(true);
        label.setManaged(true);
    }

    private static void esconder(Label label) {
        label.setVisible(false);
        label.setManaged(false);
    }
}
