package com.hartwig.actin.clinical.nki

import com.hartwig.actin.TreatmentDatabase
import com.hartwig.actin.clinical.ExtractionResult
import com.hartwig.actin.clinical.curation.CurationCategory
import com.hartwig.actin.clinical.curation.CurationResponse
import com.hartwig.actin.clinical.curation.CurationWarning
import com.hartwig.actin.clinical.curation.extraction.ExtractionEvaluation
import com.hartwig.actin.clinical.curation.translation.TranslationDatabase
import com.hartwig.actin.clinical.datamodel.treatment.history.ImmutableTreatmentHistoryDetails
import com.hartwig.actin.clinical.datamodel.treatment.history.ImmutableTreatmentHistoryEntry
import com.hartwig.actin.clinical.datamodel.treatment.history.ImmutableTreatmentStage
import com.hartwig.actin.clinical.datamodel.treatment.history.Intent
import com.hartwig.actin.clinical.datamodel.treatment.history.StopReason
import com.hartwig.actin.clinical.datamodel.treatment.history.TreatmentHistoryEntry
import com.hartwig.actin.clinical.datamodel.treatment.history.TreatmentResponse

class EhrTreatmentHistoryExtractor(
    private val treatmentDatabase: TreatmentDatabase,
    private val intentTranslation: TranslationDatabase<String>
) : EhrExtractor<List<TreatmentHistoryEntry>> {
    override fun extract(ehrPatientRecord: EhrPatientRecord): ExtractionResult<List<TreatmentHistoryEntry>> {
        val extracted = ehrPatientRecord.treatmentHistory.map {

            val treatment = treatmentDatabase.findTreatmentByName(it.treatmentName)

            val switchToTreatments = it.modifications.map { modification ->
                val modificationTreatment = treatmentDatabase.findTreatmentByName(modification.name)
                modificationTreatment?.let { t ->
                    Pair(
                        ImmutableTreatmentStage.builder()
                            .treatment(t)
                            .cycles(modification.administeredCycles)
                            .startYear(modification.date.year)
                            .startMonth(modification.date.monthValue)
                            .build(), emptySet()
                    )
                } ?: Pair(
                    null,
                    setOf(
                        CurationWarning(
                            ehrPatientRecord.patientDetails.patientId,
                            CurationCategory.ONCOLOGICAL_HISTORY,
                            modification.name,
                            "Treatment ${modification.name} not found in database"
                        )
                    )
                )
            }
            val history = ImmutableTreatmentHistoryDetails.builder()
                .stopYear(it.endDate.year)
                .stopMonth(it.endDate.monthValue)
                .stopReason(StopReason.createFromString(it.stopReason))
                .bestResponse(TreatmentResponse.createFromString(it.response))
                .switchToTreatments(switchToTreatments.mapNotNull { switch -> switch.first }.toSet())
                .cycles(it.administeredCycles)
                .build()
            val translatedIntent = CurationResponse.createFromTranslation(
                intentTranslation.find(it.intention),
                ehrPatientRecord.patientDetails.patientId,
                CurationCategory.ONCOLOGICAL_HISTORY,
                it.intention,
                "treatment intention"
            )
            Pair(
                ImmutableTreatmentHistoryEntry.builder().startYear(it.startDate.year)
                    .startMonth(it.startDate.monthValue).intents(listOf(Intent.valueOf(it.intention)))
                    .treatments(listOf(treatment))
                    .treatmentHistoryDetails(history)
                    .isTrial(it.administeredInStudy)
                    .build(), switchToTreatments.map { switch -> switch.second }.flatten().toSet()
            )
        }
        return ExtractionResult(extracted.map { it.first }, ExtractionEvaluation(warnings = extracted.map { it.second }.flatten().toSet()))
    }
}