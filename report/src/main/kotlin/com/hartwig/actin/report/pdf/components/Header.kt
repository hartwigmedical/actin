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

class Header(private val labels: ReportLabels) {

    private val logoImageData = ImageDataFactory.create(
        Header::class.java.getResourceAsStream(LOGO_RESOURCE_PATH)!!.readBytes()
    )
    private val logoWidth = LOGO_HEIGHT * logoImageData.width / logoImageData.height

    fun render(page: PdfPage) {
        val pdfCanvas = PdfCanvas(page.lastContentStream, page.resources, page.document)
        val canvas = Canvas(pdfCanvas, page.pageSize)
        canvas.add(
            Image(logoImageData).setHeight(LOGO_HEIGHT)
                .setFixedPosition(Constants.PAGE_MARGIN_LEFT, page.pageSize.height - LOGO_TOP_OFFSET)
        )

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
        private const val LOGO_RESOURCE_PATH = "/logos/hartwig_logo.jpg"
        private const val LOGO_HEIGHT = 44f
        private const val LOGO_TOP_OFFSET = 54f
    }
}