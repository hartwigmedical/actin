package com.hartwig.actin.clinical.feed.emc.extraction

import com.hartwig.actin.clinical.ExtractionResult
import com.hartwig.actin.clinical.curation.CurationCategory
import com.hartwig.actin.clinical.curation.CurationDatabaseContext
import com.hartwig.actin.clinical.curation.CurationWarning
import com.hartwig.actin.clinical.curation.extraction.CurationExtractionEvaluation
import com.hartwig.actin.clinical.curation.translation.TranslationDatabase
import com.hartwig.actin.clinical.feed.emc.FeedParseFunctions
import com.hartwig.actin.clinical.feed.emc.lab.LabEntry
import com.hartwig.actin.clinical.feed.emc.lab.LabUnitResolver
import com.hartwig.actin.clinical.sort.LabValueDescendingDateComparator
import com.hartwig.actin.datamodel.clinical.LabMeasurement
import com.hartwig.actin.datamodel.clinical.LabValue
import org.apache.logging.log4j.LogManager

class LabValueExtractor(private val laboratoryTranslation: TranslationDatabase<String>) {

    private val LOGGER = LogManager.getLogger(LabValueExtractor::class.java)

    fun extract(patientId: String, rawValues: List<LabEntry>): ExtractionResult<List<LabValue>> {
        val extractedValues = rawValues.map { entry ->
            val trimmedName = entry.codeDisplayOriginal.trim { it <= ' ' }
            val translation = laboratoryTranslation.find("${entry.codeCodeOriginal} | ${entry.codeDisplayOriginal}")
            val limits = extractLimits(entry.referenceRangeText)
            val value = entry.valueQuantityValue
            val isOutsideRef = if (limits.lower != null || limits.upper != null) {
                limits.lower != null && value < limits.lower || limits.upper != null && value > limits.upper
            } else null
            if (translation == null) {
                val warning = CurationWarning(
                    patientId = patientId,
                    category = CurationCategory.LABORATORY_TRANSLATION,
                    feedInput = "${entry.codeCodeOriginal} | $trimmedName",
                    message = "Could not find laboratory translation for lab value with code '${entry.codeCodeOriginal}' and name '$trimmedName'"
                )
                ExtractionResult(emptyList(), CurationExtractionEvaluation(warnings = setOf(warning)))
            } else {
                val newLabValue = LabValue(
                    measurement = LabMeasurement.PSA,
                    date = entry.effectiveDateTime,
                    comparator = entry.valueQuantityComparator,
                    value = value,
                    unit = LabUnitResolver.resolve(entry.valueQuantityUnit),
                    refLimitLow = limits.lower,
                    refLimitUp = limits.upper,
                    isOutsideRef = isOutsideRef
                )
                ExtractionResult(listOf(newLabValue), CurationExtractionEvaluation(laboratoryEvaluatedInputs = setOf(translation)))
            }
        }
            .fold(ExtractionResult(emptyList<LabValue>(), CurationExtractionEvaluation())) { acc, result ->
                ExtractionResult(acc.extracted + result.extracted, acc.evaluation + result.evaluation)
            }

        return extractedValues.copy(extracted = extractedValues.extracted.sortedWith(LabValueDescendingDateComparator()))
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

    companion object {
        fun create(curationDatabaseContext: CurationDatabaseContext) =
            LabValueExtractor(laboratoryTranslation = curationDatabaseContext.laboratoryTranslation)
    }
}