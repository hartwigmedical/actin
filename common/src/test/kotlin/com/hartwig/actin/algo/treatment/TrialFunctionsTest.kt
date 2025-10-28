package com.hartwig.actin.algo.treatment

import com.hartwig.actin.algo.evaluation.treatment.TrialFunctions
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory
import com.hartwig.actin.datamodel.clinical.treatment.DrugType
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import com.hartwig.actin.datamodel.clinical.treatment.history.TreatmentHistoryEntry
import org.assertj.core.api.Assertions
import org.junit.Test

class TrialFunctionsTest {
    private val treatmentEntryWithNoCategory =
        TreatmentTestFactory.treatmentHistoryEntry(setOf(TreatmentTestFactory.treatment("", true, emptySet(), emptySet())), isTrial = true)

    @Test
    fun `Should indicate possible trial match for trial treatment with matching category and no types`() {
        Assertions.assertThat(
            TrialFunctions.treatmentMayMatchAsTrial(
                treatmentEntryWithCategory(TreatmentCategory.CHEMOTHERAPY, isTrial = true), setOf(TreatmentCategory.CHEMOTHERAPY)
            )
        ).isTrue
    }

    @Test
    fun `Should indicate possible trial match for trial treatment with no category and no types for likely trial category`() {
        Assertions.assertThat(TrialFunctions.treatmentMayMatchAsTrial(treatmentEntryWithNoCategory, setOf(TreatmentCategory.CHEMOTHERAPY))).isTrue
    }

    @Test
    fun `Should indicate possible trial match for trial entry when any treatment may match criteria`() {
        Assertions.assertThat(
            TrialFunctions.treatmentMayMatchAsTrial(
                TreatmentTestFactory.treatmentHistoryEntry(
                    setOf(
                        TreatmentTestFactory.drugTreatment("", TreatmentCategory.CHEMOTHERAPY, setOf(DrugType.ANTHRACYCLINE)),
                        TreatmentTestFactory.drugTreatment("", TreatmentCategory.TARGETED_THERAPY),
                        TreatmentTestFactory.treatment("", true, emptySet(), emptySet())
                    ),
                    isTrial = true
                ),
                setOf(TreatmentCategory.CHEMOTHERAPY)
            )
        ).isTrue
    }

    @Test
    fun `Should not indicate possible trial match for trial treatment and matching category when types are known`() {
        Assertions.assertThat(
            TrialFunctions.treatmentMayMatchAsTrial(
                TreatmentTestFactory.treatmentHistoryEntry(
                    setOf(TreatmentTestFactory.drugTreatment("", TreatmentCategory.CHEMOTHERAPY, setOf(DrugType.ANTHRACYCLINE))),
                    isTrial = true
                ),
                setOf(TreatmentCategory.CHEMOTHERAPY)
            )
        ).isFalse
    }

    @Test
    fun `Should not indicate possible trial match for trial treatment and different category`() {
        Assertions.assertThat(
            TrialFunctions.treatmentMayMatchAsTrial(
                treatmentEntryWithCategory(TreatmentCategory.TARGETED_THERAPY, isTrial = true), setOf(TreatmentCategory.CHEMOTHERAPY)
            )
        ).isFalse
    }

    @Test
    fun `Should not indicate possible trial match for non-trial treatment and matching category`() {
        Assertions.assertThat(
            TrialFunctions.treatmentMayMatchAsTrial(
                treatmentEntryWithCategory(TreatmentCategory.CHEMOTHERAPY), setOf(TreatmentCategory.CHEMOTHERAPY)
            )
        ).isFalse
    }

    @Test
    fun `Should not indicate possible trial match for trial treatment and unlikely trial category`() {
        Assertions.assertThat(TrialFunctions.treatmentMayMatchAsTrial(treatmentEntryWithNoCategory, setOf(TreatmentCategory.SURGERY))).isFalse
        Assertions.assertThat(
            TrialFunctions.treatmentMayMatchAsTrial(
                treatmentEntryWithCategory(TreatmentCategory.SURGERY), setOf(TreatmentCategory.SURGERY)
            )
        ).isFalse
    }

    private fun treatmentEntryWithCategory(category: TreatmentCategory, isTrial: Boolean = false): TreatmentHistoryEntry =
        TreatmentTestFactory.treatmentHistoryEntry(setOf(TreatmentTestFactory.drugTreatment("", category)), isTrial = isTrial)
}