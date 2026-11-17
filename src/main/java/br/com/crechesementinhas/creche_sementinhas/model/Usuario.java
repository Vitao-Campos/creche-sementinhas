package br.com.crechesementinhas.creche_sementinhas.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * Usuário do sistema (funcionário que faz login).
 * Espelha a tabela "usuario" do banco.
 */
@Entity
@Table(name = "usuario")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private Integer id;

    @Column(name = "nome", nullable = false, length = 100)
    private String nome;

    @Column(name = "login", nullable = false, length = 40, unique = true)
    private String login;

    // A coluna é CHAR(60): o hash BCrypt sempre tem exatamente 60 caracteres.
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "senha_hash", nullable = false, length = 60)
    private String senhaHash;

    @Column(name = "ativo", nullable = false)
    private boolean ativo;

    /** Exigido pelo JPA para criar objetos a partir do banco. */
    protected Usuario() {
    }

    public Integer getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getLogin() {
        return login;
    }

    public String getSenhaHash() {
        return senhaHash;
    }

    public boolean isAtivo() {
        return ativo;
    }
}