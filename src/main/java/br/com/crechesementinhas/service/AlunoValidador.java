package br.com.crechesementinhas.service;

import br.com.crechesementinhas.model.Aluno;
import br.com.crechesementinhas.util.Cpf;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Regras dos DADOS do aluno (RF-02), sem acesso a banco.
 * Fica separada do AlunoService para poder ser testada sozinha.
 */
public final class AlunoValidador {

    static final int MAX_NOME = 100;
    static final int MAX_NATURALIDADE = 60;
    static final int MAX_NACIONALIDADE = 40;
    static final int MAX_OBSERVACAO = 2000;

    private AlunoValidador() {
    }

    /**
     * Limpa os dados digitados: tira espaços das pontas, troca texto vazio por null
     * e deixa só os dígitos do CPF. Altera o próprio objeto recebido.
     */
    public static void normalizar(Aluno aluno) {
        aluno.setNome(aparar(aluno.getNome()));
        aluno.setNaturalidade(aparar(aluno.getNaturalidade()));
        aluno.setNacionalidade(aparar(aluno.getNacionalidade()));
        aluno.setObservacao(aparar(aluno.getObservacao()));
        aluno.setCpf(Cpf.normalizar(aluno.getCpf()));
    }

    /**
     * Confere os dados já normalizados.
     *
     * @param aluno os dados do aluno
     * @param hoje  data de hoje (parâmetro para o teste não depender do relógio)
     * @return as mensagens de erro, uma por problema; lista vazia se estiver tudo certo
     */
    public static List<String> validar(Aluno aluno, LocalDate hoje) {
        List<String> erros = new ArrayList<>();

        if (aluno.getNome() == null) {
            erros.add("Informe o nome da criança.");
        } else if (aluno.getNome().length() > MAX_NOME) {
            erros.add("O nome pode ter no máximo " + MAX_NOME + " caracteres.");
        }

        if (aluno.getDataNascimento() == null) {
            erros.add("Informe a data de nascimento.");
        } else if (aluno.getDataNascimento().isAfter(hoje)) {
            erros.add("A data de nascimento não pode ser no futuro.");
        }

        if (aluno.getSexo() == null) {
            erros.add("Informe o sexo.");
        }

        // CPF é opcional (a criança pode não ter), mas, se vier, tem de ser válido.
        if (aluno.getCpf() != null && !Cpf.valido(aluno.getCpf())) {
            erros.add("CPF inválido.");
        }

        if (tamanho(aluno.getNaturalidade()) > MAX_NATURALIDADE) {
            erros.add("A naturalidade pode ter no máximo " + MAX_NATURALIDADE + " caracteres.");
        }
        if (tamanho(aluno.getNacionalidade()) > MAX_NACIONALIDADE) {
            erros.add("A nacionalidade pode ter no máximo " + MAX_NACIONALIDADE + " caracteres.");
        }
        if (tamanho(aluno.getObservacao()) > MAX_OBSERVACAO) {
            erros.add("A observação pode ter no máximo " + MAX_OBSERVACAO + " caracteres.");
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

    private static int tamanho(String texto) {
        return texto == null ? 0 : texto.length();
    }
}
