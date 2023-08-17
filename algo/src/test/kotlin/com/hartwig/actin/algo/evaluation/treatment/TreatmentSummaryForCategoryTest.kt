package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.treatment.TreatmentTestFactory.drugTherapy
import com.hartwig.actin.algo.evaluation.treatment.TreatmentTestFactory.treatment
import com.hartwig.actin.algo.evaluation.treatment.TreatmentTestFactory.treatmentHistoryEntry
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory
import com.hartwig.actin.clinical.datamodel.treatment.history.TreatmentHistoryEntry
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class TreatmentSummaryForCategoryTest {

    @Test
    fun shouldNotReportMatchesForEmptySummary() {
        val summary = TreatmentSummaryForCategory()
        assertThat(summary.numSpecificMatches()).isEqualTo(0)
        assertThat(summary.hasSpecificMatch()).isFalse
        assertThat(summary.numApproximateMatches).isEqualTo(0)
        assertThat(summary.hasApproximateMatch()).isFalse
        assertThat(summary.numPossibleTrialMatches).isEqualTo(0)
        assertThat(summary.hasPossibleTrialMatch()).isFalse
    }

    @Test
    fun shouldReportSpecificMatches() {
        assertThat(TreatmentSummaryForCategory(specificMatches = listOf(treatmentHistoryEntry())).hasSpecificMatch()).isTrue
    }

    @Test
    fun shouldReportApproximateMatches() {
        assertThat(TreatmentSummaryForCategory(numApproximateMatches = 1).hasApproximateMatch()).isTrue
    }

    @Test
    fun shouldReportPossibleTrialMatches() {
        assertThat(TreatmentSummaryForCategory(numPossibleTrialMatches = 1).hasPossibleTrialMatch()).isTrue
    }

    @Test
    fun shouldAddSummariesTogether() {
        val treatmentHistoryEntry1 = treatmentHistoryEntry(setOf(treatment("1", true)))
        val treatmentHistoryEntry2 = treatmentHistoryEntry(setOf(treatment("2", false)))
        val summary1 = TreatmentSummaryForCategory(listOf(treatmentHistoryEntry1), 2, 3)
        val summary2 = TreatmentSummaryForCategory(listOf(treatmentHistoryEntry2), 5, 6)
        assertThat(summary1 + summary2).isEqualTo(TreatmentSummaryForCategory(listOf(treatmentHistoryEntry1, treatmentHistoryEntry2), 7, 9))
    }

    @Test
    fun shouldCreateEmptySummaryForEmptyTreatmentList() {
        assertThat(TreatmentSummaryForCategory.createForTreatmentHistory(emptyList(), CATEGORY_TO_MATCH))
            .isEqualTo(TreatmentSummaryForCategory())
    }

    @Test
    fun shouldCountTreatmentsMatchingCategory() {
        val treatments = listOf(TREATMENT_HISTORY_ENTRY_MATCHING_CATEGORY, treatmentWithCategory(TreatmentCategory.SURGERY))
        assertThat(TreatmentSummaryForCategory.createForTreatmentHistory(treatments, CATEGORY_TO_MATCH))
            .isEqualTo(TreatmentSummaryForCategory(listOf(TREATMENT_HISTORY_ENTRY_MATCHING_CATEGORY), 0, 0))
    }

    @Test
    fun shouldCountTreatmentsMatchingCategoryAndCustomClassification() {
        val treatments = listOf(
            TREATMENT_HISTORY_ENTRY_MATCHING_CATEGORY,
            TREATMENT_HISTORY_ENTRY_MATCHING_CATEGORY_AND_NAME,
            treatmentWithCategory(TreatmentCategory.SURGERY)
        )
        assertThat(TreatmentSummaryForCategory.createForTreatmentHistory(treatments, CATEGORY_TO_MATCH) {
            it.treatmentName() == "CUSTOM"
        })
            .isEqualTo(TreatmentSummaryForCategory(listOf(TREATMENT_HISTORY_ENTRY_MATCHING_CATEGORY_AND_NAME), 0, 0))
    }

    @Test
    fun shouldCountTreatmentsMatchingCategoryAndPartialMatchToCustomClassification() {
        val treatments = listOf(
            TREATMENT_HISTORY_ENTRY_MATCHING_CATEGORY,
            TREATMENT_HISTORY_ENTRY_MATCHING_CATEGORY_AND_NAME,
            treatmentWithCategory(TreatmentCategory.SURGERY)
        )
        assertThat(TreatmentSummaryForCategory.createForTreatmentHistory(treatments, CATEGORY_TO_MATCH) {
            if (it.treatmentName() == "CUSTOM") null else false
        }).isEqualTo(TreatmentSummaryForCategory(emptyList(), 1, 0))
    }

    @Test
    fun shouldCountTrialTreatmentsIgnoringCustomClassification() {
        val treatments = listOf(
            treatmentWithCategory(TreatmentCategory.TARGETED_THERAPY, isTrial = true),
            treatmentWithCategory(TreatmentCategory.SURGERY)
        )
        assertThat(TreatmentSummaryForCategory.createForTreatmentHistory(treatments, CATEGORY_TO_MATCH) {
            it.treatmentName() == "CUSTOM"
        }).isEqualTo(TreatmentSummaryForCategory(emptyList(), 0, 1))
    }

    @Test
    fun shouldAccumulateMatchCountsForMultipleTreatments() {
        val treatments = listOf(
            TREATMENT_HISTORY_ENTRY_MATCHING_CATEGORY,
            TREATMENT_HISTORY_ENTRY_MATCHING_CATEGORY_AND_NAME,
            treatmentWithCategory(TreatmentCategory.TARGETED_THERAPY, isTrial = true),
            treatmentWithCategory(TreatmentCategory.SURGERY)
        )
        assertThat(TreatmentSummaryForCategory.createForTreatmentHistory(treatments, CATEGORY_TO_MATCH))
            .isEqualTo(
                TreatmentSummaryForCategory(
                    listOf(TREATMENT_HISTORY_ENTRY_MATCHING_CATEGORY, TREATMENT_HISTORY_ENTRY_MATCHING_CATEGORY_AND_NAME),
                    0,
                    1
                )
            )
    }

    @Test
    fun shouldNotCountTrialMatchesWhenLookingForUnlikelyTrialCategories() {
        val treatments = listOf(
            treatmentWithCategory(TreatmentCategory.TARGETED_THERAPY, isTrial = true),
            treatmentWithCategory(TreatmentCategory.SURGERY)
        )
        assertThat(TreatmentSummaryForCategory.createForTreatmentHistory(treatments, TreatmentCategory.TRANSPLANTATION))
            .isEqualTo(TreatmentSummaryForCategory(emptyList(), 0, 0))
    }

    @Test
    fun shouldIndicatePossibleTrialMatchForTrialTreatmentAndAllowedCategory() {
        assertThat(
            TreatmentSummaryForCategory.treatmentMayMatchCategoryAsTrial(
                treatmentWithCategory(TreatmentCategory.TARGETED_THERAPY, isTrial = true),
                TreatmentCategory.CHEMOTHERAPY
            )
        ).isTrue
    }

    @Test
    fun shouldNotIndicatePossibleTrialMatchForNonTrialTreatmentAndAllowedCategory() {
        assertThat(
            TreatmentSummaryForCategory.treatmentMayMatchCategoryAsTrial(
                treatmentWithCategory(TreatmentCategory.TARGETED_THERAPY),
                TreatmentCategory.CHEMOTHERAPY
            )
        ).isFalse
    }

    @Test
    fun shouldNotIndicatePossibleTrialMatchForTrialTreatmentAndUnlikelyTrialCategory() {
        assertThat(
            TreatmentSummaryForCategory.treatmentMayMatchCategoryAsTrial(
                treatmentWithCategory(TreatmentCategory.TARGETED_THERAPY, isTrial = true),
                TreatmentCategory.SURGERY
            )
        ).isFalse
    }

    companion object {
        private val CATEGORY_TO_MATCH = TreatmentCategory.CHEMOTHERAPY
        private val TREATMENT_HISTORY_ENTRY_MATCHING_CATEGORY = treatmentWithCategory(CATEGORY_TO_MATCH)
        private val TREATMENT_HISTORY_ENTRY_MATCHING_CATEGORY_AND_NAME = treatmentWithCategory(CATEGORY_TO_MATCH, "CUSTOM")

        private fun treatmentWithCategory(category: TreatmentCategory, name: String = "", isTrial: Boolean = false): TreatmentHistoryEntry =
            treatmentHistoryEntry(setOf(drugTherapy(name, category)), isTrial = isTrial)
    }
}