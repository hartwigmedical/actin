package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.clinical.datamodel.treatment.PriorTumorTreatment
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class TreatmentSummaryForCategoryTest {

    @Test
    fun shouldNotReportMatchesForEmptySummary() {
        val summary = TreatmentSummaryForCategory()
        assertThat(summary.numSpecificMatches).isEqualTo(0)
        assertThat(summary.hasSpecificMatch()).isFalse
        assertThat(summary.numApproximateMatches).isEqualTo(0)
        assertThat(summary.hasApproximateMatch()).isFalse
        assertThat(summary.numPossibleTrialMatches).isEqualTo(0)
        assertThat(summary.hasPossibleTrialMatch()).isFalse
    }

    @Test
    fun shouldReportSpecificMatches() {
        assertThat(TreatmentSummaryForCategory(numSpecificMatches = 1).hasSpecificMatch()).isTrue
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
        val summary1 = TreatmentSummaryForCategory(1, 2, 3)
        val summary2 = TreatmentSummaryForCategory(4, 5, 6)
        assertThat(summary1 + summary2).isEqualTo(TreatmentSummaryForCategory(5, 7, 9))
    }

    @Test
    fun shouldCreateEmptySummaryForEmptyTreatmentList() {
        assertThat(TreatmentSummaryForCategory.createForTreatments(emptyList(), CATEGORY_TO_MATCH))
            .isEqualTo(TreatmentSummaryForCategory())
    }

    @Test
    fun shouldCountTreatmentsMatchingCategory() {
        val treatments = listOf(
            treatmentWithCategory(CATEGORY_TO_MATCH),
            treatmentWithCategory(TreatmentCategory.SURGERY)
        )
        assertThat(TreatmentSummaryForCategory.createForTreatments(treatments, CATEGORY_TO_MATCH))
            .isEqualTo(TreatmentSummaryForCategory(1, 0, 0))
    }

    @Test
    fun shouldCountTreatmentsMatchingCategoryAndCustomClassification() {
        val treatments = listOf(
            treatmentWithCategory(CATEGORY_TO_MATCH),
            TreatmentTestFactory.builder().addCategories(CATEGORY_TO_MATCH).name("CUSTOM").build(),
            treatmentWithCategory(TreatmentCategory.SURGERY)
        )
        assertThat(
            TreatmentSummaryForCategory.createForTreatments(
                treatments,
                CATEGORY_TO_MATCH
            ) { it.name() == "CUSTOM" })
            .isEqualTo(TreatmentSummaryForCategory(1, 0, 0))
    }

    @Test
    fun shouldCountTreatmentsMatchingCategoryAndPartialMatchToCustomClassification() {
        val treatments = listOf(
            treatmentWithCategory(CATEGORY_TO_MATCH),
            TreatmentTestFactory.builder().addCategories(CATEGORY_TO_MATCH).name("CUSTOM").build(),
            treatmentWithCategory(TreatmentCategory.SURGERY)
        )
        val summary = TreatmentSummaryForCategory.createForTreatments(treatments, CATEGORY_TO_MATCH) {
            if (it.name() == "CUSTOM") null else false
        }
        assertThat(summary).isEqualTo(TreatmentSummaryForCategory(0, 1, 0))
    }

    @Test
    fun shouldCountTreatmentsWithTrialCategoryIgnoringCustomClassification() {
        val treatments = listOf(
            treatmentWithCategory(TreatmentCategory.TRIAL),
            treatmentWithCategory(TreatmentCategory.SURGERY)
        )
        assertThat(
            TreatmentSummaryForCategory.createForTreatments(
                treatments,
                CATEGORY_TO_MATCH
            ) { it.name() == "CUSTOM" })
            .isEqualTo(TreatmentSummaryForCategory(0, 0, 1))
    }

    @Test
    fun shouldAccumulateMatchCountsForMultipleTreatments() {
        val treatments = listOf(
            treatmentWithCategory(CATEGORY_TO_MATCH),
            TreatmentTestFactory.builder().addCategories(CATEGORY_TO_MATCH).name("CUSTOM").build(),
            treatmentWithCategory(TreatmentCategory.TRIAL),
            treatmentWithCategory(TreatmentCategory.SURGERY)
        )
        assertThat(TreatmentSummaryForCategory.createForTreatments(treatments, CATEGORY_TO_MATCH))
            .isEqualTo(TreatmentSummaryForCategory(2, 0, 1))
    }

    @Test
    fun shouldNotCountTrialMatchesWhenLookingForUnlikelyTrialCategories() {
        val treatments = listOf(
            treatmentWithCategory(TreatmentCategory.TRIAL),
            treatmentWithCategory(TreatmentCategory.SURGERY)
        )
        assertThat(TreatmentSummaryForCategory.createForTreatments(treatments, TreatmentCategory.TRANSPLANTATION))
            .isEqualTo(TreatmentSummaryForCategory(0, 0, 0))
    }

    @Test
    fun shouldIndicatePossibleTrialMatchForTrialTreatmentAndAllowedCategory() {
        assertThat(
            TreatmentSummaryForCategory.treatmentMayMatchCategoryAsTrial(
                treatmentWithCategory(TreatmentCategory.TRIAL),
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
                treatmentWithCategory(TreatmentCategory.TRIAL),
                TreatmentCategory.SURGERY
            )
        ).isFalse
    }

    private fun treatmentWithCategory(category: TreatmentCategory): PriorTumorTreatment =
        TreatmentTestFactory.builder().addCategories(category).build()

    companion object {
        private val CATEGORY_TO_MATCH = TreatmentCategory.CHEMOTHERAPY
    }
}