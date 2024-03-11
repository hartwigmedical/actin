package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.TestDataFactory
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.datamodel.EvaluationTestFactory
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.clinical.datamodel.TumorStage
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class DerivedTumorStageEvaluationFunctionTest {
    private val properTestPatientRecord = TestDataFactory.createProperTestPatientRecord()
    private val minimalTestPatientRecord = TestDataFactory.createMinimalTestPatientRecord()
    private val tumorStageDerivationFunction: TumorStageDerivationFunction = mockk()
    private val evaluationFunction: EvaluationFunction = mockk()
    private val derivedFunction = DerivedTumorStageEvaluationFunction(tumorStageDerivationFunction, evaluationFunction)

    @Test
    fun `Should return original function when tumor details not null`() {
        val originalEvaluation = EvaluationTestFactory.withResult(EvaluationResult.PASS)
        every { evaluationFunction.evaluate(properTestPatientRecord) } returns originalEvaluation
        assertThat(derivedFunction.evaluate(properTestPatientRecord)).isEqualTo(originalEvaluation)
    }

    @Test
    fun `Should return original function when no derived stages possible`() {
        val originalEvaluation = EvaluationTestFactory.withResult(EvaluationResult.UNDETERMINED)
        every { evaluationFunction.evaluate(minimalTestPatientRecord) } returns originalEvaluation
        every { tumorStageDerivationFunction.apply(minimalTestPatientRecord.tumor) } returns emptyList()
        assertThat(derivedFunction.evaluate(minimalTestPatientRecord)).isEqualTo(originalEvaluation)
    }

    @Test
    fun `Should follow evaluation when any single inferred stage`() {
        for (evaluationResult in EvaluationResult.values()) {
            assertSingleStageWithResult(evaluationResult)
        }
    }

    @Test
    fun `Should evaluate pass when multiple derived stages all evaluate pass`() {
        every { tumorStageDerivationFunction.apply(minimalTestPatientRecord.tumor) } returns listOf(TumorStage.I, TumorStage.II)
        every { evaluationFunction.evaluate(withStage(TumorStage.I)) } returns EvaluationTestFactory.withResult(EvaluationResult.PASS)
        every { evaluationFunction.evaluate(withStage(TumorStage.II)) } returns EvaluationTestFactory.withResult(EvaluationResult.PASS)
        val evaluation = derivedFunction.evaluate(minimalTestPatientRecord)
        assertEvaluation(EvaluationResult.PASS, evaluation)
    }

    @Test
    fun `Should evaluate pass when multiple derived and at least one passes`() {
        every { tumorStageDerivationFunction.apply(minimalTestPatientRecord.tumor) } returns listOf(TumorStage.I, TumorStage.II)
        every { evaluationFunction.evaluate(withStage(TumorStage.I)) } returns EvaluationTestFactory.withResult(EvaluationResult.PASS)
        every { evaluationFunction.evaluate(withStage(TumorStage.II)) } returns EvaluationTestFactory.withResult(EvaluationResult.FAIL)
        val evaluation = derivedFunction.evaluate(minimalTestPatientRecord)
        assertEvaluation(EvaluationResult.UNDETERMINED, evaluation)
    }

    @Test
    fun `Should evaluate fail when multiple derived and no pass or warn`() {
        every { tumorStageDerivationFunction.apply(minimalTestPatientRecord.tumor) } returns listOf(TumorStage.I, TumorStage.II)
        every { evaluationFunction.evaluate(withStage(TumorStage.I)) } returns EvaluationTestFactory.withResult(EvaluationResult.UNDETERMINED)
        every { evaluationFunction.evaluate(withStage(TumorStage.II)) } returns EvaluationTestFactory.withResult(EvaluationResult.UNDETERMINED)
        val evaluation = derivedFunction.evaluate(minimalTestPatientRecord)
        assertEvaluation(EvaluationResult.FAIL, evaluation)
    }

    @Test
    fun `Should evaluate warn when multiple derived and at least one warn and no pass`() {
        every { tumorStageDerivationFunction.apply(minimalTestPatientRecord.tumor) } returns listOf(TumorStage.I, TumorStage.II)
        every { evaluationFunction.evaluate(withStage(TumorStage.I)) } returns EvaluationTestFactory.withResult(EvaluationResult.WARN)
        every { evaluationFunction.evaluate(withStage(TumorStage.II)) } returns EvaluationTestFactory.withResult(EvaluationResult.UNDETERMINED)
        val evaluation = derivedFunction.evaluate(minimalTestPatientRecord)
        assertEvaluation(EvaluationResult.WARN, evaluation)
    }

    @Test
    fun `Should evaluate not evaluated when multiple derived and all are not evaluated`() {
        every { tumorStageDerivationFunction.apply(minimalTestPatientRecord.tumor) } returns listOf(TumorStage.I, TumorStage.II)
        every { evaluationFunction.evaluate(withStage(TumorStage.I)) } returns EvaluationTestFactory.withResult(EvaluationResult.NOT_EVALUATED)
        every { evaluationFunction.evaluate(withStage(TumorStage.II)) } returns EvaluationTestFactory.withResult(EvaluationResult.NOT_EVALUATED)
        val evaluation = derivedFunction.evaluate(minimalTestPatientRecord)
        assertEvaluation(EvaluationResult.NOT_EVALUATED, evaluation)
    }

    private fun assertSingleStageWithResult(expectedResult: EvaluationResult) {
        every { tumorStageDerivationFunction.apply(minimalTestPatientRecord.tumor) } returns listOf(TumorStage.I)
        every { evaluationFunction.evaluate(withStage(TumorStage.I)) } returns EvaluationTestFactory.withResult(expectedResult)
        val evaluation = derivedFunction.evaluate(minimalTestPatientRecord)
        assertEvaluation(expectedResult, evaluation)
    }

    private fun withStage(newStage: TumorStage?): PatientRecord {
        return minimalTestPatientRecord.copy(
            tumor = minimalTestPatientRecord.tumor.copy(stage = newStage)
        )
    }
}