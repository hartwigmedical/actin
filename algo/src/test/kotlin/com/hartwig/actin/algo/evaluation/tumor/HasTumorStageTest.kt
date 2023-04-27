package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.clinical.datamodel.TumorStage
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers
import org.mockito.Mockito

class HasTumorStageTest {
    private var derivationFunction: TumorStageDerivationFunction? = null
    private var victim: HasTumorStage? = null

    @Before
    fun setUp() {
        derivationFunction = Mockito.mock(TumorStageDerivationFunction::class.java)
        victim = HasTumorStage(derivationFunction!!, TumorStage.III)
    }

    @Test
    fun shouldEvaluateNormallyWhenTumorStageExists() {
        Mockito.`when`(derivationFunction!!.apply(ArgumentMatchers.any())).thenReturn(emptyList())
        assertEvaluation(EvaluationResult.FAIL, victim!!.evaluate(TumorTestFactory.withTumorStage(null)))
        assertEvaluation(EvaluationResult.PASS, victim!!.evaluate(TumorTestFactory.withTumorStage(TumorStage.III)))
        assertEvaluation(EvaluationResult.PASS, victim!!.evaluate(TumorTestFactory.withTumorStage(TumorStage.IIIB)))
        assertEvaluation(EvaluationResult.FAIL, victim!!.evaluate(TumorTestFactory.withTumorStage(TumorStage.IV)))
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
    fun shouldEvaluateFailWhenMultipleDerivedTumorStagesWhereAllFail() {
        assertDerivedEvaluation(EvaluationResult.FAIL, TumorStage.I, TumorStage.II)
    }

    private fun assertDerivedEvaluation(expectedResult: EvaluationResult, vararg derivedStages: TumorStage) {
        val patientRecord = TumorTestFactory.withTumorStage(null)
        val tumorDetails = patientRecord.clinical().tumor()
        Mockito.`when`(derivationFunction!!.apply(tumorDetails)).thenReturn(listOf(*derivedStages))
        assertEvaluation(expectedResult, victim!!.evaluate(patientRecord))
    }
}