package com.hartwig.actin.clinical.feed.standard.extraction

import com.hartwig.actin.clinical.ExtractionResult
import com.hartwig.actin.datamodel.clinical.ingestion.CurationCategory
import com.hartwig.actin.clinical.curation.CurationDatabase
import com.hartwig.actin.clinical.curation.CurationResponse
import com.hartwig.actin.clinical.curation.config.TreatmentHistoryEntryConfig
import com.hartwig.actin.clinical.curation.extraction.CurationExtractionEvaluation
import com.hartwig.actin.datamodel.clinical.treatment.history.TreatmentHistoryDetails
import com.hartwig.actin.datamodel.clinical.treatment.history.TreatmentHistoryEntry
import com.hartwig.feed.datamodel.DatedEntry
import com.hartwig.feed.datamodel.FeedPatientRecord
import kotlin.collections.map

private const val TREATMENT_HISTORY = "treatment history"

class StandardOncologicalHistoryExtractor(
    private val treatmentCuration: CurationDatabase<TreatmentHistoryEntryConfig>
) : StandardDataExtractor<List<TreatmentHistoryEntry>> {
    override fun extract(ehrPatientRecord: FeedPatientRecord): ExtractionResult<List<TreatmentHistoryEntry>> {
        val patientId = ehrPatientRecord.patientDetails.patientId
        val oncologicalPreviousConditions = convertFeedEntriesToOncologicalHistory(ehrPatientRecord.otherConditions, patientId)
        val oncologicalTreatmentHistory = convertFeedEntriesToOncologicalHistory(ehrPatientRecord.treatmentHistory, patientId)

        return ExtractionResult(
            merge(oncologicalTreatmentHistory.extracted, oncologicalPreviousConditions.extracted),
            oncologicalTreatmentHistory.evaluation
        )
    }

    private fun merge(
        oncologicalTreatmentHistory: List<TreatmentHistoryEntry>,
        oncologicalPreviousConditions: List<TreatmentHistoryEntry>
    ): List<TreatmentHistoryEntry> {
        val treatmentSignatures = oncologicalPreviousConditions.map { Triple(it.treatments, it.startYear, it.startMonth) }.toSet()
        return oncologicalPreviousConditions + oncologicalTreatmentHistory.filter {
            Triple(it.treatments, it.startYear, it.startMonth) !in treatmentSignatures
        }
    }

    private fun convertFeedEntriesToOncologicalHistory(
        entries: List<DatedEntry>, patientId: String
    ): ExtractionResult<List<TreatmentHistoryEntry>> =
        entries.map { ehrEntry ->
            val treatment = CurationResponse.createFromConfigs(
                treatmentCuration.find(ehrEntry.name),
                patientId,
                CurationCategory.ONCOLOGICAL_HISTORY,
                ehrEntry.name,
                TREATMENT_HISTORY
            )
            ExtractionResult(
                treatment.configs.mapNotNull { config ->
                    config.takeUnless { it.ignore }?.curated?.let { curatedTreatment ->
                        TreatmentHistoryEntry(
                            treatments = curatedTreatment.treatments,
                            startYear = curatedTreatment.startYear ?: ehrEntry.startDate.year,
                            startMonth = curatedTreatment.startMonth ?: ehrEntry.startDate.monthValue,
                            intents = curatedTreatment.intents,
                            treatmentHistoryDetails = TreatmentHistoryDetails(
                                stopYear = curatedTreatment.treatmentHistoryDetails?.stopYear ?: ehrEntry.endDate?.year,
                                stopMonth = curatedTreatment.treatmentHistoryDetails?.stopMonth
                                    ?: ehrEntry.endDate?.monthValue,
                                stopReason = curatedTreatment.treatmentHistoryDetails?.stopReason,
                                bestResponse = curatedTreatment.treatmentHistoryDetails?.bestResponse,
                                switchToTreatments = curatedTreatment.treatmentHistoryDetails?.switchToTreatments,
                                cycles = curatedTreatment.treatmentHistoryDetails?.cycles,
                                bodyLocations = curatedTreatment.treatmentHistoryDetails?.bodyLocations,
                                bodyLocationCategories = curatedTreatment.treatmentHistoryDetails?.bodyLocationCategories,
                                maintenanceTreatment = curatedTreatment.treatmentHistoryDetails?.maintenanceTreatment,
                            ),
                            isTrial = curatedTreatment.isTrial,
                            trialAcronym = curatedTreatment.trialAcronym
                        )
                    }
                }, treatment.extractionEvaluation
            )
        }
            .fold(ExtractionResult(emptyList(), CurationExtractionEvaluation())) { acc, result ->
                ExtractionResult(acc.extracted + result.extracted, acc.evaluation + result.evaluation)
            }
}