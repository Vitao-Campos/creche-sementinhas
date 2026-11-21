package br.com.crechesementinhas.service;

import br.com.crechesementinhas.dao.LogAcessoDAO;
import br.com.crechesementinhas.dao.UsuarioDAO;
import br.com.crechesementinhas.model.MotivoFalhaLogin;
import br.com.crechesementinhas.model.Usuario;
import java.sql.SQLException;
import java.util.Optional;
import org.mindrot.jbcrypt.BCrypt;

/**
 * Regras de autenticação do sistema (RF-01, RNF-01, EX-03).
 */
public class AutenticacaoService {

    // Mesma mensagem para usuário inexistente, inativo ou senha errada:
    // não revela a um invasor quais logins existem.
    // O motivo REAL fica só no log interno (tabela log_acesso).
    private static final String MSG_CREDENCIAIS_INVALIDAS = "Usuário ou senha inválidos.";

    private final UsuarioDAO usuarioDAO;
    private final LogAcessoDAO logAcessoDAO;

    public AutenticacaoService() {
        this(new UsuarioDAO(), new LogAcessoDAO());
    }

    public AutenticacaoService(UsuarioDAO usuarioDAO, LogAcessoDAO logAcessoDAO) {
        this.usuarioDAO = usuarioDAO;
        this.logAcessoDAO = logAcessoDAO;
    }

    public Usuario autenticar(String login, String senha) throws AutenticacaoException {
        if (login == null || login.isBlank() || senha == null || senha.isEmpty()) {
            throw new AutenticacaoException("Informe o usuário e a senha.");
        }

        String loginLimpo = login.trim();

        try {
            Optional<Usuario> encontrado = usuarioDAO.buscarPorLogin(loginLimpo);

            if (encontrado.isEmpty()) {
                registrarLog(loginLimpo, null, false, MotivoFalhaLogin.USUARIO_INEXISTENTE);
                throw new AutenticacaoException(MSG_CREDENCIAIS_INVALIDAS);
            }

            Usuario usuario = encontrado.get();

            if (!usuario.isAtivo()) {
                registrarLog(loginLimpo, usuario.getIdUsuario(), false, MotivoFalhaLogin.USUARIO_INATIVO);
                throw new AutenticacaoException(MSG_CREDENCIAIS_INVALIDAS);
            }

            if (!senhaConfere(senha, usuario.getSenhaHash())) {
                registrarLog(loginLimpo, usuario.getIdUsuario(), false, MotivoFalhaLogin.SENHA_INCORRETA);
                throw new AutenticacaoException(MSG_CREDENCIAIS_INVALIDAS);
            }

            usuarioDAO.atualizarUltimoLogin(usuario.getIdUsuario());
            registrarLog(loginLimpo, usuario.getIdUsuario(), true, null);
            return usuario;

        } catch (SQLException e) {
            throw new AutenticacaoException(
                    "Não foi possível acessar o banco de dados. "
                    + "Verifique se o servidor está ligado.", e);
        }
    }

    /**
     * Grava a tentativa no log de acesso (EX-03).
     * Uma falha ao gravar o LOG não deve impedir o login nem trocar a
     * mensagem da tela; por isso o erro é apenas reportado no console.
     */
    private void registrarLog(String login, Integer idUsuario,
                              boolean sucesso, MotivoFalhaLogin motivo) {
        try {
            logAcessoDAO.registrar(login, idUsuario, sucesso, motivo);
        } catch (SQLException e) {
            System.err.println("Falha ao registrar log de acesso: " + e.getMessage());
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
            return false;
        }
    }
}