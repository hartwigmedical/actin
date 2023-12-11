package com.hartwig.actin.algo.evaluation.vitalfunction

import com.hartwig.actin.algo.evaluation.vitalfunction.VitalFunctionSelector.selectBloodPressures
import com.hartwig.actin.algo.evaluation.vitalfunction.VitalFunctionSelector.selectMedianPerDay
import com.hartwig.actin.algo.evaluation.vitalfunction.VitalFunctionTestFactory.vitalFunction
import com.hartwig.actin.clinical.datamodel.VitalFunction
import com.hartwig.actin.clinical.datamodel.VitalFunctionCategory.HEART_RATE
import com.hartwig.actin.clinical.datamodel.VitalFunctionCategory.NON_INVASIVE_BLOOD_PRESSURE
import com.hartwig.actin.clinical.datamodel.VitalFunctionCategory.SPO2
import org.junit.Assert
import org.junit.Test
import java.time.LocalDate

class VitalFunctionSelectorTest {
    private val date1 = LocalDate.now()
    private val date2 = LocalDate.now().minusDays(5)
    private val date3 = LocalDate.now().minusMonths(2)

    // Testing selectMedianFunction
    @Test
    fun `Should select zero when list is empty`() {
        val vitalFunctions: List<VitalFunction> = emptyList()
        Assert.assertEquals(0, selectMedianPerDay(vitalFunctions, HEART_RATE, "unit1", 2).size.toLong())
    }

    @Test
    fun `Should select one when max entries is one`() {
        val vitalFunctions: List<VitalFunction> =
            listOf(
                vitalFunction().category(HEART_RATE).unit("unit1").date(date1).build(),
                vitalFunction().category(HEART_RATE).unit("unit1").date(date2).build()
            )
        Assert.assertEquals(1, selectMedianPerDay(vitalFunctions, HEART_RATE, "unit1", 1).size.toLong())
    }

    @Test
    fun `Should select one when list contains two entries but only one is right category`() {
        val vitalFunctions: List<VitalFunction> =
            listOf(
                vitalFunction().category(HEART_RATE).unit("unit1").date(date1).build(),
                vitalFunction().category(SPO2).unit("unit1").build()
            )
        Assert.assertEquals(
            1, selectMedianPerDay(vitalFunctions, HEART_RATE, "unit1", 2).size.toLong()
        )
    }

    @Test
    fun `Should select one when list contains one entry of right category with right unit and one with wrong unit`() {
        val vitalFunctions: List<VitalFunction> = listOf(
            vitalFunction().category(HEART_RATE).unit("unit1").date(date1).build(),
            vitalFunction().category(HEART_RATE).unit("unit2").date(date2).build()
        )
        Assert.assertEquals(
            1, selectMedianPerDay(vitalFunctions, HEART_RATE, "unit1", 2).size.toLong()
        )
    }

    @Test
    fun `Should select one when list contains two of right category and right unit with same date`() {
        val vitalFunctions: List<VitalFunction> = listOf(
            vitalFunction().category(HEART_RATE).unit("unit1").date(date1).build(),
            vitalFunction().category(HEART_RATE).unit("unit1").date(date1).build()
        )
        Assert.assertEquals(
            1, selectMedianPerDay(vitalFunctions, HEART_RATE, "unit1", 2).size.toLong()
        )
    }

    @Test
    fun `Should select two when list contains two of right category and right unit with separate date`() {
        val vitalFunctions: List<VitalFunction> = listOf(
            vitalFunction().category(HEART_RATE).unit("unit1").date(date1).build(),
            vitalFunction().category(HEART_RATE).unit("unit1").date(date2).build()
        )
        Assert.assertEquals(
            2, selectMedianPerDay(vitalFunctions, HEART_RATE, "unit1", 2).size.toLong()
        )
    }

    @Test
    fun `Should select all of right category when unitToFind is null`() {
        val vitalFunctions: List<VitalFunction> = listOf(
            vitalFunction().category(HEART_RATE).unit("unit1").date(date1).build(),
            vitalFunction().category(HEART_RATE).unit("unit2").date(date2).build()
        )
        Assert.assertEquals(
            2, selectMedianPerDay(vitalFunctions, HEART_RATE, null, 2).size.toLong()
        )
    }

    @Test
    fun `Should filter out values with ignore flag`() {
        val vitalFunctions: List<VitalFunction> = listOf(
            vitalFunction().category(HEART_RATE).date(date1).value(10.0).unit("<ignore>").build(),
            vitalFunction().category(HEART_RATE).date(date2).value(20.0).unit("<ignore>").build(),
            vitalFunction().category(HEART_RATE).date(date2.minusDays(3)).value(2.0).build()
        )
        Assert.assertEquals(
            listOf(2.0), selectMedianPerDay(vitalFunctions, HEART_RATE, null, 3).map { it.value() }
        )
    }

    @Test
    fun `Should select one median per day of most recent dates`() {
        val vitalFunctions: List<VitalFunction> = listOf(
            vitalFunction().category(HEART_RATE).date(date1).value(10.0).build(),
            vitalFunction().category(HEART_RATE).date(date1).value(15.0).build(),
            vitalFunction().category(HEART_RATE).date(date1).value(20.0).build(),
            vitalFunction().category(HEART_RATE).date(date2).value(5.0).build(),
            vitalFunction().category(HEART_RATE).date(date2.minusDays(1)).value(8.0).build(),
            vitalFunction().category(HEART_RATE).date(date2.minusDays(2)).value(12.0)
                .build(),
            vitalFunction().category(HEART_RATE).date(date2.minusDays(3)).value(14.0)
                .build(),
        )
        Assert.assertEquals(
            listOf(15.0, 5.0, 8.0), selectMedianPerDay(vitalFunctions, HEART_RATE, null, 3).map { it.value() }
        )
    }

    @Test
    fun `Should not take values outside of date cutoff`() {
        val vitalFunctions: List<VitalFunction> = listOf(
            vitalFunction().category(HEART_RATE).date(date1).value(10.0).build(),
            vitalFunction().category(HEART_RATE).date(date2).value(15.0).build(),
            vitalFunction().category(HEART_RATE).date(date3).value(20.0).build()
        )
        Assert.assertEquals(
            listOf(10.0, 15.0), selectMedianPerDay(vitalFunctions, HEART_RATE, null, 3).map { it.value() }
        )
    }

    // Testing selectBloodPressures
    @Test
    fun `Should select nothing when list is empty`() {
        val vitalFunctions: List<VitalFunction> = emptyList()
        Assert.assertEquals(0, selectBloodPressures(vitalFunctions, BloodPressureCategory.SYSTOLIC).size.toLong())
    }

    @Test
    fun `Should select blood pressures only from right category`() {
        val vitalFunctions: List<VitalFunction> = listOf(
            vitalFunction().category(NON_INVASIVE_BLOOD_PRESSURE).subcategory(BloodPressureCategory.SYSTOLIC.display()).date(date1).build(),
            vitalFunction().category(NON_INVASIVE_BLOOD_PRESSURE).subcategory(BloodPressureCategory.DIASTOLIC.display()).date(date2).build()
        )
        Assert.assertEquals(1, selectBloodPressures(vitalFunctions, BloodPressureCategory.SYSTOLIC).size.toLong())
    }

    @Test
    fun `Should filter out blood pressure values with ignore flag`() {
        val vitalFunctions: List<VitalFunction> = listOf(
            vitalFunction().category(NON_INVASIVE_BLOOD_PRESSURE).subcategory(BloodPressureCategory.SYSTOLIC.display()).date(date1)
                .value(10.0).unit("<ignore>").build(),
            vitalFunction().category(NON_INVASIVE_BLOOD_PRESSURE).subcategory(BloodPressureCategory.SYSTOLIC.display()).date(date2)
                .value(20.0).unit("<ignore>").build(),
            vitalFunction().category(NON_INVASIVE_BLOOD_PRESSURE).subcategory(BloodPressureCategory.SYSTOLIC.display())
                .date(date2.minusDays(3)).value(2.0).build()
        )
        Assert.assertEquals(
            listOf(2.0), selectBloodPressures(vitalFunctions, BloodPressureCategory.SYSTOLIC).map { it.value() }
        )
    }

    @Test
    fun `Should select one median blood pressure per day`() {
        val vitalFunctions: List<VitalFunction> = listOf(
            vitalFunction().category(NON_INVASIVE_BLOOD_PRESSURE).subcategory(BloodPressureCategory.SYSTOLIC.display()).date(date1)
                .value(130.0).build(),
            vitalFunction().category(NON_INVASIVE_BLOOD_PRESSURE).subcategory(BloodPressureCategory.SYSTOLIC.display()).date(date2)
                .value(110.0).build(),
            vitalFunction().category(NON_INVASIVE_BLOOD_PRESSURE).subcategory(BloodPressureCategory.SYSTOLIC.display()).date(date2)
                .value(115.0).build(),
            vitalFunction().category(NON_INVASIVE_BLOOD_PRESSURE).subcategory(BloodPressureCategory.SYSTOLIC.display()).date(date2)
                .value(120.0).build()
        )
        Assert.assertEquals(listOf(130.0, 115.0), selectBloodPressures(vitalFunctions, BloodPressureCategory.SYSTOLIC).map { it.value() })
    }

    @Test
    fun `Should not take blood pressure values outside of date cutoff`() {
        val vitalFunctions: List<VitalFunction> = listOf(
            vitalFunction().category(NON_INVASIVE_BLOOD_PRESSURE).subcategory(BloodPressureCategory.SYSTOLIC.display()).date(date1)
                .value(110.0).build(),
            vitalFunction().category(NON_INVASIVE_BLOOD_PRESSURE).subcategory(BloodPressureCategory.SYSTOLIC.display()).date(date2)
                .value(120.0).build(),
            vitalFunction().category(NON_INVASIVE_BLOOD_PRESSURE).subcategory(BloodPressureCategory.SYSTOLIC.display())
                .date(date3).value(130.0).build()
        )
        Assert.assertEquals(listOf(110.0, 120.0), selectBloodPressures(vitalFunctions, BloodPressureCategory.SYSTOLIC).map { it.value() })
    }

    @Test
    fun `Should select max five values`() {
        val vitalFunctions: List<VitalFunction> = listOf(
            vitalFunction().category(NON_INVASIVE_BLOOD_PRESSURE).subcategory(BloodPressureCategory.SYSTOLIC.display()).date(date1).build(),
            vitalFunction().category(NON_INVASIVE_BLOOD_PRESSURE).subcategory(BloodPressureCategory.SYSTOLIC.display()).date(date2).build(),
            vitalFunction().category(NON_INVASIVE_BLOOD_PRESSURE).subcategory(BloodPressureCategory.SYSTOLIC.display())
                .date(date1.minusDays(1)).build(),
            vitalFunction().category(NON_INVASIVE_BLOOD_PRESSURE).subcategory(BloodPressureCategory.SYSTOLIC.display())
                .date(date1.minusDays(2)).build(),
            vitalFunction().category(NON_INVASIVE_BLOOD_PRESSURE).subcategory(BloodPressureCategory.SYSTOLIC.display())
                .date(date1.minusDays(3)).build(),
            vitalFunction().category(NON_INVASIVE_BLOOD_PRESSURE).subcategory(BloodPressureCategory.SYSTOLIC.display())
                .date(date1.minusDays(4)).build()
        )
        Assert.assertEquals(5, selectBloodPressures(vitalFunctions, BloodPressureCategory.SYSTOLIC).size.toLong())
    }
}