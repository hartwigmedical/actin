package com.hartwig.actin.algo.evaluation.vitalfunction

import com.hartwig.actin.clinical.datamodel.VitalFunction
import com.hartwig.actin.clinical.datamodel.VitalFunctionCategory
import com.hartwig.actin.clinical.sort.VitalFunctionDescendingDateComparator
import java.time.LocalDateTime

internal object VitalFunctionSelector {
    private const val MAX_BLOOD_PRESSURES_TO_USE = 5

    fun selectVitalFunctions(vitalFunctions: List<VitalFunction>, categoryToFind: VitalFunctionCategory): List<VitalFunction> {
        return vitalFunctions.asSequence().filter {
            it.date() > LocalDateTime.now().minusMonths(1) && it.category() == categoryToFind
                    && it.valid()
        }.toList()
    }

    fun selectMedianPerDay(
        vitalFunctions: List<VitalFunction>, categoryToFind: VitalFunctionCategory,
        unitToFind: String?, maxEntries: Int
    ): List<VitalFunction> {
        return selectVitalFunctions(vitalFunctions, categoryToFind)
            .filter { unitToFind == null || it.unit().equals(unitToFind, ignoreCase = true) }
            .groupBy { it.date() }
            .map { VitalFunctionFunctions.selectMedianFunction(it.value) }
            .sortedWith(VitalFunctionDescendingDateComparator())
            .take(maxEntries).toList()
    }

    fun selectBloodPressures(vitalFunctions: List<VitalFunction>, category: BloodPressureCategory): List<VitalFunction> {
        return vitalFunctions.asSequence().filter {
            it.date() > LocalDateTime.now().minusMonths(1) && isBloodPressure(it) && it.subcategory()
                .equals(category.display(), ignoreCase = true) && it.valid()
        }
            .groupBy { it.date() }
            .map { VitalFunctionFunctions.selectMedianFunction(it.value) }
            .sortedWith(VitalFunctionDescendingDateComparator())
            .take(MAX_BLOOD_PRESSURES_TO_USE).toList()
    }

    private fun isBloodPressure(vitalFunction: VitalFunction): Boolean {
        return (vitalFunction.category() == VitalFunctionCategory.ARTERIAL_BLOOD_PRESSURE
                || vitalFunction.category() == VitalFunctionCategory.NON_INVASIVE_BLOOD_PRESSURE)
    }
}