package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.TumorStage
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class HasTumorStageTest {
    private val hasTumorStage = HasTumorStage(setOf(TumorStage.III))

    @Test
    fun `Should throw an exception when the set of stages to match is empty`() {
        val patientRecord = TumorTestFactory.withTumorStageAndDerivedStages(null, setOf(TumorStage.III, TumorStage.IIIB))
        Assertions.assertThatIllegalStateException().isThrownBy { HasTumorStage(emptySet()).evaluate(patientRecord) }
            .withMessage("No stages to match configured")
    }

    @Test
    fun `Should evaluate normally when tumor stage exists`() {
        assertEvaluation(EvaluationResult.PASS, hasTumorStage.evaluate(TumorTestFactory.withTumorStageAndDerivedStages(TumorStage.III)))
        assertEvaluation(EvaluationResult.PASS, hasTumorStage.evaluate(TumorTestFactory.withTumorStageAndDerivedStages(TumorStage.IIIB)))
        assertEvaluation(EvaluationResult.FAIL, hasTumorStage.evaluate(TumorTestFactory.withTumorStageAndDerivedStages(TumorStage.IV)))
    }

    @Test
    fun `Should evaluate to undetermined if patient stage and derived tumor stage are null`() {
        assertEvaluation(EvaluationResult.UNDETERMINED, hasTumorStage.evaluate(TumorTestFactory.withTumorStageAndDerivedStages(null, null)))
    }

    @Test
    fun `Should pass when all stages in the category of the patient stage are included in the stages to match`() {
        val patientRecord = TumorTestFactory.withTumorStage(TumorStage.III)
        assertEvaluation(
            EvaluationResult.PASS,
            HasTumorStage(
                setOf(
                    TumorStage.IIA,
                    TumorStage.IIB,
                    TumorStage.IIIC,
                    TumorStage.IIIB,
                    TumorStage.IIIA,
                    TumorStage.IIID
                )
            ).evaluate(patientRecord)
        )
    }

    @Test
    fun `Should be undetermined when category of the patient stage is the category of some stages to match, but not all stages of that category are requested`() {
        val patientRecord = TumorTestFactory.withTumorStage(TumorStage.III)
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            HasTumorStage(setOf(TumorStage.IIIC, TumorStage.IIIB, TumorStage.IIIA, TumorStage.IIA)).evaluate(patientRecord)
        )
    }

    @Test
    fun `Should follow non-derived evaluation when single derived tumor`() {
        assertDerivedEvaluation(EvaluationResult.PASS, TumorStage.III)
        assertDerivedEvaluation(EvaluationResult.PASS, TumorStage.IIIB)
        assertDerivedEvaluation(EvaluationResult.FAIL, TumorStage.IV)
    }

    @Test
    fun `Should pass when all derived stages are members of a stage to match`() {
        val patientRecord = TumorTestFactory.withTumorStageAndDerivedStages(null, setOf(TumorStage.IIIA, TumorStage.IIIB))
        assertEvaluation(EvaluationResult.PASS, HasTumorStage(setOf(TumorStage.III, TumorStage.IV)).evaluate(patientRecord))
    }

    @Test
    fun `Should pass when all derived stages pass individually`() {
        val patientRecord = TumorTestFactory.withTumorStageAndDerivedStages(null, setOf(TumorStage.III, TumorStage.IV))
        assertEvaluation(EvaluationResult.PASS, HasTumorStage(setOf(TumorStage.III, TumorStage.IV)).evaluate(patientRecord))
    }

    @Test
    fun `Should evaluate undetermined when multiple derived tumor stages where one passes`() {
        assertDerivedEvaluation(EvaluationResult.UNDETERMINED, TumorStage.III, TumorStage.II)
    }

    @Test
    fun `Should display correct undetermined message with derived stages`() {
        val patientRecord = TumorTestFactory.withTumorStageAndDerivedStages(null, setOf(TumorStage.IV, TumorStage.III))
        assertThat(hasTumorStage.evaluate(patientRecord).undeterminedGeneralMessages).containsExactly(
            "Unknown if tumor stage is III (data missing) - derived III or IV based on lesions"
        )
    }

    @Test
    fun `Should fail when multiple derived tumor stages where all fail`() {
        assertDerivedEvaluation(EvaluationResult.FAIL, TumorStage.I, TumorStage.II)
    }

    @Test
    fun `Should fail when derived stage is not equal to stage or the category of the stage to match`() {
        val patientRecord = TumorTestFactory.withTumorStageAndDerivedStages(null, setOf(TumorStage.III))
        assertEvaluation(EvaluationResult.FAIL, HasTumorStage(setOf(TumorStage.IIB)).evaluate(patientRecord))
    }

    @Test
    fun `Should pass when all stages in the category of the derived stage are included in the stages to match`() {
        val patientRecord = TumorTestFactory.withTumorStageAndDerivedStages(null, setOf(TumorStage.III))
        assertEvaluation(
            EvaluationResult.PASS,
            HasTumorStage(setOf(TumorStage.IIIA, TumorStage.IIIB, TumorStage.IIIC, TumorStage.IIID)).evaluate(patientRecord)
        )
    }

    @Test
    fun `Should be undetermined when category of the derived stage is the category of some stages to match but not all stages of that category are requested`() {
        val patientRecord = TumorTestFactory.withTumorStageAndDerivedStages(null, setOf(TumorStage.II, TumorStage.III))
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            HasTumorStage(setOf(TumorStage.IIIC, TumorStage.IIIA, TumorStage.IIIB, TumorStage.IIA)).evaluate(patientRecord)
        )
    }

    private fun assertDerivedEvaluation(expectedResult: EvaluationResult, vararg derivedStages: TumorStage) {
        val patientRecord = TumorTestFactory.withTumorStageAndDerivedStages(null, setOf(*derivedStages))
        assertEvaluation(expectedResult, hasTumorStage.evaluate(patientRecord))
    }
}