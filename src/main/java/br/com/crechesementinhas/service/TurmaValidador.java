package br.com.crechesementinhas.service;

import br.com.crechesementinhas.model.AlunoTurma;
import br.com.crechesementinhas.model.SituacaoFaixaEtaria;
import br.com.crechesementinhas.model.Turma;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Regras da troca de turma (EX-07) e do registro de andarilha (RF-11 / RN-08), sem acesso a banco.
 */
public final class TurmaValidador {

    static final int MAX_JUSTIFICATIVA = 200;      // a auditoria acrescenta o aviso de faixa (cabe em 255)
    static final int MAX_MOTIVO_ANDARILHA = 200;   // tamanho da coluna motivo_andarilha

    private TurmaValidador() {
    }

    /** Erros que impedem a troca de turma. Lista vazia = pode trocar. */
    public static List<String> validarTroca(AlunoTurma aluno, Turma nova, String justificativa) {
        List<String> erros = new ArrayList<>();
        if (aluno == null) {
            erros.add("Selecione a criança na tabela.");
        } else if (!aluno.temMatriculaAtiva()) {
            erros.add("Só é possível trocar a turma de uma criança com matrícula ATIVA.");
        }
        if (nova == null) {
            erros.add("Selecione a nova turma.");
        } else {
            if (aluno != null && Objects.equals(aluno.getIdTurma(), nova.getIdTurma())) {
                erros.add("A criança já está na turma " + nova.getNome() + ".");
            } else if (nova.getVagasLivres() <= 0) {
                erros.add("A turma " + nova.getNome() + " está lotada (" + nova.getCapacidade() + " vagas).");
            }
        }
        String texto = MatriculaValidador.aparar(justificativa);
        if (texto == null) {
            erros.add("Informe o motivo da troca de turma.");
        } else if (texto.length() > MAX_JUSTIFICATIVA) {
            erros.add("O motivo pode ter no máximo " + MAX_JUSTIFICATIVA + " caracteres.");
        }
        return erros;
    }

    /** Aviso (não impede): a idade da criança hoje fica fora da faixa da NOVA turma. */
    public static Optional<String> avisoFaixaNaNovaTurma(AlunoTurma aluno, Turma nova, LocalDate hoje) {
        if (aluno == null || nova == null || aluno.getDataNascimento() == null) {
            return Optional.empty();
        }
        long meses = aluno.idadeEmMeses(hoje);
        SituacaoFaixaEtaria situacao = SituacaoFaixaEtaria.de(
                meses, nova.getIdadeMinimaMeses(), nova.getIdadeMaximaMeses());
        if (!situacao.isForaDaFaixa()) {
            return Optional.empty();
        }
        return Optional.of(aluno.getNome() + " tem " + meses + " meses, fora da faixa da turma "
                + nova.getNome() + " (" + nova.getIdadeMinimaMeses() + " a "
                + nova.getIdadeMaximaMeses() + " meses).");
    }

    /** Erros que impedem gravar o registro de andarilha (RF-11: motivo e prazo previsto). */
    public static List<String> validarAndarilha(AlunoTurma aluno, boolean andarilha, String motivo,
                                                LocalDate andarilhaAte, LocalDate hoje) {
        List<String> erros = new ArrayList<>();
        if (aluno == null) {
            erros.add("Selecione a criança na tabela.");
            return erros;
        }
        if (!aluno.temMatriculaAtiva()) {
            erros.add("Só é possível registrar andarilha numa matrícula ATIVA.");
            return erros;
        }
        String texto = MatriculaValidador.aparar(motivo);
        if (andarilha && texto == null) {
            erros.add("Informe o motivo da matrícula andarilha.");
        } else if (texto != null && texto.length() > MAX_MOTIVO_ANDARILHA) {
            erros.add("O motivo pode ter no máximo " + MAX_MOTIVO_ANDARILHA + " caracteres.");
        }
        if (andarilha) {
            MatriculaValidador.validarPrazoAndarilha(andarilhaAte, hoje, erros);
        }
        String motivoNovo = andarilha ? texto : null;
        LocalDate prazoNovo = andarilha ? andarilhaAte : null;
        if (erros.isEmpty() && aluno.isAndarilha() == andarilha
                && Objects.equals(MatriculaValidador.aparar(aluno.getMotivoAndarilha()), motivoNovo)
                && Objects.equals(aluno.getAndarilhaAte(), prazoNovo)) {
            erros.add("Nada mudou no registro de andarilha.");
        }
        return erros;
    }
}
