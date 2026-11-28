package br.com.crechesementinhas.service;

import br.com.crechesementinhas.model.FichaAnamnese;
import java.util.ArrayList;
import java.util.List;

/**
 * Regras da ficha de anamnese, sem banco nem JavaFX (testável sozinha):
 * resposta "sim" com descrição obrigatória; resposta "não" descarta a descrição.
 */
public final class AnamneseValidador {

    static final int MAX = 255;

    private AnamneseValidador() {
    }

    /** Apara os textos e apaga a descrição dos itens respondidos "não". */
    public static void normalizar(FichaAnamnese f) {
        f.setDescricaoAlergia(descricao(f.isPossuiAlergia(), f.getDescricaoAlergia()));
        f.setDescricaoRestricao(descricao(f.isRestricoesAlimentares(), f.getDescricaoRestricao()));
        f.setDescricaoCirurgia(descricao(f.isPossuiCirurgia(), f.getDescricaoCirurgia()));
        f.setDescricaoProblemasSaude(descricao(f.isPossuiProblemasSaude(), f.getDescricaoProblemasSaude()));
        f.setDescricaoMedicacao(descricao(f.isUsaMedicacaoContinua(), f.getDescricaoMedicacao()));
        f.setDescricaoFratura(descricao(f.isPossuiFraturas(), f.getDescricaoFratura()));
        f.setDescricaoFormula(descricao(f.isUsaFormula(), f.getDescricaoFormula()));
        f.setReceitaAntitermico(descricao(true, f.getReceitaAntitermico()));
    }

    public static List<String> validar(FichaAnamnese f) {
        List<String> erros = new ArrayList<>();
        exigir(erros, f.isPossuiAlergia(), f.getDescricaoAlergia(), "a alergia");
        exigir(erros, f.isRestricoesAlimentares(), f.getDescricaoRestricao(), "a restrição alimentar");
        exigir(erros, f.isPossuiCirurgia(), f.getDescricaoCirurgia(), "a cirurgia");
        exigir(erros, f.isPossuiProblemasSaude(), f.getDescricaoProblemasSaude(), "o problema de saúde");
        exigir(erros, f.isUsaMedicacaoContinua(), f.getDescricaoMedicacao(), "a medicação contínua");
        exigir(erros, f.isPossuiFraturas(), f.getDescricaoFratura(), "a fratura");
        exigir(erros, f.isUsaFormula(), f.getDescricaoFormula(), "a fórmula");
        limite(erros, f.getReceitaAntitermico(), "O antitérmico autorizado");
        return erros;
    }

    private static void exigir(List<String> erros, boolean marcado, String descricao, String assunto) {
        if (marcado && (descricao == null || descricao.isEmpty())) {
            erros.add("Descreva " + assunto + " (ou desmarque o item).");
        }
        limite(erros, descricao, "A descrição sobre " + assunto);
    }

    private static void limite(List<String> erros, String texto, String nome) {
        if (texto != null && texto.length() > MAX) {
            erros.add(nome + " pode ter no máximo " + MAX + " caracteres.");
        }
    }

    private static String descricao(boolean vale, String texto) {
        if (!vale || texto == null) {
            return null;
        }
        String limpo = texto.strip();
        return limpo.isEmpty() ? null : limpo;
    }
}
