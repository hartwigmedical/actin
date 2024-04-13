package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.clinical.datamodel.TumorStage
import org.assertj.core.api.Assertions
import org.junit.Test

class HasTumorStageTest {
    val hasTumorStage = HasTumorStage(setOf(TumorStage.III))

    @Test
    fun `Should throw an exception when the set of stages to match is empty`() {
        val patientRecord = TumorTestFactory.withTumorStageAndDerivedStages(null, setOf(TumorStage.III, TumorStage.IIIB))
        Assertions.assertThatIllegalStateException().isThrownBy { HasTumorStage(emptySet()).evaluate(patientRecord) }
            .withMessage("No stages to match configured")
    }

    @Test
    fun `Should evaluate normally when tumor stage exists`() {
        assertEvaluation(EvaluationResult.FAIL, hasTumorStage.evaluate(TumorTestFactory.withTumorStageAndDerivedStages(null)))
        assertEvaluation(EvaluationResult.PASS, hasTumorStage.evaluate(TumorTestFactory.withTumorStageAndDerivedStages(TumorStage.III)))
        assertEvaluation(EvaluationResult.PASS, hasTumorStage.evaluate(TumorTestFactory.withTumorStageAndDerivedStages(TumorStage.IIIB)))
        assertEvaluation(EvaluationResult.FAIL, hasTumorStage.evaluate(TumorTestFactory.withTumorStageAndDerivedStages(TumorStage.IV)))
    }

    @Test
    fun `Should follow non-derived evaluation when single derived tumor`() {
        assertDerivedEvaluation(EvaluationResult.PASS, TumorStage.III)
        assertDerivedEvaluation(EvaluationResult.PASS, TumorStage.IIIB)
        assertDerivedEvaluation(EvaluationResult.FAIL, TumorStage.IV)
    }

    @Test
    fun `Should pass when one stage of set to match passes`() {
        val patientRecord = TumorTestFactory.withTumorStageAndDerivedStages(null, setOf(TumorStage.III, TumorStage.IIIB))
        assertEvaluation(
            EvaluationResult.PASS,
            HasTumorStage(setOf(TumorStage.III, TumorStage.IV)).evaluate(patientRecord)
        )
    }

    @Test
    fun `Should pass when all stages of set to match pass`() {
        val patientRecord = TumorTestFactory.withTumorStageAndDerivedStages(null, setOf(TumorStage.III, TumorStage.IV))
        assertEvaluation(
            EvaluationResult.PASS,
            HasTumorStage(setOf(TumorStage.III, TumorStage.IV)).evaluate(patientRecord)
        )
    }

    @Test
    fun `Should evaluate undetermined when multiple derived tumor stages where one passes`() {
        assertDerivedEvaluation(EvaluationResult.UNDETERMINED, TumorStage.III, TumorStage.II)
    }

    @Test
    fun `Should display correct undetermined message with derived stages`() {
        val patientRecord = TumorTestFactory.withTumorStageAndDerivedStages(null, setOf(TumorStage.III, TumorStage.IV))
        Assertions.assertThat(hasTumorStage.evaluate(patientRecord).undeterminedGeneralMessages).containsExactly(
            "Unknown if tumor stage is III (data missing) - derived III or IV based on lesions"
        )
    }

    @Test
    fun `Should evaluate fail when multiple derived tumor stages where all fail`() {
        assertDerivedEvaluation(EvaluationResult.FAIL, TumorStage.I, TumorStage.II)
    }

    private fun assertDerivedEvaluation(expectedResult: EvaluationResult, vararg derivedStages: TumorStage) {
        val patientRecord = TumorTestFactory.withTumorStageAndDerivedStages(null, setOf(*derivedStages))
        assertEvaluation(expectedResult, hasTumorStage.evaluate(patientRecord))
    }
}