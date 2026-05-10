package com.hartwig.actin.algo.evaluation.laboratory

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.TestPatientFactory
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.algo.EvaluationTestFactory
import com.hartwig.actin.datamodel.clinical.LabMeasurement
import com.hartwig.actin.datamodel.clinical.LabValue
import io.mockk.every
import io.mockk.mockk
import java.time.LocalDate
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class LabMeasurementEvaluatorTest {

    private val measurement = LabMeasurement.ALBUMIN
    private val record = TestPatientFactory.createMinimalTestWGSPatientRecord()

    @Test
    fun `Should propagate NotFound from selector as undetermined`() {
        assertEvaluation(EvaluationResult.UNDETERMINED,
            evaluator(notFoundSelector).evaluate(record))
    }

    @Test
    fun `Should propagate pass from function when selector returns Found`() {
        assertEvaluation(EvaluationResult.PASS,
            evaluator(foundSelector()).evaluate(record))
    }

    @Test
    fun `Should propagate fail from function when selector returns Found`() {
        assertEvaluation(EvaluationResult.FAIL,
            evaluator(foundSelector(), failingLabEvaluationFunction).evaluate(record))
    }

    @Test
    fun `Should degrade pass to recoverable pass when measurement is before minPassDate`() {
        val oldDate = TEST_DATE.minusDays(5)
        val evaluation = evaluator(foundSelector(oldDate), minPassDate = oldDate.plusDays(1)).evaluate(record)
        assertEvaluation(EvaluationResult.PASS, evaluation)
        assertThat(evaluation.recoverable).isTrue()
    }

    private fun evaluator(
        selector: LabValueSelector,
        function: LabEvaluationFunction = passingLabEvaluationFunction,
        minPassDate: LocalDate = TEST_DATE.plusDays(20)
    ) = LabMeasurementEvaluator(selector, function, TEST_DATE.minusDays(20), minPassDate)

    private val notFoundSelector = mockk<LabValueSelector>().also {
        every { it.select(any(), any()) } returns LabValueSelectionResult.NotFound(
            EvaluationFactory.recoverableUndetermined("not found")
        )
    }

    private fun foundSelector(date: LocalDate = TEST_DATE) = mockk<LabValueSelector>().also {
        every { it.select(any(), any()) } returns LabValueSelectionResult.Found(
            mapOf(measurement to LabTestFactory.create(measurement, date = date))
        )
    }

    companion object {
        private val TEST_DATE = LocalDate.of(2020, 4, 20)

        private val passingLabEvaluationFunction: LabEvaluationFunction = object : LabEvaluationFunction {
            override fun evaluate(record: PatientRecord, labMeasurement: LabMeasurement, labValue: LabValue): Evaluation =
                EvaluationTestFactory.withResult(EvaluationResult.PASS)
        }

        private val failingLabEvaluationFunction: LabEvaluationFunction = object : LabEvaluationFunction {
            override fun evaluate(record: PatientRecord, labMeasurement: LabMeasurement, labValue: LabValue): Evaluation =
                EvaluationTestFactory.withResult(EvaluationResult.FAIL)
        }
    }
}
