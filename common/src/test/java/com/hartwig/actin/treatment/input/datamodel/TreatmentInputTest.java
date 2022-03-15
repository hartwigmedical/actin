package com.hartwig.actin.treatment.input.datamodel;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class TreatmentInputTest {

    @Test
    public void canConvertAllTreatmentTypeCategories() {
        for (TreatmentInput category : TreatmentInput.values()) {
            assertEquals(category, TreatmentInput.fromString(category.display()));
        }
    }

    @Test
    public void everyTreatmentTypeCategoryHasMappedCategoryOrNames() {
        for (TreatmentInput category : TreatmentInput.values()) {
            assertTrue(category.mappedCategory() != null || category.mappedNames() != null);
        }
    }
}