package com.hartwig.actin.algo.evaluation.vitalfunction

import com.hartwig.actin.clinical.datamodel.VitalFunction
import com.hartwig.actin.clinical.datamodel.VitalFunctionCategory
import com.hartwig.actin.clinical.sort.VitalFunctionDescendingDateComparator

internal object VitalFunctionSelector {
    private const val MAX_BLOOD_PRESSURES_TO_USE = 5
    private const val MAX_AGE_MONTHS = 1
    fun select(
        vitalFunctions: List<VitalFunction>, categoryToFind: VitalFunctionCategory,
        unitToFind: String?, maxEntries: Int
    ): List<VitalFunction> {
        return vitalFunctions.filter {
            it.category == categoryToFind && (unitToFind == null || it.unit.equals(unitToFind, ignoreCase = true))
        }
            .sortedWith(VitalFunctionDescendingDateComparator())
            .take(maxEntries)
    }

    fun selectBloodPressures(vitalFunctions: List<VitalFunction>, category: BloodPressureCategory): List<VitalFunction> {
        val result =
            vitalFunctions.asSequence().filter { isBloodPressure(it) && it.subcategory.equals(category.display(), ignoreCase = true) }
                .groupBy { it.date }
                .map { VitalFunctionFunctions.selectMedianFunction(it.value) }
                .sortedWith(VitalFunctionDescendingDateComparator())
                .take(MAX_BLOOD_PRESSURES_TO_USE).toList()

        return if (result.isEmpty()) result else {
            val mostRecent = result[0].date
            result.takeWhile { !it.date.isBefore(mostRecent.minusMonths(MAX_AGE_MONTHS.toLong())) }
        }
    }

    private fun isBloodPressure(vitalFunction: VitalFunction): Boolean {
        return (vitalFunction.category == VitalFunctionCategory.ARTERIAL_BLOOD_PRESSURE
                || vitalFunction.category == VitalFunctionCategory.NON_INVASIVE_BLOOD_PRESSURE)
    }
}