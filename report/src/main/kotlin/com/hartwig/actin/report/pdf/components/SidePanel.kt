package com.hartwig.actin.report.pdf.components

import com.hartwig.actin.report.pdf.util.Formats.date
import com.hartwig.actin.report.pdf.util.Styles
import com.itextpdf.kernel.font.PdfFont
import com.itextpdf.kernel.geom.Rectangle
import com.itextpdf.kernel.pdf.PdfPage
import com.itextpdf.kernel.pdf.canvas.PdfCanvas
import com.itextpdf.layout.Canvas
import com.itextpdf.layout.element.Div
import com.itextpdf.layout.element.Paragraph
import java.time.LocalDate
import java.util.Locale

private const val ROW_SPACING = 35f
private const val VALUE_TEXT_Y_OFFSET = 18f
private const val MAX_WIDTH = 120f
private const val RECTANGLE_WIDTH = 170f
private const val RECTANGLE_HEIGHT = 84f

class SidePanel(private val patientId: String, private val reportDate: LocalDate) {

    fun render(page: PdfPage) {
        val canvas = PdfCanvas(page.lastContentStream, page.resources, page.document)
        val pageSize = page.pageSize
        canvas.rectangle(pageSize.width.toDouble(), pageSize.height.toDouble(), -RECTANGLE_WIDTH.toDouble(), -RECTANGLE_HEIGHT.toDouble())
        canvas.setFillColor(Styles.PALETTE_BLUE)
        canvas.fill()
        var sideTextIndex = 0
        val cv = Canvas(canvas, page.pageSize)
        cv.add(createDiv(pageSize, ++sideTextIndex, "Patient", patientId))
        cv.add(createDiv(pageSize, ++sideTextIndex, "Report Date", date(reportDate)))
        canvas.release()
    }

    private fun createDiv(pageSize: Rectangle, index: Int, label: String, value: String): Div {
        val div = Div()
        div.isKeepTogether = true
        var yPos = pageSize.height + 15 - index * ROW_SPACING
        val xPos = pageSize.width - RECTANGLE_WIDTH + 15
        div.add(
            Paragraph(label.uppercase(Locale.getDefault())).addStyle(Styles.sidePanelLabelStyle())
                .setFixedPosition(xPos, yPos, MAX_WIDTH)
        )
        val valueFontSize = maxPointSizeForWidth(Styles.fontBold(), 10f, 6f, value, MAX_WIDTH)
        yPos -= VALUE_TEXT_Y_OFFSET
        div.add(
            Paragraph(value).addStyle(Styles.sidePanelValueStyle().setFontSize(valueFontSize))
                .setHeight(15f)
                .setFixedPosition(xPos, yPos, MAX_WIDTH)
                .setFixedLeading(valueFontSize)
        )
        return div
    }

    private fun maxPointSizeForWidth(
        font: PdfFont, initialFontSize: Float, minFontSize: Float, text: String,
        maxWidth: Float
    ): Float {
        val fontIncrement = 0.1f
        var fontSize = initialFontSize
        var width = font.getWidth(text, initialFontSize)
        while (width > maxWidth && fontSize > minFontSize) {
            fontSize -= fontIncrement
            width = font.getWidth(text, fontSize)
        }
        return fontSize
    }
}