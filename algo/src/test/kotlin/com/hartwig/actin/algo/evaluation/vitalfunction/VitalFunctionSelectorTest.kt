package com.hartwig.actin.algo.evaluation.vitalfunction

import com.hartwig.actin.clinical.datamodel.ImmutableVitalFunction
import com.hartwig.actin.clinical.datamodel.VitalFunction
import com.hartwig.actin.clinical.datamodel.VitalFunctionCategory
import org.junit.Assert
import org.junit.Test
import java.time.LocalDate

class VitalFunctionSelectorTest {
    @Test
    fun canSelectVitalFunctions() {
        val vitalFunctions: MutableList<VitalFunction> = mutableListOf()
        Assert.assertEquals(0, VitalFunctionSelector.select(vitalFunctions, VitalFunctionCategory.HEART_RATE, "unit1", 2).size.toLong())
        vitalFunctions.add(VitalFunctionTestFactory.vitalFunction().category(VitalFunctionCategory.HEART_RATE).unit("unit1").build())
        Assert.assertEquals(1, VitalFunctionSelector.select(vitalFunctions, VitalFunctionCategory.HEART_RATE, "unit1", 2).size.toLong())
        vitalFunctions.add(VitalFunctionTestFactory.vitalFunction().category(VitalFunctionCategory.SPO2).unit("unit1").build())
        Assert.assertEquals(1, VitalFunctionSelector.select(vitalFunctions, VitalFunctionCategory.HEART_RATE, "unit1", 2).size.toLong())
        vitalFunctions.add(VitalFunctionTestFactory.vitalFunction().category(VitalFunctionCategory.HEART_RATE).unit("unit2").build())
        Assert.assertEquals(1, VitalFunctionSelector.select(vitalFunctions, VitalFunctionCategory.HEART_RATE, "unit1", 2).size.toLong())
        vitalFunctions.add(VitalFunctionTestFactory.vitalFunction().category(VitalFunctionCategory.HEART_RATE).unit("unit1").build())
        Assert.assertEquals(2, VitalFunctionSelector.select(vitalFunctions, VitalFunctionCategory.HEART_RATE, "unit1", 2).size.toLong())
        vitalFunctions.add(VitalFunctionTestFactory.vitalFunction().category(VitalFunctionCategory.HEART_RATE).unit("unit1").build())
        Assert.assertEquals(2, VitalFunctionSelector.select(vitalFunctions, VitalFunctionCategory.HEART_RATE, "unit1", 2).size.toLong())
        vitalFunctions.add(VitalFunctionTestFactory.vitalFunction().category(VitalFunctionCategory.HEART_RATE).unit("unit1").build())
        Assert.assertEquals(2, VitalFunctionSelector.select(vitalFunctions, VitalFunctionCategory.HEART_RATE, null, 2).size.toLong())
    }

    @Test
    fun canSelectBloodPressures() {
        val vitalFunctions: MutableList<VitalFunction> = mutableListOf()
        Assert.assertEquals(0, selectSystolic(vitalFunctions).size.toLong())
        val mostRecentDate = LocalDate.of(2020, 2, 2)

        // Add one systolic measure.
        vitalFunctions.add(systolic().date(mostRecentDate).build())
        Assert.assertEquals(1, selectSystolic(vitalFunctions).size.toLong())

        // Add one diastolic measure.
        vitalFunctions.add(diastolic().build())
        Assert.assertEquals(1, selectSystolic(vitalFunctions).size.toLong())

        // Add another systolic measure on same date
        vitalFunctions.add(systolic().date(mostRecentDate).build())
        Assert.assertEquals(1, selectSystolic(vitalFunctions).size.toLong())

        // Add another systolic measure too far in the past
        vitalFunctions.add(systolic().date(mostRecentDate.minusMonths(3)).build())
        Assert.assertEquals(1, selectSystolic(vitalFunctions).size.toLong())

        // Add a bunch of valid systolic measures
        vitalFunctions.add(systolic().date(mostRecentDate.minusDays(1)).build())
        vitalFunctions.add(systolic().date(mostRecentDate.minusDays(2)).build())
        vitalFunctions.add(systolic().date(mostRecentDate.minusDays(3)).build())
        vitalFunctions.add(systolic().date(mostRecentDate.minusDays(4)).build())
        vitalFunctions.add(systolic().date(mostRecentDate.minusDays(5)).build())
        vitalFunctions.add(systolic().date(mostRecentDate.minusDays(6)).build())
        val selected = selectSystolic(vitalFunctions)
        Assert.assertEquals(5, selected.size.toLong())
        Assert.assertEquals(systolic().date(mostRecentDate).build(), selected[0])
        Assert.assertEquals(systolic().date(mostRecentDate.minusDays(4)).build(), selected[4])
    }

    companion object {
        private fun selectSystolic(vitalFunctions: List<VitalFunction>): List<VitalFunction> {
            return VitalFunctionSelector.selectBloodPressures(vitalFunctions, BloodPressureCategory.SYSTOLIC)
        }

        private fun systolic(): ImmutableVitalFunction.Builder {
            return VitalFunctionTestFactory.vitalFunction()
                .category(VitalFunctionCategory.NON_INVASIVE_BLOOD_PRESSURE)
                .subcategory(BloodPressureCategory.SYSTOLIC.display())
        }

        private fun diastolic(): ImmutableVitalFunction.Builder {
            return VitalFunctionTestFactory.vitalFunction()
                .category(VitalFunctionCategory.NON_INVASIVE_BLOOD_PRESSURE)
                .subcategory(BloodPressureCategory.DIASTOLIC.display())
        }
    }
}