package br.com.crechesementinhas.service;

import br.com.crechesementinhas.dao.DocumentoDAO;
import br.com.crechesementinhas.model.Documento;
import br.com.crechesementinhas.util.PastaDocumentos;
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

    public List<Documento> listar(int idMatricula) throws SQLException {
        return dao.listarPorMatricula(idMatricula);
    }
}