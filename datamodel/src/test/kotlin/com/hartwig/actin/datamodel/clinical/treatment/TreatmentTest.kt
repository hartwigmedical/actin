package com.hartwig.actin.datamodel.clinical.treatment

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class TreatmentTest {

    @Test
    fun `Should display treatment names with spaces and capitalize first letter of each component`() {
        val treatment: Treatment = DrugTreatment(drugs = setOf(drug("TEST_DRUG"), drug("OTHER_DRUG")), name = "TEST_DRUG+OTHER_DRUG")
        assertThat(treatment.display()).isEqualTo("Test drug+Other drug")
    }

    @Test
    fun `Should display alternate treatment name when provided`() {
        val altName = "a+TOTALLY _different_ name"
        val treatment: Treatment = DrugTreatment(
            drugs = setOf(drug("TEST_DRUG"), drug("OTHER_DRUG")),
            name = "TEST_DRUG+OTHER_DRUG",
            displayOverride = altName
        )
        assertThat(treatment.display()).isEqualTo(altName)
    }

    private fun drug(drugName: String): Drug {
        return Drug(name = drugName, category = TreatmentCategory.CHEMOTHERAPY, drugTypes = emptySet())
    }
}