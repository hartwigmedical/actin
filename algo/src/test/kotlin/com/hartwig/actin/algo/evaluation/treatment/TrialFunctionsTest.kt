package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.drugTreatment
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.treatment
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.treatmentHistoryEntry
import com.hartwig.actin.datamodel.clinical.treatment.DrugType
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import com.hartwig.actin.datamodel.clinical.treatment.history.TreatmentHistoryEntry
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class TrialFunctionsTest {
    private val treatmentEntryWithNoCategory = treatmentHistoryEntry(setOf(treatment("", true, emptySet(), emptySet())), isTrial = true)

    @Test
    fun `Should indicate possible trial match for trial treatment with matching category and no types`() {
        assertThat(
            TrialFunctions.treatmentMayMatchAsTrial(
                treatmentEntryWithCategory(TreatmentCategory.CHEMOTHERAPY, isTrial = true), TreatmentCategory.CHEMOTHERAPY
            )
        ).isTrue
    }

    @Test
    fun `Should indicate possible trial match for trial treatment with no category and no types for likely trial category`() {
        assertThat(TrialFunctions.treatmentMayMatchAsTrial(treatmentEntryWithNoCategory, TreatmentCategory.CHEMOTHERAPY)).isTrue
    }

    @Test
    fun `Should indicate possible trial match for trial entry when any treatment may match criteria`() {
        assertThat(
            TrialFunctions.treatmentMayMatchAsTrial(
                treatmentHistoryEntry(
                    setOf(
                        drugTreatment("", TreatmentCategory.CHEMOTHERAPY, setOf(DrugType.ANTHRACYCLINE)),
                        drugTreatment("", TreatmentCategory.TARGETED_THERAPY),
                        treatment("", true, emptySet(), emptySet())
                    ),
                    isTrial = true
                ),
                TreatmentCategory.CHEMOTHERAPY
            )
        ).isTrue
    }

    @Test
    fun `Should not indicate possible trial match for trial treatment and matching category when types are known`() {
        assertThat(
            TrialFunctions.treatmentMayMatchAsTrial(
                treatmentHistoryEntry(
                    setOf(drugTreatment("", TreatmentCategory.CHEMOTHERAPY, setOf(DrugType.ANTHRACYCLINE))), isTrial = true
                ),
                TreatmentCategory.CHEMOTHERAPY
            )
        ).isFalse
    }

    @Test
    fun `Should not indicate possible trial match for trial treatment and different category`() {
        assertThat(
            TrialFunctions.treatmentMayMatchAsTrial(
                treatmentEntryWithCategory(TreatmentCategory.TARGETED_THERAPY, isTrial = true), TreatmentCategory.CHEMOTHERAPY
            )
        ).isFalse
    }

    @Test
    fun `Should not indicate possible trial match for non-trial treatment and matching category`() {
        assertThat(
            TrialFunctions.treatmentMayMatchAsTrial(
                treatmentEntryWithCategory(TreatmentCategory.CHEMOTHERAPY), TreatmentCategory.CHEMOTHERAPY
            )
        ).isFalse
    }

    @Test
    fun `Should not indicate possible trial match for trial treatment and unlikely trial category`() {
        assertThat(TrialFunctions.treatmentMayMatchAsTrial(treatmentEntryWithNoCategory, TreatmentCategory.SURGERY)).isFalse
        assertThat(
            TrialFunctions.treatmentMayMatchAsTrial(
                treatmentEntryWithCategory(TreatmentCategory.SURGERY), TreatmentCategory.SURGERY
            )
        ).isFalse
    }

    private fun treatmentEntryWithCategory(category: TreatmentCategory, isTrial: Boolean = false): TreatmentHistoryEntry =
        treatmentHistoryEntry(setOf(drugTreatment("", category)), isTrial = isTrial)
}