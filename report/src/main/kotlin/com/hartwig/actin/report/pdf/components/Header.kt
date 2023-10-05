package com.hartwig.actin.report.pdf.components

import com.hartwig.actin.report.pdf.util.Styles
import com.itextpdf.kernel.pdf.PdfPage
import com.itextpdf.kernel.pdf.canvas.PdfCanvas
import com.itextpdf.layout.Canvas
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Text

class Header {

    fun render(page: PdfPage) {
        val pdfCanvas = PdfCanvas(page.lastContentStream, page.resources, page.document)
        val canvas = Canvas(pdfCanvas, page.pageSize)
        canvas.add(
            Paragraph().add(Text("ACTIN Report (research-use only)").addStyle(Styles.reportTitleStyle()))
                .setFixedPosition(200f, page.pageSize.height - 40, 300f)
        )

        pdfCanvas.release()
    }
}