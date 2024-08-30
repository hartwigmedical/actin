package com.hartwig.actin.algo.evaluation.vitalfunction

import com.hartwig.actin.algo.evaluation.vitalfunction.VitalFunctionFunctions.determineMedianValue
import com.hartwig.actin.algo.evaluation.vitalfunction.VitalFunctionFunctions.selectMedianFunction
import com.hartwig.actin.algo.evaluation.vitalfunction.VitalFunctionTestFactory.vitalFunction
import com.hartwig.actin.datamodel.clinical.VitalFunctionCategory
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.data.Offset
import org.junit.Test

private const val EPSILON = 1.0E-10

class VitalFunctionFunctionsTest {

    // Testing selectMedianFunction
    @Test
    fun `Should sort values and select correct median`() {
        val vitalFunctions =
            listOf(
                vitalFunction(VitalFunctionCategory.HEART_RATE, value = 2.0),
                vitalFunction(VitalFunctionCategory.HEART_RATE, value = 1.0),
                vitalFunction(VitalFunctionCategory.HEART_RATE, value = 3.0)
            )
        assertThat(selectMedianFunction(vitalFunctions).value).isEqualTo(2.0, Offset.offset(EPSILON))
    }

    @Test
    fun `Should select one below ideal median when list is even`() {
        val vitalFunctions =
            listOf(
                vitalFunction(VitalFunctionCategory.HEART_RATE, value = 1.7),
                vitalFunction(VitalFunctionCategory.HEART_RATE, value = 3.0),
                vitalFunction(VitalFunctionCategory.HEART_RATE, value = 1.0),
                vitalFunction(VitalFunctionCategory.HEART_RATE, value = 2.0)
            )
        assertThat(selectMedianFunction(vitalFunctions).value).isEqualTo(1.7, Offset.offset(EPSILON))
    }

    // Testing determineMedianValue
    @Test
    fun `Should determine exact median value`() {
        val vitalFunctions = listOf(
            vitalFunction(VitalFunctionCategory.HEART_RATE, value = 1.0),
            vitalFunction(VitalFunctionCategory.HEART_RATE, value = 3.0)
        )
        assertThat(determineMedianValue(vitalFunctions)).isEqualTo(2.0, Offset.offset(EPSILON))
    }
}