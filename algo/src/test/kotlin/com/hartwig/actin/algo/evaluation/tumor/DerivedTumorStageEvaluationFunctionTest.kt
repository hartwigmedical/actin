package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.TestPatientFactory
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
    private val properTestPatientRecord = TestPatientFactory.createProperTestPatientRecord()
    private val minimalTestPatientRecord = TestPatientFactory.createMinimalTestPatientRecord()

    private val evaluationFunction: EvaluationFunction = mockk()
    private val derivedFunction = DerivedTumorStageEvaluationFunction(evaluationFunction)

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
        every { evaluationFunction.evaluate(withStageAndDerivedStages(TumorStage.I)) } returns EvaluationTestFactory.withResult(
            EvaluationResult.PASS
        )
        every { evaluationFunction.evaluate(withStageAndDerivedStages(TumorStage.II)) } returns EvaluationTestFactory.withResult(
            EvaluationResult.PASS
        )
        assertEvaluation(EvaluationResult.PASS, derivedFunction.evaluate(withStageAndDerivedStages()))
    }

    @Test
    fun `Should evaluate pass when multiple derived and at least one passes`() {
        every {
            evaluationFunction.evaluate(withStageAndDerivedStages(TumorStage.I))
        } returns EvaluationTestFactory.withResult(EvaluationResult.PASS)
        every {
            evaluationFunction.evaluate(withStageAndDerivedStages(TumorStage.II))
        } returns EvaluationTestFactory.withResult(EvaluationResult.FAIL)
        assertEvaluation(EvaluationResult.UNDETERMINED, derivedFunction.evaluate(withStageAndDerivedStages()))
    }

    @Test
    fun `Should evaluate fail when multiple derived and no pass or warn`() {
        every {
            evaluationFunction.evaluate(withStageAndDerivedStages(TumorStage.I))
        } returns EvaluationTestFactory.withResult(EvaluationResult.UNDETERMINED)
        every {
            evaluationFunction.evaluate(withStageAndDerivedStages(TumorStage.II))
        } returns EvaluationTestFactory.withResult(EvaluationResult.UNDETERMINED)
        assertEvaluation(EvaluationResult.FAIL, derivedFunction.evaluate(withStageAndDerivedStages()))
    }

    @Test
    fun `Should evaluate warn when multiple derived and at least one warn and no pass`() {
        every {
            evaluationFunction.evaluate(withStageAndDerivedStages(TumorStage.I))
        } returns EvaluationTestFactory.withResult(EvaluationResult.WARN)
        every {
            evaluationFunction.evaluate(withStageAndDerivedStages(TumorStage.II))
        } returns EvaluationTestFactory.withResult(EvaluationResult.UNDETERMINED)
        assertEvaluation(EvaluationResult.WARN, derivedFunction.evaluate(withStageAndDerivedStages()))
    }

    @Test
    fun `Should evaluate not evaluated when multiple derived and all are not evaluated`() {
        every {
            evaluationFunction.evaluate(withStageAndDerivedStages(TumorStage.I))
        } returns EvaluationTestFactory.withResult(EvaluationResult.NOT_EVALUATED)
        every {
            evaluationFunction.evaluate(withStageAndDerivedStages(TumorStage.II))
        } returns EvaluationTestFactory.withResult(EvaluationResult.NOT_EVALUATED)
        assertEvaluation(EvaluationResult.NOT_EVALUATED, derivedFunction.evaluate(withStageAndDerivedStages()))
    }

    private fun assertSingleStageWithResult(expectedResult: EvaluationResult) {
        every {
            evaluationFunction.evaluate(withStageAndDerivedStages(TumorStage.I, setOf(TumorStage.I)))
        } returns EvaluationTestFactory.withResult(expectedResult)
        assertEvaluation(expectedResult, derivedFunction.evaluate(withStageAndDerivedStages(derivedStages = setOf(TumorStage.I))))
    }

    private fun withStageAndDerivedStages(
        newStage: TumorStage? = null,
        derivedStages: Set<TumorStage>? = setOf(TumorStage.I, TumorStage.II)
    ): PatientRecord {
        return minimalTestPatientRecord.copy(
            tumor = minimalTestPatientRecord.tumor.copy(stage = newStage, derivedStages = derivedStages)
        )
    }
}