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
        assertEvaluation(EvaluationResult.FAIL, hasTumorStage.evaluate(TumorTestFactory.withTumorStageAndDerivedStages(null)))
        assertEvaluation(EvaluationResult.PASS, hasTumorStage.evaluate(TumorTestFactory.withTumorStageAndDerivedStages(TumorStage.III)))
        assertEvaluation(EvaluationResult.PASS, hasTumorStage.evaluate(TumorTestFactory.withTumorStageAndDerivedStages(TumorStage.IIIB)))
        assertEvaluation(EvaluationResult.FAIL, hasTumorStage.evaluate(TumorTestFactory.withTumorStageAndDerivedStages(TumorStage.IV)))
    }

    @Test
    fun `Should be undetermined when patient stage is of the same category as one of the requested stages, but not an exact match`() {
        val patientRecord = TumorTestFactory.withTumorStage(TumorStage.III)
        assertEvaluation(EvaluationResult.UNDETERMINED, HasTumorStage(setOf(TumorStage.IIIA, TumorStage.IIB)).evaluate(patientRecord))
    }

    @Test
    fun `Should pass when patient stage is of the same general category as all of the possible and requested category stages`() {
        val patientRecord = TumorTestFactory.withTumorStage(TumorStage.III)
        assertEvaluation(
            EvaluationResult.PASS,
            HasTumorStage(
                setOf(
                    TumorStage.IIA,
                    TumorStage.IIB,
                    TumorStage.IIC,
                    TumorStage.IIIC,
                    TumorStage.IIIB,
                    TumorStage.IIIA,
                    TumorStage.IIID
                )
            ).evaluate(patientRecord)
        )
    }

    @Test
    fun `Should use original stage requirements in message`() {
        val patientRecord = TumorTestFactory.withTumorStage(TumorStage.III)
        assertThat(
            HasTumorStage(
                setOf(
                    TumorStage.IIA,
                    TumorStage.IIB,
                    TumorStage.IIC,
                    TumorStage.IIIC,
                    TumorStage.IIIB,
                    TumorStage.IIIA,
                    TumorStage.IIID
                )
            ).evaluate(patientRecord).passGeneralMessages
        ).containsExactly("Patient tumor stage III meets requested stage(s) IIA or IIB or IIC or IIIA or IIIB or IIIC or IIID")
    }

    @Test
    fun `Should be undetermined when patient stage is of the same general category as some of the possible and requested category stages`() {
        val patientRecord = TumorTestFactory.withTumorStage(TumorStage.III)
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            HasTumorStage(setOf(TumorStage.IIIC, TumorStage.IIIB, TumorStage.IIIA, TumorStage.IIA)).evaluate(patientRecord)
        )
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            HasTumorStage(setOf(TumorStage.IIIC, TumorStage.IIIB, TumorStage.IIIA)).evaluate(patientRecord)
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
        val patientRecord = TumorTestFactory.withTumorStageAndDerivedStages(null, setOf(TumorStage.III, TumorStage.IV))
        assertThat(hasTumorStage.evaluate(patientRecord).undeterminedGeneralMessages).containsExactly(
            "Unknown if tumor stage is III (data missing) - derived III or IV based on lesions"
        )
    }

    @Test
    fun `Should evaluate fail when multiple derived tumor stages where all fail`() {
        assertDerivedEvaluation(EvaluationResult.FAIL, TumorStage.I, TumorStage.II)
    }

    @Test
    fun `Should fail when derived stage is not equal to the category of the stage to match`() {
        val patientRecord = TumorTestFactory.withTumorStageAndDerivedStages(null, setOf(TumorStage.III))
        assertEvaluation(EvaluationResult.FAIL, HasTumorStage(setOf(TumorStage.IIB)).evaluate(patientRecord))
    }

    @Test
    fun `Should pass when derived stage is equal to the category of all the stages to match`() {
        val patientRecord = TumorTestFactory.withTumorStageAndDerivedStages(null, setOf(TumorStage.III))
        assertEvaluation(
            EvaluationResult.PASS,
            HasTumorStage(setOf(TumorStage.IIIA, TumorStage.IIIB, TumorStage.IIIC, TumorStage.IIID)).evaluate(patientRecord)
        )
    }

    @Test
    fun `Should be undetermined when derived stage is not equal to the category of all the stages to match`() {
        val patientRecord = TumorTestFactory.withTumorStageAndDerivedStages(null, setOf(TumorStage.III))
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            HasTumorStage(setOf(TumorStage.IIIC, TumorStage.IIIA, TumorStage.IIIB)).evaluate(patientRecord)
        )
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            HasTumorStage(setOf(TumorStage.IIIC, TumorStage.IIIA, TumorStage.IIIB, TumorStage.IIA)).evaluate(patientRecord)
        )
    }

    @Test
    fun `Should be undetermined when derived stage includes one that is part of the same category, but not an exact match`() {
        val patientRecord = TumorTestFactory.withTumorStageAndDerivedStages(null, setOf(TumorStage.III, TumorStage.II))
        assertEvaluation(EvaluationResult.UNDETERMINED, HasTumorStage(setOf(TumorStage.IIIB)).evaluate(patientRecord))
    }

    private fun assertDerivedEvaluation(expectedResult: EvaluationResult, vararg derivedStages: TumorStage) {
        val patientRecord = TumorTestFactory.withTumorStageAndDerivedStages(null, setOf(*derivedStages))
        assertEvaluation(expectedResult, hasTumorStage.evaluate(patientRecord))
    }
}