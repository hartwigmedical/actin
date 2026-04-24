package com.hartwig.actin.algo.evaluation.laboratory

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.EvaluationFunctionFactory
import com.hartwig.actin.algo.evaluation.RuleMappingResourcesTestFactory
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.LabMeasurement
import com.hartwig.actin.datamodel.trial.DoubleParameter
import com.hartwig.actin.datamodel.trial.EligibilityFunction
import com.hartwig.actin.datamodel.trial.Parameter
import com.hartwig.actin.trial.input.EligibilityRule
import java.time.LocalDate
import org.junit.jupiter.api.Test

class LaboratoryRuleMapperTest {

    private val factory = EvaluationFunctionFactory.create(RuleMappingResourcesTestFactory.create())
    private val today = LocalDate.now()

    private fun evaluatorFor(rule: EligibilityRule, vararg params: Parameter<*>): EvaluationFunction =
        factory.create(EligibilityFunction(rule = rule.name, parameters = params.toList()))

    // HAS_MEASURED_CREATININE_CLEARANCE_OF_AT_LEAST_X

    private val measuredClearanceEvaluator = evaluatorFor(
        EligibilityRule.HAS_MEASURED_CREATININE_CLEARANCE_OF_AT_LEAST_X, DoubleParameter(60.0)
    )

    @Test
    fun `Should pass HAS_MEASURED_CREATININE_CLEARANCE_OF_AT_LEAST_X using direct measurement when CREATININE_CLEARANCE_24H is present`() {
        val record = LabTestFactory.withLabValue(
            LabTestFactory.create(LabMeasurement.CREATININE_CLEARANCE_24H, value = 75.0, date = today)
        )
        assertEvaluation(EvaluationResult.PASS, measuredClearanceEvaluator.evaluate(record))
    }

    @Test
    fun `Should fail HAS_MEASURED_CREATININE_CLEARANCE_OF_AT_LEAST_X when direct measurement fails even if calculated fallbacks would pass`() {
        val record = LabTestFactory.withLabValues(
            listOf(
                LabTestFactory.create(LabMeasurement.CREATININE_CLEARANCE_24H, value = 45.0, date = today),
                LabTestFactory.create(LabMeasurement.CREATININE_24U, value = 10.0, date = today),
                LabTestFactory.create(LabMeasurement.CREATININE, value = 70.0, date = today)
            )
        )
        assertEvaluation(EvaluationResult.FAIL, measuredClearanceEvaluator.evaluate(record))
    }

    @Test
    fun `Should pass HAS_MEASURED_CREATININE_CLEARANCE_OF_AT_LEAST_X using daily-total calculation when direct measurement is absent`() {
        val record = LabTestFactory.withLabValues(
            listOf(
                LabTestFactory.create(LabMeasurement.CREATININE_24U, value = 10.0, date = today),
                LabTestFactory.create(LabMeasurement.CREATININE, value = 70.0, date = today)
            )
        )
        assertEvaluation(EvaluationResult.PASS, measuredClearanceEvaluator.evaluate(record))
    }

    @Test
    fun `Should pass HAS_MEASURED_CREATININE_CLEARANCE_OF_AT_LEAST_X using urine-concentration calculation when direct and daily-total are absent`() {
        val record = LabTestFactory.withLabValues(
            listOf(
                LabTestFactory.create(LabMeasurement.CREATININE_URINE, value = 5.0, date = today),
                LabTestFactory.create(LabMeasurement.URINE_VOLUME_24H, value = 1500.0, date = today),
                LabTestFactory.create(LabMeasurement.CREATININE, value = 70.0, date = today)
            )
        )
        assertEvaluation(EvaluationResult.PASS, measuredClearanceEvaluator.evaluate(record))
    }

    @Test
    fun `Should be undetermined for HAS_MEASURED_CREATININE_CLEARANCE_OF_AT_LEAST_X when all measurement paths are absent`() {
        assertEvaluation(EvaluationResult.UNDETERMINED, measuredClearanceEvaluator.evaluate(LabTestFactory.withLabValues(emptyList())))
    }

}
