package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory
import com.hartwig.actin.clinical.datamodel.treatment.history.TreatmentHistoryEntry

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
            classifier: (TreatmentHistoryEntry) -> Boolean? = { true }
        ): TreatmentSummaryForCategory {
            val trialMatchesAllowed = TrialFunctions.categoryAllowsTrialMatches(category)
            return treatmentHistory.map { treatmentHistoryEntry ->
                val matchesCategory = treatmentHistoryEntry.categories().contains(category)
                val classification = classifier(treatmentHistoryEntry)
                TreatmentSummaryForCategory(
                    if (matchesCategory && classification == true) listOf(treatmentHistoryEntry) else emptyList(),
                    if (matchesCategory && classification == null) 1 else 0,
                    if (trialMatchesAllowed && treatmentHistoryEntry.isTrial && (!matchesCategory || classification == false)) 1 else 0
                )
            }.fold(TreatmentSummaryForCategory()) { acc, element -> acc + element }
        }
    }
}