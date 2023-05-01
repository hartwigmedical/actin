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
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions
import org.junit.Before
import org.junit.Test

class DerivedTumorStageEvaluationFunctionTest {
    private var victim: DerivedTumorStageEvaluationFunction? = null
    private var tumorStageDerivationFunction: TumorStageDerivationFunction? = null
    private var evaluationFunction: EvaluationFunction? = null

    @Before
    fun setUp() {
        tumorStageDerivationFunction = mockk()
        evaluationFunction = mockk()
        victim = DerivedTumorStageEvaluationFunction(tumorStageDerivationFunction!!, evaluationFunction!!)
    }

    @Test
    fun shouldReturnOriginalFunctionWhenTumorDetailsNotNull() {
        val originalEvaluation = EvaluationTestFactory.withResult(EvaluationResult.PASS)
        every { evaluationFunction!!.evaluate(PROPER_TEST_PATIENT_RECORD) } returns originalEvaluation
        Assertions.assertThat(victim!!.evaluate(PROPER_TEST_PATIENT_RECORD)).isEqualTo(originalEvaluation)
    }

    @Test
    fun shouldReturnOriginalFunctionWhenNoDerivedStagesPossible() {
        val originalEvaluation = EvaluationTestFactory.withResult(EvaluationResult.UNDETERMINED)
        every { evaluationFunction!!.evaluate(MINIMAL_TEST_PATIENT_RECORD) } returns originalEvaluation
        every { tumorStageDerivationFunction!!.apply(MINIMAL_TEST_PATIENT_RECORD.clinical().tumor()) } returns emptyList()
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
        every { tumorStageDerivationFunction!!.apply(MINIMAL_TEST_PATIENT_RECORD.clinical().tumor()) } returns listOf(
            TumorStage.I,
            TumorStage.II
        )
        every { evaluationFunction!!.evaluate(withStage(TumorStage.I)) } returns EvaluationTestFactory.withResult(EvaluationResult.PASS)
        every { evaluationFunction!!.evaluate(withStage(TumorStage.II)) } returns EvaluationTestFactory.withResult(EvaluationResult.PASS)
        val evaluation = victim!!.evaluate(MINIMAL_TEST_PATIENT_RECORD)
        assertEvaluation(EvaluationResult.PASS, evaluation)
    }

    @Test
    fun shouldEvaluatePassWhenMultipleDerivedAndAtLeastOnePasses() {
        every { tumorStageDerivationFunction!!.apply(MINIMAL_TEST_PATIENT_RECORD.clinical().tumor()) } returns listOf(
            TumorStage.I,
            TumorStage.II
        )
        every { evaluationFunction!!.evaluate(withStage(TumorStage.I)) } returns EvaluationTestFactory.withResult(EvaluationResult.PASS)
        every { evaluationFunction!!.evaluate(withStage(TumorStage.II)) } returns EvaluationTestFactory.withResult(EvaluationResult.FAIL)
        val evaluation = victim!!.evaluate(MINIMAL_TEST_PATIENT_RECORD)
        assertEvaluation(EvaluationResult.UNDETERMINED, evaluation)
    }

    @Test
    fun shouldEvaluateFailWhenMultipleDerivedAndNoPassOrWarn() {
        every { tumorStageDerivationFunction!!.apply(MINIMAL_TEST_PATIENT_RECORD.clinical().tumor()) } returns listOf(
            TumorStage.I,
            TumorStage.II
        )
        every { evaluationFunction!!.evaluate(withStage(TumorStage.I)) } returns EvaluationTestFactory.withResult(EvaluationResult.UNDETERMINED)
        every { evaluationFunction!!.evaluate(withStage(TumorStage.II)) } returns EvaluationTestFactory.withResult(EvaluationResult.UNDETERMINED)
        val evaluation = victim!!.evaluate(MINIMAL_TEST_PATIENT_RECORD)
        assertEvaluation(EvaluationResult.FAIL, evaluation)
    }

    @Test
    fun shouldEvaluateWarnWhenMultipleDerivedAndAtLeastOneWarnAndNoPass() {
        every { tumorStageDerivationFunction!!.apply(MINIMAL_TEST_PATIENT_RECORD.clinical().tumor()) } returns listOf(
            TumorStage.I,
            TumorStage.II
        )
        every { evaluationFunction!!.evaluate(withStage(TumorStage.I)) } returns EvaluationTestFactory.withResult(EvaluationResult.WARN)
        every { evaluationFunction!!.evaluate(withStage(TumorStage.II)) } returns EvaluationTestFactory.withResult(EvaluationResult.UNDETERMINED)
        val evaluation = victim!!.evaluate(MINIMAL_TEST_PATIENT_RECORD)
        assertEvaluation(EvaluationResult.WARN, evaluation)
    }

    @Test
    fun shouldEvaluateNotEvaluatedWhenMultipleDerivedAndAllAreNotEvaluated() {
        every { tumorStageDerivationFunction!!.apply(MINIMAL_TEST_PATIENT_RECORD.clinical().tumor()) } returns listOf(
            TumorStage.I,
            TumorStage.II
        )
        every { evaluationFunction!!.evaluate(withStage(TumorStage.I)) } returns EvaluationTestFactory.withResult(EvaluationResult.NOT_EVALUATED)
        every { evaluationFunction!!.evaluate(withStage(TumorStage.II)) } returns EvaluationTestFactory.withResult(EvaluationResult.NOT_EVALUATED)
        val evaluation = victim!!.evaluate(MINIMAL_TEST_PATIENT_RECORD)
        assertEvaluation(EvaluationResult.NOT_EVALUATED, evaluation)
    }

    private fun assertSingleStageWithResult(expectedResult: EvaluationResult) {
        every { tumorStageDerivationFunction!!.apply(MINIMAL_TEST_PATIENT_RECORD.clinical().tumor()) } returns listOf(TumorStage.I)
        every { evaluationFunction!!.evaluate(withStage(TumorStage.I)) } returns EvaluationTestFactory.withResult(expectedResult)
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