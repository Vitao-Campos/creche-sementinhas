package br.com.crechesementinhas.service;

import br.com.crechesementinhas.model.AlunoResponsavel;
import br.com.crechesementinhas.model.Responsavel;
import br.com.crechesementinhas.util.Cpf;
import br.com.crechesementinhas.util.Telefone;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Regras dos DADOS do responsável e do vínculo com a criança (RF-02),
 * sem acesso a banco, para poder ser testada sozinha.
 */
public final class ResponsavelValidador {

    static final int MAX_NOME = 100;
    static final int MAX_EMAIL = 100;
    static final int MAX_ENDERECO = 150;
    static final int MAX_PROFISSAO = 60;
    static final int MAX_PARENTESCO = 30;

    // Conferência simples (algo@dominio.ext): o objetivo é pegar erro de digitação, não validar o servidor.
    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    private ResponsavelValidador() {
    }

    /** Limpa o que foi digitado: apara espaços, vazio vira null, CPF/telefone/CEP ficam só com dígitos. */
    public static void normalizar(Responsavel r) {
        r.setNome(aparar(r.getNome()));
        r.setEmail(aparar(r.getEmail()));
        r.setEndereco(aparar(r.getEndereco()));
        r.setProfissao(aparar(r.getProfissao()));
        r.setCpf(Cpf.normalizar(r.getCpf()));
        r.setTelefone(Telefone.normalizar(r.getTelefone()));
        String cep = r.getCep() == null ? null : r.getCep().replaceAll("\\D", "");
        r.setCep(cep == null || cep.isEmpty() ? null : cep);
    }

    /** Confere os dados já normalizados. Lista vazia = tudo certo. */
    public static List<String> validar(Responsavel r) {
        List<String> erros = new ArrayList<>();

        if (r.getNome() == null) {
            erros.add("Informe o nome do responsável.");
        } else if (r.getNome().length() > MAX_NOME) {
            erros.add("O nome pode ter no máximo " + MAX_NOME + " caracteres.");
        }

        // CPF é obrigatório para o responsável (e único, conferido no service).
        if (r.getCpf() == null) {
            erros.add("Informe o CPF do responsável.");
        } else if (!Cpf.valido(r.getCpf())) {
            erros.add("CPF inválido.");
        }

        if (r.getTelefone() == null) {
            erros.add("Informe o telefone do responsável.");
        } else if (!Telefone.valido(r.getTelefone())) {
            erros.add("Telefone inválido. Use DDD + número, por exemplo (11) 91234-5678.");
        }

        if (r.getEmail() != null) {
            if (r.getEmail().length() > MAX_EMAIL) {
                erros.add("O e-mail pode ter no máximo " + MAX_EMAIL + " caracteres.");
            } else if (!EMAIL.matcher(r.getEmail()).matches()) {
                erros.add("E-mail inválido.");
            }
        }

        if (r.getCep() != null && r.getCep().length() != 8) {
            erros.add("CEP inválido. Informe os 8 dígitos.");
        }
        if (tamanho(r.getEndereco()) > MAX_ENDERECO) {
            erros.add("O endereço pode ter no máximo " + MAX_ENDERECO + " caracteres.");
        }
        if (tamanho(r.getProfissao()) > MAX_PROFISSAO) {
            erros.add("A profissão pode ter no máximo " + MAX_PROFISSAO + " caracteres.");
        }
        return erros;
    }

    /** Apara o parentesco (a tela não faz isso sozinha). */
    public static void normalizarVinculo(AlunoResponsavel v) {
        v.setGrauParentesco(aparar(v.getGrauParentesco()));
    }

    /** Confere o vínculo já normalizado: criança, parentesco e datas. */
    public static List<String> validarVinculo(AlunoResponsavel v) {
        List<String> erros = new ArrayList<>();

        if (v.getIdAluno() == null) {
            erros.add("Escolha a criança.");
        }
        if (v.getGrauParentesco() == null) {
            erros.add("Informe o grau de parentesco.");
        } else if (v.getGrauParentesco().length() > MAX_PARENTESCO) {
            erros.add("O parentesco pode ter no máximo " + MAX_PARENTESCO + " caracteres.");
        }
        if (v.getDataInicio() == null) {
            erros.add("Informe a data de início do vínculo.");
        } else if (v.getDataFim() != null && v.getDataFim().isBefore(v.getDataInicio())) {
            erros.add("A data de fim não pode ser anterior à data de início.");
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
