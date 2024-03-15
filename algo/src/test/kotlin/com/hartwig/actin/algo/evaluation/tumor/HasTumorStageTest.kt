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
        every { derivationFunction.apply(any()) } returns null
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
    fun `Should pass when one stage of set to match passes`() {
        val patientRecord = TumorTestFactory.withTumorStage(null)
        val tumorDetails = patientRecord.clinical.tumor
        val derivationFunction = mockk<TumorStageDerivationFunction>()
        every { derivationFunction.apply(tumorDetails) } returns setOf(TumorStage.III, TumorStage.IIIB)
        assertEvaluation(EvaluationResult.PASS, HasTumorStage(derivationFunction, setOf(TumorStage.III, TumorStage.IV)).evaluate(patientRecord))
    }

    @Test
    fun `Should pass when all stages of set to match pass`() {
        val patientRecord = TumorTestFactory.withTumorStage(null)
        val tumorDetails = patientRecord.clinical.tumor
        val derivationFunction = mockk<TumorStageDerivationFunction>()
        every { derivationFunction.apply(tumorDetails) } returns setOf(TumorStage.III, TumorStage.IV)
        assertEvaluation(EvaluationResult.PASS, HasTumorStage(derivationFunction, setOf(TumorStage.III, TumorStage.IV)).evaluate(patientRecord))
    }

    @Test
    fun `Should evaluate undetermined when multiple derived tumor stages where one passes`() {
        assertDerivedEvaluation(EvaluationResult.UNDETERMINED, TumorStage.III, TumorStage.II)
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
        return HasTumorStage(derivationFunction, setOf(TumorStage.III))
    }

    private fun assertDerivedEvaluation(expectedResult: EvaluationResult, vararg derivedStages: TumorStage) {
        val patientRecord = TumorTestFactory.withTumorStage(null)
        val tumorDetails = patientRecord.clinical.tumor
        val derivationFunction = mockk<TumorStageDerivationFunction>()
        every { derivationFunction.apply(tumorDetails) } returns setOf(*derivedStages)
        assertEvaluation(expectedResult, tumorStageFunction(derivationFunction).evaluate(patientRecord))
    }
}