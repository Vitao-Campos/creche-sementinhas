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

    /** Devolve o identificador da ficha de anamnese. */
    public Integer getIdFichaAnamnese() { return idFichaAnamnese; }
    /** Define o identificador da ficha de anamnese. */
    public void setIdFichaAnamnese(Integer id) { this.idFichaAnamnese = id; }
    /** Devolve o identificador da matrícula. */
    public Integer getIdMatricula() { return idMatricula; }
    /** Define o identificador da matrícula. */
    public void setIdMatricula(Integer idMatricula) { this.idMatricula = idMatricula; }
    /** Indica se o uso de imagem da criança é autorizado. */
    public boolean isAutorizacaoImagem() { return autorizacaoImagem; }
    /** Define se o uso de imagem da criança é autorizado. */
    public void setAutorizacaoImagem(boolean v) { this.autorizacaoImagem = v; }
    /** Indica se a criança tem alergia. */
    public boolean isPossuiAlergia() { return possuiAlergia; }
    /** Define se a criança tem alergia. */
    public void setPossuiAlergia(boolean v) { this.possuiAlergia = v; }
    /** Devolve a descrição da alergia. */
    public String getDescricaoAlergia() { return descricaoAlergia; }
    /** Define a descrição da alergia. */
    public void setDescricaoAlergia(String v) { this.descricaoAlergia = v; }
    /** Indica se a criança tem restrição alimentar. */
    public boolean isRestricoesAlimentares() { return restricoesAlimentares; }
    /** Define se a criança tem restrição alimentar. */
    public void setRestricoesAlimentares(boolean v) { this.restricoesAlimentares = v; }
    /** Devolve a descrição da restrição alimentar. */
    public String getDescricaoRestricao() { return descricaoRestricao; }
    /** Define a descrição da restrição alimentar. */
    public void setDescricaoRestricao(String v) { this.descricaoRestricao = v; }
    /** Devolve o antitérmico autorizado. */
    public String getReceitaAntitermico() { return receitaAntitermico; }
    /** Define o antitérmico autorizado. */
    public void setReceitaAntitermico(String v) { this.receitaAntitermico = v; }
    /** Indica se a criança já fez cirurgia. */
    public boolean isPossuiCirurgia() { return possuiCirurgia; }
    /** Define se a criança já fez cirurgia. */
    public void setPossuiCirurgia(boolean v) { this.possuiCirurgia = v; }
    /** Devolve a descrição das cirurgias. */
    public String getDescricaoCirurgia() { return descricaoCirurgia; }
    /** Define a descrição das cirurgias. */
    public void setDescricaoCirurgia(String v) { this.descricaoCirurgia = v; }
    /** Indica se a criança tem problemas de saúde. */
    public boolean isPossuiProblemasSaude() { return possuiProblemasSaude; }
    /** Define se a criança tem problemas de saúde. */
    public void setPossuiProblemasSaude(boolean v) { this.possuiProblemasSaude = v; }
    /** Devolve a descrição dos problemas de saúde. */
    public String getDescricaoProblemasSaude() { return descricaoProblemasSaude; }
    /** Define a descrição dos problemas de saúde. */
    public void setDescricaoProblemasSaude(String v) { this.descricaoProblemasSaude = v; }
    /** Indica se a criança usa medicação contínua. */
    public boolean isUsaMedicacaoContinua() { return usaMedicacaoContinua; }
    /** Define se a criança usa medicação contínua. */
    public void setUsaMedicacaoContinua(boolean v) { this.usaMedicacaoContinua = v; }
    /** Devolve a descrição da medicação contínua. */
    public String getDescricaoMedicacao() { return descricaoMedicacao; }
    /** Define a descrição da medicação contínua. */
    public void setDescricaoMedicacao(String v) { this.descricaoMedicacao = v; }
    /** Indica se a criança já teve fraturas. */
    public boolean isPossuiFraturas() { return possuiFraturas; }
    /** Define se a criança já teve fraturas. */
    public void setPossuiFraturas(boolean v) { this.possuiFraturas = v; }
    /** Devolve a descrição das fraturas. */
    public String getDescricaoFratura() { return descricaoFratura; }
    /** Define a descrição das fraturas. */
    public void setDescricaoFratura(String v) { this.descricaoFratura = v; }
    /** Indica se a criança usa mamadeira. */
    public boolean isUsaMamadeira() { return usaMamadeira; }
    /** Define se a criança usa mamadeira. */
    public void setUsaMamadeira(boolean v) { this.usaMamadeira = v; }
    /** Indica se a criança usa fórmula. */
    public boolean isUsaFormula() { return usaFormula; }
    /** Define se a criança usa fórmula. */
    public void setUsaFormula(boolean v) { this.usaFormula = v; }
    /** Devolve a descrição da fórmula. */
    public String getDescricaoFormula() { return descricaoFormula; }
    /** Define a descrição da fórmula. */
    public void setDescricaoFormula(String v) { this.descricaoFormula = v; }
    /** Indica se a criança usa chupeta. */
    public boolean isUsaChupeta() { return usaChupeta; }
    /** Define se a criança usa chupeta. */
    public void setUsaChupeta(boolean v) { this.usaChupeta = v; }
    /** Indica se a criança usa fralda. */
    public boolean isUsaFralda() { return usaFralda; }
    /** Define se a criança usa fralda. */
    public void setUsaFralda(boolean v) { this.usaFralda = v; }
}
