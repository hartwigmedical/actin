package com.hartwig.actin.algo.evaluation.vitalfunction

import com.hartwig.actin.algo.evaluation.vitalfunction.VitalFunctionSelector.selectBloodPressures
import com.hartwig.actin.algo.evaluation.vitalfunction.VitalFunctionSelector.selectMedianPerDay
import com.hartwig.actin.algo.evaluation.vitalfunction.VitalFunctionTestFactory.vitalFunction
import com.hartwig.actin.algo.evaluation.vitalfunction.VitalFunctionTestFactory.withVitalFunctions
import com.hartwig.actin.datamodel.clinical.VitalFunction
import com.hartwig.actin.datamodel.clinical.VitalFunctionCategory.HEART_RATE
import com.hartwig.actin.datamodel.clinical.VitalFunctionCategory.NON_INVASIVE_BLOOD_PRESSURE
import com.hartwig.actin.datamodel.clinical.VitalFunctionCategory.SPO2
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime

class VitalFunctionSelectorTest {
    private val minimumDate = LocalDate.of(2023, 12, 1)
    private val recentDate = LocalDateTime.of(2023, 12, 2, 0, 0)
    private val oldDate = recentDate.minusMonths(2)

    // Testing selectMedianFunction
    @Test
    fun `Should select zero when list is empty`() {
        assertThat(selectMedianPerDay(withVitalFunctions(emptyList()), HEART_RATE, 2, minimumDate)).hasSize(0)
    }

    @Test
    fun `Should select one when max entries is one`() {
        val vitalFunctions: List<VitalFunction> =
            listOf(
                vitalFunction(category = HEART_RATE, date = recentDate, valid = true),
                vitalFunction(category = HEART_RATE, date = recentDate.plusDays(1), valid = true)
            )
        assertThat(selectMedianPerDay(withVitalFunctions(vitalFunctions), HEART_RATE, 1, minimumDate)).hasSize(1)
    }

    @Test
    fun `Should select one when list contains two entries but only one is right category`() {
        val vitalFunctions: List<VitalFunction> =
            listOf(
                vitalFunction(category = HEART_RATE, date = recentDate, valid = true),
                vitalFunction(category = SPO2, valid = true)
            )
        assertThat(selectMedianPerDay(withVitalFunctions(vitalFunctions), HEART_RATE, 2, minimumDate)).hasSize(1)
    }

    @Test
    fun `Should select one when list contains one valid and one invalid entry`() {
        val vitalFunctions: List<VitalFunction> = listOf(
            vitalFunction(category = HEART_RATE, date = recentDate, valid = true),
            vitalFunction(category = HEART_RATE, date = recentDate.plusDays(1), valid = false)
        )
        assertThat(selectMedianPerDay(withVitalFunctions(vitalFunctions), HEART_RATE, 2, minimumDate)).hasSize(1)
    }

    @Test
    fun `Should select one when list contains two of right category and right unit with same date`() {
        val vitalFunctions: List<VitalFunction> = listOf(
            vitalFunction(category = HEART_RATE, date = recentDate, valid = true),
            vitalFunction(category = HEART_RATE, date = recentDate, valid = true)
        )
        assertThat(selectMedianPerDay(withVitalFunctions(vitalFunctions), HEART_RATE, 2, minimumDate)).hasSize(1)
    }

    @Test
    fun `Should select two when list contains two of right category and right unit with separate date`() {
        val vitalFunctions: List<VitalFunction> = listOf(
            vitalFunction(category = HEART_RATE, unit = "unit1", date = recentDate),
            vitalFunction(category = HEART_RATE, unit = "unit1", date = recentDate.plusDays(1))
        )
        assertThat(selectMedianPerDay(withVitalFunctions(vitalFunctions), HEART_RATE, 2, minimumDate)).hasSize(2)
    }

    @Test
    fun `Should select all of right category when unitToFind is null`() {
        val vitalFunctions: List<VitalFunction> = listOf(
            vitalFunction(category = HEART_RATE, unit = "unit1", date = recentDate),
            vitalFunction(category = HEART_RATE, unit = "unit2", date = recentDate.plusDays(1))
        )
        assertThat(selectMedianPerDay(withVitalFunctions(vitalFunctions), HEART_RATE, 2, minimumDate)).hasSize(2)
    }

    @Test
    fun `Should filter out invalid values`() {
        val vitalFunctions: List<VitalFunction> = listOf(
            vitalFunction(category = HEART_RATE, date = recentDate, value = 10.0, unit = "kg", valid = false),
            vitalFunction(category = HEART_RATE, date = recentDate, value = 15.0, unit = "kg", valid = false),
            vitalFunction(category = HEART_RATE, date = recentDate, value = 50.0)
        )
        assertThat(selectMedianPerDay(withVitalFunctions(vitalFunctions), HEART_RATE, 3, minimumDate).map(VitalFunction::value))
            .isEqualTo(listOf(50.0))
    }

    @Test
    fun `Should select one median per day of most recent dates`() {
        val vitalFunctions: List<VitalFunction> = listOf(
            vitalFunction(category = HEART_RATE, date = recentDate.plusDays(2), value = 10.0),
            vitalFunction(category = HEART_RATE, date = recentDate.plusDays(2), value = 15.0),
            vitalFunction(category = HEART_RATE, date = recentDate.plusDays(2), value = 20.0),
            vitalFunction(category = HEART_RATE, date = recentDate.plusDays(1), value = 5.0),
            vitalFunction(category = HEART_RATE, date = recentDate, value = 8.0),
            vitalFunction(category = HEART_RATE, date = recentDate, value = 12.0),
            vitalFunction(category = HEART_RATE, date = recentDate, value = 14.0),
        )
        assertThat(selectMedianPerDay(withVitalFunctions(vitalFunctions), HEART_RATE, 3, minimumDate).map(VitalFunction::value))
            .isEqualTo(listOf(15.0, 5.0, 12.0))
    }

    @Test
    fun `Should not take values outside of date cutoff`() {
        val vitalFunctions: List<VitalFunction> = listOf(
            vitalFunction(category = HEART_RATE, date = recentDate, value = 10.0),
            vitalFunction(category = HEART_RATE, date = recentDate.plusDays(1), value = 15.0),
            vitalFunction(category = HEART_RATE, date = oldDate, value = 20.0)
        )
        assertThat(selectMedianPerDay(withVitalFunctions(vitalFunctions), HEART_RATE, 3, minimumDate).map(VitalFunction::value))
            .isEqualTo(listOf(15.0, 10.0))
    }

    // Testing selectBloodPressures
    @Test
    fun `Should select nothing when list is empty`() {
        assertThat(selectBloodPressures(withVitalFunctions(emptyList()), BloodPressureCategory.SYSTOLIC, minimumDate)).hasSize(0)
    }

    @Test
    fun `Should select blood pressures only from right category`() {
        val vitalFunctions: List<VitalFunction> = listOf(
            systolic(recentDate),
            systolic(recentDate).copy(subcategory = BloodPressureCategory.DIASTOLIC.display())
        )
        assertThat(selectBloodPressures(withVitalFunctions(vitalFunctions), BloodPressureCategory.SYSTOLIC, minimumDate)).hasSize(1)
    }

    @Test
    fun `Should filter out invalid blood pressure values`() {
        val vitalFunctions: List<VitalFunction> = listOf(
            systolic(date = recentDate, value = 10.0, valid = false),
            systolic(date = recentDate, value = 20.0, valid = false),
            systolic(date = recentDate, value = 2.0)
        )
        assertThat(
            selectBloodPressures(withVitalFunctions(vitalFunctions), BloodPressureCategory.SYSTOLIC, minimumDate).map(VitalFunction::value)
        ).isEqualTo(listOf(2.0))
    }

    @Test
    fun `Should select one median blood pressure per day`() {
        val vitalFunctions: List<VitalFunction> = listOf(
            systolic(date = recentDate.plusDays(1), value = 130.0),
            systolic(recentDate, value = 110.0),
            systolic(recentDate, value = 115.0),
            systolic(recentDate, value = 120.0)
        )
        assertThat(
            selectBloodPressures(withVitalFunctions(vitalFunctions), BloodPressureCategory.SYSTOLIC, minimumDate).map(VitalFunction::value)
        ).isEqualTo(listOf(130.0, 115.0))
    }

    @Test
    fun `Should not take blood pressure values outside of date cutoff`() {
        val vitalFunctions: List<VitalFunction> = listOf(
            systolic(date = recentDate.plusDays(1), value = 110.0),
            systolic(date = recentDate, value = 120.0),
            systolic(date = oldDate, value = 130.0)
        )
        assertThat(
            selectBloodPressures(withVitalFunctions(vitalFunctions), BloodPressureCategory.SYSTOLIC, minimumDate).map(VitalFunction::value)
        ).isEqualTo(listOf(110.0, 120.0))
    }

    @Test
    fun `Should select max five values`() {
        val vitalFunctions: List<VitalFunction> = listOf(
            systolic(date = recentDate.plusDays(5)),
            systolic(date = recentDate.plusDays(4)),
            systolic(date = recentDate.plusDays(3)),
            systolic(date = recentDate.plusDays(2)),
            systolic(date = recentDate.plusDays(1)),
            systolic(date = recentDate)
        )
        assertThat(selectBloodPressures(withVitalFunctions(vitalFunctions), BloodPressureCategory.SYSTOLIC, minimumDate)).hasSize(5)
    }

    private fun systolic(date: LocalDateTime, value: Double = 0.0, valid: Boolean = true): VitalFunction {
        return vitalFunction(
            category = NON_INVASIVE_BLOOD_PRESSURE,
            subcategory = BloodPressureCategory.SYSTOLIC.display(),
            date = date,
            value = value,
            valid = valid
        )
    }
}