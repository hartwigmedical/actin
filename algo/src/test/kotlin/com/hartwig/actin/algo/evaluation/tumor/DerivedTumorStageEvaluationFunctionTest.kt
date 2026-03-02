package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.TestPatientFactory
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.algo.EvaluationTestFactory
import com.hartwig.actin.datamodel.clinical.TumorStage
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class DerivedTumorStageEvaluationFunctionTest {
    
    private val properTestPatientRecord = TestPatientFactory.createProperTestPatientRecord()
    private val minimalTestPatientRecord = TestPatientFactory.createMinimalTestWGSPatientRecord()
    private val evaluationFunction: EvaluationFunction = mockk()
    private val derivedFunction = DerivedTumorStageEvaluationFunction(evaluationFunction, "something")

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
        for (evaluationResult in EvaluationResult.entries) {
            assertSingleStageWithResult(evaluationResult)
        }
    }

    @Test
    fun `Should pass when multiple derived stages all evaluate pass`() {
        every { evaluationFunction.evaluate(withStageAndDerivedStages(TumorStage.I)) } returns EvaluationTestFactory.withResult(
            EvaluationResult.PASS
        )
        every { evaluationFunction.evaluate(withStageAndDerivedStages(TumorStage.II)) } returns EvaluationTestFactory.withResult(
            EvaluationResult.PASS
        )
        assertEvaluation(EvaluationResult.PASS, derivedFunction.evaluate(withStageAndDerivedStages()))
    }

    @Test
    fun `Should be not evaluated when multiple derived stages all evaluate not evaluated`() {
        every {
            evaluationFunction.evaluate(withStageAndDerivedStages(TumorStage.I))
        } returns EvaluationTestFactory.withResult(EvaluationResult.NOT_EVALUATED)
        every {
            evaluationFunction.evaluate(withStageAndDerivedStages(TumorStage.II))
        } returns EvaluationTestFactory.withResult(EvaluationResult.NOT_EVALUATED)
        assertEvaluation(EvaluationResult.NOT_EVALUATED, derivedFunction.evaluate(withStageAndDerivedStages()))
    }

    @Test
    fun `Should be undetermined when multiple derived stages all evaluate undetermined`() {
        every {
            evaluationFunction.evaluate(withStageAndDerivedStages(TumorStage.I))
        } returns EvaluationTestFactory.withResult(EvaluationResult.UNDETERMINED)
        every {
            evaluationFunction.evaluate(withStageAndDerivedStages(TumorStage.II))
        } returns EvaluationTestFactory.withResult(EvaluationResult.UNDETERMINED)
        assertEvaluation(EvaluationResult.UNDETERMINED, derivedFunction.evaluate(withStageAndDerivedStages()))
    }

    @Test
    fun `Should warn when multiple derived stages all evaluate warn`() {
        every {
            evaluationFunction.evaluate(withStageAndDerivedStages(TumorStage.I))
        } returns EvaluationTestFactory.withResult(EvaluationResult.WARN)
        every {
            evaluationFunction.evaluate(withStageAndDerivedStages(TumorStage.II))
        } returns EvaluationTestFactory.withResult(EvaluationResult.WARN)
        assertEvaluation(EvaluationResult.WARN, derivedFunction.evaluate(withStageAndDerivedStages()))
    }

    @Test
    fun `Should fail when multiple derived stages all evaluate fail`() {
        every {
            evaluationFunction.evaluate(withStageAndDerivedStages(TumorStage.I))
        } returns EvaluationTestFactory.withResult(EvaluationResult.FAIL)
        every {
            evaluationFunction.evaluate(withStageAndDerivedStages(TumorStage.II))
        } returns EvaluationTestFactory.withResult(EvaluationResult.FAIL)
        assertEvaluation(EvaluationResult.FAIL, derivedFunction.evaluate(withStageAndDerivedStages()))
    }

    @Test
    fun `Should be undetermined when multiple derived with fixed message prefix`() {
        every {
            evaluationFunction.evaluate(withStageAndDerivedStages(TumorStage.I))
        } returns EvaluationTestFactory.withResult(EvaluationResult.PASS)
        every {
            evaluationFunction.evaluate(withStageAndDerivedStages(TumorStage.II))
        } returns EvaluationTestFactory.withResult(EvaluationResult.FAIL)

        val result = derivedFunction.evaluate(withStageAndDerivedStages())
        assertEvaluation(EvaluationResult.UNDETERMINED, result)
        assertThat(result.undeterminedMessagesStrings()).containsExactly("Undetermined if patient has something")
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