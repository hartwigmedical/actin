package com.hartwig.actin.report.pdf.components

import com.google.common.collect.Lists
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
    private val footerTemplates: MutableList<FooterTemplate> = Lists.newArrayList()
    fun render(page: PdfPage) {
        val canvas = PdfCanvas(page.lastContentStream, page.resources, page.document)
        val pageNumber = page.document.getPageNumber(page)
        val template = PdfFormXObject(Rectangle(0f, 0f, 450f, 20f))
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

    private class FooterTemplate internal constructor(private val pageNumber: Int, private val template: PdfFormXObject) {
        fun renderFooter(document: PdfDocument, totalPageCount: Int) {
            val displayString = "$pageNumber/$totalPageCount"
            val canvas = Canvas(template, document)
            val pageNumberParagraph = Paragraph().add(displayString).addStyle(Styles.pageNumberStyle())
            canvas.showTextAligned(pageNumberParagraph, 0f, 0f, TextAlignment.LEFT)
            val disclaimer = ("All results and data described in this report are for research use only and have not been generated using a "
                    + "clinically validated and controlled procedure.")
            val disclaimerParagraph = Paragraph(disclaimer).setMaxWidth(400f).addStyle(Styles.deemphasizedStyle())
            canvas.showTextAligned(disclaimerParagraph, 50f, 0f, TextAlignment.LEFT)
        }
    }
}