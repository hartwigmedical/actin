package com.hartwig.actin.clinical.curation.extraction

import com.hartwig.actin.clinical.curation.CurationWarning
import com.hartwig.actin.clinical.curation.translation.LaboratoryTranslation
import com.hartwig.actin.clinical.curation.translation.Translation

data class ExtractionEvaluation(
    val warnings: Set<CurationWarning> = emptySet(),
    val primaryTumorEvaluatedInputs: Set<String> = emptySet(),
    val treatmentHistoryEntryEvaluatedInputs: Set<String> = emptySet(),
    val secondPrimaryEvaluatedInputs: Set<String> = emptySet(),
    val lesionLocationEvaluatedInputs: Set<String> = emptySet(),
    val nonOncologicalHistoryEvaluatedInputs: Set<String> = emptySet(),
    val ecgEvaluatedInputs: Set<String> = emptySet(),
    val infectionEvaluatedInputs: Set<String> = emptySet(),
    val periodBetweenUnitEvaluatedInputs: Set<String> = emptySet(),
    val complicationEvaluatedInputs: Set<String> = emptySet(),
    val toxicityEvaluatedInputs: Set<String> = emptySet(),
    val molecularTestEvaluatedInputs: Set<String> = emptySet(),
    val medicationNameEvaluatedInputs: Set<String> = emptySet(),
    val medicationDosageEvaluatedInputs: Set<String> = emptySet(),
    val intoleranceEvaluatedInputs: Set<String> = emptySet(),
    val administrationRouteEvaluatedInputs: Set<Translation> = emptySet(),
    val laboratoryEvaluatedInputs: Set<LaboratoryTranslation> = emptySet(),
    val toxicityTranslationEvaluatedInputs: Set<Translation> = emptySet(),
    val dosageUnitEvaluatedInputs: Set<Translation> = emptySet()
) {
    operator fun plus(other: ExtractionEvaluation?): ExtractionEvaluation {
        return if (other == null) this else ExtractionEvaluation(
            warnings = warnings + other.warnings,
            primaryTumorEvaluatedInputs = primaryTumorEvaluatedInputs + other.primaryTumorEvaluatedInputs,
            treatmentHistoryEntryEvaluatedInputs = treatmentHistoryEntryEvaluatedInputs + other.treatmentHistoryEntryEvaluatedInputs,
            secondPrimaryEvaluatedInputs = secondPrimaryEvaluatedInputs + other.secondPrimaryEvaluatedInputs,
            lesionLocationEvaluatedInputs = lesionLocationEvaluatedInputs + other.lesionLocationEvaluatedInputs,
            nonOncologicalHistoryEvaluatedInputs = nonOncologicalHistoryEvaluatedInputs + other.nonOncologicalHistoryEvaluatedInputs,
            ecgEvaluatedInputs = ecgEvaluatedInputs + other.ecgEvaluatedInputs,
            infectionEvaluatedInputs = infectionEvaluatedInputs + other.infectionEvaluatedInputs,
            periodBetweenUnitEvaluatedInputs = periodBetweenUnitEvaluatedInputs + other.periodBetweenUnitEvaluatedInputs,
            complicationEvaluatedInputs = complicationEvaluatedInputs + other.complicationEvaluatedInputs,
            toxicityEvaluatedInputs = toxicityEvaluatedInputs + other.toxicityEvaluatedInputs,
            molecularTestEvaluatedInputs = molecularTestEvaluatedInputs + other.molecularTestEvaluatedInputs,
            medicationNameEvaluatedInputs = medicationNameEvaluatedInputs + other.medicationNameEvaluatedInputs,
            medicationDosageEvaluatedInputs = medicationDosageEvaluatedInputs + other.medicationDosageEvaluatedInputs,
            intoleranceEvaluatedInputs = intoleranceEvaluatedInputs + other.intoleranceEvaluatedInputs,
            administrationRouteEvaluatedInputs = administrationRouteEvaluatedInputs + other.administrationRouteEvaluatedInputs,
            laboratoryEvaluatedInputs = laboratoryEvaluatedInputs + other.laboratoryEvaluatedInputs,
            toxicityTranslationEvaluatedInputs = toxicityTranslationEvaluatedInputs + other.toxicityTranslationEvaluatedInputs,
            dosageUnitEvaluatedInputs = dosageUnitEvaluatedInputs + other.dosageUnitEvaluatedInputs
        )
    }
}
