package com.hartwig.actin.clinical.feed.external

import com.fasterxml.jackson.databind.ObjectMapper
import com.hartwig.actin.TreatmentDatabase
import com.hartwig.actin.clinical.ExtractionResult
import com.hartwig.actin.clinical.curation.extraction.CurationExtractionEvaluation
import com.hartwig.actin.clinical.datamodel.treatment.DrugTreatment
import com.hartwig.actin.clinical.datamodel.treatment.OtherTreatment
import com.hartwig.actin.clinical.datamodel.treatment.Radiotherapy
import com.hartwig.actin.clinical.datamodel.treatment.history.Intent
import com.hartwig.actin.clinical.datamodel.treatment.history.StopReason
import com.hartwig.actin.clinical.datamodel.treatment.history.TreatmentHistoryDetails
import com.hartwig.actin.clinical.datamodel.treatment.history.TreatmentHistoryEntry
import com.hartwig.actin.clinical.datamodel.treatment.history.TreatmentResponse
import com.hartwig.actin.clinical.datamodel.treatment.history.TreatmentStage

class EhrTreatmentHistoryExtractor(
    private val treatmentDatabase: TreatmentDatabase
) : EhrExtractor<List<TreatmentHistoryEntry>> {
    private val json = ObjectMapper()
    override fun extract(ehrPatientRecord: EhrPatientRecord): ExtractionResult<List<TreatmentHistoryEntry>> {
        val extracted = ehrPatientRecord.treatmentHistory.map {

            val treatment = treatmentDatabase.findTreatmentByName(it.treatmentName) ?: throw missingTreatmentException(it.treatmentName)

            val switchToTreatments = it.modifications?.map { modification ->
                TreatmentStage(
                    cycles = modification.administeredCycles,
                    startYear = modification.date.year,
                    startMonth = modification.date.monthValue,
                    treatment = treatment
                )
            }
            val historyDetails =
                TreatmentHistoryDetails(
                    stopYear = it.endDate?.year,
                    stopMonth = it.endDate?.monthValue,
                    stopReason = it.stopReason?.let { stopReason -> StopReason.valueOf(stopReason) },
                    bestResponse = it.response?.let { response -> TreatmentResponse.valueOf(response) },
                    switchToTreatments = switchToTreatments,
                    cycles = it.administeredCycles,
                )
            TreatmentHistoryEntry(
                startYear = it.startDate.year,
                startMonth = it.startDate.monthValue,
                intents = it.intention?.let { intent -> setOf(Intent.valueOf(intent)) },
                treatments = setOfNotNull(treatment),
                treatmentHistoryDetails = historyDetails,
                isTrial = it.administeredInStudy
            )
        }
        return ExtractionResult(extracted, CurationExtractionEvaluation())
    }

    private fun missingTreatmentException(treatment: String): IllegalArgumentException {
        return IllegalArgumentException(templates(treatment))
    }

    private fun templates(it: String) =
        "Treatment with name $it does not exist in database. Please add with one of the following templates: " + listOf(
            DrugTreatment(name = it, synonyms = emptySet(), isSystemic = false, drugs = emptySet()),
            Radiotherapy(name = it, synonyms = emptySet(), isSystemic = false),
            OtherTreatment(name = it, synonyms = emptySet(), isSystemic = false, categories = emptySet())
        ).map { templates ->
            json.writeValueAsString(templates).replace("isSystemic\":false", "isSystemic\":?")
                .replace("\"displayOverride\":null,", "")
                .replace("\"maxCycles\":null,", "")
        }
}