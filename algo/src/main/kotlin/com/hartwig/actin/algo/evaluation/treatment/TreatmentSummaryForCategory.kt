package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.clinical.datamodel.treatment.Treatment
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory
import com.hartwig.actin.clinical.datamodel.treatment.history.TreatmentHistoryEntry

data class TreatmentSummaryForCategory(
    val specificMatches: Set<TreatmentHistoryEntry> = emptySet(),
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

        fun createForTreatmentHistory(
            treatmentHistory: List<TreatmentHistoryEntry>,
            category: TreatmentCategory,
            classifier: (TreatmentHistoryEntry) -> Boolean? = { true }
        ): TreatmentSummaryForCategory {
            val trialMatchesAllowed = categoryAllowsTrialMatches(category)
            return treatmentHistory.map { treatmentHistoryEntry ->
                val categories = treatmentHistoryEntry.treatments().flatMap(Treatment::categories)
                val matchesCategory = categories.contains(category)
                val classification = classifier(treatmentHistoryEntry)
                TreatmentSummaryForCategory(
                    if (matchesCategory && classification == true) setOf(treatmentHistoryEntry) else emptySet(),
                    if (matchesCategory && classification == null) 1 else 0,
                    if (trialMatchesAllowed && categories.contains(TreatmentCategory.TRIAL)) 1 else 0
                )
            }.fold(TreatmentSummaryForCategory()) { acc, element -> acc + element }
        }

        fun treatmentMayMatchCategoryAsTrial(treatmentHistoryEntry: TreatmentHistoryEntry, category: TreatmentCategory): Boolean {
            return categoryAllowsTrialMatches(category) && treatmentHistoryEntry.treatments()
                .flatMap(Treatment::categories)
                .contains(TreatmentCategory.TRIAL)
        }

        private fun categoryAllowsTrialMatches(category: TreatmentCategory): Boolean {
            return !CATEGORIES_NOT_MATCHING_TRIALS.contains(category)
        }
    }
}