package com.hartwig.actin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import java.io.IOException;
import java.util.Set;

import com.google.common.io.Resources;
import com.hartwig.actin.clinical.datamodel.treatment.Drug;
import com.hartwig.actin.clinical.datamodel.treatment.DrugType;
import com.hartwig.actin.clinical.datamodel.treatment.Therapy;
import com.hartwig.actin.clinical.datamodel.treatment.Treatment;
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class TreatmentDatabaseTest {
    @Test
    public void shouldFindExistingTreatmentByName() throws IOException {
        assertThat(treatmentDatabase().findTreatmentByName("nonexistent")).isNull();

        Treatment treatment = treatmentDatabase().findTreatmentByName("Capecitabine+Oxaliplatin");
        assertThat(treatment).isNotNull();
        assertThat(treatment.categories()).containsExactly(TreatmentCategory.CHEMOTHERAPY);
        assertThat(treatment.isSystemic()).isTrue();
        assertThat(((Therapy) treatment).drugs()).extracting(Drug::name, Drug::drugTypes)
                .containsExactlyInAnyOrder(tuple("CAPECITABINE", Set.of(DrugType.ANTIMETABOLITE)),
                        tuple("OXALIPLATIN", Set.of(DrugType.PLATINUM_COMPOUND)));
    }

    @Test
    public void shouldFindExistingDrugByName() throws IOException {
        assertThat(treatmentDatabase().findDrugByName("nonexistent")).isNull();
        Drug drug = treatmentDatabase().findDrugByName("Capecitabine");
        assertThat(drug).isNotNull();
        assertThat(drug.name()).isEqualTo("CAPECITABINE");
        assertThat(drug.drugTypes()).containsExactly(DrugType.ANTIMETABOLITE);
    }

    @NotNull
    private static TreatmentDatabase treatmentDatabase() throws IOException {
        return TreatmentDatabaseFactory.createFromPath(Resources.getResource("clinical").getPath());
    }
}