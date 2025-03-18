package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.datamodel.clinical.treatment.Treatment
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import com.hartwig.actin.datamodel.clinical.treatment.history.TreatmentHistoryEntry

object TrialFunctions {

    private val CATEGORIES_NOT_MATCHING_TRIALS = setOf(
        TreatmentCategory.TRANSPLANTATION,
        TreatmentCategory.RADIOTHERAPY,
        TreatmentCategory.SUPPORTIVE_TREATMENT,
        TreatmentCategory.SURGERY
    )

    fun treatmentMayMatchAsTrial(
        treatmentHistoryEntry: TreatmentHistoryEntry,
        categories: Iterable<TreatmentCategory>,
        treatmentCannotBeMatchedSpecifically: (Treatment) -> Boolean = { it.types().isEmpty() }
    ): Boolean {
        return categories.any(::categoryAllowsTrialMatches) && treatmentHistoryEntry.isTrial && treatmentHistoryEntry.allTreatments().any {
            (it.categories().isEmpty() || categories.intersect(it.categories()).isNotEmpty()) && treatmentCannotBeMatchedSpecifically(it)
        }
    }

    fun categoryAllowsTrialMatches(category: TreatmentCategory): Boolean {
        return !CATEGORIES_NOT_MATCHING_TRIALS.contains(category)
    }
}