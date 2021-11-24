package com.hartwig.actin.clinical.util;

import static org.junit.Assert.assertEquals;

import java.util.Set;

import com.google.common.collect.Sets;
import com.hartwig.actin.clinical.datamodel.TreatmentCategory;

import org.apache.logging.log4j.util.Strings;
import org.junit.Test;

public class TreatmentCategoryDisplayTest {

    @Test
    public void allTreatmentCategoriesCanBeConvertedBackAndForth() {
        for (TreatmentCategory category : TreatmentCategory.values()) {
            Set<TreatmentCategory> set = Sets.newHashSet(category);
            assertEquals(set, TreatmentCategoryDisplay.fromString(TreatmentCategoryDisplay.toString(set)));
        }
    }

    @Test
    public void canConvertCategoriesToStrings() {
        assertEquals(Strings.EMPTY, TreatmentCategoryDisplay.toString(Sets.newHashSet()));
        assertEquals("Chemotherapy", TreatmentCategoryDisplay.toString(Sets.newHashSet(TreatmentCategory.CHEMOTHERAPY)));
        assertEquals("Antiviral therapy", TreatmentCategoryDisplay.toString(Sets.newHashSet(TreatmentCategory.ANTIVIRAL_THERAPY)));
        assertEquals("Chemotherapy, Radiotherapy",
                TreatmentCategoryDisplay.toString(Sets.newHashSet(TreatmentCategory.CHEMOTHERAPY, TreatmentCategory.RADIOTHERAPY)));
    }

    @Test
    public void canConvertStringsToCategories() {
        assertEquals(Sets.newHashSet(TreatmentCategory.ANTIVIRAL_THERAPY), TreatmentCategoryDisplay.fromString("Antiviral therapy"));
    }
}