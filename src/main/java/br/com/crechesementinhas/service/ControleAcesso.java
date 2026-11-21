package br.com.crechesementinhas.service;

import br.com.crechesementinhas.model.Perfil;
import br.com.crechesementinhas.model.Permissao;
import br.com.crechesementinhas.util.Sessao;

/**
 * Verifica as permissões do usuário logado (RNF-03).
 *
 * Uso pelas frentes:
 *   - Na tela:    botaoDesligar.setDisable(!ControleAcesso.temPermissao(Permissao.DESLIGAR_ALUNO));
 *   - No service: ControleAcesso.exigir(Permissao.DESLIGAR_ALUNO);
 */
public final class ControleAcesso {

    private ControleAcesso() {
    }

    /**
     * Princípio do "negar por padrão": sem sessão ou com cargo
     * desconhecido, a resposta é sempre NÃO.
     */
    public static boolean temPermissao(Permissao permissao) {
        if (!Sessao.isAtiva()) {
            return false;
        }
        String cargo = Sessao.getUsuarioLogado().getCargo();
        return Perfil.deCargo(cargo)
                .map(perfil -> perfil.possui(permissao))
                .orElse(false);
    }

    /** Barra a operação se o usuário não tiver a permissão. */
    public static void exigir(Permissao permissao) {
        if (!temPermissao(permissao)) {
            throw new SecurityException("Seu perfil não tem permissão para esta operação.");
        }
    }
}