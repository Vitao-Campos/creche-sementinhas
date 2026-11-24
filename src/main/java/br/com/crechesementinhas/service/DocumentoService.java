package br.com.crechesementinhas.service;

import br.com.crechesementinhas.dao.DocumentoDAO;
import br.com.crechesementinhas.model.Documento;
import br.com.crechesementinhas.model.Permissao;
import br.com.crechesementinhas.util.PastaDocumentos;
import br.com.crechesementinhas.util.Transacao;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;

/**
 * Regras do UPLOAD de anexos na pasta digital do aluno (RF-06 / EX-05).
 */
public class DocumentoService {

    /** Tipos que aparecem na caixa de seleção da tela. */
    public static final List<String> TIPOS = List.of(
            "Foto 3x4",
            "Certidão de nascimento",
            "Carteira de vacinação",
            "Receita médica",
            "Laudo médico",
            "Atestado",
            "Documento do responsável",
            "Contrato assinado",
            "Outro");

    /** EX-05: só PDF, JPEG e PNG... */
    private static final Set<String> EXTENSOES_ACEITAS = Set.of("pdf", "jpg", "jpeg", "png");

    /** ...de até 5 MB (5 x 1024 x 1024 bytes). */
    private static final long TAMANHO_MAXIMO = 5L * 1024 * 1024;

    private static final DateTimeFormatter CARIMBO = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    private final DocumentoDAO dao = new DocumentoDAO();
        private final AuditoriaService auditoria = new AuditoriaService();

    public List<Documento> listar(int idMatricula) throws SQLException {
        return dao.listarPorMatricula(idMatricula);
    }
    
    /**
     * Valida o arquivo, copia para a pasta da matrícula e grava no banco.
     */
    public void anexar(int idMatricula, String tipo, File arquivo) throws IOException, SQLException {
                ControleAcesso.exigir(Permissao.CADASTRAR_MATRICULA); // RNF-03
        if (tipo == null || tipo.isBlank()) {
            throw new IllegalArgumentException("Selecione o tipo do documento.");
        }
        if (arquivo == null || !arquivo.isFile()) {
            throw new IllegalArgumentException("Arquivo não encontrado.");
        }

        // EX-05: formato
        String extensao = extensaoDe(arquivo.getName());
        if (!EXTENSOES_ACEITAS.contains(extensao)) {
            throw new IllegalArgumentException(
                    "EX-05: formato não aceito (." + extensao + "). Envie PDF, JPEG ou PNG de até 5 MB.");
        }
        // EX-05: tamanho
        long tamanho = arquivo.length();
        if (tamanho == 0) {
            throw new IllegalArgumentException("O arquivo está vazio.");
        }
        if (tamanho > TAMANHO_MAXIMO) {
            throw new IllegalArgumentException(String.format(
                    "EX-05: o arquivo tem %.1f MB. O limite é 5 MB.", tamanho / (1024.0 * 1024.0)));
        }

        // Pasta da matrícula, ex.: .../documentos/matricula_7/
        String subpasta = "matricula_" + idMatricula;
        Path pastaMatricula = PastaDocumentos.raiz().resolve(subpasta);
        Files.createDirectories(pastaMatricula);

        // Nome único: data/hora + nome original "limpo" (sem acentos/espaços estranhos)
        String nomeLimpo = arquivo.getName().replaceAll("[^a-zA-Z0-9._-]", "_");
        String nomeFinal = LocalDateTime.now().format(CARIMBO) + "_" + nomeLimpo;
        Path destino = pastaMatricula.resolve(nomeFinal);

        Files.copy(arquivo.toPath(), destino, StandardCopyOption.REPLACE_EXISTING);

        Documento d = new Documento();
        d.setIdMatricula(idMatricula);
        d.setNome(cortar(arquivo.getName(), 100));   // coluna nome é VARCHAR(100)
        d.setTipo(cortar(tipo, 40));                  // coluna tipo é VARCHAR(40)
        d.setArquivo(subpasta + "/" + nomeFinal);     // caminho RELATIVO à pasta de documentos
        d.setData(LocalDate.now());
        d.setStatus("ENTREGUE");

        try {
            // RNF-02: documento e auditoria na MESMA transação
            Transacao.executar(conexao -> {
                int idDocumento = dao.inserir(conexao, d);
                auditoria.registrarInsercao(conexao, "documento", idDocumento);
                return null;
            });
        } catch (SQLException | RuntimeException e) {
            Files.deleteIfExists(destino); // se o banco falhou, não deixa arquivo "órfão"
            throw e;
        }
    }

    private String extensaoDe(String nome) {
        int ponto = nome.lastIndexOf('.');
        return ponto < 0 ? "" : nome.substring(ponto + 1).toLowerCase();
    }

    private String cortar(String texto, int maximo) {
        return texto.length() <= maximo ? texto : texto.substring(0, maximo);
    }
    
    /** A secretaria/direção conferiu o documento. */
    public void validar(Documento d) throws SQLException {
        ControleAcesso.exigir(Permissao.CADASTRAR_MATRICULA); // RNF-03
        if (d == null) {
            throw new IllegalArgumentException("Selecione um documento na tabela.");
        }
                // RNF-02: alteração e auditoria na MESMA transação
        Transacao.executar(conexao -> {
            dao.alterarStatus(conexao, d.getIdDocumento(), "VALIDADO");
            auditoria.registrarAlteracao(conexao, "documento", d.getIdDocumento(),
                    "status", d.getStatus(), "VALIDADO", null);
            return null;
        });
    }

    /** Caminho completo do arquivo no disco (para abrir). */
    public File arquivoDe(Documento d) {
        File f = PastaDocumentos.raiz().resolve(d.getArquivo()).toFile();
        if (!f.exists()) {
            throw new IllegalArgumentException("Arquivo não encontrado na pasta de documentos: " + f);
        }
        return f;
    }
}