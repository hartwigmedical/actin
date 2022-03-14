package com.hartwig.actin.treatment.input.datamodel;

import static org.junit.Assert.assertEquals;

import com.hartwig.actin.treatment.input.single.TumorTypeCategory;

import org.junit.Test;

public class TumorTypeCategoryTest {

    @Test
    public void canConvertAllTumorTypeCategories() {
        for (TumorTypeCategory category : TumorTypeCategory.values()) {
            assertEquals(category, TumorTypeCategory.fromString(category.display()));
        }
    }
}