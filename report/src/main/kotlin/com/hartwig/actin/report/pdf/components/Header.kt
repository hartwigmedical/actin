package com.hartwig.actin.report.pdf.components

import com.hartwig.actin.report.pdf.ReportLabels
import com.hartwig.actin.report.pdf.util.Constants
import com.hartwig.actin.report.pdf.util.Styles
import com.itextpdf.io.image.ImageDataFactory
import com.itextpdf.kernel.pdf.PdfPage
import com.itextpdf.kernel.pdf.canvas.PdfCanvas
import com.itextpdf.layout.Canvas
import com.itextpdf.layout.element.Image
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Text
import com.itextpdf.layout.properties.TextAlignment
import java.io.File

class Header(private val labels: ReportLabels, logoPath: String?) {

    private val logoImageData = logoPath?.let { ImageDataFactory.create(File(it).readBytes()) }
    private val logoWidth = logoImageData?.let { LOGO_HEIGHT * it.width / it.height } ?: 0f

    fun render(page: PdfPage) {
        val pdfCanvas = PdfCanvas(page.lastContentStream, page.resources, page.document)
        val canvas = Canvas(pdfCanvas, page.pageSize)
        logoImageData?.let {
            canvas.add(
                Image(it).setHeight(LOGO_HEIGHT)
                    .setFixedPosition(Constants.PAGE_MARGIN_LEFT, page.pageSize.height - LOGO_TOP_OFFSET)
            )
        }

        val titleAreaStart = Constants.PAGE_MARGIN_LEFT + logoWidth
        val titleAreaEnd = page.pageSize.width - Constants.SIDE_PANEL_WIDTH
        canvas.add(
            Paragraph().add(Text(labels.report.title()).addStyle(Styles.reportTitleStyle()))
                .setTextAlignment(TextAlignment.CENTER)
                .setFixedPosition(titleAreaStart, page.pageSize.height - 40, titleAreaEnd - titleAreaStart)
        )

        pdfCanvas.release()
    }

    companion object {
        private const val LOGO_HEIGHT = 44f
        private const val LOGO_TOP_OFFSET = 54f
    }
}