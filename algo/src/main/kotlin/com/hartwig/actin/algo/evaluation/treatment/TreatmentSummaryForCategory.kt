package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.clinical.datamodel.treatment.PriorTumorTreatment
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory

data class TreatmentSummaryForCategory(
    val numSpecificMatches: Int = 0,
    val numApproximateMatches: Int = 0,
    val numPossibleTrialMatches: Int = 0
) {

    fun hasSpecificMatch() = numSpecificMatches > 0

    fun hasApproximateMatch() = numApproximateMatches > 0

    fun hasPossibleTrialMatch() = numPossibleTrialMatches > 0

    operator fun plus(other: TreatmentSummaryForCategory): TreatmentSummaryForCategory {
        return TreatmentSummaryForCategory(
            numSpecificMatches + other.numSpecificMatches,
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

        fun createForTreatments(
            treatments: List<PriorTumorTreatment>,
            category: TreatmentCategory,
            customClassifier: (PriorTumorTreatment) -> Boolean? = { true }
        ): TreatmentSummaryForCategory {
            val trialMatchesAllowed = categoryAllowsTrialMatches(category)
            return treatments.map { treatment ->
                val matchesCategory = treatment.categories().contains(category)
                val classification = customClassifier(treatment)
                TreatmentSummaryForCategory(
                    if (matchesCategory && classification == true) 1 else 0,
                    if (matchesCategory && classification == null) 1 else 0,
                    if (trialMatchesAllowed && treatment.categories().contains(TreatmentCategory.TRIAL)) 1 else 0
                )
            }.fold(TreatmentSummaryForCategory()) { acc, element -> acc + element }
        }

        fun treatmentMayMatchCategoryAsTrial(treatment: PriorTumorTreatment, category: TreatmentCategory): Boolean {
            return categoryAllowsTrialMatches(category) && treatment.categories().contains(TreatmentCategory.TRIAL)
        }

        private fun categoryAllowsTrialMatches(category: TreatmentCategory): Boolean {
            return !CATEGORIES_NOT_MATCHING_TRIALS.contains(category)
        }
    }
}