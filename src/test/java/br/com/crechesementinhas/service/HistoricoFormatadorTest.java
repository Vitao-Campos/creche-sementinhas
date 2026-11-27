package br.com.crechesementinhas.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import br.com.crechesementinhas.model.TipoAlteracao;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class HistoricoFormatadorTest {

    @Test
    void dataHoraNoFormatoBrasileiro() {
        assertEquals("05/03/2026 14:07", HistoricoFormatador.dataHora(LocalDateTime.of(2026, 3, 5, 14, 7, 59)));
        assertEquals("", HistoricoFormatador.dataHora(null));
    }

    @Test
    void rotulosConhecidosEDesconhecidos() {
        assertEquals("Vínculo com responsável", HistoricoFormatador.entidade("aluno_responsavel"));
        assertEquals("tabela_nova", HistoricoFormatador.entidade("tabela_nova"));
        assertEquals("Data de nascimento", HistoricoFormatador.campo("data_nascimento"));
        assertEquals("—", HistoricoFormatador.campo(null));
        assertEquals("Cadastro", HistoricoFormatador.acao(TipoAlteracao.INSERCAO));
        assertEquals("Alteração", HistoricoFormatador.acao(TipoAlteracao.ALTERACAO));
        assertEquals("Exclusão", HistoricoFormatador.acao(TipoAlteracao.EXCLUSAO));
    }

    @Test
    void valoresFicamLegiveis() {
        assertEquals("(vazio)", HistoricoFormatador.valor("email", null));
        assertEquals("(vazio)", HistoricoFormatador.valor("email", " "));
        assertEquals("Sim", HistoricoFormatador.valor("ativo", "true"));
        assertEquals("Não", HistoricoFormatador.valor("ativo", "false"));
        assertEquals("01/03/2026", HistoricoFormatador.valor("data_fim", "2026-03-01"));
        assertEquals("529.982.247-25", HistoricoFormatador.valor("cpf", "52998224725"));
        assertEquals("(11) 91234-5678", HistoricoFormatador.valor("telefone", "11912345678"));
        assertEquals("90010-000", HistoricoFormatador.valor("cep", "90010000"));
        assertEquals("Rua A, 10", HistoricoFormatador.valor("endereco", "Rua A, 10"));
    }

    @Test
    void registroSemCampoMostraTraco() {
        assertEquals("—", HistoricoFormatador.valor(null, null));
    }

    @Test
    void dataInvalidaVoltaComoVeio() {
        assertEquals("2026-13-45", HistoricoFormatador.valor("data_fim", "2026-13-45"));
    }

    @Test
    void sobreJuntaTipoENome() {
        assertEquals("Aluno — Ana Lima", HistoricoFormatador.sobre("aluno", "Ana Lima"));
        assertEquals("Aluno", HistoricoFormatador.sobre("aluno", null));
    }
}
