package com.hartwig.actin.clinical.datamodel.treatment

import org.assertj.core.api.Assertions
import org.junit.Test

class TreatmentTest {
    @Test
    fun shouldDisplayTreatmentNamesWithSpacesAndCapitalizeFirstLetterOfEachComponent() {
        val treatment: Treatment =
            ImmutableDrugTreatment.builder().addDrugs(drug("TEST_DRUG"), drug("OTHER_DRUG")).name("TEST_DRUG+OTHER_DRUG").build()
        Assertions.assertThat(treatment.display()).isEqualTo("Test drug+Other drug")
    }

    @Test
    fun shouldDisplayAlternateTreatmentNameWhenProvided() {
        val altName = "a+TOTALLY _different_ name"
        val treatment: Treatment = ImmutableDrugTreatment.builder()
            .addDrugs(drug("TEST_DRUG"), drug("OTHER_DRUG"))
            .name("TEST_DRUG+OTHER_DRUG")
            .displayOverride(altName)
            .build()
        Assertions.assertThat(treatment.display()).isEqualTo(altName)
    }

    companion object {
        private fun drug(drugName: String): ImmutableDrug {
            return ImmutableDrug.builder().name(drugName).category(TreatmentCategory.CHEMOTHERAPY).drugTypes(emptySet()).build()
        }
    }
}