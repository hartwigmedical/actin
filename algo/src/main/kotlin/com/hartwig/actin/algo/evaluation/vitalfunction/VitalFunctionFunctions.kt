package com.hartwig.actin.algo.evaluation.vitalfunction

import com.hartwig.actin.datamodel.clinical.VitalFunction
import kotlin.math.ceil

internal object VitalFunctionFunctions {
    fun selectMedianFunction(vitalFunctions: Iterable<VitalFunction>): VitalFunction {
        val values = sortedValues(vitalFunctions)
        val median = values[ceil(values.size / 2.0).toInt() - 1]
        for (vitalFunction in vitalFunctions) {
            if (vitalFunction.value.compareTo(median) == 0) {
                return vitalFunction
            }
        }
        throw IllegalStateException("Could not determine median vital function from $vitalFunctions")
    }

    fun determineMedianValue(vitalFunctions: Iterable<VitalFunction>): Double {
        val values = sortedValues(vitalFunctions)
        val index = ceil(values.size / 2.0).toInt() - 1
        return if (values.size % 2 == 0) {
            0.5 * (values[index] + values[index + 1])
        } else {
            values[index]
        }
    }

    private fun sortedValues(vitalFunctions: Iterable<VitalFunction>): List<Double> {
        return vitalFunctions.map { it.value }.sortedWith(Comparator.naturalOrder())
    }
}