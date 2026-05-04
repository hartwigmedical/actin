package com.hartwig.actin.algo.evaluation.laboratory

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.LabMeasurement
import io.mockk.called
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.time.LocalDate
import org.junit.jupiter.api.Test

class MultiLabMeasurementEvaluatorTest {

    private val recentDate = LocalDate.now()
    private val minValidDate = recentDate.minusDays(90)
    private val minPassDate = recentDate.minusDays(30)
    private val measurement1 = LabMeasurement.entries[0]
    private val measurement2 = LabMeasurement.entries[1]

    private val evaluationFunction = mockk<MultiLabEvaluationFunction>()

    @Test
    fun `Should pass when all measurements are on the same date`() {
        every { evaluationFunction.evaluate(any(), any()) } returns EvaluationFactory.recoverablePass("pass")
        val record = recordWith(measurement1 to recentDate, measurement2 to recentDate)
        assertEvaluation(EvaluationResult.PASS, evaluator().evaluate(record))
    }

    @Test
    fun `Should propagate fail from function when all measurements are on the same date`() {
        every { evaluationFunction.evaluate(any(), any()) } returns EvaluationFactory.recoverableFail("fail")
        val record = recordWith(measurement1 to recentDate, measurement2 to recentDate)
        assertEvaluation(EvaluationResult.FAIL, evaluator().evaluate(record))
    }

    @Test
    fun `Should propagate undetermined from function when all measurements are on the same date`() {
        every { evaluationFunction.evaluate(any(), any()) } returns EvaluationFactory.recoverableUndetermined("undetermined")
        val record = recordWith(measurement1 to recentDate, measurement2 to recentDate)
        assertEvaluation(EvaluationResult.UNDETERMINED, evaluator().evaluate(record))
    }

    @Test
    fun `Should be undetermined when measurements are on different dates`() {
        val record = recordWith(measurement1 to recentDate, measurement2 to recentDate.minusDays(2))
        assertEvaluation(EvaluationResult.UNDETERMINED, evaluator().evaluate(record))
    }

    @Test
    fun `Should use older same-date pair when most recent date has incomplete measurements`() {
        val olderDate = recentDate.minusDays(3)
        every { evaluationFunction.evaluate(any(), any()) } returns EvaluationFactory.recoverablePass("pass")
        val record = recordWith(measurement1 to olderDate, measurement2 to olderDate, measurement1 to recentDate)
        assertEvaluation(EvaluationResult.PASS, evaluator().evaluate(record))
        verify { evaluationFunction.evaluate(any(), match { it.values.all { v -> v.date == olderDate } }) }
    }

    @Test
    fun `Should be undetermined when a measurement is missing`() {
        val record = recordWith(measurement1 to recentDate)
        assertEvaluation(EvaluationResult.UNDETERMINED, evaluator().evaluate(record))
        verify { evaluationFunction wasNot called }
    }

    @Test
    fun `Should be undetermined when all values are older than minValidDate`() {
        val oldDate = minValidDate.minusDays(1)
        val record = recordWith(measurement1 to oldDate, measurement2 to oldDate)
        assertEvaluation(EvaluationResult.UNDETERMINED, evaluator().evaluate(record))
        verify { evaluationFunction wasNot called }
    }

    @Test
    fun `Should degrade PASS to recoverable pass when same-date values are before minPassDate`() {
        val oldDate = minPassDate.minusDays(1)
        every { evaluationFunction.evaluate(any(), any()) } returns EvaluationFactory.recoverablePass("pass")
        val record = recordWith(measurement1 to oldDate, measurement2 to oldDate)
        assertEvaluation(EvaluationResult.PASS, evaluator().evaluate(record))
    }

    @Test
    fun `Should pass when requireSameDate=false and measurements are on different dates`() {
        every { evaluationFunction.evaluate(any(), any()) } returns EvaluationFactory.recoverablePass("pass")
        val record = recordWith(measurement1 to recentDate, measurement2 to recentDate.minusDays(2))
        assertEvaluation(EvaluationResult.PASS, evaluator(minValidDate, requireSameDate = false).evaluate(record))
    }

    @Test
    fun `Should pass with most recent valid value per measurement when requireSameDate=false`() {
        every { evaluationFunction.evaluate(any(), any()) } returns EvaluationFactory.recoverablePass("pass")
        val record = recordWith(
            measurement1 to recentDate,
            measurement1 to recentDate.minusDays(5),
            measurement2 to recentDate.minusDays(3)
        )
        assertEvaluation(EvaluationResult.PASS, evaluator(requireSameDate = false).evaluate(record))
        verify { evaluationFunction.evaluate(any(), match { it[measurement1]?.date == recentDate && it[measurement2]?.date == recentDate.minusDays(3) }) }
    }

    @Test
    fun `Should be undetermined when requireSameDate=false and a measurement is entirely absent`() {
        val record = recordWith(measurement1 to recentDate)
        assertEvaluation(EvaluationResult.UNDETERMINED, evaluator(requireSameDate = false).evaluate(record))
        verify { evaluationFunction wasNot called }
    }

    @Test
    fun `Should be undetermined when requireSameDate=false and all values for a measurement are too old`() {
        val oldDate = minValidDate.minusDays(1)
        val record = recordWith(measurement1 to recentDate, measurement2 to oldDate)
        assertEvaluation(EvaluationResult.UNDETERMINED, evaluator(requireSameDate = false).evaluate(record))
        verify { evaluationFunction wasNot called }
    }

    @Test
    fun `Should degrade PASS when requireSameDate=false and oldest measurement is before minPassDate`() {
        every { evaluationFunction.evaluate(any(), any()) } returns EvaluationFactory.recoverablePass("pass")
        val record = recordWith(measurement1 to recentDate, measurement2 to minPassDate.minusDays(5))
        assertEvaluation(EvaluationResult.PASS, evaluator(requireSameDate = false).evaluate(record))
    }

    private fun evaluator(minValid: LocalDate = minValidDate, requireSameDate: Boolean = true) = MultiLabMeasurementEvaluator(
        measurements = setOf(measurement1, measurement2),
        function = evaluationFunction,
        minValidDate = minValid,
        minPassDate = minPassDate,
        requireSameDate = requireSameDate
    )

    private fun recordWith(vararg pairs: Pair<LabMeasurement, LocalDate>) =
        LabTestFactory.withLabValues(pairs.map { (measurement, date) -> LabTestFactory.create(measurement, date = date) })

}
