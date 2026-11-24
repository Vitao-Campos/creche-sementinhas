package br.com.crechesementinhas.model;

import java.time.LocalDateTime;

/**
 * Uma linha do histórico de alterações, pronta para exibir (RNF-02):
 * já traz o NOME de quem alterou e de quem/o que foi afetado.
 *
 * @param referencia nome da pessoa afetada (criança, responsável, autorizado); pode ser null
 */
public record RegistroHistorico(
        LocalDateTime dataHora,
        String usuario,
        String entidade,
        int idEntidade,
        String referencia,
        TipoAlteracao tipoAlteracao,
        String campoAlterado,
        String valorAnterior,
        String valorNovo,
        String justificativa) {
}
