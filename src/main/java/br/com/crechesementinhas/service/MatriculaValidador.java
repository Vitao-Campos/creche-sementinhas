package br.com.crechesementinhas.service;

import br.com.crechesementinhas.model.Aluno;
import br.com.crechesementinhas.model.Matricula;
import br.com.crechesementinhas.model.Turma;
import br.com.crechesementinhas.util.CalculadoraIdade;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Regras dos dados da matrícula e do desligamento, sem acesso a banco.
 */
public final class MatriculaValidador {

    static final int MAX_MOTIVO_ANDARILHA = 200;   // tamanho da coluna motivo_andarilha
    // Coluna justificativa tem 255: sobram ~100 para "Último mês: ..." e "Aviso prévio de N dias..."
    static final int MAX_JUSTIFICATIVA = 150;
    static final int MESES_MAX_ANTECEDENCIA = 12;  // matrícula pode começar até 1 ano à frente

    private MatriculaValidador() {
    }

    /** Erros que impedem a matrícula. Lista vazia = pode matricular. */
    public static List<String> validarEfetivacao(Aluno aluno, Turma turma, LocalDate dataMatricula,
                                                 boolean andarilha, String motivoAndarilha,
                                                 boolean jaTemMatriculaAtiva, LocalDate hoje) {
        List<String> erros = new ArrayList<>();
        if (aluno == null || aluno.getIdAluno() == null) {
            erros.add("Selecione a criança (cadastre-a antes na tela Alunos).");
        } else if (jaTemMatriculaAtiva) {
            erros.add("Esta criança já tem uma matrícula ATIVA.");
        }
        if (turma == null) {
            erros.add("Selecione a turma.");
        } else if (turma.getVagasLivres() <= 0) {
            erros.add("A turma " + turma.getNome() + " está lotada ("
                    + turma.getCapacidade() + " vagas).");
        }
        if (dataMatricula == null) {
            erros.add("Informe a data de início da matrícula.");
        } else {
            if (aluno != null && aluno.getDataNascimento() != null
                    && dataMatricula.isBefore(aluno.getDataNascimento())) {
                erros.add("A data da matrícula não pode ser antes do nascimento da criança.");
            }
            if (dataMatricula.isAfter(hoje.plusMonths(MESES_MAX_ANTECEDENCIA))) {
                erros.add("A data da matrícula pode ser no máximo " + MESES_MAX_ANTECEDENCIA
                        + " meses à frente.");
            }
        }
        String motivo = aparar(motivoAndarilha);
        if (andarilha && motivo == null) {
            erros.add("Informe o motivo da matrícula andarilha.");
        } else if (motivo != null && motivo.length() > MAX_MOTIVO_ANDARILHA) {
            erros.add("O motivo pode ter no máximo " + MAX_MOTIVO_ANDARILHA + " caracteres.");
        }
        return erros;
    }

    /**
     * Aviso (não impede): a idade da criança na data da matrícula está fora da faixa da turma.
     * O usuário precisa confirmar para seguir.
     */
    public static Optional<String> avisoFaixaEtaria(Aluno aluno, Turma turma, LocalDate dataMatricula) {
        if (aluno == null || turma == null || dataMatricula == null || aluno.getDataNascimento() == null) {
            return Optional.empty();
        }
        long meses = CalculadoraIdade.emMeses(aluno.getDataNascimento(), dataMatricula);
        if (turma.aceitaIdade(meses)) {
            return Optional.empty();
        }
        return Optional.of("Na data da matrícula a criança terá "
                + CalculadoraIdade.descrever(aluno.getDataNascimento(), dataMatricula)
                + " (" + meses + " meses), fora da faixa da turma " + turma.getNome()
                + " (" + turma.getIdadeMinimaMeses() + " a " + turma.getIdadeMaximaMeses() + " meses).");
    }

    /** Erros que impedem o desligamento. */
    public static List<String> validarDesligamento(Matricula matricula, LocalDate desligamento,
                                                   String justificativa, LocalDate hoje) {
        List<String> erros = new ArrayList<>();
        if (matricula == null || matricula.getIdMatricula() == null) {
            erros.add("Selecione uma criança com matrícula ATIVA.");
        } else if (!matricula.isAtiva()) {
            erros.add("Só matrículas ATIVAS podem ser encerradas.");
        }
        if (desligamento == null) {
            erros.add("Informe a data de desligamento (último dia de frequência).");
        } else {
            if (matricula != null && matricula.getDataMatricula() != null
                    && desligamento.isBefore(matricula.getDataMatricula())) {
                erros.add("A data de desligamento não pode ser antes da data da matrícula.");
            }
            // RN-12: aviso menor que 30 dias NÃO bloqueia aqui; o service exige
            // a confirmação da Direção/Suporte e registra a exceção na auditoria.
        }
        String texto = aparar(justificativa);
        if (texto == null) {
            erros.add("Informe o motivo do desligamento.");
        } else if (texto.length() > MAX_JUSTIFICATIVA) {
            erros.add("O motivo pode ter no máximo " + MAX_JUSTIFICATIVA + " caracteres.");
        }
        return erros;
    }

    static String aparar(String texto) {
        if (texto == null) {
            return null;
        }
        String limpo = texto.trim();
        return limpo.isEmpty() ? null : limpo;
    }
}
