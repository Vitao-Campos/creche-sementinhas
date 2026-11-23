package br.com.crechesementinhas.controller;

import br.com.crechesementinhas.model.Usuario;
import br.com.crechesementinhas.util.Navegacao;
import br.com.crechesementinhas.util.Sessao;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;

/**
 * Controller do menu principal (menu.fxml).
 * Cada integrante substitui o "emDesenvolvimento" do seu botão
 * pela abertura da tela da sua frente.
 */
public class MenuController {

    @FXML
    private Label labelUsuario;

    /**
     * Chamado automaticamente pelo JavaFX depois de carregar o FXML,
     * quando os componentes @FXML já estão prontos.
     */
    @FXML
    private void initialize() {
        Usuario usuario = Sessao.getUsuarioLogado();
        labelUsuario.setText(usuario.getNome() + " (" + usuario.getCargo() + ")");
    }

    @FXML
    private void novaMatricula() {
        emDesenvolvimento("Nova Matrícula", "B");
    }

    @FXML
    private void consultarAlunos() {
        Navegacao.abrir("aluno.fxml", "Alunos");
    }

    @FXML
    private void responsaveis() {
        Navegacao.abrir("responsavel.fxml", "Responsáveis");
    }

    @FXML
    private void anamnese() {
        emDesenvolvimento("Ficha de Anamnese", "D");
    }

    @FXML
    private void contratos() {
        emDesenvolvimento("Contratos e Documentos", "F");
    }

    @FXML
    private void turmas() {
        emDesenvolvimento("Turmas", "E");
    }

    @FXML
    private void sair() {
        Sessao.encerrar();
        Navegacao.abrir("login.fxml", "Login");
    }

    private void emDesenvolvimento(String funcionalidade, String frente) {
        Alert aviso = new Alert(Alert.AlertType.INFORMATION);
        aviso.setTitle("Em desenvolvimento");
        aviso.setHeaderText(funcionalidade);
        aviso.setContentText("Esta tela está sendo desenvolvida pela frente " + frente + ".");
        aviso.showAndWait();
    }
}