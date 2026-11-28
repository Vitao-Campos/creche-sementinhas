package br.com.crechesementinhas.model;

import java.time.LocalDateTime;

/**
 * Representa um colaborador da creche autorizado a usar o sistema.
 * Espelha a tabela "usuario" do banco.
 */
public class Usuario {

    private Integer idUsuario;
    private String nome;
    private String login;
    private String senhaHash;
    private String cargo;
    private String setor;
    private boolean ativo;
    private LocalDateTime ultimoLogin;
    private LocalDateTime dataCriacao;

    /** Devolve o identificador do usuário. */
    public Integer getIdUsuario() {
        return idUsuario;
    }

    /** Define o identificador do usuário. */
    public void setIdUsuario(Integer idUsuario) {
        this.idUsuario = idUsuario;
    }

    /** Devolve o nome. */
    public String getNome() {
        return nome;
    }

    /** Define o nome. */
    public void setNome(String nome) {
        this.nome = nome;
    }

    /** Devolve o login. */
    public String getLogin() {
        return login;
    }

    /** Define o login. */
    public void setLogin(String login) {
        this.login = login;
    }

    /** Devolve o hash da senha. */
    public String getSenhaHash() {
        return senhaHash;
    }

    /** Define o hash da senha. */
    public void setSenhaHash(String senhaHash) {
        this.senhaHash = senhaHash;
    }

    /** Devolve o cargo (perfil). */
    public String getCargo() {
        return cargo;
    }

    /** Define o cargo (perfil). */
    public void setCargo(String cargo) {
        this.cargo = cargo;
    }

    /** Devolve o setor. */
    public String getSetor() {
        return setor;
    }

    /** Define o setor. */
    public void setSetor(String setor) {
        this.setor = setor;
    }

    /** Indica se o registro está ativo. */
    public boolean isAtivo() {
        return ativo;
    }

    /** Define se o registro está ativo. */
    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }

    /** Devolve a data e hora do último acesso. */
    public LocalDateTime getUltimoLogin() {
        return ultimoLogin;
    }

    /** Define a data e hora do último acesso. */
    public void setUltimoLogin(LocalDateTime ultimoLogin) {
        this.ultimoLogin = ultimoLogin;
    }

    /** Devolve a data de criação. */
    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }

    /** Define a data de criação. */
    public void setDataCriacao(LocalDateTime dataCriacao) {
        this.dataCriacao = dataCriacao;
    }

    /**
     * Segurança: o hash da senha NUNCA entra no toString,
     * para não vazar em logs ou mensagens de erro.
     */
    @Override
    public String toString() {
        return "Usuario{id=" + idUsuario + ", login='" + login + "', cargo='" + cargo + "'}";
    }
}