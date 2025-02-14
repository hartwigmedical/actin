package com.hartwig.actin.clinical.feed.emc.extraction

import com.hartwig.actin.clinical.ExtractionResult
import com.hartwig.actin.clinical.curation.CurationCategory
import com.hartwig.actin.clinical.curation.CurationDatabase
import com.hartwig.actin.clinical.curation.CurationDatabaseContext
import com.hartwig.actin.clinical.curation.CurationResponse
import com.hartwig.actin.clinical.curation.config.LabMeasurementConfig
import com.hartwig.actin.clinical.curation.extraction.CurationExtractionEvaluation
import com.hartwig.actin.clinical.feed.emc.FeedParseFunctions
import com.hartwig.actin.clinical.feed.emc.lab.LabEntry
import com.hartwig.actin.clinical.feed.emc.lab.LabUnitResolver
import com.hartwig.actin.datamodel.clinical.LabValue
import org.apache.logging.log4j.LogManager

class LabValueExtractor(private val laboratoryCuration: CurationDatabase<LabMeasurementConfig>) {

    private val logger = LogManager.getLogger(LabValueExtractor::class.java)

    fun extract(patientId: String, entries: List<LabEntry>): ExtractionResult<List<LabValue>> {
        return entries.map { entry ->
            val value = entry.valueQuantityValue
            val limits = extractLimits(entry.referenceRangeText)
            val isOutsideRef = if (limits.lower != null || limits.upper != null) {
                limits.lower != null && value < limits.lower || limits.upper != null && value > limits.upper
            } else null
            val curationResponse = CurationResponse.createFromConfigs(
                laboratoryCuration.find("${entry.codeCodeOriginal} | ${entry.codeDisplayOriginal}"),
                patientId,
                CurationCategory.LABORATORY,
                "${entry.codeCodeOriginal} | ${entry.codeDisplayOriginal}",
                "laboratory"
            )
            val curatedLab = curationResponse.config()?.takeIf { !it.ignore }?.let {
                LabValue(
                    measurement = it.labMeasurement,
                    date = entry.effectiveDateTime,
                    comparator = entry.valueQuantityComparator,
                    value = value,
                    unit = LabUnitResolver.resolve(entry.valueQuantityUnit),
                    refLimitLow = limits.lower,
                    refLimitUp = limits.upper,
                    isOutsideRef = isOutsideRef
                )
            }
            ExtractionResult(listOfNotNull(curatedLab), curationResponse.extractionEvaluation)
        }
            .fold(ExtractionResult(emptyList(), CurationExtractionEvaluation())) { acc, extractionResult ->
                ExtractionResult(acc.extracted + extractionResult.extracted, acc.evaluation + extractionResult.evaluation)
            }
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
                    logger.warn("Could not parse lab value referenceRangeText '{}'", referenceRangeText)
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
            LabValueExtractor(laboratoryCuration = curationDatabaseContext.laboratoryCuration)
    }
}