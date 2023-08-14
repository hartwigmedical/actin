package com.hartwig.actin.treatment.input.datamodel;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

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
            String input = treatmentType.toString().replace("_", " ").toLowerCase();
            TreatmentCategoryInput treatmentCategoryInput = TreatmentCategoryInput.fromString(input);
           
            assertThat(treatmentCategoryInput.mappedType()).isEqualTo(treatmentType);
            assertThat(treatmentCategoryInput.mappedCategory()).isEqualTo(treatmentType.category());
            assertThat(TreatmentCategoryInput.treatmentTypeFromString(input)).isEqualTo(treatmentType);
        }
    }
}