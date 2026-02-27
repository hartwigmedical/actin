package com.hartwig.actin.algo.evaluation.vitalfunction

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.VitalFunctionCategory
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime

class HasSufficientBloodPressureTest {

    private val function = HasSufficientBloodPressure(BloodPressureCategory.SYSTOLIC, 140, LocalDate.of(2023, 12, 1))

    @Test
    fun `Should fail when systolic blood pressure under minimum`() {
        val bloodPressures = listOf(
            VitalFunctionTestFactory.vitalFunction(
                category = VitalFunctionCategory.NON_INVASIVE_BLOOD_PRESSURE,
                subcategory = BloodPressureCategory.SYSTOLIC.display(),
                date = LocalDateTime.of(2023, 12, 2, 0, 0),
                value = 95.0
            )
        )
        assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(VitalFunctionTestFactory.withVitalFunctions(bloodPressures))
        )
    }
}