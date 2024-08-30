package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.datamodel.clinical.treatment.Treatment
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import com.hartwig.actin.datamodel.clinical.treatment.history.TreatmentHistoryEntry

data class TreatmentSummaryForCategory(
    val specificMatches: List<TreatmentHistoryEntry> = emptyList(),
    val numApproximateMatches: Int = 0,
    val numPossibleTrialMatches: Int = 0
) {

    fun numSpecificMatches() = specificMatches.size

    fun hasSpecificMatch() = specificMatches.isNotEmpty()

    fun hasApproximateMatch() = numApproximateMatches > 0

    fun hasPossibleTrialMatch() = numPossibleTrialMatches > 0

    operator fun plus(other: TreatmentSummaryForCategory): TreatmentSummaryForCategory {
        return TreatmentSummaryForCategory(
            specificMatches + other.specificMatches,
            numApproximateMatches + other.numApproximateMatches,
            numPossibleTrialMatches + other.numPossibleTrialMatches
        )
    }

    companion object {
        fun createForTreatmentHistory(
            treatmentHistory: List<TreatmentHistoryEntry>,
            category: TreatmentCategory,
            classifier: (TreatmentHistoryEntry) -> Boolean? = { true },
            treatmentEligibleToMatchTrials: (Treatment) -> Boolean = { it.types().isEmpty() },
            treatmentHistoryEntryMayMatchTrials: (TreatmentHistoryEntry) -> Boolean = { true }
        ): TreatmentSummaryForCategory {
            return treatmentHistory.map { treatmentHistoryEntry ->
                val matchesCategory = treatmentHistoryEntry.categories().contains(category)
                val classification = classifier(treatmentHistoryEntry)
                when {
                    matchesCategory && classification == true -> TreatmentSummaryForCategory(listOf(treatmentHistoryEntry))
                    matchesCategory && classification == null -> TreatmentSummaryForCategory(numApproximateMatches = 1)
                    else -> {
                        val mayMatchAsTrial = TrialFunctions.treatmentMayMatchAsTrial(
                            treatmentHistoryEntry, category, treatmentEligibleToMatchTrials
                        ) && treatmentHistoryEntryMayMatchTrials(treatmentHistoryEntry)

                        TreatmentSummaryForCategory(numPossibleTrialMatches = if (mayMatchAsTrial) 1 else 0)
                    }
                }
            }.fold(TreatmentSummaryForCategory(), TreatmentSummaryForCategory::plus)
        }
    }
}