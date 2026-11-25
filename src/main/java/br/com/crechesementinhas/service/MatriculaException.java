package br.com.crechesementinhas.service;

/**
 * Erro de regra de negócio na matrícula ou no desligamento (EX-06),
 * com mensagem pronta para mostrar ao usuário.
 * Não é "checked" para atravessar o bloco da Transacao (que faz rollback).
 */
public class MatriculaException extends RuntimeException {

    public MatriculaException(String mensagem) {
        super(mensagem);
    }
}
