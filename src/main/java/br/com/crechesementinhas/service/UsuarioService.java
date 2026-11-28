package br.com.crechesementinhas.service;

import br.com.crechesementinhas.dao.UsuarioDAO;
import br.com.crechesementinhas.model.Perfil;
import br.com.crechesementinhas.model.Permissao;
import br.com.crechesementinhas.model.Usuario;
import br.com.crechesementinhas.util.Sessao;
import br.com.crechesementinhas.util.Transacao;
import java.sql.SQLException;
import java.util.List;
import java.util.regex.Pattern;
import org.mindrot.jbcrypt.BCrypt;

/**
 * Regras de gerenciamento de usuários (RNF-01, RNF-02, RNF-03).
 * Todas as operações exigem a permissão GERENCIAR_USUARIOS (perfil Suporte).
 */
public class UsuarioService {

    private static final String ENTIDADE = "usuario";
    private static final int TAMANHO_MAXIMO_NOME = 100;
    private static final int TAMANHO_MINIMO_SENHA = 8;

    // Regex: de 3 a 40 caracteres, só letras minúsculas, números, ponto e sublinhado.
    // Compilada UMA vez (static final): compilar regex é caro; reutilizar é O(1).
    private static final Pattern FORMATO_LOGIN = Pattern.compile("^[a-z0-9._]{3,40}$");

    private static final Pattern TEM_LETRA = Pattern.compile("[A-Za-zÀ-ÿ]");
    private static final Pattern TEM_NUMERO = Pattern.compile("\\d");

    private final UsuarioDAO usuarioDAO;
    private final AuditoriaService auditoria;

    /** Cria o serviço com os acessos ao banco padrão. */
    public UsuarioService() {
        this(new UsuarioDAO(), new AuditoriaService());
    }

    /** Cria o serviço com os acessos informados (usado em testes). */
    public UsuarioService(UsuarioDAO usuarioDAO, AuditoriaService auditoria) {
        this.usuarioDAO = usuarioDAO;
        this.auditoria = auditoria;
    }

    // ===================== OPERAÇÕES =====================

    /** Lista todos os usuários (exige permissão para gerenciar usuários). */
    public List<Usuario> listar() throws UsuarioException {
        ControleAcesso.exigir(Permissao.GERENCIAR_USUARIOS);
        try {
            return usuarioDAO.listarTodos();
        } catch (SQLException e) {
            throw erroDeBanco(e);
        }
    }

    /** @return o id do usuário criado */
    public int cadastrar(String nome, String login, String senha, Perfil perfil, String setor)
            throws UsuarioException {

        ControleAcesso.exigir(Permissao.GERENCIAR_USUARIOS);

        String nomeLimpo = validarNome(nome);
        String loginLimpo = validarLogin(login);
        validarSenha(senha);
        validarPerfil(perfil);

        try {
            if (usuarioDAO.buscarPorLogin(loginLimpo).isPresent()) {
                throw new UsuarioException("Já existe um usuário com o login \"" + loginLimpo + "\".");
            }

            Usuario novo = new Usuario();
            novo.setNome(nomeLimpo);
            novo.setLogin(loginLimpo);
            novo.setSenhaHash(BCrypt.hashpw(senha, BCrypt.gensalt())); // RNF-01
            novo.setCargo(perfil.getDescricao());
            novo.setSetor(textoOuNulo(setor));
            novo.setAtivo(true);

            // Inserção e auditoria juntas: ou as duas acontecem, ou nenhuma (RNF-02).
            return Transacao.executar(conexao -> {
                int id = usuarioDAO.inserir(conexao, novo);
                auditoria.registrarInsercao(conexao, ENTIDADE, id);
                return id;
            });

        } catch (SQLException e) {
            throw erroDeBanco(e);
        }
    }

    /** Altera nome, perfil, setor e situação do usuário, registrando no histórico. */
    public void atualizar(int idUsuario, String nome, Perfil perfil, String setor, boolean ativo)
            throws UsuarioException {

        ControleAcesso.exigir(Permissao.GERENCIAR_USUARIOS);

        String nomeLimpo = validarNome(nome);
        validarPerfil(perfil);

        try {
            Usuario antigo = usuarioDAO.buscarPorId(idUsuario)
                    .orElseThrow(() -> new UsuarioException("Usuário não encontrado."));

            protegerContraAutoBloqueio(antigo, perfil, ativo);

            Usuario novo = new Usuario();
            novo.setIdUsuario(idUsuario);
            novo.setNome(nomeLimpo);
            novo.setCargo(perfil.getDescricao());
            novo.setSetor(textoOuNulo(setor));
            novo.setAtivo(ativo);

            Transacao.executar(conexao -> {
                usuarioDAO.atualizar(conexao, novo);

                // Um registro por campo alterado; campos iguais são ignorados pela auditoria.
                auditoria.registrarAlteracao(conexao, ENTIDADE, idUsuario, "nome",
                        antigo.getNome(), novo.getNome(), null);
                auditoria.registrarAlteracao(conexao, ENTIDADE, idUsuario, "cargo",
                        antigo.getCargo(), novo.getCargo(), null);
                auditoria.registrarAlteracao(conexao, ENTIDADE, idUsuario, "setor",
                        antigo.getSetor(), novo.getSetor(), null);
                auditoria.registrarAlteracao(conexao, ENTIDADE, idUsuario, "ativo",
                        antigo.isAtivo(), novo.isAtivo(), null);
                return null;
            });

        } catch (SQLException e) {
            throw erroDeBanco(e);
        }
    }

    /** Define uma nova senha para o usuário, guardada em hash. */
    public void redefinirSenha(int idUsuario, String novaSenha) throws UsuarioException {
        ControleAcesso.exigir(Permissao.GERENCIAR_USUARIOS);
        validarSenha(novaSenha);

        try {
            usuarioDAO.buscarPorId(idUsuario)
                    .orElseThrow(() -> new UsuarioException("Usuário não encontrado."));

            String novoHash = BCrypt.hashpw(novaSenha, BCrypt.gensalt()); // RNF-01

            Transacao.executar(conexao -> {
                usuarioDAO.atualizarSenha(conexao, idUsuario, novoHash);
                // NUNCA gravar senha ou hash no histórico: só o fato de ter mudado.
                auditoria.registrarAlteracao(conexao, ENTIDADE, idUsuario, "senha",
                        "(oculta)", "(alterada)", "Redefinição de senha");
                return null;
            });

        } catch (SQLException e) {
            throw erroDeBanco(e);
        }
    }

    // ===================== VALIDAÇÕES =====================
    // "static" e sem acesso ao banco: podem ser testadas com JUnit sem banco de dados.

    /** Confere o nome e devolve o texto sem os espaços das pontas. */
    static String validarNome(String nome) throws UsuarioException {
        if (nome == null || nome.isBlank()) {
            throw new UsuarioException("Informe o nome.");
        }
        String limpo = nome.trim();
        if (limpo.length() > TAMANHO_MAXIMO_NOME) {
            throw new UsuarioException("O nome pode ter no máximo " + TAMANHO_MAXIMO_NOME + " caracteres.");
        }
        return limpo;
    }

    /** Confere o formato do login e o devolve em minúsculas. */
    static String validarLogin(String login) throws UsuarioException {
        if (login == null || login.isBlank()) {
            throw new UsuarioException("Informe o login.");
        }
        String limpo = login.trim().toLowerCase();
        if (!FORMATO_LOGIN.matcher(limpo).matches()) {
            throw new UsuarioException(
                    "O login deve ter de 3 a 40 caracteres: letras, números, ponto ou sublinhado, sem espaços.");
        }
        return limpo;
    }

    /** Confere se a senha tem o tamanho mínimo, com letras e números. */
    static void validarSenha(String senha) throws UsuarioException {
        if (senha == null || senha.length() < TAMANHO_MINIMO_SENHA
                || !TEM_LETRA.matcher(senha).find()
                || !TEM_NUMERO.matcher(senha).find()) {
            throw new UsuarioException(
                    "A senha deve ter pelo menos " + TAMANHO_MINIMO_SENHA + " caracteres, com letras e números.");
        }
    }

    /** Exige que um perfil tenha sido escolhido. */
    private static void validarPerfil(Perfil perfil) throws UsuarioException {
        if (perfil == null) {
            throw new UsuarioException("Selecione o perfil.");
        }
    }

    /** Impede que o usuário logado se desative ou mude o próprio perfil (evita ficar trancado fora). */
    private void protegerContraAutoBloqueio(Usuario alvo, Perfil novoPerfil, boolean novoAtivo)
            throws UsuarioException {

        int idLogado = Sessao.getUsuarioLogado().getIdUsuario();
        if (alvo.getIdUsuario() != idLogado) {
            return;
        }
        if (!novoAtivo) {
            throw new UsuarioException("Você não pode desativar o seu próprio usuário.");
        }
        if (!novoPerfil.getDescricao().equalsIgnoreCase(alvo.getCargo())) {
            throw new UsuarioException("Você não pode alterar o seu próprio perfil.");
        }
    }

    /** Remove os espaços das pontas; devolve nulo se o texto ficar vazio. */
    private static String textoOuNulo(String texto) {
        return (texto == null || texto.isBlank()) ? null : texto.trim();
    }

    /** Converte o erro do banco em um erro de usuário com mensagem para a tela. */
    private static UsuarioException erroDeBanco(SQLException e) {
        return new UsuarioException("Não foi possível acessar o banco de dados.", e);
    }
}