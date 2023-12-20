package com.hartwig.actin.algo.evaluation.vitalfunction

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.clinical.datamodel.ImmutableVitalFunction
import com.hartwig.actin.clinical.datamodel.VitalFunctionCategory
import org.junit.Test
import java.time.LocalDateTime

class HasLimitedBloodPressureTest {

    private val referenceDate = LocalDateTime.of(2020, 11, 19, 12, 30, 0)
    private val function = HasLimitedBloodPressure(BloodPressureCategory.SYSTOLIC, 140)

    @Test
    fun `Should fail when systolic blood pressure above maximum`() {
        val bloodPressures = listOf(systolic().date(referenceDate).value(145.0).build())
        assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(VitalFunctionTestFactory.withVitalFunctions(bloodPressures))
        )
    }

    companion object {
        private fun systolic(): ImmutableVitalFunction.Builder {
            return VitalFunctionTestFactory.vitalFunction()
                .category(VitalFunctionCategory.NON_INVASIVE_BLOOD_PRESSURE)
                .subcategory(BloodPressureCategory.SYSTOLIC.display())
        }
    }
}
