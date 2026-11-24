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

    public int getIdDocumento() { return idDocumento; }
    public void setIdDocumento(int idDocumento) { this.idDocumento = idDocumento; }

    public int getIdMatricula() { return idMatricula; }
    public void setIdMatricula(int idMatricula) { this.idMatricula = idMatricula; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public String getArquivo() { return arquivo; }
    public void setArquivo(String arquivo) { this.arquivo = arquivo; }

    public LocalDate getData() { return data; }
    public void setData(LocalDate data) { this.data = data; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}