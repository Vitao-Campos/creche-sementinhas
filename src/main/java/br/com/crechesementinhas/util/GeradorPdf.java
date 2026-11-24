package br.com.crechesementinhas.util;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.HeaderFooter;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Gera um arquivo PDF a partir de um texto (RF-09), usando a biblioteca OpenPDF.
 *
 * Regras simples de formatação do texto recebido:
 *  - parágrafos são separados por uma linha em branco;
 *  - o PRIMEIRO parágrafo vira o título (centralizado, negrito);
 *  - parágrafos que começam com "CLÁUSULA" ficam em negrito;
 *  - parágrafos que começam com "____" (linha de assinatura) ficam centralizados.
 */
public final class GeradorPdf {

    private GeradorPdf() {
    }

    public static void gerar(String texto, File destino) throws IOException {
        // Folha A4 com margens de 2 cm (56 pontos) de cada lado.
        Document pdf = new Document(PageSize.A4, 56, 56, 56, 56);

        Font fonteTitulo = new Font(Font.HELVETICA, 13, Font.BOLD);
        Font fonteNegrito = new Font(Font.HELVETICA, 11, Font.BOLD);
        Font fonteNormal = new Font(Font.HELVETICA, 11, Font.NORMAL);

        try (FileOutputStream saida = new FileOutputStream(destino)) {
            PdfWriter.getInstance(pdf, saida);

            // Rodapé com o número da página
            HeaderFooter rodape = new HeaderFooter(new Phrase("Página ", new Font(Font.HELVETICA, 9)), true);
            rodape.setAlignment(Element.ALIGN_CENTER);
            rodape.setBorder(0);
            pdf.setFooter(rodape);

            pdf.open();

            // \R = qualquer quebra de linha (Windows ou Linux)
            String[] paragrafos = texto.split("\\R\\s*\\R");
            for (int i = 0; i < paragrafos.length; i++) {
                String trecho = paragrafos[i].strip();
                if (trecho.isEmpty()) {
                    continue;
                }

                Paragraph p;
                if (i == 0) {
                    p = new Paragraph(trecho, fonteTitulo);
                    p.setAlignment(Element.ALIGN_CENTER);
                    p.setSpacingAfter(16);
                } else if (trecho.startsWith("CLÁUSULA")) {
                    p = new Paragraph(trecho, fonteNegrito);
                    p.setSpacingBefore(6);
                    p.setSpacingAfter(4);
                } else if (trecho.startsWith("____")) {
                    p = new Paragraph(trecho, fonteNormal);
                    p.setAlignment(Element.ALIGN_CENTER);
                    p.setSpacingBefore(28);
                } else {
                    p = new Paragraph(trecho, fonteNormal);
                    p.setAlignment(Element.ALIGN_JUSTIFIED);
                    p.setSpacingAfter(8);
                }
                pdf.add(p);
            }
        } catch (DocumentException e) {
            throw new IOException("Erro ao montar o PDF: " + e.getMessage(), e);
        } finally {
            if (pdf.isOpen()) {
                pdf.close();
            }
        }
    }
}