package com.hartwig.actin.clinical.feed.standard

import com.hartwig.actin.clinical.ExtractionResult
import com.hartwig.actin.clinical.curation.CurationCategory
import com.hartwig.actin.clinical.curation.CurationDatabase
import com.hartwig.actin.clinical.curation.CurationResponse
import com.hartwig.actin.clinical.curation.config.TreatmentHistoryEntryConfig
import com.hartwig.actin.clinical.curation.extraction.CurationExtractionEvaluation
import com.hartwig.actin.clinical.datamodel.treatment.history.Intent
import com.hartwig.actin.clinical.datamodel.treatment.history.StopReason
import com.hartwig.actin.clinical.datamodel.treatment.history.TreatmentHistoryDetails
import com.hartwig.actin.clinical.datamodel.treatment.history.TreatmentHistoryEntry
import com.hartwig.actin.clinical.datamodel.treatment.history.TreatmentResponse
import com.hartwig.actin.clinical.datamodel.treatment.history.TreatmentStage

private const val TREATMENT_HISTORY = "treatment history"

class EhrTreatmentHistoryExtractor(
    private val treatmentCuration: CurationDatabase<TreatmentHistoryEntryConfig>
) : EhrExtractor<List<TreatmentHistoryEntry>> {
    override fun extract(ehrPatientRecord: EhrPatientRecord): ExtractionResult<List<TreatmentHistoryEntry>> {
        return ehrPatientRecord.treatmentHistory.map {

            val treatment = CurationResponse.createFromConfigs(
                treatmentCuration.find(it.treatmentName),
                ehrPatientRecord.patientDetails.hashedIdBase64(),
                CurationCategory.ONCOLOGICAL_HISTORY,
                it.treatmentName,
                TREATMENT_HISTORY,
            )

            treatment.config()?.let { curatedTreatment ->
                val switchToTreatments = treatmentStages(it, ehrPatientRecord)
                ExtractionResult(
                    listOf(
                        TreatmentHistoryEntry(
                            startYear = it.startDate.year,
                            startMonth = it.startDate.monthValue,
                            intents = it.intention?.let { intent -> setOf(Intent.valueOf(intent)) },
                            treatments = curatedTreatment.curated!!.treatments,
                            treatmentHistoryDetails = TreatmentHistoryDetails(
                                stopYear = it.endDate?.year,
                                stopMonth = it.endDate?.monthValue,
                                stopReason = it.stopReason?.let { stopReason -> StopReason.valueOf(stopReason) },
                                bestResponse = it.response?.let { response -> TreatmentResponse.valueOf(response) },
                                switchToTreatments = switchToTreatments.extracted,
                                cycles = it.administeredCycles,
                            ),
                            isTrial = it.administeredInStudy
                        )
                    ), switchToTreatments.evaluation
                )
            } ?: ExtractionResult(emptyList(), treatment.extractionEvaluation)
        }.fold(ExtractionResult(emptyList(), CurationExtractionEvaluation())) { acc, result ->
            ExtractionResult(acc.extracted + result.extracted, acc.evaluation + result.evaluation)
        }
    }

    private fun treatmentStages(
        it: EhrTreatmentHistory,
        ehrPatientRecord: EhrPatientRecord
    ): ExtractionResult<List<TreatmentStage>> {
        return it.modifications?.map { modification ->
            val modificationTreatment = CurationResponse.createFromConfigs(
                treatmentCuration.find(modification.name),
                ehrPatientRecord.patientDetails.hashedIdBase64(),
                CurationCategory.ONCOLOGICAL_HISTORY,
                modification.name,
                TREATMENT_HISTORY,
            )
            modificationTreatment.config()?.let { curatedModificaton ->
                ExtractionResult(
                    listOf(
                        TreatmentStage(
                            cycles = modification.administeredCycles,
                            startYear = modification.date.year,
                            startMonth = modification.date.monthValue,
                            treatment = curatedModificaton.curated!!.treatments.first(),
                        )
                    ), modificationTreatment.extractionEvaluation
                )
            } ?: ExtractionResult(emptyList(), modificationTreatment.extractionEvaluation)
        }?.fold(ExtractionResult(emptyList(), CurationExtractionEvaluation())) { acc, result ->
            ExtractionResult(acc.extracted + result.extracted, acc.evaluation + result.evaluation)
        } ?: ExtractionResult(emptyList(), CurationExtractionEvaluation())
    }
}