package br.com.crechesementinhas.model;

import br.com.crechesementinhas.util.Telefone;

/**
 * Autorização de uma pessoa para uma criança (RF-02, RN-04).
 * Espelha "aluno_pessoa_autorizada"; nome, RG e telefone vêm do JOIN, só para exibir.
 */
public class AlunoPessoaAutorizada {

    private Integer idAlunoPessoaAutorizada;
    private Integer idAluno;
    private Integer idPessoaAutorizada;
    private String nome;
    private String rg;
    private String telefone;
    private String parentesco;
    private TipoAutorizacao tipoAutorizacao;
    private boolean ativo;

    /** Devolve o telefone com máscara. */
    public String getTelefoneFormatado() {
        return Telefone.formatar(telefone);
    }

    /** Devolve o identificador da autorização da pessoa. */
    public Integer getIdAlunoPessoaAutorizada() { return idAlunoPessoaAutorizada; }
    /** Define o identificador da autorização da pessoa. */
    public void setIdAlunoPessoaAutorizada(Integer id) { this.idAlunoPessoaAutorizada = id; }
    /** Devolve o identificador do aluno. */
    public Integer getIdAluno() { return idAluno; }
    /** Define o identificador do aluno. */
    public void setIdAluno(Integer idAluno) { this.idAluno = idAluno; }
    /** Devolve o identificador da pessoa autorizada. */
    public Integer getIdPessoaAutorizada() { return idPessoaAutorizada; }
    /** Define o identificador da pessoa autorizada. */
    public void setIdPessoaAutorizada(Integer id) { this.idPessoaAutorizada = id; }
    /** Devolve o nome. */
    public String getNome() { return nome; }
    /** Define o nome. */
    public void setNome(String nome) { this.nome = nome; }
    /** Devolve o RG. */
    public String getRg() { return rg; }
    /** Define o RG. */
    public void setRg(String rg) { this.rg = rg; }
    /** Devolve o telefone. */
    public String getTelefone() { return telefone; }
    /** Define o telefone. */
    public void setTelefone(String telefone) { this.telefone = telefone; }
    /** Devolve o grau de parentesco. */
    public String getParentesco() { return parentesco; }
    /** Define o grau de parentesco. */
    public void setParentesco(String parentesco) { this.parentesco = parentesco; }
    /** Devolve o tipo de autorização (retirada, emergência ou ambos). */
    public TipoAutorizacao getTipoAutorizacao() { return tipoAutorizacao; }
    /** Define o tipo de autorização (retirada, emergência ou ambos). */
    public void setTipoAutorizacao(TipoAutorizacao tipo) { this.tipoAutorizacao = tipo; }
    /** Indica se o registro está ativo. */
    public boolean isAtivo() { return ativo; }
    /** Define se o registro está ativo. */
    public void setAtivo(boolean ativo) { this.ativo = ativo; }
}
