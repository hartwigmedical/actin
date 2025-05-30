package com.hartwig.actin.report.pdf.util

import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.itextpdf.kernel.colors.DeviceRgb
import com.itextpdf.layout.Style
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

object Formats {

    const val VALUE_UNKNOWN = "Unknown"
    const val VALUE_NONE = "None"
    const val VALUE_NOT_AVAILABLE = "N/A"
    const val COMMA_SEPARATOR = ", "
    const val STANDARD_KEY_WIDTH = 210f
    const val STANDARD_INNER_TABLE_WIDTH_DECREASE = 5f
    const val DATE_UNKNOWN = "Date unknown"
    const val ITALIC_TEXT_MARKER = "{i}"

    private val DATE_FORMAT = DateTimeFormatter.ofPattern("dd-MMM-yyyy")
    private val DECIMAL_FORMAT_SYMBOLS = DecimalFormatSymbols.getInstance(Locale.ENGLISH)
    private val TWO_DIGIT_FORMAT = DecimalFormat("#.##", DECIMAL_FORMAT_SYMBOLS)
    private val SINGLE_DIGIT_FORMAT = DecimalFormat("#.#", DECIMAL_FORMAT_SYMBOLS)
    private val FORCED_SINGLE_DIGIT_FORMAT = DecimalFormat("#0.0", DECIMAL_FORMAT_SYMBOLS)
    private val NO_DIGIT_FORMAT = DecimalFormat("#", DECIMAL_FORMAT_SYMBOLS)
    private val PERCENTAGE_FORMAT = DecimalFormat("#'%'", DECIMAL_FORMAT_SYMBOLS)
    private val SINGLE_DIGIT_PERCENTAGE_FORMAT = DecimalFormat("#.#'%'", DECIMAL_FORMAT_SYMBOLS)

    fun twoDigitNumber(number: Double): String {
        return TWO_DIGIT_FORMAT.format(number)
    }

    fun singleDigitNumber(number: Number): String {
        return SINGLE_DIGIT_FORMAT.format(number)
    }

    fun forcedSingleDigitNumber(number: Number): String {
        return FORCED_SINGLE_DIGIT_FORMAT.format(number)
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

    fun daysToMonths(number: Double): String {
        return singleDigitNumber(number / 30.44)
    }
    
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
        return if (value != DATE_UNKNOWN) {
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
            "Yes" -> Styles.PALETTE_GREEN
            "No" -> Styles.PALETTE_RED
            else -> Styles.PALETTE_YES_OR_NO_UNCLEAR
        }
    }
}