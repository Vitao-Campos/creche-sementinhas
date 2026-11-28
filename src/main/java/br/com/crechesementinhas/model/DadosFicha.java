package br.com.crechesementinhas.model;

import java.util.List;

/** Tudo que entra na ficha impressa: cabeçalho, respostas (null se ainda não preenchida), responsáveis e autorizados. */
public record DadosFicha(CabecalhoFicha cabecalho, FichaAnamnese ficha,
                         List<AlunoResponsavel> responsaveis, List<AlunoPessoaAutorizada> autorizados) {
}
