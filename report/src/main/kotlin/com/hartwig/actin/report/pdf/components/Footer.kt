package com.hartwig.actin.report.pdf.components

import com.hartwig.actin.report.pdf.util.Styles
import com.itextpdf.kernel.geom.Rectangle
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfPage
import com.itextpdf.kernel.pdf.canvas.PdfCanvas
import com.itextpdf.kernel.pdf.xobject.PdfFormXObject
import com.itextpdf.layout.Canvas
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.properties.TextAlignment

class Footer {

    private val footerTemplates: MutableList<FooterTemplate> = mutableListOf()

    fun render(page: PdfPage) {
        val canvas = PdfCanvas(page.lastContentStream, page.resources, page.document)
        val pageNumber = page.document.getPageNumber(page)
        val template = PdfFormXObject(Rectangle(0f, 0f, 450f, 25f))
        canvas.addXObjectAt(template, 58f, 20f)
        footerTemplates.add(FooterTemplate(pageNumber, template))
        canvas.release()
    }

    fun writePageCounts(document: PdfDocument) {
        val totalPageCount = document.numberOfPages
        for (tpl in footerTemplates) {
            tpl.renderFooter(document, totalPageCount)
        }
    }

    private class FooterTemplate(private val pageNumber: Int, private val template: PdfFormXObject) {

        fun renderFooter(document: PdfDocument, totalPageCount: Int) {
            val canvas = Canvas(template, document)

            val pageNumberString = "$pageNumber/$totalPageCount"
            val pageNumberParagraph = Paragraph().add(pageNumberString).addStyle(Styles.pageNumberStyle())
            canvas.showTextAligned(pageNumberParagraph, 0f, 0f, TextAlignment.LEFT)

            val disclaimer =
                "All results and data described in this report are for Research Use Only and have NOT been generated " +
                        "using a clinically validated and controlled procedure nor is it a validated medical device. " +
                        "The results should NOT be used for diagnostic or treatment purposes. " +
                        "No rights can be derived from the content of this report."
            val disclaimerParagraph = Paragraph(disclaimer).setMaxWidth(420f).addStyle(Styles.disclaimerStyle())
            canvas.showTextAligned(disclaimerParagraph, 30f, 10f, TextAlignment.LEFT)

            val attribution = "Gene and variant annotations and related content are powered by Genomenon Cancer Knowledgebase (CKB)."
            val attributionParagraph = Paragraph(attribution).setMaxWidth(420f).addStyle(Styles.disclaimerStyle())
            canvas.showTextAligned(attributionParagraph, 30f, 0f, TextAlignment.LEFT)
        }
    }
}