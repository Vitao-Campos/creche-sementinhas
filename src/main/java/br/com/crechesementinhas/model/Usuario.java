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

    public Integer getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Integer idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getSenhaHash() {
        return senhaHash;
    }

    public void setSenhaHash(String senhaHash) {
        this.senhaHash = senhaHash;
    }

    public String getCargo() {
        return cargo;
    }

    public void setCargo(String cargo) {
        this.cargo = cargo;
    }

    public String getSetor() {
        return setor;
    }

    public void setSetor(String setor) {
        this.setor = setor;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }

    public LocalDateTime getUltimoLogin() {
        return ultimoLogin;
    }

    public void setUltimoLogin(LocalDateTime ultimoLogin) {
        this.ultimoLogin = ultimoLogin;
    }

    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }

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