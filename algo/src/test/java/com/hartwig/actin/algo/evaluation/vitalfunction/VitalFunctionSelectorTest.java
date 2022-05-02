package com.hartwig.actin.algo.evaluation.vitalfunction;

import static org.junit.Assert.assertEquals;

import java.time.LocalDate;
import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.clinical.datamodel.ImmutableVitalFunction;
import com.hartwig.actin.clinical.datamodel.VitalFunction;
import com.hartwig.actin.clinical.datamodel.VitalFunctionCategory;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class VitalFunctionSelectorTest {

    @Test
    public void canSelectVitalFunctions() {
        List<VitalFunction> vitalFunctions = Lists.newArrayList();
        assertEquals(0, VitalFunctionSelector.select(vitalFunctions, VitalFunctionCategory.HEART_RATE, "unit1", 2).size());

        vitalFunctions.add(VitalFunctionTestFactory.vitalFunction().category(VitalFunctionCategory.HEART_RATE).unit("unit1").build());
        assertEquals(1, VitalFunctionSelector.select(vitalFunctions, VitalFunctionCategory.HEART_RATE, "unit1", 2).size());

        vitalFunctions.add(VitalFunctionTestFactory.vitalFunction().category(VitalFunctionCategory.SPO2).unit("unit1").build());
        assertEquals(1, VitalFunctionSelector.select(vitalFunctions, VitalFunctionCategory.HEART_RATE, "unit1", 2).size());

        vitalFunctions.add(VitalFunctionTestFactory.vitalFunction().category(VitalFunctionCategory.HEART_RATE).unit("unit2").build());
        assertEquals(1, VitalFunctionSelector.select(vitalFunctions, VitalFunctionCategory.HEART_RATE, "unit1", 2).size());

        vitalFunctions.add(VitalFunctionTestFactory.vitalFunction().category(VitalFunctionCategory.HEART_RATE).unit("unit1").build());
        assertEquals(2, VitalFunctionSelector.select(vitalFunctions, VitalFunctionCategory.HEART_RATE, "unit1", 2).size());

        vitalFunctions.add(VitalFunctionTestFactory.vitalFunction().category(VitalFunctionCategory.HEART_RATE).unit("unit1").build());
        assertEquals(2, VitalFunctionSelector.select(vitalFunctions, VitalFunctionCategory.HEART_RATE, "unit1", 2).size());

        vitalFunctions.add(VitalFunctionTestFactory.vitalFunction().category(VitalFunctionCategory.HEART_RATE).unit("unit1").build());
        assertEquals(2, VitalFunctionSelector.select(vitalFunctions, VitalFunctionCategory.HEART_RATE, null, 2).size());
    }

    @Test
    public void canSelectBloodPressures() {
        List<VitalFunction> vitalFunctions = Lists.newArrayList();

        assertEquals(0, selectSystolic(vitalFunctions).size());

        LocalDate mostRecentDate = LocalDate.of(2020, 2, 2);

        // Add one systolic measure.
        vitalFunctions.add(systolic().date(mostRecentDate).build());
        assertEquals(1, selectSystolic(vitalFunctions).size());

        // Add one diastolic measure.
        vitalFunctions.add(diastolic().build());
        assertEquals(1, selectSystolic(vitalFunctions).size());

        // Add another systolic measure on same date
        vitalFunctions.add(systolic().date(mostRecentDate).build());
        assertEquals(1, selectSystolic(vitalFunctions).size());

        // Add another systolic measure too far in the past
        vitalFunctions.add(systolic().date(mostRecentDate.minusMonths(3)).build());
        assertEquals(1, selectSystolic(vitalFunctions).size());

        // Add bunch of valid systolic measures
        vitalFunctions.add(systolic().date(mostRecentDate.minusDays(1)).build());
        vitalFunctions.add(systolic().date(mostRecentDate.minusDays(2)).build());
        vitalFunctions.add(systolic().date(mostRecentDate.minusDays(3)).build());
        vitalFunctions.add(systolic().date(mostRecentDate.minusDays(4)).build());
        vitalFunctions.add(systolic().date(mostRecentDate.minusDays(5)).build());
        vitalFunctions.add(systolic().date(mostRecentDate.minusDays(6)).build());

        List<VitalFunction> selected = selectSystolic(vitalFunctions);
        assertEquals(5, selected.size());
        assertEquals(systolic().date(mostRecentDate).build(), selected.get(0));
        assertEquals(systolic().date(mostRecentDate.minusDays(4)).build(), selected.get(4));
    }

    @NotNull
    private static List<VitalFunction> selectSystolic(@NotNull List<VitalFunction> vitalFunctions) {
        return VitalFunctionSelector.selectRelevant(vitalFunctions, BloodPressureCategory.SYSTOLIC);
    }

    @NotNull
    private static ImmutableVitalFunction.Builder systolic() {
        return VitalFunctionTestFactory.vitalFunction()
                .category(VitalFunctionCategory.NON_INVASIVE_BLOOD_PRESSURE)
                .subcategory(BloodPressureCategory.SYSTOLIC.display());
    }

    @NotNull
    private static ImmutableVitalFunction.Builder diastolic() {
        return VitalFunctionTestFactory.vitalFunction()
                .category(VitalFunctionCategory.NON_INVASIVE_BLOOD_PRESSURE)
                .subcategory(BloodPressureCategory.DIASTOLIC.display());
    }
}