package com.hartwig.actin.clinical.curation.extraction

import com.hartwig.actin.clinical.curation.CurationWarning
import com.hartwig.actin.clinical.curation.translation.LaboratoryIdentifiers
import com.hartwig.actin.clinical.curation.translation.Translation

data class CurationExtractionEvaluation(
    val warnings: Set<CurationWarning> = emptySet(),
    val primaryTumorEvaluatedInputs: Set<String> = emptySet(),
    val treatmentHistoryEntryEvaluatedInputs: Set<String> = emptySet(),
    val secondPrimaryEvaluatedInputs: Set<String> = emptySet(),
    val lesionLocationEvaluatedInputs: Set<String> = emptySet(),
    val comorbidityEvaluatedInputs: Set<String> = emptySet(),
    val ecgEvaluatedInputs: Set<String> = emptySet(),
    val infectionEvaluatedInputs: Set<String> = emptySet(),
    val periodBetweenUnitEvaluatedInputs: Set<String> = emptySet(),
    val molecularTestEvaluatedInputs: Set<String> = emptySet(),
    val sequencingTestEvaluatedInputs: Set<String> = emptySet(),
    val medicationNameEvaluatedInputs: Set<String> = emptySet(),
    val medicationDosageEvaluatedInputs: Set<String> = emptySet(),
    val administrationRouteEvaluatedInputs: Set<Translation<String>> = emptySet(),
    val laboratoryEvaluatedInputs: Set<Translation<LaboratoryIdentifiers>> = emptySet(),
    val toxicityTranslationEvaluatedInputs: Set<Translation<String>> = emptySet(),
    val dosageUnitEvaluatedInputs: Set<Translation<String>> = emptySet(),
    val surgeryTranslationEvaluatedInputs: Set<String> = emptySet()
) {
    operator fun plus(other: CurationExtractionEvaluation?): CurationExtractionEvaluation {
        return if (other == null) this else CurationExtractionEvaluation(
            warnings = warnings + other.warnings,
            primaryTumorEvaluatedInputs = primaryTumorEvaluatedInputs + other.primaryTumorEvaluatedInputs,
            treatmentHistoryEntryEvaluatedInputs = treatmentHistoryEntryEvaluatedInputs + other.treatmentHistoryEntryEvaluatedInputs,
            secondPrimaryEvaluatedInputs = secondPrimaryEvaluatedInputs + other.secondPrimaryEvaluatedInputs,
            lesionLocationEvaluatedInputs = lesionLocationEvaluatedInputs + other.lesionLocationEvaluatedInputs,
            comorbidityEvaluatedInputs = comorbidityEvaluatedInputs + other.comorbidityEvaluatedInputs,
            ecgEvaluatedInputs = ecgEvaluatedInputs + other.ecgEvaluatedInputs,
            infectionEvaluatedInputs = infectionEvaluatedInputs + other.infectionEvaluatedInputs,
            periodBetweenUnitEvaluatedInputs = periodBetweenUnitEvaluatedInputs + other.periodBetweenUnitEvaluatedInputs,
            molecularTestEvaluatedInputs = molecularTestEvaluatedInputs + other.molecularTestEvaluatedInputs,
            sequencingTestEvaluatedInputs = sequencingTestEvaluatedInputs + other.sequencingTestEvaluatedInputs,
            medicationNameEvaluatedInputs = medicationNameEvaluatedInputs + other.medicationNameEvaluatedInputs,
            medicationDosageEvaluatedInputs = medicationDosageEvaluatedInputs + other.medicationDosageEvaluatedInputs,
            administrationRouteEvaluatedInputs = administrationRouteEvaluatedInputs + other.administrationRouteEvaluatedInputs,
            laboratoryEvaluatedInputs = laboratoryEvaluatedInputs + other.laboratoryEvaluatedInputs,
            toxicityTranslationEvaluatedInputs = toxicityTranslationEvaluatedInputs + other.toxicityTranslationEvaluatedInputs,
            dosageUnitEvaluatedInputs = dosageUnitEvaluatedInputs + other.dosageUnitEvaluatedInputs,
            surgeryTranslationEvaluatedInputs = surgeryTranslationEvaluatedInputs + other.surgeryTranslationEvaluatedInputs
        )
    }
}