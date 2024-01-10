package com.hartwig.actin.algo.evaluation.vitalfunction

import com.hartwig.actin.algo.evaluation.vitalfunction.VitalFunctionSelector.selectBloodPressures
import com.hartwig.actin.algo.evaluation.vitalfunction.VitalFunctionSelector.selectMedianPerDay
import com.hartwig.actin.algo.evaluation.vitalfunction.VitalFunctionTestFactory.vitalFunction
import com.hartwig.actin.algo.evaluation.vitalfunction.VitalFunctionTestFactory.withVitalFunctions
import com.hartwig.actin.clinical.datamodel.VitalFunction
import com.hartwig.actin.clinical.datamodel.VitalFunctionCategory.HEART_RATE
import com.hartwig.actin.clinical.datamodel.VitalFunctionCategory.NON_INVASIVE_BLOOD_PRESSURE
import com.hartwig.actin.clinical.datamodel.VitalFunctionCategory.SPO2
import org.junit.Assert
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime

class VitalFunctionSelectorTest {
    private val minimalDate = LocalDate.of(2023, 12, 1)
    private val recentDate = LocalDateTime.of(2023, 12, 2, 0, 0)
    private val oldDate = recentDate.minusMonths(2)

    // Testing selectMedianFunction
    @Test
    fun `Should select zero when list is empty`() {
        val vitalFunctions: List<VitalFunction> = emptyList()
        Assert.assertEquals(0, selectMedianPerDay(withVitalFunctions(vitalFunctions), HEART_RATE, 2, minimalDate).size.toLong())
    }

    @Test
    fun `Should select one when max entries is one`() {
        val vitalFunctions: List<VitalFunction> =
            listOf(
                vitalFunction().category(HEART_RATE).date(recentDate).valid(true).build(),
                vitalFunction().category(HEART_RATE).date(recentDate.plusDays(1)).valid(true).build()
            )
        Assert.assertEquals(1, selectMedianPerDay(withVitalFunctions(vitalFunctions), HEART_RATE, 1, minimalDate).size.toLong())
    }

    @Test
    fun `Should select one when list contains two entries but only one is right category`() {
        val vitalFunctions: List<VitalFunction> =
            listOf(
                vitalFunction().category(HEART_RATE).date(recentDate).valid(true).build(),
                vitalFunction().category(SPO2).valid(true).build()
            )
        Assert.assertEquals(
            1, selectMedianPerDay(withVitalFunctions(vitalFunctions), HEART_RATE, 2, minimalDate).size.toLong()
        )
    }

    @Test
    fun `Should select one when list contains one valid and one invalid entry`() {
        val vitalFunctions: List<VitalFunction> = listOf(
            vitalFunction().category(HEART_RATE).date(recentDate).valid(true).build(),
            vitalFunction().category(HEART_RATE).date(recentDate.plusDays(1)).valid(false).build()
        )
        Assert.assertEquals(
            1, selectMedianPerDay(withVitalFunctions(vitalFunctions), HEART_RATE, 2, minimalDate).size.toLong()
        )
    }

    @Test
    fun `Should select one when list contains two of right category and right unit with same date`() {
        val vitalFunctions: List<VitalFunction> = listOf(
            vitalFunction().category(HEART_RATE).date(recentDate).valid(true).build(),
            vitalFunction().category(HEART_RATE).date(recentDate).valid(true).build()
        )
        Assert.assertEquals(
            1, selectMedianPerDay(withVitalFunctions(vitalFunctions), HEART_RATE, 2, minimalDate).size.toLong()
        )
    }

    @Test
    fun `Should select two when list contains two of right category and right unit with separate date`() {
        val vitalFunctions: List<VitalFunction> = listOf(
            vitalFunction().category(HEART_RATE).unit("unit1").date(recentDate).valid(true).build(),
            vitalFunction().category(HEART_RATE).unit("unit1").date(recentDate.plusDays(1)).valid(true).build()
        )
        Assert.assertEquals(
            2, selectMedianPerDay(withVitalFunctions(vitalFunctions), HEART_RATE, 2, minimalDate).size.toLong()
        )
    }

    @Test
    fun `Should select all of right category when unitToFind is null`() {
        val vitalFunctions: List<VitalFunction> = listOf(
            vitalFunction().category(HEART_RATE).unit("unit1").date(recentDate).valid(true).build(),
            vitalFunction().category(HEART_RATE).unit("unit2").date(recentDate.plusDays(1)).valid(true).build()
        )
        Assert.assertEquals(
            2, selectMedianPerDay(withVitalFunctions(vitalFunctions), HEART_RATE, 2, minimalDate).size.toLong()
        )
    }

    @Test
    fun `Should filter out invalid values`() {
        val vitalFunctions: List<VitalFunction> = listOf(
            vitalFunction().category(HEART_RATE).date(recentDate).value(10.0).unit("kg").valid(false).build(),
            vitalFunction().category(HEART_RATE).date(recentDate).value(15.0).unit("kg").valid(false).build(),
            vitalFunction().category(HEART_RATE).date(recentDate).value(50.0).valid(true).build()
        )
        Assert.assertEquals(
            listOf(50.0), selectMedianPerDay(withVitalFunctions(vitalFunctions), HEART_RATE, 3, minimalDate).map { it.value() }
        )
    }

    @Test
    fun `Should select one median per day of most recent dates`() {
        val vitalFunctions: List<VitalFunction> = listOf(
            vitalFunction().category(HEART_RATE).date(recentDate.plusDays(2)).value(10.0).valid(true).build(),
            vitalFunction().category(HEART_RATE).date(recentDate.plusDays(2)).value(15.0).valid(true).build(),
            vitalFunction().category(HEART_RATE).date(recentDate.plusDays(2)).value(20.0).valid(true).build(),
            vitalFunction().category(HEART_RATE).date(recentDate.plusDays(1)).value(5.0).valid(true).build(),
            vitalFunction().category(HEART_RATE).date(recentDate).value(8.0).valid(true).build(),
            vitalFunction().category(HEART_RATE).date(recentDate).value(12.0).valid(true).build(),
            vitalFunction().category(HEART_RATE).date(recentDate).value(14.0).valid(true).build(),
        )
        Assert.assertEquals(
            listOf(15.0, 5.0, 12.0), selectMedianPerDay(withVitalFunctions(vitalFunctions), HEART_RATE, 3, minimalDate).map { it.value() }
        )
    }

    @Test
    fun `Should not take values outside of date cutoff`() {
        val vitalFunctions: List<VitalFunction> = listOf(
            vitalFunction().category(HEART_RATE).date(recentDate).value(10.0).valid(true).build(),
            vitalFunction().category(HEART_RATE).date(recentDate.plusDays(1)).value(15.0).valid(true).build(),
            vitalFunction().category(HEART_RATE).date(oldDate).value(20.0).valid(true).build()
        )
        Assert.assertEquals(
            listOf(15.0, 10.0), selectMedianPerDay(withVitalFunctions(vitalFunctions), HEART_RATE, 3, minimalDate).map { it.value() }
        )
    }

    // Testing selectBloodPressures
    @Test
    fun `Should select nothing when list is empty`() {
        val vitalFunctions: List<VitalFunction> = emptyList()
        Assert.assertEquals(
            0,
            selectBloodPressures(withVitalFunctions(vitalFunctions), BloodPressureCategory.SYSTOLIC, minimalDate).size.toLong()
        )
    }

    @Test
    fun `Should select blood pressures only from right category`() {
        val vitalFunctions: List<VitalFunction> = listOf(
            vitalFunction().category(NON_INVASIVE_BLOOD_PRESSURE).subcategory(BloodPressureCategory.SYSTOLIC.display()).date(recentDate)
                .valid(true).build(),
            vitalFunction().category(NON_INVASIVE_BLOOD_PRESSURE).subcategory(BloodPressureCategory.DIASTOLIC.display()).date(recentDate)
                .valid(true).build()
        )
        Assert.assertEquals(
            1,
            selectBloodPressures(withVitalFunctions(vitalFunctions), BloodPressureCategory.SYSTOLIC, minimalDate).size.toLong()
        )
    }

    @Test
    fun `Should filter out invalid blood pressure values`() {
        val vitalFunctions: List<VitalFunction> = listOf(
            vitalFunction().category(NON_INVASIVE_BLOOD_PRESSURE).subcategory(BloodPressureCategory.SYSTOLIC.display()).date(recentDate)
                .value(10.0).valid(false).build(),
            vitalFunction().category(NON_INVASIVE_BLOOD_PRESSURE).subcategory(BloodPressureCategory.SYSTOLIC.display()).date(recentDate)
                .value(20.0).valid(false).build(),
            vitalFunction().category(NON_INVASIVE_BLOOD_PRESSURE).subcategory(BloodPressureCategory.SYSTOLIC.display())
                .date(recentDate).value(2.0).valid(true).build()
        )
        Assert.assertEquals(
            listOf(2.0),
            selectBloodPressures(withVitalFunctions(vitalFunctions), BloodPressureCategory.SYSTOLIC, minimalDate).map { it.value() }
        )
    }

    @Test
    fun `Should select one median blood pressure per day`() {
        val vitalFunctions: List<VitalFunction> = listOf(
            vitalFunction().category(NON_INVASIVE_BLOOD_PRESSURE).subcategory(BloodPressureCategory.SYSTOLIC.display())
                .date(recentDate.plusDays(1))
                .value(130.0).valid(true).build(),
            vitalFunction().category(NON_INVASIVE_BLOOD_PRESSURE).subcategory(BloodPressureCategory.SYSTOLIC.display()).date(recentDate)
                .value(110.0).valid(true).build(),
            vitalFunction().category(NON_INVASIVE_BLOOD_PRESSURE).subcategory(BloodPressureCategory.SYSTOLIC.display()).date(recentDate)
                .value(115.0).valid(true).build(),
            vitalFunction().category(NON_INVASIVE_BLOOD_PRESSURE).subcategory(BloodPressureCategory.SYSTOLIC.display()).date(recentDate)
                .value(120.0).valid(true).build()
        )
        Assert.assertEquals(
            listOf(130.0, 115.0),
            selectBloodPressures(withVitalFunctions(vitalFunctions), BloodPressureCategory.SYSTOLIC, minimalDate).map { it.value() })
    }

    @Test
    fun `Should not take blood pressure values outside of date cutoff`() {
        val vitalFunctions: List<VitalFunction> = listOf(
            vitalFunction().category(NON_INVASIVE_BLOOD_PRESSURE).subcategory(BloodPressureCategory.SYSTOLIC.display())
                .date(recentDate.plusDays(1))
                .value(110.0).valid(true).build(),
            vitalFunction().category(NON_INVASIVE_BLOOD_PRESSURE).subcategory(BloodPressureCategory.SYSTOLIC.display()).date(recentDate)
                .value(120.0).valid(true).build(),
            vitalFunction().category(NON_INVASIVE_BLOOD_PRESSURE).subcategory(BloodPressureCategory.SYSTOLIC.display())
                .date(oldDate).value(130.0).valid(true).build()
        )
        Assert.assertEquals(
            listOf(110.0, 120.0),
            selectBloodPressures(withVitalFunctions(vitalFunctions), BloodPressureCategory.SYSTOLIC, minimalDate).map { it.value() })
    }

    @Test
    fun `Should select max five values`() {
        val vitalFunctions: List<VitalFunction> = listOf(
            vitalFunction().category(NON_INVASIVE_BLOOD_PRESSURE).subcategory(BloodPressureCategory.SYSTOLIC.display())
                .date(recentDate.plusDays(5))
                .valid(true).build(),
            vitalFunction().category(NON_INVASIVE_BLOOD_PRESSURE).subcategory(BloodPressureCategory.SYSTOLIC.display())
                .date(recentDate.plusDays(4))
                .valid(true).build(),
            vitalFunction().category(NON_INVASIVE_BLOOD_PRESSURE).subcategory(BloodPressureCategory.SYSTOLIC.display())
                .date(recentDate.plusDays(3)).valid(true).build(),
            vitalFunction().category(NON_INVASIVE_BLOOD_PRESSURE).subcategory(BloodPressureCategory.SYSTOLIC.display())
                .date(recentDate.plusDays(2)).valid(true).build(),
            vitalFunction().category(NON_INVASIVE_BLOOD_PRESSURE).subcategory(BloodPressureCategory.SYSTOLIC.display())
                .date(recentDate.plusDays(1)).valid(true).build(),
            vitalFunction().category(NON_INVASIVE_BLOOD_PRESSURE).subcategory(BloodPressureCategory.SYSTOLIC.display())
                .date(recentDate).valid(true).build()
        )
        Assert.assertEquals(
            5,
            selectBloodPressures(withVitalFunctions(vitalFunctions), BloodPressureCategory.SYSTOLIC, minimalDate).size.toLong()
        )
    }
}