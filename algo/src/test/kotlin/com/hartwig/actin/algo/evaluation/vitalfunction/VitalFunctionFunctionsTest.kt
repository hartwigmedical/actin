package com.hartwig.actin.algo.evaluation.vitalfunction

import com.hartwig.actin.algo.evaluation.vitalfunction.VitalFunctionFunctions.determineMedianValue
import com.hartwig.actin.algo.evaluation.vitalfunction.VitalFunctionFunctions.selectMedianFunction
import com.hartwig.actin.clinical.datamodel.VitalFunctionCategory
import org.junit.Assert
import org.junit.Test

class VitalFunctionFunctionsTest {

    // Testing selectMedianFunction
    @Test
    fun `Should sort values and select correct median`() {
        val builder = VitalFunctionTestFactory.vitalFunction().category(VitalFunctionCategory.HEART_RATE)
        val vitalFunctions =
            listOf(builder.value(2.0).valid(true).build(), builder.value(1.0).valid(true).build(), builder.value(3.0).valid(true).build())
        Assert.assertEquals(2.0, selectMedianFunction(vitalFunctions).value(), EPSILON)
    }

    @Test
    fun `Should select one below ideal median when list is even`() {
        val builder = VitalFunctionTestFactory.vitalFunction().category(VitalFunctionCategory.HEART_RATE)
        val vitalFunctions =
            listOf(
                builder.value(1.7).valid(true).build(),
                builder.value(3.0).valid(true).build(),
                builder.value(1.0).valid(true).build(),
                builder.value(2.0).valid(true).build()
            )
        Assert.assertEquals(1.7, selectMedianFunction(vitalFunctions).value(), EPSILON)
    }

    // Testing determineMedianValue
    @Test
    fun `Should determine exact median value`() {
        val builder = VitalFunctionTestFactory.vitalFunction().category(VitalFunctionCategory.HEART_RATE)
        val vitalFunctions = listOf(
            builder.value(1.0).valid(true).build(),
            builder.value(3.0).valid(true).build()
        )
        Assert.assertEquals(2.0, determineMedianValue(vitalFunctions), EPSILON)
    }

    companion object {
        private const val EPSILON = 1.0E-10
    }
}