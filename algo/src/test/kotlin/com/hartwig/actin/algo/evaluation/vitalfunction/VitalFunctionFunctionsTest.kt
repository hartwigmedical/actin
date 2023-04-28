package com.hartwig.actin.algo.evaluation.vitalfunction

import com.hartwig.actin.clinical.datamodel.VitalFunction
import com.hartwig.actin.clinical.datamodel.VitalFunctionCategory
import org.junit.Assert
import org.junit.Test

class VitalFunctionFunctionsTest {
    @Test
    fun canSelectMedianFunction() {
        val vitalFunctions: MutableList<VitalFunction> = mutableListOf()
        val builder = VitalFunctionTestFactory.vitalFunction().category(VitalFunctionCategory.HEART_RATE)
        vitalFunctions.add(builder.value(1.0).build())
        Assert.assertEquals(1.0, VitalFunctionFunctions.selectMedianFunction(vitalFunctions).value(), EPSILON)
        vitalFunctions.add(builder.value(2.0).build())
        Assert.assertEquals(1.0, VitalFunctionFunctions.selectMedianFunction(vitalFunctions).value(), EPSILON)
        vitalFunctions.add(builder.value(3.0).build())
        Assert.assertEquals(2.0, VitalFunctionFunctions.selectMedianFunction(vitalFunctions).value(), EPSILON)
    }

    @Test
    fun canDetermineMedianValue() {
        val vitalFunctions: MutableList<VitalFunction> = mutableListOf()
        val builder = VitalFunctionTestFactory.vitalFunction().category(VitalFunctionCategory.HEART_RATE)
        vitalFunctions.add(builder.value(1.0).build())
        Assert.assertEquals(1.0, VitalFunctionFunctions.determineMedianValue(vitalFunctions), EPSILON)
        vitalFunctions.add(builder.value(2.0).build())
        Assert.assertEquals(1.5, VitalFunctionFunctions.determineMedianValue(vitalFunctions), EPSILON)
        vitalFunctions.add(builder.value(3.0).build())
        Assert.assertEquals(2.0, VitalFunctionFunctions.determineMedianValue(vitalFunctions), EPSILON)
    }

    companion object {
        private const val EPSILON = 1.0E-10
    }
}