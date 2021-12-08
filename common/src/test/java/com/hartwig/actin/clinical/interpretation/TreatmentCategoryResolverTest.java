package com.hartwig.actin.clinical.interpretation;

import static org.junit.Assert.assertEquals;

import java.util.Set;

import com.google.common.collect.Sets;
import com.hartwig.actin.clinical.datamodel.TreatmentCategory;

import org.apache.logging.log4j.util.Strings;
import org.junit.Test;

public class TreatmentCategoryResolverTest {

    @Test
    public void allTreatmentCategoriesCanBeConvertedBackAndForth() {
        for (TreatmentCategory category : TreatmentCategory.values()) {
            Set<TreatmentCategory> set = Sets.newHashSet(category);
            assertEquals(set, TreatmentCategoryResolver.fromStringList(TreatmentCategoryResolver.toString(set)));
        }
    }

    @Test
    public void canConvertCategoriesToStrings() {
        assertEquals(Strings.EMPTY, TreatmentCategoryResolver.toString(Sets.newHashSet()));
        assertEquals("Chemotherapy", TreatmentCategoryResolver.toString(Sets.newHashSet(TreatmentCategory.CHEMOTHERAPY)));
        assertEquals("Antiviral therapy", TreatmentCategoryResolver.toString(Sets.newHashSet(TreatmentCategory.ANTIVIRAL_THERAPY)));

        Set<TreatmentCategory> categories = Sets.newTreeSet();
        categories.add(TreatmentCategory.CHEMOTHERAPY);
        categories.add(TreatmentCategory.RADIOTHERAPY);
        assertEquals("Chemotherapy, Radiotherapy", TreatmentCategoryResolver.toString(categories));
    }

    @Test
    public void canConvertStringsToCategories() {
        assertEquals(Sets.newHashSet(TreatmentCategory.ANTIVIRAL_THERAPY), TreatmentCategoryResolver.fromStringList("Antiviral therapy"));
    }
}