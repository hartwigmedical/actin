package com.hartwig.actin.algo.evaluation.vitalfunction

import com.hartwig.actin.algo.calendar.ReferenceDateProviderFactory
import com.hartwig.actin.clinical.sort.VitalFunctionDescendingDateComparator
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.clinical.VitalFunction
import com.hartwig.actin.datamodel.clinical.VitalFunctionCategory
import java.time.LocalDate

internal object VitalFunctionSelector {

    private const val MAX_BLOOD_PRESSURES_TO_USE = 5
    private const val MAX_AGE_MONTHS = 1

    fun selectRecentVitalFunctionsWrongUnit(
        record: PatientRecord, categoryToFind: VitalFunctionCategory
    ): List<VitalFunction> {
        val referenceDate = ReferenceDateProviderFactory.create(record, true).date().minusMonths(MAX_AGE_MONTHS.toLong())
        return record.vitalFunctions.filter {
            it.date.toLocalDate() > referenceDate && it.category == categoryToFind && EXPECTED_UNIT[categoryToFind] != it.unit
        }
    }

    fun selectMedianPerDay(
        record: PatientRecord, categoryToFind: VitalFunctionCategory, maxEntries: Int, minimalDate: LocalDate
    ): List<VitalFunction> {
        return record.vitalFunctions.asSequence().filter {
            it.date.toLocalDate() > minimalDate && it.category == categoryToFind && it.valid
        }
            .groupBy { it.date }
            .map { VitalFunctionFunctions.selectMedianFunction(it.value) }
            .sortedWith(VitalFunctionDescendingDateComparator())
            .take(maxEntries).toList()
    }

    fun selectBloodPressures(record: PatientRecord, category: BloodPressureCategory, minimalDate: LocalDate): List<VitalFunction> {
        return record.vitalFunctions.asSequence().filter {
            it.date.toLocalDate() > minimalDate && isBloodPressure(it) && it.subcategory
                .equals(category.display(), ignoreCase = true) && it.valid
        }
            .groupBy { it.date }
            .map { VitalFunctionFunctions.selectMedianFunction(it.value) }
            .sortedWith(VitalFunctionDescendingDateComparator())
            .take(MAX_BLOOD_PRESSURES_TO_USE).toList()
    }

    private fun isBloodPressure(vitalFunction: VitalFunction): Boolean {
        return (vitalFunction.category == VitalFunctionCategory.ARTERIAL_BLOOD_PRESSURE
                || vitalFunction.category == VitalFunctionCategory.NON_INVASIVE_BLOOD_PRESSURE)
    }

    private val EXPECTED_UNIT = mapOf(
        VitalFunctionCategory.HEART_RATE to "bpm", VitalFunctionCategory.SPO2 to "percent",
        VitalFunctionCategory.NON_INVASIVE_BLOOD_PRESSURE to "mmhg", VitalFunctionCategory.ARTERIAL_BLOOD_PRESSURE to "mmhg"
    )
}