package br.com.crechesementinhas.model;

import static br.com.crechesementinhas.model.Permissao.CADASTRAR_MATRICULA;
import static br.com.crechesementinhas.model.Permissao.CONFIRMAR_MATRICULA;
import static br.com.crechesementinhas.model.Permissao.DESLIGAR_ALUNO;
import static br.com.crechesementinhas.model.Permissao.VER_RELATORIOS_FINANCEIROS;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;

/**
 * Perfis de acesso e suas permissões (RNF-03).
 * A "descricao" deve ser IGUAL ao texto gravado na coluna usuario.cargo.
 *
 * Para mudar quem pode o quê, altere apenas a lista de permissões abaixo.
 */
public enum Perfil {

    SECRETARIA("Secretaria",
            EnumSet.of(CADASTRAR_MATRICULA, CONFIRMAR_MATRICULA)),

    DIRECAO("Direção",
            EnumSet.of(CADASTRAR_MATRICULA, CONFIRMAR_MATRICULA,
                       DESLIGAR_ALUNO, VER_RELATORIOS_FINANCEIROS)),

    SUPORTE("Suporte",
            EnumSet.allOf(Permissao.class));

    private final String descricao;
    private final Set<Permissao> permissoes;

    /** Cria o perfil com a descrição mostrada na tela e as permissões que ele concede. */
    Perfil(String descricao, EnumSet<Permissao> permissoes) {
        this.descricao = descricao;
        // Imutável: ninguém consegue adicionar permissões em tempo de execução.
        this.permissoes = Collections.unmodifiableSet(permissoes);
    }

    /** Devolve a descrição. */
    public String getDescricao() {
        return descricao;
    }

    /**
     * Desempenho: EnumSet guarda as permissões como bits (um "long"),
     * então contains() é O(1), uma única operação de máquina.
     */
    public boolean possui(Permissao permissao) {
        return permissoes.contains(permissao);
    }

    /** Converte o texto da coluna "cargo" no perfil correspondente. */
    public static Optional<Perfil> deCargo(String cargo) {
        if (cargo == null) {
            return Optional.empty();
        }
        for (Perfil perfil : values()) {
            if (perfil.descricao.equalsIgnoreCase(cargo.trim())) {
                return Optional.of(perfil);
            }
        }
        return Optional.empty();
    }
}