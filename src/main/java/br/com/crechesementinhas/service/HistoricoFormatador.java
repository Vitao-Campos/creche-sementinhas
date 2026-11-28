package br.com.crechesementinhas.service;

import br.com.crechesementinhas.model.TipoAlteracao;
import br.com.crechesementinhas.util.Cpf;
import br.com.crechesementinhas.util.Telefone;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Map;

/**
 * Transforma os dados crus do histórico (nomes de tabela e coluna, "true", "2026-03-01")
 * em texto legível para a tela. Sem acesso a banco nem a JavaFX, para ser testada sozinha.
 */
public final class HistoricoFormatador {

    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/uuuu");
    private static final DateTimeFormatter DATA_HORA = DateTimeFormatter.ofPattern("dd/MM/uuuu HH:mm");

    private static final Map<String, String> ENTIDADES = Map.of(
            "aluno", "Aluno",
            "responsavel", "Responsável",
            "aluno_responsavel", "Vínculo com responsável",
            "pessoa_autorizada", "Pessoa autorizada",
            "aluno_pessoa_autorizada", "Autorização para buscar",
            "matricula", "Matrícula",
            "ficha_anamnese", "Ficha de anamnese");

    private static final Map<String, String> CAMPOS = Map.ofEntries(
            Map.entry("nome", "Nome"),
            Map.entry("data_nascimento", "Data de nascimento"),
            Map.entry("cpf", "CPF"),
            Map.entry("sexo", "Sexo"),
            Map.entry("naturalidade", "Naturalidade"),
            Map.entry("nacionalidade", "Nacionalidade"),
            Map.entry("observacao", "Observações"),
            Map.entry("telefone", "Telefone"),
            Map.entry("email", "E-mail"),
            Map.entry("cep", "CEP"),
            Map.entry("endereco", "Endereço"),
            Map.entry("profissao", "Profissão"),
            Map.entry("data_fim", "Data de fim do vínculo"),
            Map.entry("ativo", "Autorização ativa"),
            Map.entry("id_turma", "Turma"),
            Map.entry("andarilha", "Andarilha"),
            Map.entry("motivo_andarilha", "Motivo da andarilha"),
            Map.entry("andarilha_ate", "Andarilha até"),
            Map.entry("status", "Situação"),
            Map.entry("data_encerramento", "Data de encerramento"),
            Map.entry("autorizacao_imagem", "Autoriza uso de imagem"),
            Map.entry("possui_alergia", "Possui alergia"),
            Map.entry("descricao_alergia", "Descrição da alergia"),
            Map.entry("restricoes_alimentares", "Restrição alimentar"),
            Map.entry("descricao_restricao", "Descrição da restrição alimentar"),
            Map.entry("receita_antitermico", "Antitérmico autorizado"),
            Map.entry("possui_cirurgia", "Já fez cirurgia"),
            Map.entry("descricao_cirurgia", "Descrição da cirurgia"),
            Map.entry("possui_problemas_saude", "Problemas de saúde"),
            Map.entry("descricao_problemas_saude", "Descrição dos problemas de saúde"),
            Map.entry("usa_medicacao_continua", "Medicação contínua"),
            Map.entry("descricao_medicacao", "Descrição da medicação"),
            Map.entry("possui_fraturas", "Já teve fraturas"),
            Map.entry("descricao_fratura", "Descrição da fratura"),
            Map.entry("usa_mamadeira", "Usa mamadeira"),
            Map.entry("usa_formula", "Usa fórmula"),
            Map.entry("descricao_formula", "Descrição da fórmula"),
            Map.entry("usa_chupeta", "Usa chupeta"),
            Map.entry("usa_fralda", "Usa fralda"));

    /** Classe só com métodos estáticos: não é instanciada. */
    private HistoricoFormatador() {
    }

    /** "dd/MM/aaaa HH:mm"; vazio se a data for null. */
    public static String dataHora(LocalDateTime dataHora) {
        return dataHora == null ? "" : DATA_HORA.format(dataHora);
    }

    /** Nome da tabela do histórico em português, para a tela. */
    public static String entidade(String entidade) {
        return ENTIDADES.getOrDefault(entidade, entidade);
    }

    /** Nome da ação (cadastro, alteração ou exclusão), para a tela. */
    public static String acao(TipoAlteracao tipo) {
        return switch (tipo) {
            case INSERCAO -> "Cadastro";
            case ALTERACAO -> "Alteração";
            case EXCLUSAO -> "Exclusão";
        };
    }

    /** Nome do campo para o usuário; "—" quando o registro não é de um campo (cadastro, exclusão). */
    public static String campo(String campo) {
        if (campo == null || campo.isBlank()) {
            return "—";
        }
        return CAMPOS.getOrDefault(campo, campo);
    }

    /**
     * Valor para exibir: vazio vira "(vazio)", true/false viram Sim/Não, datas ficam dd/mm/aaaa
     * e CPF, telefone e CEP aparecem com máscara. Registros sem campo (cadastro) mostram "—".
     */
    public static String valor(String campo, String valor) {
        if (campo == null || campo.isBlank()) {
            return "—";
        }
        if (valor == null || valor.isBlank()) {
            return "(vazio)";
        }
        if ("true".equals(valor)) {
            return "Sim";
        }
        if ("false".equals(valor)) {
            return "Não";
        }
        switch (campo) {
            case "cpf":
                return Cpf.formatar(valor);
            case "telefone":
                return Telefone.formatar(valor);
            case "cep":
                return valor.length() == 8 ? valor.substring(0, 5) + "-" + valor.substring(5) : valor;
            default:
                break;
        }
        if (valor.length() == 10 && valor.charAt(4) == '-') {
            try {
                return DATA.format(LocalDate.parse(valor));
            } catch (DateTimeParseException e) {
                return valor;
            }
        }
        return valor;
    }

    /** "Aluno — Ana Lima" ou só "Aluno" quando não há nome para mostrar. */
    public static String sobre(String entidade, String referencia) {
        String rotulo = entidade(entidade);
        return referencia == null || referencia.isBlank() ? rotulo : rotulo + " — " + referencia;
    }
}
