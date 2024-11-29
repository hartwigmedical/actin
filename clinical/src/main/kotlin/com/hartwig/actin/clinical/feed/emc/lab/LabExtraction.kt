package com.hartwig.actin.clinical.feed.emc.lab

import com.hartwig.actin.clinical.feed.emc.FeedParseFunctions
import com.hartwig.actin.datamodel.clinical.LabValue
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

    private fun extractLimits(referenceRangeText: String): Limits {
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

    private fun findSeparatingHyphenIndex(referenceRangeText: String): Int {
        return referenceRangeText.indexOfFirst(Char::isDigit).takeIf { it != -1 }
            ?.let { indexOfFirstDigit ->
                referenceRangeText.indexOf("-", startIndex = indexOfFirstDigit).takeIf { it != -1 }
            }
            ?: throw IllegalArgumentException("Could not determine separating hyphen index from $referenceRangeText")
    }

    data class Limits(val lower: Double?, val upper: Double?)
}