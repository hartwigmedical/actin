package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.clinical.datamodel.TreatmentTestFactory
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory
import com.hartwig.actin.clinical.datamodel.treatment.history.TreatmentHistoryEntry
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class TrialFunctionsTest {

    @Test
    fun shouldIndicatePossibleTrialMatchForTrialTreatmentAndAllowedCategory() {
        assertThat(
            TrialFunctions.treatmentMayMatchCategoryAsTrial(
                treatmentWithCategory(TreatmentCategory.TARGETED_THERAPY, isTrial = true), TreatmentCategory.CHEMOTHERAPY
            )
        ).isTrue
    }

    @Test
    fun shouldNotIndicatePossibleTrialMatchForNonTrialTreatmentAndAllowedCategory() {
        assertThat(
            TrialFunctions.treatmentMayMatchCategoryAsTrial(
                treatmentWithCategory(TreatmentCategory.TARGETED_THERAPY), TreatmentCategory.CHEMOTHERAPY
            )
        ).isFalse
    }

    @Test
    fun shouldNotIndicatePossibleTrialMatchForTrialTreatmentAndUnlikelyTrialCategory() {
        assertThat(
            TrialFunctions.treatmentMayMatchCategoryAsTrial(
                treatmentWithCategory(TreatmentCategory.TARGETED_THERAPY, isTrial = true), TreatmentCategory.SURGERY
            )
        ).isFalse
    }

    private fun treatmentWithCategory(category: TreatmentCategory, name: String = "", isTrial: Boolean = false): TreatmentHistoryEntry =
        TreatmentTestFactory.treatmentHistoryEntry(setOf(TreatmentTestFactory.drugTreatment(name, category)), isTrial = isTrial)
}