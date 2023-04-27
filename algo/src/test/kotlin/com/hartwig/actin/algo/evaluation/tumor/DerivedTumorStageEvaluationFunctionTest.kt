package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.ImmutablePatientRecord
import com.hartwig.actin.PatientRecord
import com.hartwig.actin.TestDataFactory
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.datamodel.EvaluationTestFactory
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.clinical.datamodel.ImmutableClinicalRecord
import com.hartwig.actin.clinical.datamodel.ImmutableTumorDetails
import com.hartwig.actin.clinical.datamodel.TumorStage
import org.assertj.core.api.Assertions
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito

class DerivedTumorStageEvaluationFunctionTest {
    private var victim: DerivedTumorStageEvaluationFunction? = null
    private var tumorStageDerivationFunction: TumorStageDerivationFunction? = null
    private var evaluationFunction: EvaluationFunction? = null

    @Before
    fun setUp() {
        tumorStageDerivationFunction = Mockito.mock(TumorStageDerivationFunction::class.java)
        evaluationFunction = Mockito.mock(EvaluationFunction::class.java)
        victim = DerivedTumorStageEvaluationFunction(tumorStageDerivationFunction!!, evaluationFunction!!)
    }

    @Test
    fun shouldReturnOriginalFunctionWhenTumorDetailsNotNull() {
        val originalEvaluation = EvaluationTestFactory.withResult(EvaluationResult.PASS)
        Mockito.`when`(evaluationFunction!!.evaluate(PROPER_TEST_PATIENT_RECORD)).thenReturn(originalEvaluation)
        Assertions.assertThat(victim!!.evaluate(PROPER_TEST_PATIENT_RECORD)).isEqualTo(originalEvaluation)
    }

    @Test
    fun shouldReturnOriginalFunctionWhenNoDerivedStagesPossible() {
        val originalEvaluation = EvaluationTestFactory.withResult(EvaluationResult.UNDETERMINED)
        Mockito.`when`(evaluationFunction!!.evaluate(MINIMAL_TEST_PATIENT_RECORD)).thenReturn(originalEvaluation)
        Mockito.`when`(tumorStageDerivationFunction!!.apply(MINIMAL_TEST_PATIENT_RECORD.clinical().tumor())).thenReturn(emptyList())
        Assertions.assertThat(victim!!.evaluate(MINIMAL_TEST_PATIENT_RECORD)).isEqualTo(originalEvaluation)
    }

    @Test
    fun shouldFollowEvaluationWhenAnySingleInferredStage() {
        for (evaluationResult in EvaluationResult.values()) {
            assertSingleStageWithResult(evaluationResult)
        }
    }

    @Test
    fun shouldEvaluatePassWhenMultipleDerivedStagesAllEvaluatePass() {
        Mockito.`when`(tumorStageDerivationFunction!!.apply(MINIMAL_TEST_PATIENT_RECORD.clinical().tumor())).thenReturn(
            listOf(
                TumorStage.I,
                TumorStage.II
            )
        )
        Mockito.`when`(evaluationFunction!!.evaluate(withStage(TumorStage.I)))
            .thenReturn(EvaluationTestFactory.withResult(EvaluationResult.PASS))
        Mockito.`when`(evaluationFunction!!.evaluate(withStage(TumorStage.II)))
            .thenReturn(EvaluationTestFactory.withResult(EvaluationResult.PASS))
        val evaluation = victim!!.evaluate(MINIMAL_TEST_PATIENT_RECORD)
        assertEvaluation(EvaluationResult.PASS, evaluation)
    }

    @Test
    fun shouldEvaluatePassWhenMultipleDerivedAndAtLeastOnePasses() {
        Mockito.`when`(tumorStageDerivationFunction!!.apply(MINIMAL_TEST_PATIENT_RECORD.clinical().tumor())).thenReturn(
            listOf(
                TumorStage.I,
                TumorStage.II
            )
        )
        Mockito.`when`(evaluationFunction!!.evaluate(withStage(TumorStage.I)))
            .thenReturn(EvaluationTestFactory.withResult(EvaluationResult.PASS))
        Mockito.`when`(evaluationFunction!!.evaluate(withStage(TumorStage.II)))
            .thenReturn(EvaluationTestFactory.withResult(EvaluationResult.FAIL))
        val evaluation = victim!!.evaluate(MINIMAL_TEST_PATIENT_RECORD)
        assertEvaluation(EvaluationResult.UNDETERMINED, evaluation)
    }

    @Test
    fun shouldEvaluateFailWhenMultipleDerivedAndNoPassOrWarn() {
        Mockito.`when`(tumorStageDerivationFunction!!.apply(MINIMAL_TEST_PATIENT_RECORD.clinical().tumor())).thenReturn(
            listOf(
                TumorStage.I,
                TumorStage.II
            )
        )
        Mockito.`when`(evaluationFunction!!.evaluate(withStage(TumorStage.I)))
            .thenReturn(EvaluationTestFactory.withResult(EvaluationResult.UNDETERMINED))
        Mockito.`when`(evaluationFunction!!.evaluate(withStage(TumorStage.II)))
            .thenReturn(EvaluationTestFactory.withResult(EvaluationResult.UNDETERMINED))
        val evaluation = victim!!.evaluate(MINIMAL_TEST_PATIENT_RECORD)
        assertEvaluation(EvaluationResult.FAIL, evaluation)
    }

    @Test
    fun shouldEvaluateWarnWhenMultipleDerivedAndAtLeastOneWarnAndNoPass() {
        Mockito.`when`(tumorStageDerivationFunction!!.apply(MINIMAL_TEST_PATIENT_RECORD.clinical().tumor())).thenReturn(
            listOf(
                TumorStage.I,
                TumorStage.II
            )
        )
        Mockito.`when`(evaluationFunction!!.evaluate(withStage(TumorStage.I)))
            .thenReturn(EvaluationTestFactory.withResult(EvaluationResult.WARN))
        Mockito.`when`(evaluationFunction!!.evaluate(withStage(TumorStage.II)))
            .thenReturn(EvaluationTestFactory.withResult(EvaluationResult.UNDETERMINED))
        val evaluation = victim!!.evaluate(MINIMAL_TEST_PATIENT_RECORD)
        assertEvaluation(EvaluationResult.WARN, evaluation)
    }

    @Test
    fun shouldEvaluateNotEvaluatedWhenMultipleDerivedAndAllAreNotEvaluated() {
        Mockito.`when`(tumorStageDerivationFunction!!.apply(MINIMAL_TEST_PATIENT_RECORD.clinical().tumor())).thenReturn(
            listOf(
                TumorStage.I,
                TumorStage.II
            )
        )
        Mockito.`when`(evaluationFunction!!.evaluate(withStage(TumorStage.I)))
            .thenReturn(EvaluationTestFactory.withResult(EvaluationResult.NOT_EVALUATED))
        Mockito.`when`(evaluationFunction!!.evaluate(withStage(TumorStage.II)))
            .thenReturn(EvaluationTestFactory.withResult(EvaluationResult.NOT_EVALUATED))
        val evaluation = victim!!.evaluate(MINIMAL_TEST_PATIENT_RECORD)
        assertEvaluation(EvaluationResult.NOT_EVALUATED, evaluation)
    }

    private fun assertSingleStageWithResult(expectedResult: EvaluationResult) {
        Mockito.`when`(tumorStageDerivationFunction!!.apply(MINIMAL_TEST_PATIENT_RECORD.clinical().tumor()))
            .thenReturn(listOf(TumorStage.I))
        Mockito.`when`(evaluationFunction!!.evaluate(withStage(TumorStage.I))).thenReturn(EvaluationTestFactory.withResult(expectedResult))
        val evaluation = victim!!.evaluate(MINIMAL_TEST_PATIENT_RECORD)
        assertEvaluation(expectedResult, evaluation)
    }

    companion object {
        private val PROPER_TEST_PATIENT_RECORD: PatientRecord = TestDataFactory.createProperTestPatientRecord()
        private val MINIMAL_TEST_PATIENT_RECORD: PatientRecord = TestDataFactory.createMinimalTestPatientRecord()
        private fun withStage(newStage: TumorStage?): PatientRecord {
            return ImmutablePatientRecord.copyOf(MINIMAL_TEST_PATIENT_RECORD)
                .withClinical(
                    ImmutableClinicalRecord.copyOf(MINIMAL_TEST_PATIENT_RECORD.clinical())
                        .withTumor(ImmutableTumorDetails.copyOf(MINIMAL_TEST_PATIENT_RECORD.clinical().tumor()).withStage(newStage))
                )
        }
    }
}