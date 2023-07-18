package com.hartwig.actin.treatment.input.datamodel;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.util.Set;

import com.hartwig.actin.clinical.datamodel.treatment.DrugType;
import com.hartwig.actin.clinical.datamodel.treatment.OtherTreatmentType;
import com.hartwig.actin.clinical.datamodel.treatment.RadiotherapyType;
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory;
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentType;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class TreatmentCategoryInputTest {

    @Test
    public void shouldCreateForAllCategories() {
        for (TreatmentCategory category : TreatmentCategory.values()) {
            assertThat(TreatmentCategoryInput.fromString(category.display()).mappedCategory()).isEqualTo(category);
        }
    }

    @Test
    public void shouldCreateForAllDrugTypes() {
        assertCreationFromEnumStrings(DrugType.values());
    }

    @Test
    public void shouldCreateForAllRadiotherapyTypes() {
        assertCreationFromEnumStrings(RadiotherapyType.values());
    }

    @Test
    public void shouldCreateForAllOtherTreatmentTypes() {
        assertCreationFromEnumStrings(OtherTreatmentType.values());
    }

    private static void assertCreationFromEnumStrings(@NotNull TreatmentType[] values) {
        for (TreatmentType treatmentType : values) {
            TreatmentCategoryInput treatmentCategoryInput = TreatmentCategoryInput.fromString(treatmentType.display());
            assertThat(treatmentCategoryInput.mappedTypes()).isEqualTo(Set.of(treatmentType));
            assertThat(treatmentCategoryInput.mappedCategory()).isEqualTo(treatmentType.category());
            assertThat(TreatmentCategoryInput.treatmentTypeFromString(treatmentType.display())).isEqualTo(treatmentType);
        }
    }
}