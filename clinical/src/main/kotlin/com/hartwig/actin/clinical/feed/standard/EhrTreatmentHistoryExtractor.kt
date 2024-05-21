package com.hartwig.actin.clinical.feed.standard

import com.hartwig.actin.clinical.ExtractionResult
import com.hartwig.actin.clinical.curation.CurationCategory
import com.hartwig.actin.clinical.curation.CurationDatabase
import com.hartwig.actin.clinical.curation.CurationResponse
import com.hartwig.actin.clinical.curation.config.NonOncologicalHistoryConfig
import com.hartwig.actin.clinical.curation.config.TreatmentHistoryEntryConfig
import com.hartwig.actin.clinical.curation.extraction.CurationExtractionEvaluation
import com.hartwig.actin.clinical.datamodel.treatment.Treatment
import com.hartwig.actin.clinical.datamodel.treatment.history.Intent
import com.hartwig.actin.clinical.datamodel.treatment.history.StopReason
import com.hartwig.actin.clinical.datamodel.treatment.history.TreatmentHistoryDetails
import com.hartwig.actin.clinical.datamodel.treatment.history.TreatmentHistoryEntry
import com.hartwig.actin.clinical.datamodel.treatment.history.TreatmentResponse
import com.hartwig.actin.clinical.datamodel.treatment.history.TreatmentStage

private const val TREATMENT_HISTORY = "treatment history"

class EhrTreatmentHistoryExtractor(
    private val treatmentCuration: CurationDatabase<TreatmentHistoryEntryConfig>,
    private val nonOncologicalHistoryCuration: CurationDatabase<NonOncologicalHistoryConfig>
) : EhrExtractor<List<TreatmentHistoryEntry>> {
    override fun extract(ehrPatientRecord: EhrPatientRecord): ExtractionResult<List<TreatmentHistoryEntry>> {
        val oncologicalTreatmentHistory = oncologicalTreatmentHistory(ehrPatientRecord)
        val oncologicalPreviousConditions = getOncologicalPreviousConditions(ehrPatientRecord)

        return ExtractionResult(
            oncologicalTreatmentHistory.extracted + oncologicalPreviousConditions.extracted,
            oncologicalTreatmentHistory.evaluation + oncologicalPreviousConditions.evaluation
        )
    }

    private fun getOncologicalPreviousConditions(ehrPatientRecord: EhrPatientRecord) =
        ehrPatientRecord.priorOtherConditions.map { ehrPreviousCondition ->
            if (nonOncologicalHistoryCuration.find(ehrPreviousCondition.name).isEmpty()) {
                val treatment = CurationResponse.createFromConfigs(
                    treatmentCuration.find(ehrPreviousCondition.name),
                    ehrPatientRecord.patientDetails.hashedId,
                    CurationCategory.ONCOLOGICAL_HISTORY,
                    ehrPreviousCondition.name,
                    TREATMENT_HISTORY,
                    false
                )
                ExtractionResult(
                    treatment.configs.mapNotNull { config ->
                        config.curated?.let { curatedTreatment ->
                            TreatmentHistoryEntry(
                                startYear = ehrPreviousCondition.startDate.year,
                                startMonth = ehrPreviousCondition.startDate.monthValue,
                                treatments = curatedTreatment.treatments,
                                intents = curatedTreatment.intents,
                                treatmentHistoryDetails = TreatmentHistoryDetails(
                                    stopYear = ehrPreviousCondition.endDate?.year,
                                    stopMonth = ehrPreviousCondition.endDate?.monthValue,
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
            } else {
                ExtractionResult(
                    emptyList(),
                    CurationExtractionEvaluation(treatmentHistoryEntryEvaluatedInputs = setOf(ehrPreviousCondition.name.lowercase()))
                )
            }
        }.fold<ExtractionResult<List<TreatmentHistoryEntry>>, ExtractionResult<List<TreatmentHistoryEntry>>>(
            ExtractionResult(emptyList(), CurationExtractionEvaluation())
        ) { acc, result ->
            ExtractionResult(acc.extracted + result.extracted, acc.evaluation + result.evaluation)
        }

    private fun oncologicalTreatmentHistory(ehrPatientRecord: EhrPatientRecord): ExtractionResult<List<TreatmentHistoryEntry>> =
        ehrPatientRecord.treatmentHistory.map { ehrTreatmentHistory ->

            val treatment = CurationResponse.createFromConfigs(
                treatmentCuration.find(ehrTreatmentHistory.treatmentName),
                ehrPatientRecord.patientDetails.hashedId,
                CurationCategory.ONCOLOGICAL_HISTORY,
                ehrTreatmentHistory.treatmentName,
                TREATMENT_HISTORY,
            )

            treatment.config()?.let { curatedTreatment ->
                if (!curatedTreatment.ignore) {
                    val switchToTreatments = treatmentStages(curatedTreatment.curated?.treatments, ehrTreatmentHistory, ehrPatientRecord)
                    ExtractionResult(
                        listOf(
                            TreatmentHistoryEntry(
                                startYear = ehrTreatmentHistory.startDate.year,
                                startMonth = ehrTreatmentHistory.startDate.monthValue,
                                intents = ehrTreatmentHistory.intention?.let { intent -> setOf(parseIntent(intent)) },
                                treatments = curatedTreatment.curated!!.treatments,
                                treatmentHistoryDetails = TreatmentHistoryDetails(
                                    stopYear = ehrTreatmentHistory.endDate?.year,
                                    stopMonth = ehrTreatmentHistory.endDate?.monthValue,
                                    stopReason = ehrTreatmentHistory.stopReason?.let { stopReason -> StopReason.valueOf(stopReason) },
                                    bestResponse = ehrTreatmentHistory.response?.let { response -> TreatmentResponse.valueOf(response) },
                                    switchToTreatments = switchToTreatments.extracted,
                                    cycles = ehrTreatmentHistory.administeredCycles,
                                    bodyLocations = curatedTreatment.curated.treatmentHistoryDetails?.bodyLocations,
                                    bodyLocationCategories = curatedTreatment.curated.treatmentHistoryDetails?.bodyLocationCategories,
                                    maintenanceTreatment = curatedTreatment.curated.treatmentHistoryDetails?.maintenanceTreatment,
                                ),
                                isTrial = ehrTreatmentHistory.administeredInStudy,
                                trialAcronym = curatedTreatment.curated.trialAcronym

                            )
                        ), switchToTreatments.evaluation + treatment.extractionEvaluation
                    )
                } else {
                    null
                }
            } ?: ExtractionResult(emptyList(), treatment.extractionEvaluation)
        }.fold(
            ExtractionResult(emptyList(), CurationExtractionEvaluation())
        ) { acc, result ->
            ExtractionResult(acc.extracted + result.extracted, acc.evaluation + result.evaluation)
        }

    private fun parseIntent(intent: String) = Intent.valueOf(intent.trim().uppercase())

    private fun treatmentStages(
        treatments: Set<Treatment>?,
        ehrTreatmentHistory: EhrTreatmentHistory,
        ehrPatientRecord: EhrPatientRecord
    ): ExtractionResult<List<TreatmentStage>> {
        return ehrTreatmentHistory.modifications?.map { modification ->
            val modificationTreatment = CurationResponse.createFromConfigs(
                treatmentCuration.find(modification.name),
                ehrPatientRecord.patientDetails.hashedId,
                CurationCategory.ONCOLOGICAL_HISTORY,
                modification.name,
                TREATMENT_HISTORY,
            )
            ExtractionResult(
                listOf(
                    TreatmentStage(
                        cycles = modification.administeredCycles,
                        startYear = modification.date.year,
                        startMonth = modification.date.monthValue,
                        treatment = modificationTreatment.config()?.curated?.treatments?.first() ?: treatments?.firstOrNull()
                        ?: throw IllegalStateException("Unable to curate or to fall back on original treatment for modification '${modification.name}'")
                    )
                ), modificationTreatment.extractionEvaluation
            )
        }?.fold(ExtractionResult(emptyList(), CurationExtractionEvaluation())) { acc, result ->
            ExtractionResult(acc.extracted + result.extracted, acc.evaluation + result.evaluation)
        } ?: ExtractionResult(emptyList(), CurationExtractionEvaluation())
    }
}