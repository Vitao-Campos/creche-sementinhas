package br.com.crechesementinhas.model;

import java.time.LocalDate;

/**
 * Um arquivo anexado à pasta digital do aluno (tabela "documento") - RF-06.
 * Ex.: foto 3x4, certidão, receita, laudo médico, atestado.
 */
public class Documento {

    private int idDocumento;
    private int idMatricula;
    private String nome;      // nome original do arquivo (ex.: certidao.pdf)
    private String tipo;      // ex.: "Certidão de nascimento"
    private String arquivo;   // caminho do arquivo DENTRO da pasta de documentos
    private LocalDate data;   // data em que foi anexado
    private String status;    // PENDENTE, ENTREGUE ou VALIDADO

    /** Devolve o identificador do documento. */
    public int getIdDocumento() { return idDocumento; }
    /** Define o identificador do documento. */
    public void setIdDocumento(int idDocumento) { this.idDocumento = idDocumento; }

    /** Devolve o identificador da matrícula. */
    public int getIdMatricula() { return idMatricula; }
    /** Define o identificador da matrícula. */
    public void setIdMatricula(int idMatricula) { this.idMatricula = idMatricula; }

    /** Devolve o nome. */
    public String getNome() { return nome; }
    /** Define o nome. */
    public void setNome(String nome) { this.nome = nome; }

    /** Devolve o tipo. */
    public String getTipo() { return tipo; }
    /** Define o tipo. */
    public void setTipo(String tipo) { this.tipo = tipo; }

    /** Devolve o caminho do arquivo. */
    public String getArquivo() { return arquivo; }
    /** Define o caminho do arquivo. */
    public void setArquivo(String arquivo) { this.arquivo = arquivo; }

    /** Devolve a data. */
    public LocalDate getData() { return data; }
    /** Define a data. */
    public void setData(LocalDate data) { this.data = data; }

    /** Devolve a situação. */
    public String getStatus() { return status; }
    /** Define a situação. */
    public void setStatus(String status) { this.status = status; }
}