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

    private Sessao() {
    }

    public static void iniciar(Usuario usuario) {
        usuarioLogado = Objects.requireNonNull(usuario, "Usuário não pode ser nulo.");
    }

    public static Usuario getUsuarioLogado() {
        if (usuarioLogado == null) {
            throw new IllegalStateException("Nenhum usuário logado.");
        }
        return usuarioLogado;
    }

    public static boolean isAtiva() {
        return usuarioLogado != null;
    }

    public static void encerrar() {
        usuarioLogado = null;
    }
}