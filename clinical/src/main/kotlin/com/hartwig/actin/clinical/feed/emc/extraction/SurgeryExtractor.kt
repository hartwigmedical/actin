package com.hartwig.actin.clinical.feed.emc.extraction

import com.hartwig.actin.clinical.ExtractionResult
import com.hartwig.actin.clinical.curation.CurationDatabase
import com.hartwig.actin.clinical.curation.CurationDatabaseContext
import com.hartwig.actin.clinical.curation.CurationResponse
import com.hartwig.actin.clinical.curation.config.SurgeryNameConfig
import com.hartwig.actin.clinical.curation.extraction.CurationExtractionEvaluation
import com.hartwig.actin.datamodel.clinical.Surgery
import com.hartwig.actin.datamodel.clinical.SurgeryStatus
import com.hartwig.actin.datamodel.clinical.ingestion.CurationCategory
import com.hartwig.feed.datamodel.FeedSurgery
import org.apache.logging.log4j.LogManager

class SurgeryExtractor(private val surgeryNameCuration: CurationDatabase<SurgeryNameConfig>) {

    fun extract(patientId: String, entries: List<FeedSurgery>): ExtractionResult<List<Surgery>> {
        return entries.map { entry ->
            val name = entry.name ?: throw IllegalArgumentException("Surgery name missing for patient $patientId")
            val curationResponse = CurationResponse.createFromConfigs(
                surgeryNameCuration.find(name),
                patientId,
                CurationCategory.SURGERY_NAME,
                name,
                "surgery"
            )
            val curatedSurgery = curationResponse.config()?.takeIf { !it.ignore }?.let {
                Surgery(
                    name = it.name,
                    endDate = entry.endDate,
                    status = resolveSurgeryStatus(entry.status),
                    treatmentType = it.treatmentType,
                )
            }
            ExtractionResult(listOfNotNull(curatedSurgery), curationResponse.extractionEvaluation)
        }.fold(ExtractionResult(emptyList(), CurationExtractionEvaluation())) { acc, extractionResult ->
            ExtractionResult(acc.extracted + extractionResult.extracted, acc.evaluation + extractionResult.evaluation)
        }
    }

    private fun resolveSurgeryStatus(status: String?): SurgeryStatus {
        val valueToFind = status?.trim { it <= ' ' }?.replace("-".toRegex(), "_")
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
        fun create(curationDatabaseContext: CurationDatabaseContext) = SurgeryExtractor(curationDatabaseContext.surgeryNameCuration)
    }
}