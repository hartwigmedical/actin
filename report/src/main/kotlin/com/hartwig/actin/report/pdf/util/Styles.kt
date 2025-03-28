package com.hartwig.actin.report.pdf.util

import com.itextpdf.io.font.FontProgram
import com.itextpdf.io.font.FontProgramFactory
import com.itextpdf.io.font.PdfEncodings
import com.itextpdf.kernel.colors.DeviceRgb
import com.itextpdf.kernel.font.PdfFont
import com.itextpdf.kernel.font.PdfFontFactory
import com.itextpdf.layout.Style
import com.itextpdf.layout.borders.SolidBorder
import java.io.IOException

object Styles {

    val PALETTE_WHITE = DeviceRgb(255, 255, 255)
    val PALETTE_BLACK = DeviceRgb(0, 0, 0)
    val PALETTE_MID_GREY = DeviceRgb(101, 106, 108)
    val PALETTE_BLUE = DeviceRgb(74, 134, 232)
    val PALETTE_EVALUATION_PASS = DeviceRgb(0, 150, 0)
    val PALETTE_EVALUATION_WARN = DeviceRgb(255, 130, 0)
    val PALETTE_EVALUATION_FAILED = DeviceRgb(231, 85, 85)
    val PALETTE_EVALUATION_UNCLEAR = DeviceRgb(85, 85, 85)
    val PALETTE_GREEN = DeviceRgb(0, 150, 0)
    val PALETTE_RED = DeviceRgb(231, 85, 85)
    val PALETTE_YES_OR_NO_UNCLEAR = DeviceRgb(85, 85, 85)
    val PALETTE_WARN = PALETTE_EVALUATION_WARN
    val BORDER = SolidBorder(PALETTE_MID_GREY, 0.25f)

    private const val FONT_REGULAR_PATH = "fonts/nimbus-sans/NimbusSansL-Regular.ttf"
    private const val FONT_BOLD_PATH = "fonts/nimbus-sans/NimbusSansL-Bold.ttf"
    private const val FONT_ITALIC_PATH = "fonts/nimbus-sans/NimbusSansL-Italic.ttf"
    private const val FONT_ITALIC_BOLD_PATH = "fonts/nimbus-sans/NimbusSansL-Bold Italic.ttf"
    private var fontRegular = createFont(FONT_REGULAR_PATH)
    private var fontBold = createFont(FONT_BOLD_PATH)
    private var fontItalic = createFont(FONT_ITALIC_PATH)
    private var fontItalicBold = createFont(FONT_ITALIC_BOLD_PATH)

    fun initialize() {
        // Fonts must be re-initialized for each report
        fontRegular = createFont(FONT_REGULAR_PATH)
        fontBold = createFont(FONT_BOLD_PATH)
        fontItalic = createFont(FONT_ITALIC_PATH)
        fontItalicBold = createFont(FONT_ITALIC_BOLD_PATH)
    }

    fun reportTitleStyle(): Style {
        return Style().setFont(fontBold()).setFontSize(13f).setFontColor(PALETTE_BLACK)
    }

    fun chapterTitleStyle(): Style {
        return Style().setFont(fontBold()).setFontSize(11f).setFontColor(PALETTE_BLACK)
    }

    fun tableTitleStyle(): Style {
        return Style().setFont(fontBold()).setFontSize(9f).setFontColor(PALETTE_BLUE)
    }

    fun tableSubTitleStyle(): Style {
        return Style().setFont(fontBold()).setFontSize(8f).setFontColor(PALETTE_BLUE)
    }

    fun tableSubStyle(): Style {
        return Style().setFont(fontRegular()).setFontSize(7f).setFontColor(PALETTE_BLACK)
    }

    fun tableHeaderStyle(): Style {
        return Style().setFont(fontBold()).setFontSize(8f).setFontColor(PALETTE_MID_GREY)
    }

    fun tableContentStyle(): Style {
        return Style().setFont(fontRegular()).setFontSize(8f).setFontColor(PALETTE_BLACK)
    }

    fun tableNoticeStyle(): Style {
        return Style().setFont(fontBold()).setFontSize(8f).setFontColor(PALETTE_WARN)
    }

    fun tableKeyStyle(): Style {
        return Style().setFont(fontRegular()).setFontSize(8f).setFontColor(PALETTE_BLACK)
    }

    fun tableUnknownStyle(): Style {
        return Style().setFont(fontRegular()).setFontSize(8f).setFontColor(PALETTE_BLACK)
    }

    fun tableHighlightStyle(): Style {
        return Style().setFont(fontBold()).setFontSize(8f).setFontColor(PALETTE_BLACK)
    }

    fun reportHeaderLabelStyle(): Style {
        return Style().setFont(fontRegular()).setFontSize(8f).setFontColor(PALETTE_BLACK)
    }

    fun reportHeaderValueStyle(): Style {
        return Style().setFont(fontBold()).setFontSize(9f).setFontColor(PALETTE_BLUE)
    }

    fun pageNumberStyle(): Style {
        return Style().setFont(fontBold()).setFontSize(8f).setFontColor(PALETTE_BLUE)
    }

    fun sidePanelLabelStyle(): Style {
        return Style().setFont(fontBold()).setFontSize(8f).setFontColor(PALETTE_WHITE)
    }

    fun sidePanelValueStyle(): Style {
        return Style().setFont(fontBold()).setFontSize(11f).setFontColor(PALETTE_WHITE)
    }

    fun disclaimerStyle(): Style {
        return Style().setFont(fontRegular()).setFontSize(6f).setFontColor(PALETTE_MID_GREY)
    }

    fun urlStyle(): Style {
        return Style().setFont(fontRegular()).setFontSize(8f).setFontColor(PALETTE_BLUE).setUnderline()
    }

    fun fontRegular(): PdfFont {
        // Each PDF needs its own private font objects, but they can be static as long as they are re-initialized for each report.
        return fontRegular
    }

    fun fontBold(): PdfFont {
        // Each PDF needs its own private font objects, but they can be static as long as they are re-initialized for each report.
        return fontBold
    }

    fun fontItalic(): PdfFont {
        return fontItalic
    }

    fun fontItalicBold() : PdfFont {
        return fontItalicBold
    }

    private fun createFont(fontPath: String): PdfFont {
        return PdfFontFactory.createFont(loadFontProgram(fontPath), PdfEncodings.IDENTITY_H)
    }

    private fun loadFontProgram(resourcePath: String): FontProgram {
        return try {
            FontProgramFactory.createFont(resourcePath)
        } catch (exception: IOException) {
            // Should never happen, fonts are loaded from code
            throw IllegalStateException(exception)
        }
    }
}