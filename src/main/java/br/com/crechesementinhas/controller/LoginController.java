package br.com.crechesementinhas.controller;

import br.com.crechesementinhas.model.Usuario;
import br.com.crechesementinhas.service.AutenticacaoException;
import br.com.crechesementinhas.service.AutenticacaoService;
import br.com.crechesementinhas.util.Sessao;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;

/**
 * Controller da tela de login (login.fxml). RF-01.
 */
public class LoginController {

    // @FXML liga cada atributo ao componente com o mesmo fx:id no FXML.
    @FXML
    private TextField campoLogin;

    @FXML
    private PasswordField campoSenha;

    @FXML
    private Label labelErro;

    private final AutenticacaoService autenticacaoService = new AutenticacaoService();

    /** Chamado pelo botão "Entrar" (onAction="#entrar") ou pela tecla Enter. */
    @FXML
    private void entrar() {
        try {
            Usuario usuario = autenticacaoService.autenticar(
                    campoLogin.getText(), campoSenha.getText());

            Sessao.iniciar(usuario);

            // PROVISÓRIO: no próximo passo, aqui abriremos a tela de menu.
            mostrarMensagem("Bem-vindo, " + usuario.getNome() + "!", false);

        } catch (AutenticacaoException e) {
            mostrarMensagem(e.getMessage(), true);
            campoSenha.clear();         // por segurança, apaga a senha digitada
            campoSenha.requestFocus();  // coloca o cursor de volta na senha
        }
    }

    private void mostrarMensagem(String texto, boolean erro) {
        labelErro.setText(texto);
        labelErro.setTextFill(erro ? Color.web("#c62828") : Color.web("#2e7d32"));
        labelErro.setVisible(true);
        labelErro.setManaged(true);
    }
}