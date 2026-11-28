package br.com.crechesementinhas.controller;

import br.com.crechesementinhas.model.Perfil;
import br.com.crechesementinhas.model.Usuario;
import br.com.crechesementinhas.service.UsuarioException;
import br.com.crechesementinhas.service.UsuarioService;
import br.com.crechesementinhas.util.Navegacao;
import java.util.Optional;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.util.StringConverter;

/**
 * Controller da tela de gerenciamento de usuários (usuarios.fxml).
 * As regras (validação, permissão, auditoria) ficam no UsuarioService.
 */
public class UsuariosController {

    @FXML private TableView<Usuario> tabelaUsuarios;
    @FXML private TableColumn<Usuario, String> colunaNome;
    @FXML private TableColumn<Usuario, String> colunaLogin;
    @FXML private TableColumn<Usuario, String> colunaPerfil;
    @FXML private TableColumn<Usuario, String> colunaSetor;
    @FXML private TableColumn<Usuario, String> colunaSituacao;

    @FXML private Label labelTituloFormulario;
    @FXML private Label labelSenha;
    @FXML private Label labelMensagem;
    @FXML private TextField campoNome;
    @FXML private TextField campoLogin;
    @FXML private PasswordField campoSenha;
    @FXML private ComboBox<Perfil> comboPerfil;
    @FXML private TextField campoSetor;
    @FXML private CheckBox checkAtivo;
    @FXML private javafx.scene.control.Button botaoRedefinirSenha;

    private final UsuarioService usuarioService = new UsuarioService();

    // null = modo cadastro; preenchido = modo edição
    private Usuario usuarioSelecionado;

    /** Prepara a tela de usuários: tabela, perfis, formulário e lista inicial. */
    @FXML
    private void initialize() {
        configurarTabela();
        configurarComboPerfil();
        carregarUsuarios();
        prepararCadastro();

        // Ao clicar numa linha da tabela, entra no modo edição.
        tabelaUsuarios.getSelectionModel().selectedItemProperty().addListener(
                (observavel, anterior, selecionado) -> {
                    if (selecionado != null) {
                        prepararEdicao(selecionado);
                    }
                });
    }

    // ===================== CONFIGURAÇÃO =====================

    /** Define o que cada coluna da tabela de usuários mostra. */
    private void configurarTabela() {
        // Cada coluna "sabe" qual informação do Usuario deve exibir.
        colunaNome.setCellValueFactory(celula -> new SimpleStringProperty(celula.getValue().getNome()));
        colunaLogin.setCellValueFactory(celula -> new SimpleStringProperty(celula.getValue().getLogin()));
        colunaPerfil.setCellValueFactory(celula -> new SimpleStringProperty(celula.getValue().getCargo()));
        colunaSetor.setCellValueFactory(celula -> new SimpleStringProperty(celula.getValue().getSetor()));
        colunaSituacao.setCellValueFactory(celula ->
                new SimpleStringProperty(celula.getValue().isAtivo() ? "Ativo" : "Inativo"));
    }

    /** Preenche a caixa de perfis e define o texto mostrado de cada um. */
    private void configurarComboPerfil() {
        // Perfil.values(): se um perfil novo (ex.: Professor) for criado, ele aparece aqui sozinho.
        comboPerfil.getItems().setAll(Perfil.values());

        // O converter define o TEXTO exibido: "Direção" em vez de "DIRECAO".
        comboPerfil.setConverter(new StringConverter<>() {
            @Override
            public String toString(Perfil perfil) {
                return perfil == null ? "" : perfil.getDescricao();
            }

            @Override
            public Perfil fromString(String texto) {
                return null; // o ComboBox não é editável, então não é usado
            }
        });
    }

    /** Recarrega a lista de usuários. */
    private void carregarUsuarios() {
        try {
            tabelaUsuarios.getItems().setAll(usuarioService.listar());
        } catch (UsuarioException | SecurityException e) {
            mostrarMensagem(e.getMessage(), true);
        }
    }

    // ===================== MODOS DO FORMULÁRIO =====================

    /** Limpa o formulário para cadastrar um usuário novo. */
    private void prepararCadastro() {
        usuarioSelecionado = null;
        tabelaUsuarios.getSelectionModel().clearSelection();

        labelTituloFormulario.setText("Novo usuário");
        campoNome.clear();
        campoLogin.clear();
        campoLogin.setDisable(false);
        labelSenha.setText("Senha");
        campoSenha.clear();
        comboPerfil.getSelectionModel().clearSelection();
        campoSetor.clear();
        checkAtivo.setSelected(true);
        checkAtivo.setDisable(true);          // todo usuário novo nasce ativo
        botaoRedefinirSenha.setDisable(true);
        esconderMensagem();
    }

    /** Preenche o formulário com o usuário escolhido, para edição (o login não muda). */
    private void prepararEdicao(Usuario usuario) {
        usuarioSelecionado = usuario;

        labelTituloFormulario.setText("Editar usuário");
        campoNome.setText(usuario.getNome());
        campoLogin.setText(usuario.getLogin());
        campoLogin.setDisable(true);          // o login é a identidade: não muda
        labelSenha.setText("Nova senha (só para redefinir)");
        campoSenha.clear();
        comboPerfil.getSelectionModel().select(Perfil.deCargo(usuario.getCargo()).orElse(null));
        campoSetor.setText(usuario.getSetor());
        checkAtivo.setSelected(usuario.isAtivo());
        checkAtivo.setDisable(false);
        botaoRedefinirSenha.setDisable(false);
        esconderMensagem();
    }

    // ===================== AÇÕES DOS BOTÕES =====================

    /** Começa o cadastro de um usuário novo. */
    @FXML
    private void novo() {
        prepararCadastro();
    }

    /** Cadastra o usuário novo ou grava a alteração do usuário escolhido. */
    @FXML
    private void salvar() {
        try {
            String mensagem;
            if (usuarioSelecionado == null) {
                usuarioService.cadastrar(campoNome.getText(), campoLogin.getText(),
                        campoSenha.getText(), comboPerfil.getValue(), campoSetor.getText());
                mensagem = "Usuário cadastrado com sucesso.";
            } else {
                usuarioService.atualizar(usuarioSelecionado.getIdUsuario(), campoNome.getText(),
                        comboPerfil.getValue(), campoSetor.getText(), checkAtivo.isSelected());
                mensagem = "Alterações salvas com sucesso.";
            }

            carregarUsuarios();
            prepararCadastro();
            mostrarMensagem(mensagem, false);

        } catch (UsuarioException | SecurityException e) {
            mostrarMensagem(e.getMessage(), true);
        }
    }

    /** Define uma nova senha para o usuário escolhido. */
    @FXML
    private void redefinirSenha() {
        if (usuarioSelecionado == null) {
            return;
        }

        Alert confirmacao = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacao.setTitle("Redefinir senha");
        confirmacao.setHeaderText("Redefinir a senha de " + usuarioSelecionado.getNome() + "?");
        confirmacao.setContentText("A senha atual deixará de funcionar.");
        Optional<ButtonType> resposta = confirmacao.showAndWait();

        if (resposta.isEmpty() || resposta.get() != ButtonType.OK) {
            return;
        }

        try {
            usuarioService.redefinirSenha(usuarioSelecionado.getIdUsuario(), campoSenha.getText());
            campoSenha.clear();
            mostrarMensagem("Senha redefinida com sucesso.", false);
        } catch (UsuarioException | SecurityException e) {
            mostrarMensagem(e.getMessage(), true);
        }
    }

    /** Volta ao menu principal. */
    @FXML
    private void voltar() {
        Navegacao.abrir("menu.fxml", "Menu");
    }

    // ===================== MENSAGENS =====================

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
}