package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.washout.WashoutTestFactory
import com.hartwig.actin.datamodel.clinical.treatment.Drug
import com.hartwig.actin.datamodel.clinical.treatment.DrugType
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class MedicationFunctionsTest {

    private val chemotherapyDrug = Drug(
        name = "Chemotherapy drug",
        category = TreatmentCategory.CHEMOTHERAPY,
        drugTypes = setOf(DrugType.ALKYLATING_AGENT)
    )

    private val immunotherapyDrug = Drug(
        name = "Immunotherapy drug",
        category = TreatmentCategory.IMMUNOTHERAPY,
        drugTypes = setOf(DrugType.ABL_TYROSINE_KINASE_INHIBITOR)
    )

    private val medicationWithChemotherapy = WashoutTestFactory.medication().copy(drug = chemotherapyDrug)
    private val medicationWithImmunotherapy = WashoutTestFactory.medication().copy(drug = immunotherapyDrug)

    @Test
    fun `Should return true if medication has the specified category`() {
        assertThat(MedicationFunctions.hasCategory(medicationWithChemotherapy, TreatmentCategory.CHEMOTHERAPY)).isTrue()
        assertThat(MedicationFunctions.hasCategory(medicationWithImmunotherapy, TreatmentCategory.CHEMOTHERAPY)).isFalse()
    }

    @Test
    fun `Should return true if medication has one of the specified drug types`() {
        assertThat(MedicationFunctions.hasDrugType(medicationWithChemotherapy, setOf(DrugType.ALKYLATING_AGENT))).isTrue()
        assertThat(MedicationFunctions.hasDrugType(medicationWithImmunotherapy, setOf(DrugType.ALKYLATING_AGENT))).isFalse()
    }

    @Test
    fun `Should return true if medication does not have any of the ignore types`() {
        assertThat(
            MedicationFunctions.doesNotHaveIgnoreType(
                medicationWithChemotherapy,
                setOf(DrugType.ABL_TYROSINE_KINASE_INHIBITOR)
            )
        ).isTrue()
        assertThat(
            MedicationFunctions.doesNotHaveIgnoreType(
                medicationWithImmunotherapy,
                setOf(DrugType.ABL_TYROSINE_KINASE_INHIBITOR)
            )
        ).isFalse()
    }
}