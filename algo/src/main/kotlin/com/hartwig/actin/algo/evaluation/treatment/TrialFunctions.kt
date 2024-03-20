package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.clinical.datamodel.treatment.Treatment
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory
import com.hartwig.actin.clinical.datamodel.treatment.history.TreatmentHistoryEntry

object TrialFunctions {
    private val CATEGORIES_NOT_MATCHING_TRIALS = setOf(
        TreatmentCategory.TRANSPLANTATION,
        TreatmentCategory.RADIOTHERAPY,
        TreatmentCategory.SUPPORTIVE_TREATMENT,
        TreatmentCategory.SURGERY
    )

    fun treatmentMayMatchAsTrial(
        treatmentHistoryEntry: TreatmentHistoryEntry,
        category: TreatmentCategory,
        treatmentCannotBeMatchedSpecifically: (Treatment) -> Boolean = { it.types().isEmpty() }
    ): Boolean {
        return categoryAllowsTrialMatches(category) && treatmentHistoryEntry.isTrial && treatmentHistoryEntry.allTreatments().any {
            (it.categories().isEmpty() || category in it.categories()) && treatmentCannotBeMatchedSpecifically(it)
        }
    }

    fun categoryAllowsTrialMatches(category: TreatmentCategory): Boolean {
        return !CATEGORIES_NOT_MATCHING_TRIALS.contains(category)
    }
}