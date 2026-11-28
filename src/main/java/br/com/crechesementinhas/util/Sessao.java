package br.com.crechesementinhas.util;

import br.com.crechesementinhas.model.Usuario;
import java.util.Objects;

/**
 * Guarda o usuário logado enquanto o sistema está aberto.
 * Usado pelo menu (perfis, RNF-03) e pela auditoria (RNF-02).
 */
public final class Sessao {

    // static: existe uma única sessão para o programa inteiro.
    private static Usuario usuarioLogado;

    /** Classe só com métodos estáticos: não é instanciada. */
    private Sessao() {
    }

    /** Guarda o usuário que acabou de entrar no sistema. */
    public static void iniciar(Usuario usuario) {
        usuarioLogado = Objects.requireNonNull(usuario, "Usuário não pode ser nulo.");
    }

    /** Devolve o usuário que está logado. */
    public static Usuario getUsuarioLogado() {
        if (usuarioLogado == null) {
            throw new IllegalStateException("Nenhum usuário logado.");
        }
        return usuarioLogado;
    }

    /** Indica se a turma está ativa. */
    public static boolean isAtiva() {
        return usuarioLogado != null;
    }

    /** Apaga o usuário logado (sair do sistema). */
    public static void encerrar() {
        usuarioLogado = null;
    }
}