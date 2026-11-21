package br.com.crechesementinhas.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Um registro da trilha de auditoria (RNF-02).
 * É um "record": imutável por natureza, como todo registro de auditoria deve ser.
 *
 * @param idHistorico null antes de ser gravado no banco
 */
public record HistoricoAlteracao(
        Integer idHistorico,
        int idUsuario,
        LocalDateTime dataHora,
        String entidade,
        int idEntidade,
        TipoAlteracao tipoAlteracao,
        String campoAlterado,
        String valorAnterior,
        String valorNovo,
        String justificativa) {

    // Construtor compacto: valida os campos obrigatórios (NOT NULL no banco).
    public HistoricoAlteracao {
        Objects.requireNonNull(dataHora, "dataHora é obrigatória");
        Objects.requireNonNull(entidade, "entidade é obrigatória");
        Objects.requireNonNull(tipoAlteracao, "tipoAlteracao é obrigatório");
    }
}