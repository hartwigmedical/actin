package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.clinical.datamodel.TumorStage
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions
import org.junit.Test

class HasTumorStageTest {

    @Test
    fun shouldEvaluateNormallyWhenTumorStageExists() {
        val derivationFunction = mockk<TumorStageDerivationFunction>()
        every { derivationFunction.apply(any()) } returns emptySet()
        val victim = tumorStageFunction(derivationFunction)
        assertEvaluation(EvaluationResult.FAIL, victim.evaluate(TumorTestFactory.withTumorStage(null)))
        assertEvaluation(EvaluationResult.PASS, victim.evaluate(TumorTestFactory.withTumorStage(TumorStage.III)))
        assertEvaluation(EvaluationResult.PASS, victim.evaluate(TumorTestFactory.withTumorStage(TumorStage.IIIB)))
        assertEvaluation(EvaluationResult.FAIL, victim.evaluate(TumorTestFactory.withTumorStage(TumorStage.IV)))
    }

    @Test
    fun shouldFollowNonDerivedEvaluationWhenSingleDerivedTumor() {
        assertDerivedEvaluation(EvaluationResult.PASS, TumorStage.III)
        assertDerivedEvaluation(EvaluationResult.PASS, TumorStage.IIIB)
        assertDerivedEvaluation(EvaluationResult.FAIL, TumorStage.IV)
    }

    @Test
    fun shouldEvaluateUndeterminedWhenMultipleDerivedTumorStagesWhereOnePasses() {
        assertDerivedEvaluation(EvaluationResult.UNDETERMINED, TumorStage.III, TumorStage.IIIB)
    }

    @Test
    fun `Should display correct undetermined message with derived stages`() {
        val patientRecord = TumorTestFactory.withTumorStage(null)
        val tumorDetails = patientRecord.clinical.tumor
        val derivationFunction = mockk<TumorStageDerivationFunction>()
        every { derivationFunction.apply(tumorDetails) } returns setOf(TumorStage.III, TumorStage.IV)
        Assertions.assertThat((tumorStageFunction(derivationFunction).evaluate(patientRecord)).undeterminedGeneralMessages).containsExactly(
            "Missing tumor stage details - assumed III or IV based on lesions"
        )
    }

    @Test
    fun shouldEvaluateFailWhenMultipleDerivedTumorStagesWhereAllFail() {
        assertDerivedEvaluation(EvaluationResult.FAIL, TumorStage.I, TumorStage.II)
    }

    private fun tumorStageFunction(derivationFunction: TumorStageDerivationFunction): HasTumorStage {
        return HasTumorStage(derivationFunction, TumorStage.III)
    }

    private fun assertDerivedEvaluation(expectedResult: EvaluationResult, vararg derivedStages: TumorStage) {
        val patientRecord = TumorTestFactory.withTumorStage(null)
        val tumorDetails = patientRecord.clinical.tumor
        val derivationFunction = mockk<TumorStageDerivationFunction>()
        every { derivationFunction.apply(tumorDetails) } returns setOf(*derivedStages)
        assertEvaluation(expectedResult, tumorStageFunction(derivationFunction).evaluate(patientRecord))
    }
}