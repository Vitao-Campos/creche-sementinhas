package br.com.crechesementinhas.model;

/** Linha da lista de matrículas ativas, com a situação da ficha de anamnese. */
public record MatriculaComFicha(int idMatricula, String numeroMatricula, String nomeAluno,
                                boolean fichaPreenchida) {
}
