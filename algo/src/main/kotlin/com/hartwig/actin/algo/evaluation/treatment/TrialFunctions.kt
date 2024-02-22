package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory
import com.hartwig.actin.clinical.datamodel.treatment.history.TreatmentHistoryEntry

object TrialFunctions {
    private val CATEGORIES_NOT_MATCHING_TRIALS = setOf(
        TreatmentCategory.TRANSPLANTATION,
        TreatmentCategory.CAR_T,
        TreatmentCategory.TCR_T,
        TreatmentCategory.GENE_THERAPY,
        TreatmentCategory.PROPHYLACTIC_TREATMENT,
        TreatmentCategory.RADIOTHERAPY,
        TreatmentCategory.ANTIVIRAL_THERAPY,
        TreatmentCategory.SUPPORTIVE_TREATMENT,
        TreatmentCategory.SURGERY
    )

    fun treatmentMayMatchCategoryAsTrial(treatmentHistoryEntry: TreatmentHistoryEntry, category: TreatmentCategory): Boolean {
        return categoryAllowsTrialMatches(category) && treatmentHistoryEntry.isTrial
    }

    fun categoryAllowsTrialMatches(category: TreatmentCategory): Boolean {
        return !CATEGORIES_NOT_MATCHING_TRIALS.contains(category)
    }
}