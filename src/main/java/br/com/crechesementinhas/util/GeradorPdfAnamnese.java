package br.com.crechesementinhas.util;

import br.com.crechesementinhas.model.AlunoPessoaAutorizada;
import br.com.crechesementinhas.model.AlunoResponsavel;
import br.com.crechesementinhas.model.CabecalhoFicha;
import br.com.crechesementinhas.model.DadosFicha;
import br.com.crechesementinhas.model.FichaAnamnese;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.HeaderFooter;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Gera o PDF da ficha de anamnese para visualização e impressão (RF-09), com OpenPDF.
 * Ficha ainda sem respostas sai em branco, com "[ ] Sim [ ] Não" para preencher à mão.
 */
public final class GeradorPdfAnamnese {

    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/uuuu");
    private static final Font TITULO = new Font(Font.HELVETICA, 14, Font.BOLD);
    private static final Font SECAO = new Font(Font.HELVETICA, 10, Font.BOLD, new Color(40, 60, 45));
    private static final Font ROTULO = new Font(Font.HELVETICA, 9, Font.BOLD);
    private static final Font TEXTO = new Font(Font.HELVETICA, 9, Font.NORMAL);
    private static final Color VERDE = new Color(222, 234, 224); // verde bem claro, sóbrio

    /** Classe só com métodos estáticos: não é instanciada. */
    private GeradorPdfAnamnese() {
    }

    /** Gera o PDF da ficha de anamnese no arquivo de destino (uma página A4). */
    public static void gerar(DadosFicha dados, File destino) throws IOException {
        Document pdf = new Document(PageSize.A4, 36, 36, 30, 40);
        try (FileOutputStream saida = new FileOutputStream(destino)) {
            PdfWriter.getInstance(pdf, saida);
            HeaderFooter rodape = new HeaderFooter(new Phrase("Página ", new Font(Font.HELVETICA, 9)), true);
            rodape.setAlignment(Element.ALIGN_CENTER);
            rodape.setBorder(0);
            pdf.setFooter(rodape);
            pdf.open();

            Paragraph escola = new Paragraph("CRECHE SEMENTINHAS", new Font(Font.HELVETICA, 10, Font.NORMAL));
            escola.setAlignment(Element.ALIGN_CENTER);
            pdf.add(escola);
            Paragraph titulo = new Paragraph("FICHA DE ANAMNESE", TITULO);
            titulo.setAlignment(Element.ALIGN_CENTER);
            titulo.setSpacingAfter(4);
            pdf.add(titulo);

            pdf.add(secao("Dados da criança"));
            pdf.add(tabelaCrianca(dados.cabecalho()));
            pdf.add(secao("Responsáveis"));
            pdf.add(tabelaResponsaveis(dados.responsaveis()));
            pdf.add(secao("Pessoas autorizadas a buscar a criança"));
            pdf.add(tabelaAutorizados(dados.autorizados()));

            FichaAnamnese f = dados.ficha();
            pdf.add(secao("Saúde"));
            pdf.add(tabelaSaude(f));
            pdf.add(secao("Rotina e autorizações"));
            pdf.add(tabelaRotina(f));

            Paragraph assinatura = new Paragraph(
                    "\n______________________________________________\n"
                    + "Assinatura do responsável legal          Data: ____/____/________", TEXTO);
            assinatura.setAlignment(Element.ALIGN_CENTER);
            assinatura.setSpacingBefore(14);
            pdf.add(assinatura);
            Paragraph emissao = new Paragraph("Emitido em " + DATA.format(LocalDate.now())
                    + ". Esta ficha retrata a situação atual de saúde da criança e pode ser atualizada a qualquer tempo.",
                    new Font(Font.HELVETICA, 7));
            emissao.setSpacingBefore(6);
            pdf.add(emissao);
            // Fechar AQUI, antes de o arquivo ser fechado: é o close() que grava o final do PDF.
            // Fechar depois (num finally) deixa o arquivo incompleto e o leitor diz que está danificado.
            pdf.close();
        } catch (RuntimeException e) {
            throw new IOException("Erro ao montar o PDF: " + e.getMessage(), e);
        }
    }

    // ------------------------------------------------------------------ blocos

    /** Monta a faixa de título de uma seção da ficha. */
    private static PdfPTable secao(String texto) {
        PdfPTable t = new PdfPTable(1);
        t.setWidthPercentage(100);
        t.setSpacingBefore(7);
        PdfPCell c = new PdfPCell(new Phrase(texto, SECAO));
        c.setBackgroundColor(VERDE);
        c.setBorder(Rectangle.NO_BORDER);
        c.setPadding(3);
        t.addCell(c);
        return t;
    }

    /** Monta a tabela com os dados da criança e da matrícula. */
    private static PdfPTable tabelaCrianca(CabecalhoFicha c) {
        PdfPTable t = tabela(2, 1, 3);
        linha(t, "Nome", c.nomeAluno());
        linha(t, "Nascimento", DATA.format(c.dataNascimento()) + "  ("
                + CalculadoraIdade.descrever(c.dataNascimento(), LocalDate.now()) + ")");
        linha(t, "Matrícula", c.numeroMatricula());
        linha(t, "Turma", c.nomeTurma());
        return t;
    }

    /** Monta a tabela dos responsáveis vinculados. */
    private static PdfPTable tabelaResponsaveis(List<AlunoResponsavel> lista) {
        PdfPTable t = tabela(3, 3, 2, 2);
        cabecalho(t, "Nome", "Parentesco / papel", "Telefone");
        if (lista.isEmpty()) {
            vazio(t, 3, "Nenhum responsável vinculado.");
        }
        for (AlunoResponsavel v : lista) {
            String papel = v.getGrauParentesco()
                    + (v.isResponsavelLegal() ? " — legal" : "")
                    + (v.isResponsavelFinanceiro() ? " — financeiro" : "");
            celula(t, v.getNomeResponsavel(), TEXTO);
            celula(t, papel, TEXTO);
            celula(t, Telefone.formatar(Telefone.normalizar(v.getTelefoneResponsavel())), TEXTO);
        }
        return t;
    }

    /** Monta a tabela das pessoas autorizadas a buscar a criança. */
    private static PdfPTable tabelaAutorizados(List<AlunoPessoaAutorizada> lista) {
        PdfPTable t = tabela(3, 3, 2, 2);
        cabecalho(t, "Nome", "Parentesco / autorização", "Telefone");
        if (lista.isEmpty()) {
            vazio(t, 3, "Nenhuma pessoa autorizada cadastrada.");
        }
        for (AlunoPessoaAutorizada a : lista) {
            String parentesco = a.getParentesco() == null || a.getParentesco().isBlank() ? "" : a.getParentesco() + " — ";
            celula(t, a.getNome(), TEXTO);
            celula(t, parentesco + a.getTipoAutorizacao(), TEXTO);
            celula(t, a.getTelefoneFormatado(), TEXTO);
        }
        return t;
    }

    /** Monta a tabela com as respostas sobre saúde (em branco se a ficha não foi preenchida). */
    private static PdfPTable tabelaSaude(FichaAnamnese f) {
        PdfPTable t = tabela(3, 3, 2, 5);
        cabecalho(t, "Item", "Resposta", "Descrição");
        boolean b = f == null;
        item(t, "Possui alergia?", b, !b && f.isPossuiAlergia(), b ? null : f.getDescricaoAlergia());
        item(t, "Possui restrição alimentar?", b, !b && f.isRestricoesAlimentares(), b ? null : f.getDescricaoRestricao());
        item(t, "Possui problemas de saúde?", b, !b && f.isPossuiProblemasSaude(), b ? null : f.getDescricaoProblemasSaude());
        item(t, "Usa medicação contínua?", b, !b && f.isUsaMedicacaoContinua(), b ? null : f.getDescricaoMedicacao());
        item(t, "Já fez cirurgia?", b, !b && f.isPossuiCirurgia(), b ? null : f.getDescricaoCirurgia());
        item(t, "Já teve fraturas?", b, !b && f.isPossuiFraturas(), b ? null : f.getDescricaoFratura());
        celula(t, "Antitérmico autorizado", ROTULO);
        celula(t, "", TEXTO);
        celula(t, b ? "" : valor(f.getReceitaAntitermico()), TEXTO);
        return t;
    }

    /** Monta a tabela com as respostas sobre rotina e autorizações. */
    private static PdfPTable tabelaRotina(FichaAnamnese f) {
        PdfPTable t = tabela(3, 3, 2, 5);
        cabecalho(t, "Item", "Resposta", "Descrição");
        boolean b = f == null;
        item(t, "Usa mamadeira?", b, !b && f.isUsaMamadeira(), null);
        item(t, "Usa fórmula?", b, !b && f.isUsaFormula(), b ? null : f.getDescricaoFormula());
        item(t, "Usa chupeta?", b, !b && f.isUsaChupeta(), null);
        item(t, "Usa fralda?", b, !b && f.isUsaFralda(), null);
        item(t, "Autoriza o uso de imagem?", b, !b && f.isAutorizacaoImagem(), null);
        return t;
    }

    // ------------------------------------------------------------------ células

    /** Cria uma tabela com o número de colunas e as larguras relativas informadas. */
    private static PdfPTable tabela(int colunas, float... larguras) {
        PdfPTable t = new PdfPTable(colunas);
        t.setWidthPercentage(100);
        try {
            t.setWidths(larguras.length == colunas ? larguras : new float[] {1, 3});
        } catch (DocumentException e) {
            throw new IllegalStateException(e);
        }
        return t;
    }

    /** Acrescenta uma linha de rótulo e valor. */
    private static void linha(PdfPTable t, String rotulo, String valor) {
        celula(t, rotulo, ROTULO);
        celula(t, valor, TEXTO);
    }

    /** Acrescenta uma linha de pergunta, resposta (Sim/Não) e descrição. */
    private static void item(PdfPTable t, String pergunta, boolean emBranco, boolean sim, String descricao) {
        celula(t, pergunta, ROTULO);
        String resposta = emBranco ? "[   ] Sim   [   ] Não" : sim ? "[ X ] Sim   [   ] Não" : "[   ] Sim   [ X ] Não";
        celula(t, resposta, TEXTO);
        celula(t, emBranco ? "" : valor(descricao), TEXTO);
    }

    /** Acrescenta a linha de cabeçalho da tabela. */
    private static void cabecalho(PdfPTable t, String... titulos) {
        for (String titulo : titulos) {
            PdfPCell c = new PdfPCell(new Phrase(titulo, ROTULO));
            c.setBackgroundColor(new Color(240, 244, 241));
            c.setPadding(3);
            t.addCell(c);
        }
    }

    /** Acrescenta uma linha de aviso que ocupa todas as colunas. */
    private static void vazio(PdfPTable t, int colunas, String texto) {
        PdfPCell c = new PdfPCell(new Phrase(texto, TEXTO));
        c.setColspan(colunas);
        c.setPadding(3);
        t.addCell(c);
    }

    /** Acrescenta uma célula com o texto e a fonte informados. */
    private static void celula(PdfPTable t, String texto, Font fonte) {
        PdfPCell c = new PdfPCell(new Phrase(texto == null ? "" : texto, fonte));
        c.setPadding(3);
        c.setMinimumHeight(15);
        t.addCell(c);
    }

    /** Devolve o texto, ou um traço quando está vazio. */
    private static String valor(String texto) {
        return texto == null || texto.isBlank() ? "—" : texto;
    }
}
