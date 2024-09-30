package com.hartwig.actin.clinical.feed.emc.extraction

import com.hartwig.actin.clinical.ExtractionResult
import com.hartwig.actin.clinical.curation.CurationCategory
import com.hartwig.actin.clinical.curation.CurationDatabaseContext
import com.hartwig.actin.clinical.curation.CurationResponse
import com.hartwig.actin.clinical.curation.extraction.CurationExtractionEvaluation
import com.hartwig.actin.clinical.curation.translation.TranslationDatabase
import com.hartwig.actin.clinical.feed.emc.surgery.SurgeryEntry
import com.hartwig.actin.datamodel.clinical.Surgery
import com.hartwig.actin.datamodel.clinical.SurgeryStatus
import org.apache.logging.log4j.LogManager

class SurgeryExtractor(private val surgeryTranslations: TranslationDatabase<String>) {

    fun extract(patientId: String, entries: List<SurgeryEntry>): ExtractionResult<List<Surgery>> {
        return entries.map { entry: SurgeryEntry ->
            val translation = surgeryTranslations.find(entry.codeCodingDisplayOriginal)
            val curationResponse = CurationResponse.createFromTranslation(
                translation,
                patientId,
                CurationCategory.SURGERY_TRANSLATION,
                entry.codeCodingDisplayOriginal,
                "surgery"
            )
            val surgery = Surgery(
                name = translation?.translated ?: entry.codeCodingDisplayOriginal,
                endDate = entry.periodEnd,
                status = resolveSurgeryStatus(entry.encounterStatus)
            )
            ExtractionResult(surgery, curationResponse.extractionEvaluation)
        }.fold(ExtractionResult(emptyList(), CurationExtractionEvaluation())) { acc, extractionResult ->
            ExtractionResult(acc.extracted + extractionResult.extracted, acc.evaluation + extractionResult.evaluation)
        }
    }

    private fun resolveSurgeryStatus(status: String): SurgeryStatus {
        val valueToFind = status.trim { it <= ' ' }.replace("-".toRegex(), "_")
        for (option in SurgeryStatus.entries) {
            if (option.toString().equals(valueToFind, ignoreCase = true)) {
                return option
            }
        }
        LOGGER.warn("Could not resolve surgery status '{}'", status)
        return SurgeryStatus.UNKNOWN
    }

    companion object {
        private val LOGGER = LogManager.getLogger(SurgeryExtractor::class.java)
        fun create(curationDatabaseContext: CurationDatabaseContext) = SurgeryExtractor(curationDatabaseContext.surgeryTranslation)
    }
}