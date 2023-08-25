package com.hartwig.actin.clinical.datamodel.treatment;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class TreatmentTest {

    @Test
    public void shouldDisplayTreatmentNamesWithSpacesAndCapitalizeFirstLetterOfEachComponent() {
        Treatment treatment =
                ImmutableDrugTherapy.builder().addDrugs(drug("TEST_DRUG"), drug("OTHER_DRUG")).name("TEST_DRUG+OTHER_DRUG").build();
        assertThat(treatment.display()).isEqualTo("Test drug+Other drug");
    }

    @NotNull
    private static ImmutableDrug drug(String drugName) {
        return ImmutableDrug.builder().name(drugName).category(TreatmentCategory.CHEMOTHERAPY).drugTypes(Collections.emptySet()).build();
    }
}