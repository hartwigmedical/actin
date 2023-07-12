package com.hartwig.actin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import java.io.IOException;
import java.util.Set;

import com.google.common.io.Resources;
import com.hartwig.actin.clinical.datamodel.treatment.Drug;
import com.hartwig.actin.clinical.datamodel.treatment.DrugClass;
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
        assertThat(((Therapy) treatment).drugs()).extracting(Drug::name, Drug::drugClasses)
                .containsExactlyInAnyOrder(tuple("Capecitabine", Set.of(DrugClass.ANTIMETABOLITE)),
                        tuple("Oxaliplatin", Set.of(DrugClass.PLATINUM_COMPOUND)));
    }

    @Test
    public void shouldFindExistingDrugByName() throws IOException {
        assertThat(treatmentDatabase().findDrugByName("nonexistent")).isNull();
        Drug drug = treatmentDatabase().findDrugByName("Capecitabine");
        assertThat(drug).isNotNull();
        assertThat(drug.name()).isEqualTo("Capecitabine");
        assertThat(drug.drugClasses()).containsExactly(DrugClass.ANTIMETABOLITE);
    }

    @NotNull
    private static TreatmentDatabase treatmentDatabase() throws IOException {
        return TreatmentDatabaseFactory.createFromPath(Resources.getResource("clinical").getPath());
    }
}