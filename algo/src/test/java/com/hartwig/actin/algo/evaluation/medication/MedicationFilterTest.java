package com.hartwig.actin.algo.evaluation.medication;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.hartwig.actin.clinical.datamodel.Medication;

import org.jetbrains.annotations.NotNull;
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
    public void canFilterOnOneTermInName() {
        List<Medication> medications = Lists.newArrayList();

        medications.add(MedicationTestFactory.active().name("name 1").build());
        medications.add(MedicationTestFactory.active().name("name 1 with some extension").build());
        medications.add(MedicationTestFactory.active().name("name 2").build());

        List<Medication> filtered = MedicationFilter.withTermInName(medications, "Name 1");

        assertEquals(2, filtered.size());
        assertNotNull(findByName(filtered, "name 1"));
        assertNotNull(findByName(filtered, "name 1 with some extension"));
    }

    @Test
    public void canFilterOnAnyTermInName() {
        List<Medication> medications = Lists.newArrayList();

        medications.add(MedicationTestFactory.active().name("name 1").build());
        medications.add(MedicationTestFactory.active().name("name 1 with some extension").build());
        medications.add(MedicationTestFactory.active().name("name 2").build());
        medications.add(MedicationTestFactory.active().name("name 3").build());

        List<Medication> filtered = MedicationFilter.withAnyTermInName(medications, Sets.newHashSet("Name 1", "2"));

        assertEquals(3, filtered.size());
        assertNotNull(findByName(filtered, "name 1"));
        assertNotNull(findByName(filtered, "name 1 with some extension"));
        assertNotNull(findByName(filtered, "name 2"));
    }

    @Test
    public void canFilterOnOneExactCategory() {
        List<Medication> medications = Lists.newArrayList();

        medications.add(MedicationTestFactory.active().name("no categories").build());
        medications.add(MedicationTestFactory.active().name("wrong categories").addCategories("wrong category 1").build());
        medications.add(MedicationTestFactory.active().name("right categories").addCategories("category 1", "category 2").build());

        List<Medication> filtered = MedicationFilter.withExactCategory(medications, "Category 1");

        assertEquals(1, filtered.size());
        assertEquals("right categories", filtered.get(0).name());
    }

    @Test
    public void canFilterOnAnyExactCategory() {
        List<Medication> medications = Lists.newArrayList();

        medications.add(MedicationTestFactory.active().name("no categories").build());
        medications.add(MedicationTestFactory.active().name("wrong categories").addCategories("wrong category 1").build());
        medications.add(MedicationTestFactory.active().name("right category 1").addCategories("category 1", "category 2").build());
        medications.add(MedicationTestFactory.active().name("right category 2").addCategories("category 3").build());

        List<Medication> filtered = MedicationFilter.withAnyExactCategory(medications, Sets.newHashSet("Category 1", "Category 3"));

        assertEquals(2, filtered.size());
        assertNotNull(findByName(medications, "right category 1"));
        assertNotNull(findByName(medications, "right category 2"));
    }

    @NotNull
    private static Medication findByName(@NotNull List<Medication> medications, @NotNull String nameToFind) {
        for (Medication medication : medications) {
            if (medication.name().equals(nameToFind)) {
                return medication;
            }
        }

        throw new IllegalStateException("Could not find medication with name: " + nameToFind);
    }
}