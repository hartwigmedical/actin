package com.hartwig.actin.treatment.input.datamodel;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class TreatmentTypeCategoryTest {

    @Test
    public void canConvertAllTreatmentTypeCategories() {
        for (TreatmentTypeCategory category : TreatmentTypeCategory.values()) {
            assertEquals(category, TreatmentTypeCategory.fromString(category.display()));
        }
    }

    @Test
    public void everyTreatmentTypeCategoryHasMappedCategoryOrNames() {
        for (TreatmentTypeCategory category : TreatmentTypeCategory.values()) {
            assertTrue(category.mappedCategory() != null || category.mappedNames() != null);
        }
    }
}