package com.hartwig.actin.clinical.feed.lab

import com.google.common.annotations.VisibleForTesting
import com.hartwig.actin.clinical.datamodel.ImmutableLabValue
import com.hartwig.actin.clinical.datamodel.LabValue
import com.hartwig.actin.clinical.feed.FeedParseFunctions
import org.apache.logging.log4j.LogManager

object LabExtraction {
    private val LOGGER = LogManager.getLogger(LabExtraction::class.java)

    fun extract(entry: LabEntry): LabValue {
        val limits = extractLimits(entry.referenceRangeText)
        val value = entry.valueQuantityValue
        var isOutsideRef: Boolean? = null
        if (limits.lower() != null || limits.upper() != null) {
            isOutsideRef = limits.lower() != null && value < limits.lower()!! || limits.upper() != null && value > limits.upper()!!
        }
        return ImmutableLabValue.builder()
            .date(entry.effectiveDateTime)
            .code(entry.codeCodeOriginal)
            .name(entry.codeDisplayOriginal)
            .comparator(entry.valueQuantityComparator)
            .value(value)
            .unit(LabUnitResolver.resolve(entry.valueQuantityUnit))
            .refLimitLow(limits.lower())
            .refLimitUp(limits.upper())
            .isOutsideRef(isOutsideRef)
            .build()
    }

    @VisibleForTesting
    fun extractLimits(referenceRangeText: String): Limits {
        var lower: Double? = null
        var upper: Double? = null
        if (referenceRangeText.contains(">")) {
            val index = referenceRangeText.indexOf(">")
            lower = FeedParseFunctions.parseDouble(referenceRangeText.substring(index + 1).trim { it <= ' ' })
        } else if (referenceRangeText.contains("<")) {
            val index = referenceRangeText.indexOf("<")
            upper = FeedParseFunctions.parseDouble(referenceRangeText.substring(index + 1).trim { it <= ' ' })
        } else if (referenceRangeText.contains("-")) {
            val separatingHyphenIndex = findSeparatingHyphenIndex(referenceRangeText)
            lower = FeedParseFunctions.parseDouble(referenceRangeText.substring(0, separatingHyphenIndex))
            upper = FeedParseFunctions.parseDouble(referenceRangeText.substring(separatingHyphenIndex + 1))
        } else if (!referenceRangeText.isEmpty()) {
            LOGGER.warn("Could not parse lab value referenceRangeText '{}'", referenceRangeText)
        }
        return Limits(lower, upper)
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

    class Limits(private val lower: Double?, private val upper: Double?) {
        @VisibleForTesting
        fun lower(): Double? {
            return lower
        }

        @VisibleForTesting
        fun upper(): Double? {
            return upper
        }
    }
}