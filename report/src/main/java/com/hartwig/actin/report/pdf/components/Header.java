package com.hartwig.actin.report.pdf.components;

import com.hartwig.actin.report.pdf.util.Styles;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.layout.Canvas;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Text;

import org.jetbrains.annotations.NotNull;

public class Header {

    public Header() {
    }

    public void renderHeader(@NotNull PdfPage page) {
        PdfCanvas pdfCanvas = new PdfCanvas(page.getLastContentStream(), page.getResources(), page.getDocument());
        Canvas cv = new Canvas(pdfCanvas, page.getPageSize());

        cv.add(new Paragraph().add(new Text("ACTIN Report").addStyle(Styles.reportTitleStyle())
                .setFixedPosition(200, page.getPageSize().getHeight() - 40, 300)));

        pdfCanvas.release();
    }
}
