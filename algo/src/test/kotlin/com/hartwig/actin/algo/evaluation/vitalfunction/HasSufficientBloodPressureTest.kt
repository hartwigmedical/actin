package com.hartwig.actin.algo.evaluation.vitalfunction

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.clinical.datamodel.ImmutableVitalFunction
import com.hartwig.actin.clinical.datamodel.VitalFunctionCategory
import org.junit.Test
import java.time.LocalDate

class HasSufficientBloodPressureTest {

    private val referenceDate = LocalDate.of(2020, 11, 19)
    private val function = HasSufficientBloodPressure(BloodPressureCategory.SYSTOLIC, 100)

    @Test
    fun `Should fail when systolic blood pressure under minimum`() {
        val bloodPressures = listOf(
            systolic().date(referenceDate).value(95.0).build())

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