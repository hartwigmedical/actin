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
import com.itextpdf.layout.element.Text
import java.time.LocalDate
import java.util.*

private const val ROW_SPACING = 35f
private const val VALUE_TEXT_Y_OFFSET = 18f
private const val MAX_WIDTH = 120f
private const val RECTANGLE_WIDTH = 170f
private const val RECTANGLE_HEIGHT = 84f
private const val INITIAL_FONT_SIZE = 10f
private const val MIN_FONT_SIZE = 6f
private const val FONT_HEIGHT = 15f

class SidePanel(private val patientId: String, private val sourcePatientId: String?, private val reportDate: LocalDate) {

    fun render(page: PdfPage) {
        val canvas = PdfCanvas(page.lastContentStream, page.resources, page.document)
        val pageSize = page.pageSize
        canvas.rectangle(pageSize.width.toDouble(), pageSize.height.toDouble(), -RECTANGLE_WIDTH.toDouble(), -RECTANGLE_HEIGHT.toDouble())
        canvas.setFillColor(Styles.PALETTE_BLUE)
        canvas.fill()
        var sideTextIndex = 0
        val cv = Canvas(canvas, page.pageSize)
        val (value, extra) = sourcePatientId.takeUnless { it.isNullOrBlank() }?.let { it to patientId } ?: patientId to null
        cv.add(createDiv(pageSize, ++sideTextIndex, "Patient", value, extra))
        cv.add(createDiv(pageSize, ++sideTextIndex, "Report Date", date(reportDate)))
        canvas.release()
    }

    private fun createDiv(pageSize: Rectangle, index: Int, label: String, value: String, extra: String? = null): Div {
        val div = Div()
        div.isKeepTogether = true
        var yPos = pageSize.height + FONT_HEIGHT - index * ROW_SPACING
        val xPos = pageSize.width - RECTANGLE_WIDTH + FONT_HEIGHT
        div.add(
            Paragraph(label.uppercase(Locale.getDefault())).addStyle(Styles.sidePanelLabelStyle())
                .setFixedPosition(xPos, yPos, MAX_WIDTH)
        )
        val valueFontSize = maxPointSizeForWidth(Styles.fontBold(), INITIAL_FONT_SIZE, MIN_FONT_SIZE, value, MAX_WIDTH)
        yPos -= VALUE_TEXT_Y_OFFSET
        div.add(
            Paragraph()
                .add(Text(value).setFontSize(valueFontSize))
                .apply {
                    if (!extra.isNullOrBlank()) {
                        add(Text(" ($extra)").setFontSize(valueFontSize * 0.75f))
                    }
                }
                .addStyle(Styles.sidePanelValueStyle())
                .setHeight(FONT_HEIGHT)
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