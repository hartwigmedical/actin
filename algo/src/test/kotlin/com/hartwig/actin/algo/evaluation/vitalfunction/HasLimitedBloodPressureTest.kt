package com.hartwig.actin.algo.evaluation.vitalfunction

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.algo.evaluation.vitalfunction.VitalFunctionTestFactory.vitalFunction
import com.hartwig.actin.clinical.datamodel.VitalFunctionCategory
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime

class HasLimitedBloodPressureTest {

    private val function = HasLimitedBloodPressure(BloodPressureCategory.SYSTOLIC, 140, LocalDate.of(2023, 12, 1))

    @Test
    fun `Should fail when systolic blood pressure above maximum and outside margin of error`() {
        val bloodPressures = listOf(
            vitalFunction(
                category = VitalFunctionCategory.NON_INVASIVE_BLOOD_PRESSURE,
                subcategory = BloodPressureCategory.SYSTOLIC.display(),
                date = LocalDateTime.of(2023, 12, 2, 0, 0),
                value = 150.0,
                valid = true
            )
        )
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(VitalFunctionTestFactory.withVitalFunctions(bloodPressures)))
    }

}
