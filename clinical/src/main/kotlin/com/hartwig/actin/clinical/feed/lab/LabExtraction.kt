package com.hartwig.actin.clinical.feed.lab

import com.google.common.annotations.VisibleForTesting
import com.hartwig.actin.clinical.datamodel.LabValue
import com.hartwig.actin.clinical.feed.FeedParseFunctions
import org.apache.logging.log4j.LogManager

object LabExtraction {
    private val LOGGER = LogManager.getLogger(LabExtraction::class.java)

    fun extract(entry: LabEntry): LabValue {
        val limits = extractLimits(entry.referenceRangeText)
        val value = entry.valueQuantityValue
        val isOutsideRef = if (limits.lower != null || limits.upper != null) {
            limits.lower != null && value < limits.lower || limits.upper != null && value > limits.upper
        } else null
        return LabValue(
            date = entry.effectiveDateTime,
            code = entry.codeCodeOriginal,
            name = entry.codeDisplayOriginal,
            comparator = entry.valueQuantityComparator,
            value = value,
            unit = LabUnitResolver.resolve(entry.valueQuantityUnit),
            refLimitLow = limits.lower,
            refLimitUp = limits.upper,
            isOutsideRef = isOutsideRef
        )
    }

    @VisibleForTesting
    fun extractLimits(referenceRangeText: String): Limits {
        return when {
            referenceRangeText.contains(">") -> {
                val index = referenceRangeText.indexOf(">")
                val lower = FeedParseFunctions.parseDouble(referenceRangeText.substring(index + 1).trim { it <= ' ' })
                Limits(lower, null)
            }

            referenceRangeText.contains("<") -> {
                val index = referenceRangeText.indexOf("<")
                val upper = FeedParseFunctions.parseDouble(referenceRangeText.substring(index + 1).trim { it <= ' ' })
                Limits(null, upper)
            }

            referenceRangeText.contains("-") -> {
                val separatingHyphenIndex = findSeparatingHyphenIndex(referenceRangeText)
                val lower = FeedParseFunctions.parseDouble(referenceRangeText.substring(0, separatingHyphenIndex))
                val upper = FeedParseFunctions.parseDouble(referenceRangeText.substring(separatingHyphenIndex + 1))
                Limits(lower, upper)
            }

            else -> {
                if (referenceRangeText.isNotEmpty()) {
                    LOGGER.warn("Could not parse lab value referenceRangeText '{}'", referenceRangeText)
                }
                Limits(null, null)
            }
        }
    }

    @VisibleForTesting
    fun findSeparatingHyphenIndex(referenceRangeText: String): Int {
        assert(referenceRangeText.contains("-"))
        var isReadingDigit = false
        for (i in referenceRangeText.indices) {
            if (isReadingDigit && referenceRangeText[i] == '-') {
                return i
            } else if (isDigit(referenceRangeText[i])) {
                isReadingDigit = true
            }
        }
        throw IllegalArgumentException("Could not determine separating hyphen index from $referenceRangeText")
    }

    private fun isDigit(character: Char): Boolean {
        return character in '0'..'9'
    }

    data class Limits(val lower: Double?, val upper: Double?)
}