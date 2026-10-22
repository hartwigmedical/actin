package com.hartwig.actin.report.pdf.util

import com.hartwig.actin.report.pdf.util.PaletteColor.BLACK
import com.hartwig.actin.report.pdf.util.PaletteColor.BLUE
import com.hartwig.actin.report.pdf.util.PaletteColor.DARK_GREY
import com.hartwig.actin.report.pdf.util.PaletteColor.EVALUATION_WARN
import com.hartwig.actin.report.pdf.util.PaletteColor.MID_GREY
import com.hartwig.actin.report.pdf.util.PaletteColor.WHITE
import com.itextpdf.io.font.FontProgram
import com.itextpdf.io.font.FontProgramFactory
import com.itextpdf.io.font.PdfEncodings
import com.itextpdf.kernel.colors.DeviceRgb
import com.itextpdf.kernel.font.PdfFont
import com.itextpdf.kernel.font.PdfFontFactory
import com.itextpdf.layout.Style
import java.io.File
import java.io.IOException
import java.util.Properties

object Styles {

    private val DEFAULT_PALETTE = PaletteColor.entries.associateWith { it.default }
    private var palette = DEFAULT_PALETTE

    fun color(color: PaletteColor): DeviceRgb = palette.getValue(color)

    const val REGULAR_FONT_SIZE = 8f
    const val SUB_FONT_SIZE = 7f
    const val SMALL_FONT_SIZE = 6f
    const val SMALLER_FONT_SIZE = 5f
    const val TABLE_HEADER_FONT_SIZE = 7f

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

    fun loadColors(colorsPath: String? = null) {
        val overrides = colorsPath?.let { path ->
            val properties = Properties().apply { File(path).bufferedReader(Charsets.UTF_8).use(::load) }
            properties.stringPropertyNames().associate { key ->
                val color = PaletteColor.entries.find { it.key == key }
                require(color != null) { "Unknown color '$key' in $path, expected one of ${PaletteColor.entries.map { it.key }}" }
                color to parseColor(key, properties.getProperty(key))
            }
        } ?: emptyMap()
        palette = DEFAULT_PALETTE + overrides
    }

    private fun parseColor(key: String, value: String): DeviceRgb {
        val rgb = value.split(",").map { it.trim().toIntOrNull()?.takeIf { component -> component in 0..255 } }
        require(rgb.size == 3 && rgb.all { it != null }) { "Color '$key' must be formatted as 'r,g,b' with values 0-255: $value" }
        return DeviceRgb(rgb[0]!!, rgb[1]!!, rgb[2]!!)
    }

    fun reportTitleStyle(): Style {
        return Style().setFont(fontBold()).setFontSize(17f).setFontColor(color(BLUE))
    }

    fun chapterTitleStyle(): Style {
        return Style().setFont(fontBold()).setFontSize(16f).setFontColor(color(BLUE))
    }

    fun tableTitleStyle(): Style {
        return Style().setFont(fontBold()).setFontSize(11f).setFontColor(color(BLUE))
    }

    fun tableSubTitleStyle(): Style {
        return Style().setFont(fontRegular()).setFontSize(REGULAR_FONT_SIZE).setFontColor(color(BLUE))
    }

    fun tableSubStyle(): Style {
        return Style().setFont(fontRegular()).setFontSize(SUB_FONT_SIZE).setFontColor(color(BLACK))
    }

    fun tableHeaderStyle(): Style {
        return Style().setFont(fontRegular()).setFontSize(TABLE_HEADER_FONT_SIZE).setFontColor(color(MID_GREY))
    }

    fun tableContentStyle(): Style {
        return Style().setFont(fontRegular()).setFontSize(REGULAR_FONT_SIZE).setFontColor(color(DARK_GREY))
    }

    fun tableNoticeStyle(): Style {
        return Style().setFont(fontBold()).setFontSize(REGULAR_FONT_SIZE).setFontColor(color(EVALUATION_WARN))
    }

    fun tableKeyStyle(): Style {
        return Style().setFont(fontRegular()).setFontSize(REGULAR_FONT_SIZE).setFontColor(color(DARK_GREY))
    }

    fun tableUnknownStyle(): Style {
        return Style().setFont(fontRegular()).setFontSize(REGULAR_FONT_SIZE).setFontColor(color(DARK_GREY))
    }

    fun tableHighlightStyle(): Style {
        return Style().setFont(fontBold()).setFontSize(REGULAR_FONT_SIZE).setFontColor(color(BLACK))
    }

    fun reportHeaderLabelStyle(): Style {
        return Style().setFont(fontRegular()).setFontSize(REGULAR_FONT_SIZE).setFontColor(color(BLACK))
    }

    fun reportHeaderValueStyle(): Style {
        return Style().setFont(fontBold()).setFontSize(9f).setFontColor(color(BLUE))
    }

    fun pageNumberStyle(): Style {
        return Style().setFont(fontBold()).setFontSize(REGULAR_FONT_SIZE).setFontColor(color(BLUE))
    }

    fun sidePanelLabelStyle(): Style {
        return Style().setFont(fontBold()).setFontSize(REGULAR_FONT_SIZE).setFontColor(color(WHITE))
    }

    fun sidePanelValueStyle(): Style {
        return Style().setFont(fontBold()).setFontSize(11f).setFontColor(color(WHITE))
    }

    fun disclaimerStyle(): Style {
        return Style().setFont(fontRegular()).setFontSize(SMALLER_FONT_SIZE).setFontColor(color(MID_GREY))
    }

    fun urlStyle(): Style {
        return Style().setFont(fontRegular()).setFontSize(REGULAR_FONT_SIZE).setFontColor(color(BLUE)).setUnderline()
    }

    private fun fontRegular(): PdfFont {
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

    private fun createFont(fontPath: String): PdfFont {
        return PdfFontFactory.createFont(loadFontProgram(fontPath), PdfEncodings.IDENTITY_H)
    }

    private fun loadFontProgram(resourcePath: String): FontProgram {
        return try {
            FontProgramFactory.createFont(resourcePath)
        } catch (exception: IOException) {
            throw IllegalStateException(exception)
        }
    }
}