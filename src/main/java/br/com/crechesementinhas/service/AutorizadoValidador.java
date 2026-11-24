package br.com.crechesementinhas.service;

import br.com.crechesementinhas.model.AlunoPessoaAutorizada;
import br.com.crechesementinhas.util.Telefone;
import java.util.ArrayList;
import java.util.List;

/**
 * Regras dos DADOS da pessoa autorizada (RF-12), sem acesso a banco.
 * Trabalha sobre AlunoPessoaAutorizada: nome, RG e telefone da pessoa + parentesco e tipo.
 */
public final class AutorizadoValidador {

    static final int MAX_NOME = 100;
    static final int MAX_RG = 14;
    static final int MAX_PARENTESCO = 30;

    private AutorizadoValidador() {
    }

    /** Apara espaços, vazio vira null, telefone fica só com dígitos, RG sem espaços nas pontas. */
    public static void normalizar(AlunoPessoaAutorizada a) {
        a.setNome(aparar(a.getNome()));
        a.setRg(aparar(a.getRg()));
        a.setParentesco(aparar(a.getParentesco()));
        a.setTelefone(Telefone.normalizar(a.getTelefone()));
    }

    /** Confere os dados já normalizados. Lista vazia = tudo certo. */
    public static List<String> validar(AlunoPessoaAutorizada a) {
        List<String> erros = new ArrayList<>();

        if (a.getIdAluno() == null) {
            erros.add("Escolha na lista a criança.");
        }
        if (a.getNome() == null) {
            erros.add("Informe o nome da pessoa autorizada.");
        } else if (a.getNome().length() > MAX_NOME) {
            erros.add("O nome pode ter no máximo " + MAX_NOME + " caracteres.");
        }
        if (a.getRg() == null) {
            erros.add("Informe o RG da pessoa autorizada.");
        } else if (a.getRg().length() > MAX_RG) {
            erros.add("O RG pode ter no máximo " + MAX_RG + " caracteres.");
        }
        if (a.getTelefone() == null) {
            erros.add("Informe o telefone da pessoa autorizada.");
        } else if (!Telefone.valido(a.getTelefone())) {
            erros.add("Telefone inválido. Use DDD + número, por exemplo (51) 91234-5678.");
        }
        if (a.getParentesco() != null && a.getParentesco().length() > MAX_PARENTESCO) {
            erros.add("O parentesco pode ter no máximo " + MAX_PARENTESCO + " caracteres.");
        }
        if (a.getTipoAutorizacao() == null) {
            erros.add("Informe o que a pessoa pode fazer (retirada, emergência ou ambos).");
        }
        return erros;
    }

    private static String aparar(String texto) {
        if (texto == null) {
            return null;
        }
        String limpo = texto.trim();
        return limpo.isEmpty() ? null : limpo;
    }
}
