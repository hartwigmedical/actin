package com.hartwig.actin.algo.evaluation.laboratory

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.LabMeasurement
import org.junit.jupiter.api.Test
import java.time.LocalDate

class MeasuredCreatinineClearanceEvaluatorTest {

    private val today = LocalDate.now()
    private val minValidDate = today.minusDays(90)
    private val minPassDate = today.minusDays(30)

    private fun evaluator(minValue: Double = 60.0) =
        MeasuredCreatinineClearanceEvaluator(minValue, minValidDate, minPassDate)

    @Test
    fun `Should pass using direct measurement when CREATININE_CLEARANCE_24H is present`() {
        val record = LabTestFactory.withLabValue(
            LabTestFactory.create(LabMeasurement.CREATININE_CLEARANCE_24H, value = 75.0, date = today)
        )
        assertEvaluation(EvaluationResult.PASS, evaluator().evaluate(record))
    }

    @Test
    fun `Should fail on direct measurement even if calculated fallbacks would pass`() {
        val record = LabTestFactory.withLabValues(listOf(
            LabTestFactory.create(LabMeasurement.CREATININE_CLEARANCE_24H, value = 45.0, date = today),
            LabTestFactory.create(LabMeasurement.CREATININE_24U, value = 10.0, date = today),
            LabTestFactory.create(LabMeasurement.CREATININE, value = 70.0, date = today)
        ))
        assertEvaluation(EvaluationResult.FAIL, evaluator().evaluate(record))
    }

    @Test
    fun `Should pass using daily-total calculation when direct measurement is absent`() {
        val record = LabTestFactory.withLabValues(listOf(
            LabTestFactory.create(LabMeasurement.CREATININE_24U, value = 10.0, date = today),
            LabTestFactory.create(LabMeasurement.CREATININE, value = 70.0, date = today)
        ))
        assertEvaluation(EvaluationResult.PASS, evaluator().evaluate(record))
    }

    @Test
    fun `Should pass using urine-concentration calculation when direct and daily-total are absent`() {
        val record = LabTestFactory.withLabValues(listOf(
            LabTestFactory.create(LabMeasurement.CREATININE_URINE, value = 5.0, date = today),
            LabTestFactory.create(LabMeasurement.URINE_VOLUME_24H, value = 1500.0, date = today),
            LabTestFactory.create(LabMeasurement.CREATININE, value = 70.0, date = today)
        ))
        assertEvaluation(EvaluationResult.PASS, evaluator().evaluate(record))
    }

    @Test
    fun `Should be undetermined when all measurement paths are absent`() {
        assertEvaluation(EvaluationResult.UNDETERMINED, evaluator().evaluate(LabTestFactory.withLabValues(emptyList())))
    }
}
