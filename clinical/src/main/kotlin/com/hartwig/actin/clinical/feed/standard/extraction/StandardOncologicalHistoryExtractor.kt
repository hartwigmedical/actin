package com.hartwig.actin.clinical.feed.standard.extraction

import com.hartwig.actin.clinical.ExtractionResult
import com.hartwig.actin.clinical.curation.CurationCategory
import com.hartwig.actin.clinical.curation.CurationDatabase
import com.hartwig.actin.clinical.curation.CurationResponse
import com.hartwig.actin.clinical.curation.config.TreatmentHistoryEntryConfig
import com.hartwig.actin.clinical.curation.extraction.CurationExtractionEvaluation
import com.hartwig.actin.clinical.feed.standard.ProvidedPatientRecord
import com.hartwig.actin.clinical.feed.standard.ProvidedTreatmentHistory
import com.hartwig.actin.datamodel.clinical.treatment.Treatment
import com.hartwig.actin.datamodel.clinical.treatment.history.Intent
import com.hartwig.actin.datamodel.clinical.treatment.history.StopReason
import com.hartwig.actin.datamodel.clinical.treatment.history.TreatmentHistoryDetails
import com.hartwig.actin.datamodel.clinical.treatment.history.TreatmentHistoryEntry
import com.hartwig.actin.datamodel.clinical.treatment.history.TreatmentResponse
import com.hartwig.actin.datamodel.clinical.treatment.history.TreatmentStage

private const val TREATMENT_HISTORY = "treatment history"

class StandardOncologicalHistoryExtractor(
    private val treatmentCuration: CurationDatabase<TreatmentHistoryEntryConfig>
) : StandardDataExtractor<List<TreatmentHistoryEntry>> {
    override fun extract(ehrPatientRecord: ProvidedPatientRecord): ExtractionResult<List<TreatmentHistoryEntry>> {
        val oncologicalTreatmentHistory = oncologicalTreatmentHistory(ehrPatientRecord)
        val oncologicalPreviousConditions = getOncologicalPreviousConditions(ehrPatientRecord)

        return ExtractionResult(
            merge(oncologicalTreatmentHistory.extracted, oncologicalPreviousConditions.extracted),
            oncologicalTreatmentHistory.evaluation
        )
    }

    private fun merge(
        oncologicalTreatmentHistory: List<TreatmentHistoryEntry>,
        oncologicalPreviousConditions: List<TreatmentHistoryEntry>
    ): List<TreatmentHistoryEntry> {
        val treatmentSignatures = oncologicalTreatmentHistory.map { Triple(it.treatments, it.startYear, it.startMonth) }.toSet()
        return oncologicalTreatmentHistory + oncologicalPreviousConditions.filter { Triple(it.treatments, it.startYear, it.startMonth) not in treatmentSignatures }
    }

    private fun getOncologicalPreviousConditions(ehrPatientRecord: ProvidedPatientRecord) =
        ehrPatientRecord.priorOtherConditions.map { ehrPreviousCondition ->
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
                            startYear = curatedTreatment.startYear ?: ehrPreviousCondition.startDate?.year,
                            startMonth = curatedTreatment.startMonth ?: ehrPreviousCondition.startDate?.monthValue,
                            treatments = curatedTreatment.treatments,
                            intents = curatedTreatment.intents,
                            treatmentHistoryDetails = TreatmentHistoryDetails(
                                stopYear = curatedTreatment.treatmentHistoryDetails?.stopYear ?: ehrPreviousCondition.endDate?.year,
                                stopMonth = curatedTreatment.treatmentHistoryDetails?.stopMonth
                                    ?: ehrPreviousCondition.endDate?.monthValue,
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
        }.fold<ExtractionResult<List<TreatmentHistoryEntry>>, ExtractionResult<List<TreatmentHistoryEntry>>>(
            ExtractionResult(emptyList(), CurationExtractionEvaluation())
        ) { acc, result ->
            ExtractionResult(acc.extracted + result.extracted, acc.evaluation + result.evaluation)
        }

    private fun oncologicalTreatmentHistory(ehrPatientRecord: ProvidedPatientRecord): ExtractionResult<List<TreatmentHistoryEntry>> =
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
                                startYear = curatedTreatment.curated?.startYear ?: ehrTreatmentHistory.startDate.year,
                                startMonth = curatedTreatment.curated?.startMonth ?: ehrTreatmentHistory.startDate.monthValue,
                                intents = ehrTreatmentHistory.intention?.let { intent -> setOf(parseIntent(intent)) },
                                treatments = curatedTreatment.curated!!.treatments,
                                treatmentHistoryDetails = TreatmentHistoryDetails(
                                    stopYear = curatedTreatment.curated.treatmentHistoryDetails?.stopYear
                                        ?: ehrTreatmentHistory.endDate?.year,
                                    stopMonth = curatedTreatment.curated.treatmentHistoryDetails?.stopMonth
                                        ?: ehrTreatmentHistory.endDate?.monthValue,
                                    stopReason = curatedTreatment.curated.treatmentHistoryDetails?.stopReason
                                        ?: ehrTreatmentHistory.stopReason?.let { stopReason -> StopReason.valueOf(stopReason) },
                                    bestResponse = curatedTreatment.curated.treatmentHistoryDetails?.bestResponse
                                        ?: ehrTreatmentHistory.response?.let { response -> TreatmentResponse.valueOf(response) },
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
        ehrTreatmentHistory: ProvidedTreatmentHistory,
        ehrPatientRecord: ProvidedPatientRecord
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