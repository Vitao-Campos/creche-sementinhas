package br.com.crechesementinhas.controller;

import br.com.crechesementinhas.model.Usuario;
import br.com.crechesementinhas.util.Navegacao;
import br.com.crechesementinhas.util.Sessao;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import br.com.crechesementinhas.model.Permissao;
import br.com.crechesementinhas.service.ControleAcesso;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

/**
 * Controller do menu principal (menu.fxml): cada botão abre a tela correspondente.
 */
public class MenuController {

    @FXML
    private Label labelUsuario;
    
    @FXML
    private Button botaoUsuarios;

    @FXML
    private BorderPane raiz;

    @FXML
    private VBox barraLateral;

    @FXML
    private VBox areaLogo;

    @FXML
    private ImageView imagemEmblema;

    @FXML
    private ImageView imagemNome;

    /**
     * Chamado automaticamente pelo JavaFX depois de carregar o FXML,
     * quando os componentes @FXML já estão prontos.
     */
    @FXML
    private void initialize() {
        Usuario usuario = Sessao.getUsuarioLogado();
        labelUsuario.setText(usuario.getNome() + " (" + usuario.getCargo() + ")");
        
        // RNF-03: só quem pode gerenciar usuários (perfil Suporte) vê o botão.
        boolean podeGerenciarUsuarios = ControleAcesso.temPermissao(Permissao.GERENCIAR_USUARIOS);
        botaoUsuarios.setVisible(podeGerenciarUsuarios);
        botaoUsuarios.setManaged(podeGerenciarUsuarios);

        ajustarLayoutResponsivo();
    }

    /**
     * Mantém o menu lateral com cerca de 20% da largura da janela (limitado pelos
     * mínimo/máximo do FXML) e faz o emblema e o nome acompanharem o tamanho da área central.
     */
    private void ajustarLayoutResponsivo() {
        barraLateral.prefWidthProperty().bind(raiz.widthProperty().multiply(0.20));

        imagemEmblema.fitWidthProperty().bind(
                javafx.beans.binding.Bindings.max(120.0,
                        javafx.beans.binding.Bindings.min(areaLogo.heightProperty().multiply(0.46),
                                areaLogo.widthProperty().multiply(0.55))));
        imagemEmblema.fitHeightProperty().bind(imagemEmblema.fitWidthProperty());

        imagemNome.fitWidthProperty().bind(
                javafx.beans.binding.Bindings.max(160.0,
                        javafx.beans.binding.Bindings.min(imagemEmblema.fitWidthProperty().multiply(1.75),
                                areaLogo.widthProperty().multiply(0.9))));
    }

    /** Abre a tela de matrícula. */
    @FXML
    private void novaMatricula() {
        Navegacao.abrir("matricula.fxml", "Matrícula");
    }

    /** Abre a tela de alunos. */
    @FXML
    private void consultarAlunos() {
        Navegacao.abrir("aluno.fxml", "Alunos");
    }

    /** Abre a tela de responsáveis. */
    @FXML
    private void responsaveis() {
        Navegacao.abrir("responsavel.fxml", "Responsáveis");
    }

    /** Abre a tela da ficha de anamnese. */
    @FXML
    private void anamnese() {
        Navegacao.abrir("anamnese.fxml", "Ficha de Anamnese");
    }

    /** Abre a tela de contratos e documentos. */
    @FXML
    private void contratos() {
        Navegacao.abrir("ContratoDocumentos.fxml", "Contrato e documentos");
    }

    /** Abre a tela de turmas. */
    @FXML
    private void turmas() {
        Navegacao.abrir("turmas.fxml", "Turmas");
    }

    /** Abre a tela de usuários (só para o perfil que gerencia usuários). */
    @FXML
    private void usuarios() {
        Navegacao.abrir("usuarios.fxml", "Usuários");
    }

    /** Encerra a sessão e volta para a tela de login. */
    @FXML
    private void sair() {
        Sessao.encerrar();
        Navegacao.abrir("login.fxml", "Login");
    }
}