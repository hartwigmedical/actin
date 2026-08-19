package com.hartwig.actin.report.pdf.components

import com.hartwig.actin.report.pdf.ReportLabels
import com.hartwig.actin.report.pdf.util.Formats
import com.hartwig.actin.report.pdf.util.Styles
import com.itextpdf.kernel.geom.Rectangle
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfPage
import com.itextpdf.kernel.pdf.canvas.PdfCanvas
import com.itextpdf.kernel.pdf.xobject.PdfFormXObject
import com.itextpdf.layout.Canvas
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.properties.TextAlignment
import java.time.LocalDate

private const val FOOTER_PARAGRAPH_X = 25f
private const val FOOTER_PARAGRAPH_WIDTH = 425f

class Footer(private val reportDate: LocalDate, private val labels: ReportLabels) {

    private val footerTemplates: MutableList<FooterTemplate> = mutableListOf()

    fun render(page: PdfPage) {
        val canvas = PdfCanvas(page.lastContentStream, page.resources, page.document)
        val pageNumber = page.document.getPageNumber(page)
        val template = PdfFormXObject(Rectangle(0f, 0f, 450f, 35f))
        canvas.addXObjectAt(template, 50f, 18f)
        footerTemplates.add(FooterTemplate(pageNumber, template, reportDate, labels))
        canvas.release()
    }

    fun writePageCounts(document: PdfDocument) {
        val totalPageCount = document.numberOfPages
        for (tpl in footerTemplates) {
            tpl.renderFooter(document, totalPageCount)
        }
    }

    private class FooterTemplate(
        private val pageNumber: Int,
        private val template: PdfFormXObject,
        private val reportDate: LocalDate,
        private val labels: ReportLabels
    ) {

        fun renderFooter(document: PdfDocument, totalPageCount: Int) {
            val canvas = Canvas(template, document)

            val pageNumberParagraph = Paragraph().add("$pageNumber/$totalPageCount").addStyle(Styles.pageNumberStyle())
            canvas.showTextAligned(pageNumberParagraph, 0f, 0f, TextAlignment.LEFT)

            val researchDisclaimerParagraph = Paragraph(labels.footer.researchDisclaimer())
                .setMaxWidth(FOOTER_PARAGRAPH_WIDTH)
                .addStyle(Styles.disclaimerStyle())
            canvas.showTextAligned(researchDisclaimerParagraph, FOOTER_PARAGRAPH_X, 20f, TextAlignment.LEFT)

            val ctgovDisclaimerParagraph = Paragraph(labels.footer.ctgovDisclaimer(Formats.date(reportDate)))
                .setMaxWidth(FOOTER_PARAGRAPH_WIDTH)
                .addStyle(Styles.disclaimerStyle())
            canvas.showTextAligned(ctgovDisclaimerParagraph, FOOTER_PARAGRAPH_X, 7f, TextAlignment.LEFT)

            val attributionsParagraph = Paragraph(labels.footer.attributions())
                .setMaxWidth(FOOTER_PARAGRAPH_WIDTH)
                .addStyle(Styles.disclaimerStyle())
            canvas.showTextAligned(attributionsParagraph, FOOTER_PARAGRAPH_X, 0f, TextAlignment.LEFT)
        }
    }
}