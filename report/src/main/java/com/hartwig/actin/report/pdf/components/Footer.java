package com.hartwig.actin.report.pdf.components;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.report.pdf.util.Styles;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.kernel.pdf.xobject.PdfFormXObject;
import com.itextpdf.layout.Canvas;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.properties.TextAlignment;

import org.jetbrains.annotations.NotNull;

public class Footer {

    private final List<FooterTemplate> footerTemplates = Lists.newArrayList();

    public Footer() {
    }

    public void render(@NotNull PdfPage page) {
        PdfCanvas canvas = new PdfCanvas(page.getLastContentStream(), page.getResources(), page.getDocument());

        int pageNumber = page.getDocument().getPageNumber(page);
        PdfFormXObject template = new PdfFormXObject(new Rectangle(0, 0, 450, 20));
        canvas.addXObjectAt(template, 58, 20);
        footerTemplates.add(new FooterTemplate(pageNumber, template));

        canvas.release();
    }

    public void writePageCounts(@NotNull PdfDocument document) {
        int totalPageCount = document.getNumberOfPages();
        for (FooterTemplate tpl : footerTemplates) {
            tpl.renderFooter(document, totalPageCount);
        }
    }

    private static class FooterTemplate {

        private final int pageNumber;
        @NotNull
        private final PdfFormXObject template;

        FooterTemplate(int pageNumber, @NotNull PdfFormXObject template) {
            this.pageNumber = pageNumber;
            this.template = template;
        }

        void renderFooter(@NotNull PdfDocument document, int totalPageCount) {
            String displayString = pageNumber + "/" + totalPageCount;

            Canvas canvas = new Canvas(template, document);
            Paragraph pageNumberParagraph = new Paragraph().add(displayString).addStyle(Styles.pageNumberStyle());
            canvas.showTextAligned(pageNumberParagraph, 0, 0, TextAlignment.LEFT);

            String disclaimer =
                    "All results and data described in this report are for research use only and have not been generated using a "
                            + "clinically validated and controlled procedure. These results should not be used for clinical decision making.";
            Paragraph disclaimerParagraph = new Paragraph(disclaimer).setMaxWidth(400).addStyle(Styles.deemphasizedStyle());
            canvas.showTextAligned(disclaimerParagraph, 50, 0, TextAlignment.LEFT);
        }
    }
}
