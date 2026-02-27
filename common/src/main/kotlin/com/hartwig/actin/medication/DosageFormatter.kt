package com.hartwig.actin.medication

import com.hartwig.actin.datamodel.clinical.Dosage
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

object DosageFormatter {
    private val DECIMAL_FORMAT_SYMBOLS = DecimalFormatSymbols.getInstance(Locale.ENGLISH)
    private val TWO_DECIMAL_PLACES_FORMAT = DecimalFormat("#.##", DECIMAL_FORMAT_SYMBOLS)
    private val NO_DECIMAL_PLACES_FORMAT = DecimalFormat("#", DECIMAL_FORMAT_SYMBOLS)
    private val SPECIFIC_OR_UNKNOWN = setOf("specific prescription", "unknown prescription")

    fun formatDosage(dosage: Dosage): String {
        val dosageMin = formatDosageLimit(dosage.dosageMin)
        val dosageMax = formatDosageLimit(dosage.dosageMax)
        val dosageString = if (dosageMin == dosageMax) dosageMin else "$dosageMin - $dosageMax"

        return when (val dosageUnit = dosage.dosageUnit) {
            null -> "unknown prescription"
            in SPECIFIC_OR_UNKNOWN -> dosageUnit
            else -> listOfNotNull("if needed".takeIf { dosage.ifNeeded == true }, dosageString, dosageUnit).joinToString(" ")
        }
    }

    fun formatFrequency(dosage: Dosage): String {
        val frequency = if (dosage.frequency != null && dosage.frequency != 0.0) {
            TWO_DECIMAL_PLACES_FORMAT.format(dosage.frequency!!)
        } else "?"

        val frequencyUnit = dosage.frequencyUnit
        val periodBetweenValue = dosage.periodBetweenValue
        return when {
            frequencyUnit == null -> "unknown prescription"

            frequencyUnit in SPECIFIC_OR_UNKNOWN + "once" -> frequencyUnit

            dosage.periodBetweenUnit != null && periodBetweenValue != null -> {
                "$frequency / ${NO_DECIMAL_PLACES_FORMAT.format(periodBetweenValue + 1)} ${dosage.periodBetweenUnit}"
            }

            else -> "$frequency / $frequencyUnit"
        }
    }

    private fun formatDosageLimit(dosageLimit: Double?) =
        if (dosageLimit != null && dosageLimit != 0.0) TWO_DECIMAL_PLACES_FORMAT.format(dosageLimit) else "?"
}