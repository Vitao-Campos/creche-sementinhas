package br.com.crechesementinhas.service;

import br.com.crechesementinhas.dao.UsuarioDAO;
import br.com.crechesementinhas.model.Usuario;
import java.sql.SQLException;
import org.mindrot.jbcrypt.BCrypt;

/**
 * Regras de autenticação do sistema (RF-01, RNF-01, EX-03).
 */
public class AutenticacaoService {

    // Mesma mensagem para usuário inexistente, inativo ou senha errada:
    // não revela a um invasor quais logins existem.
    private static final String MSG_CREDENCIAIS_INVALIDAS = "Usuário ou senha inválidos.";

    private final UsuarioDAO usuarioDAO;

    public AutenticacaoService() {
        this(new UsuarioDAO());
    }

    // Construtor que recebe o DAO de fora (injeção de dependência):
    // permite trocar o DAO por um "falso" em testes automatizados.
    public AutenticacaoService(UsuarioDAO usuarioDAO) {
        this.usuarioDAO = usuarioDAO;
    }

    /**
     * Valida login e senha.
     *
     * @return o usuário autenticado
     * @throws AutenticacaoException com mensagem pronta para a tela
     */
    public Usuario autenticar(String login, String senha) throws AutenticacaoException {
        if (login == null || login.isBlank() || senha == null || senha.isEmpty()) {
            throw new AutenticacaoException("Informe o usuário e a senha.");
        }

        try {
            Usuario usuario = usuarioDAO.buscarPorLogin(login.trim())
                    .orElseThrow(() -> new AutenticacaoException(MSG_CREDENCIAIS_INVALIDAS));

            if (!usuario.isAtivo() || !senhaConfere(senha, usuario.getSenhaHash())) {
                throw new AutenticacaoException(MSG_CREDENCIAIS_INVALIDAS);
            }

            usuarioDAO.atualizarUltimoLogin(usuario.getIdUsuario());
            return usuario;

        } catch (SQLException e) {
            throw new AutenticacaoException(
                    "Não foi possível acessar o banco de dados. "
                    + "Verifique se o servidor está ligado.", e);
        }
    }

    /**
     * Compara a senha digitada com o hash guardado (RNF-01).
     * Desempenho: o BCrypt com custo 10 leva dezenas de milissegundos
     * DE PROPÓSITO, o que torna ataques de tentativa e erro muito lentos.
     */
    private boolean senhaConfere(String senhaDigitada, String hashGuardado) {
        try {
            return BCrypt.checkpw(senhaDigitada, hashGuardado);
        } catch (IllegalArgumentException e) {
            return false; // hash inválido no banco: trata como senha errada
        }
    }
}