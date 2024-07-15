package com.hartwig.actin.report.pdf.util

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.itextpdf.kernel.colors.DeviceRgb
import com.itextpdf.layout.Style
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

object Formats {
    const val VALUE_UNKNOWN = "Unknown"
    const val VALUE_NONE = "None"
    const val VALUE_COMING_SOON = "Coming soon"
    const val VALUE_NOT_AVAILABLE = "N/A"
    const val COMMA_SEPARATOR = ", "
    const val STANDARD_KEY_WIDTH = 210f
    private val NON_HIGHLIGHT_VALUES = setOf(VALUE_COMING_SOON)
    const val DATE_UNKNOWN = "Date unknown"
    private val DATE_FORMAT = DateTimeFormatter.ofPattern("dd-MMM-yyyy")
    private val DECIMAL_FORMAT_SYMBOLS = DecimalFormatSymbols.getInstance(Locale.ENGLISH)
    private val TWO_DIGIT_FORMAT = DecimalFormat("#.##", DECIMAL_FORMAT_SYMBOLS)
    private val SINGLE_DIGIT_FORMAT = DecimalFormat("#.#", DECIMAL_FORMAT_SYMBOLS)
    private val NO_DIGIT_FORMAT = DecimalFormat("#", DECIMAL_FORMAT_SYMBOLS)
    private val PERCENTAGE_FORMAT = DecimalFormat("#'%'", DECIMAL_FORMAT_SYMBOLS)
    private val SINGLE_DIGIT_PERCENTAGE_FORMAT = DecimalFormat("#.#'%'", DECIMAL_FORMAT_SYMBOLS)

    fun twoDigitNumber(number: Double): String {
        return TWO_DIGIT_FORMAT.format(number)
    }

    fun singleDigitNumber(number: Double): String {
        return SINGLE_DIGIT_FORMAT.format(number)
    }

    fun noDigitNumber(number: Double): String {
        return NO_DIGIT_FORMAT.format(number)
    }

    fun percentage(number: Double): String {
        return PERCENTAGE_FORMAT.format(number * 100)
    }

    fun singleDigitPercentage(number: Double): String {
        return SINGLE_DIGIT_PERCENTAGE_FORMAT.format(number * 100)
    }

    @JvmOverloads
    fun date(date: LocalDate?, fallback: String = DATE_UNKNOWN): String {
        return if (date != null) DATE_FORMAT.format(date) else fallback
    }

    fun yesNoUnknown(bool: Boolean?): String {
        if (bool == null) {
            return VALUE_UNKNOWN
        }
        return if (bool) "Yes" else "No"
    }

    fun valueOrDefault(value: String, defaultValue: String): String {
        return value.ifEmpty { defaultValue }
    }

    fun styleForTableValue(value: String): Style {
        return if (!NON_HIGHLIGHT_VALUES.contains(value) && !DATE_UNKNOWN.equals(value)) {
            Styles.tableHighlightStyle()
        } else Styles.tableUnknownStyle()
    }

    fun fontColorForEvaluation(evaluation: EvaluationResult): DeviceRgb {
        return when (evaluation) {
            EvaluationResult.PASS -> Styles.PALETTE_EVALUATION_PASS
            EvaluationResult.WARN -> Styles.PALETTE_EVALUATION_WARN
            EvaluationResult.FAIL -> Styles.PALETTE_EVALUATION_FAILED
            else -> Styles.PALETTE_EVALUATION_UNCLEAR
        }
    }

    fun fontColorForYesNo(yesNo: String): DeviceRgb {
        return when (yesNo) {
            "Yes" -> Styles.PALETTE_YES_OR_NO_YES
            "No" -> Styles.PALETTE_YES_OR_NO_NO
            else -> Styles.PALETTE_YES_OR_NO_UNCLEAR
        }
    }
}