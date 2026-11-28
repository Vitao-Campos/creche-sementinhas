package br.com.crechesementinhas.model;

import java.time.LocalDate;

/** Dados da criança e da matrícula que aparecem no topo da ficha (RF-05: nada é digitado de novo). */
public record CabecalhoFicha(int idMatricula, String numeroMatricula, String status, int idAluno,
                             String nomeAluno, LocalDate dataNascimento, String nomeTurma) {
}
