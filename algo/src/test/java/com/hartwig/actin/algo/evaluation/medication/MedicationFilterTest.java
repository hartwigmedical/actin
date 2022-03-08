package com.hartwig.actin.algo.evaluation.medication;

import static org.junit.Assert.assertEquals;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.clinical.datamodel.Medication;

import org.junit.Test;

public class MedicationFilterTest {

    @Test
    public void canFilterForActive() {
        List<Medication> medications = Lists.newArrayList();

        medications.add(MedicationTestFactory.builder().active(null).name("no active").build());
        medications.add(MedicationTestFactory.builder().active(false).name("active false").build());
        medications.add(MedicationTestFactory.builder().active(true).name("active").build());

        List<Medication> filtered = MedicationFilter.active(medications);

        assertEquals(1, filtered.size());
        assertEquals("active", filtered.get(0).name());
    }

    @Test
    public void canFilterOnExactCategory() {
        List<Medication> medications = Lists.newArrayList();

        medications.add(MedicationTestFactory.active().name("no categories").build());
        medications.add(MedicationTestFactory.active().name("wrong categories").addCategories("wrong category 1").build());
        medications.add(MedicationTestFactory.active().name("right categories").addCategories("category 1", "category 2").build());

        List<Medication> filtered = MedicationFilter.withExactCategory(medications, "Category 1");

        assertEquals(1, filtered.size());
        assertEquals("right categories", filtered.get(0).name());
    }
}