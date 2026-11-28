package br.com.crechesementinhas.model;

/**
 * Ficha de anamnese (saúde e rotina) de uma matrícula: uma por matrícula (RN-10).
 * Cada item "sim/não" tem, quando faz sentido, uma descrição que só vale se a resposta for sim.
 */
public class FichaAnamnese {

    private Integer idFichaAnamnese;
    private Integer idMatricula;
    private boolean autorizacaoImagem;
    private boolean possuiAlergia;
    private String descricaoAlergia;
    private boolean restricoesAlimentares;
    private String descricaoRestricao;
    private String receitaAntitermico;
    private boolean possuiCirurgia;
    private String descricaoCirurgia;
    private boolean possuiProblemasSaude;
    private String descricaoProblemasSaude;
    private boolean usaMedicacaoContinua;
    private String descricaoMedicacao;
    private boolean possuiFraturas;
    private String descricaoFratura;
    private boolean usaMamadeira;
    private boolean usaFormula;
    private String descricaoFormula;
    private boolean usaChupeta;
    private boolean usaFralda;

    public Integer getIdFichaAnamnese() { return idFichaAnamnese; }
    public void setIdFichaAnamnese(Integer id) { this.idFichaAnamnese = id; }
    public Integer getIdMatricula() { return idMatricula; }
    public void setIdMatricula(Integer idMatricula) { this.idMatricula = idMatricula; }
    public boolean isAutorizacaoImagem() { return autorizacaoImagem; }
    public void setAutorizacaoImagem(boolean v) { this.autorizacaoImagem = v; }
    public boolean isPossuiAlergia() { return possuiAlergia; }
    public void setPossuiAlergia(boolean v) { this.possuiAlergia = v; }
    public String getDescricaoAlergia() { return descricaoAlergia; }
    public void setDescricaoAlergia(String v) { this.descricaoAlergia = v; }
    public boolean isRestricoesAlimentares() { return restricoesAlimentares; }
    public void setRestricoesAlimentares(boolean v) { this.restricoesAlimentares = v; }
    public String getDescricaoRestricao() { return descricaoRestricao; }
    public void setDescricaoRestricao(String v) { this.descricaoRestricao = v; }
    public String getReceitaAntitermico() { return receitaAntitermico; }
    public void setReceitaAntitermico(String v) { this.receitaAntitermico = v; }
    public boolean isPossuiCirurgia() { return possuiCirurgia; }
    public void setPossuiCirurgia(boolean v) { this.possuiCirurgia = v; }
    public String getDescricaoCirurgia() { return descricaoCirurgia; }
    public void setDescricaoCirurgia(String v) { this.descricaoCirurgia = v; }
    public boolean isPossuiProblemasSaude() { return possuiProblemasSaude; }
    public void setPossuiProblemasSaude(boolean v) { this.possuiProblemasSaude = v; }
    public String getDescricaoProblemasSaude() { return descricaoProblemasSaude; }
    public void setDescricaoProblemasSaude(String v) { this.descricaoProblemasSaude = v; }
    public boolean isUsaMedicacaoContinua() { return usaMedicacaoContinua; }
    public void setUsaMedicacaoContinua(boolean v) { this.usaMedicacaoContinua = v; }
    public String getDescricaoMedicacao() { return descricaoMedicacao; }
    public void setDescricaoMedicacao(String v) { this.descricaoMedicacao = v; }
    public boolean isPossuiFraturas() { return possuiFraturas; }
    public void setPossuiFraturas(boolean v) { this.possuiFraturas = v; }
    public String getDescricaoFratura() { return descricaoFratura; }
    public void setDescricaoFratura(String v) { this.descricaoFratura = v; }
    public boolean isUsaMamadeira() { return usaMamadeira; }
    public void setUsaMamadeira(boolean v) { this.usaMamadeira = v; }
    public boolean isUsaFormula() { return usaFormula; }
    public void setUsaFormula(boolean v) { this.usaFormula = v; }
    public String getDescricaoFormula() { return descricaoFormula; }
    public void setDescricaoFormula(String v) { this.descricaoFormula = v; }
    public boolean isUsaChupeta() { return usaChupeta; }
    public void setUsaChupeta(boolean v) { this.usaChupeta = v; }
    public boolean isUsaFralda() { return usaFralda; }
    public void setUsaFralda(boolean v) { this.usaFralda = v; }
}
