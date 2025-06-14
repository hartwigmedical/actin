package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.doid.DoidConstants
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.algo.evaluation.molecular.PDL1EvaluationFunctions.evaluatePDL1byIhc
import com.hartwig.actin.algo.evaluation.util.ValueComparison
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.TestPatientFactory
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.TumorDetails
import com.hartwig.actin.doid.TestDoidModelFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

private const val MEASURE = "measure"
private const val TPS = "TPS"
private const val CPS = "CPS"
private const val PDL1_REFERENCE = 2.0
private val doidModel =
    TestDoidModelFactory.createWithOneParentChild(DoidConstants.LUNG_CANCER_DOID, DoidConstants.LUNG_NON_SMALL_CELL_CARCINOMA_DOID)

class PDL1EvaluationFunctionsTest {

    private val pdl1Test = MolecularTestFactory.ihcTest(item = "PD-L1", measure = MEASURE)

    @Test
    fun `Should evaluate to undetermined if some PD-L1 tests are passing and others failing`() {
        val record = MolecularTestFactory.withIhcTests(
            pdl1Test.copy(scoreValue = 1.0), pdl1Test.copy(scoreValue = 3.0)
        )
        evaluateFunctions(EvaluationResult.UNDETERMINED, record)
    }

    @Test
    fun `Should evaluate to undetermined with no prior tests`() {
        val record = MolecularTestFactory.withMolecularTests(emptyList())
        evaluateFunctions(EvaluationResult.UNDETERMINED, record)
    }

    @Test
    fun `Should evaluate to undetermined when no test contains result`() {
        val record = MolecularTestFactory.withIhcTests(pdl1Test)
        evaluateFunctions(EvaluationResult.UNDETERMINED, record)
    }

    @Test
    fun `Should fail with message when molecular history only contains tests with other measure types `() {
        val record = MolecularTestFactory.withIhcTests(
            MolecularTestFactory.ihcTest(item = "PD-L1", measure = "wrong"),
            MolecularTestFactory.ihcTest(item = "PD-L1", measure = "other wrong")
        )
        evaluateFunctions(EvaluationResult.FAIL, record)
        assertMessage(record, "PD-L1 tests not in correct unit ($MEASURE)")
    }

    @Test
    fun `Should use any measurement type when requested measure in function is an empty string`() {
        val record = MolecularTestFactory.withIhcTests(
            MolecularTestFactory.ihcTest(item = "PD-L1", scoreValue = PDL1_REFERENCE, measure = "wrong")
        )
        evaluateFunctions(EvaluationResult.PASS, record, measure = null)
    }

    @Test
    fun `Should evaluate to undetermined when measure matches but score value is empty`() {
        val record = MolecularTestFactory.withIhcTests(pdl1Test.copy(scoreValue = null))
        evaluateFunctions(EvaluationResult.UNDETERMINED, record)
    }

    @Test
    fun `Should assume that measurement type is TPS if tumor type is non-small cell lung cancer and measurement is null`() {
        val record = TestPatientFactory.createMinimalTestWGSPatientRecord().copy(
            tumor = TumorDetails(
                doids = setOf(DoidConstants.LUNG_NON_SMALL_CELL_CARCINOMA_DOID)
            ), ihcTests = listOf(pdl1Test.copy(measure = null, scoreValue = 2.0))
        )
        evaluateFunctions(EvaluationResult.PASS, record, measure = "TPS")
    }

    // Tests specific for evaluateLimitedPDL1byIHC
    @Test
    fun `Should pass when test value is below max`() {
        val record =
            MolecularTestFactory.withIhcTests(pdl1Test.copy(scoreValue = PDL1_REFERENCE.minus(0.5)))
        assertEvaluation(EvaluationResult.PASS, evaluatePDL1byIhc(record, MEASURE, PDL1_REFERENCE, doidModel, evaluateMaxPDL1 = true))
    }

    @Test
    fun `Should pass when test value is equal to maximum value`() {
        val record =
            MolecularTestFactory.withIhcTests(pdl1Test.copy(scoreValue = PDL1_REFERENCE))
        assertEvaluation(EvaluationResult.PASS, evaluatePDL1byIhc(record, MEASURE, PDL1_REFERENCE, doidModel, evaluateMaxPDL1 = true))
    }

    @Test
    fun `Should evaluate to undetermined when it is unclear if test value is below maximum due to its comparator`() {
        val record =
            MolecularTestFactory.withIhcTests(pdl1Test.copy(scoreValue = 1.0, scoreValuePrefix = ValueComparison.LARGER_THAN))
        val evaluation = evaluatePDL1byIhc(record, MEASURE, PDL1_REFERENCE, doidModel, evaluateMaxPDL1 = true)
        assertEvaluation(EvaluationResult.UNDETERMINED, evaluation)
        assertThat(evaluation.undeterminedMessagesStrings()).containsExactly(
            "Undetermined if PD-L1 expression (> 1.0) below maximum of 2.0"
        )
    }

    @Test
    fun `Should fail when test value is above maximum value`() {
        val record =
            MolecularTestFactory.withIhcTests(pdl1Test.copy(scoreValue = PDL1_REFERENCE.plus(1.0)))
        assertEvaluation(EvaluationResult.FAIL, evaluatePDL1byIhc(record, MEASURE, PDL1_REFERENCE, doidModel, evaluateMaxPDL1 = true))
    }

    // Tests specific for evaluateSufficientPDL1byIHC
    @Test
    fun `Should pass when test value is above min`() {
        val record =
            MolecularTestFactory.withIhcTests(pdl1Test.copy(scoreValue = PDL1_REFERENCE.plus(0.5)))
        assertEvaluation(EvaluationResult.PASS, evaluatePDL1byIhc(record, MEASURE, PDL1_REFERENCE, doidModel, evaluateMaxPDL1 = false))
    }

    @Test
    fun `Should pass when test value is equal to minimum value`() {
        val record =
            MolecularTestFactory.withIhcTests(pdl1Test.copy(scoreValue = PDL1_REFERENCE))
        assertEvaluation(EvaluationResult.PASS, evaluatePDL1byIhc(record, MEASURE, PDL1_REFERENCE, doidModel, evaluateMaxPDL1 = false))
    }

    @Test
    fun `Should evaluate to undetermined when it is unclear if test value is above minimum due to its comparator`() {
        val record = MolecularTestFactory.withIhcTests(pdl1Test.copy(scoreValue = 3.0, scoreValuePrefix = ValueComparison.SMALLER_THAN))
        val evaluation = evaluatePDL1byIhc(record, MEASURE, PDL1_REFERENCE, doidModel, evaluateMaxPDL1 = false)
        assertEvaluation(EvaluationResult.UNDETERMINED, evaluation)
        assertThat(evaluation.undeterminedMessagesStrings()).containsExactly(
            "Undetermined if PD-L1 expression (< 3.0) above minimum of 2.0"
        )
    }

    @Test
    fun `Should fail when test value is below minimum value`() {
        val record = MolecularTestFactory.withIhcTests(pdl1Test.copy(scoreValue = PDL1_REFERENCE.minus(1.0)))
        assertEvaluation(EvaluationResult.FAIL, evaluatePDL1byIhc(record, MEASURE, PDL1_REFERENCE, doidModel, evaluateMaxPDL1 = false))
    }

    @Test
    fun `Should evaluate to undetermined when TPS test result is negative and evaluating against max PDL1 of 0,5`() {
        val record = MolecularTestFactory.withIhcTests(pdl1Test.copy(scoreText = "negative", measure = TPS))
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            evaluatePDL1byIhc(record, TPS, pdl1Reference = 0.5, doidModel = doidModel, evaluateMaxPDL1 = true)
        )
    }

    @Test
    fun `Should pass when TPS test result is negative and evaluating against max PDL1 of 10,0`() {
        val record = MolecularTestFactory.withIhcTests(pdl1Test.copy(scoreText = "negative", measure = TPS))
        assertEvaluation(
            EvaluationResult.PASS,
            evaluatePDL1byIhc(record, TPS, pdl1Reference = 10.0, doidModel = doidModel, evaluateMaxPDL1 = true)
        )
    }

    @Test
    fun `Should pass when tumor type is lung cancer and PD-L1 test result of undefined type is negative and evaluating against max PDL1 of 10,0`() {
        val record = MolecularTestFactory.withIhcTests(pdl1Test.copy(scoreText = "negative", measure = null))
            .copy(tumor = TumorDetails(doids = setOf(DoidConstants.LUNG_CANCER_DOID)))

        assertEvaluation(
            EvaluationResult.PASS,
            evaluatePDL1byIhc(record, TPS, pdl1Reference = 10.0, doidModel = doidModel, evaluateMaxPDL1 = true)
        )
    }

    @Test
    fun `Should evaluate to undetermined when TPS test result is positive and evaluating against max PDL1 of 10,0`() {
        val record = MolecularTestFactory.withIhcTests(pdl1Test.copy(scoreText = "positive", measure = TPS))
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            evaluatePDL1byIhc(record, TPS, pdl1Reference = 10.0, doidModel = doidModel, evaluateMaxPDL1 = true)
        )
    }

    @Test
    fun `Should pass when TPS test result is positive and evaluating against min PDL1 of 1,0`() {
        val record = MolecularTestFactory.withIhcTests(pdl1Test.copy(scoreText = "positive", measure = TPS))
        assertEvaluation(
            EvaluationResult.PASS,
            evaluatePDL1byIhc(record, TPS, pdl1Reference = 1.0, doidModel = doidModel, evaluateMaxPDL1 = false)
        )
    }

    @Test
    fun `Should pass when tumor type is lung cancer and PD-L1 test result of undefined type is positive and evaluating against min PDL1 of 1,0`() {
        val record = MolecularTestFactory.withIhcTests(pdl1Test.copy(scoreText = "positive", measure = null))
            .copy(tumor = TumorDetails(doids = setOf(DoidConstants.LUNG_CANCER_DOID)))

        assertEvaluation(
            EvaluationResult.PASS,
            evaluatePDL1byIhc(record, TPS, pdl1Reference = 1.0, doidModel = doidModel, evaluateMaxPDL1 = false)
        )
    }

    @Test
    fun `Should evaluate to undetermined when TPS test result is positive and evaluating against min PDL1 of 10,0`() {
        val record = MolecularTestFactory.withIhcTests(pdl1Test.copy(scoreText = "positive", measure = TPS))
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            evaluatePDL1byIhc(record, TPS, pdl1Reference = 10.0, doidModel = doidModel, evaluateMaxPDL1 = false)
        )
    }

    @Test
    fun `Should fail when TPS test result is negative and evaluating against min PDL1 of 1,0`() {
        val record = MolecularTestFactory.withIhcTests(pdl1Test.copy(scoreText = "negative", measure = TPS))
        assertEvaluation(
            EvaluationResult.FAIL,
            evaluatePDL1byIhc(record, TPS, pdl1Reference = 1.0, doidModel = doidModel, evaluateMaxPDL1 = false)
        )
    }

    @Test
    fun `Should fail when tumor type is lung cancer and PD-L1 test result of undefined type is negative and evaluating against min PD-L1 of 1,0`() {
        val record = MolecularTestFactory.withIhcTests(pdl1Test.copy(scoreText = "negative", measure = null))
            .copy(tumor = TumorDetails(doids = setOf(DoidConstants.LUNG_CANCER_DOID)))

        assertEvaluation(
            EvaluationResult.FAIL,
            evaluatePDL1byIhc(record, TPS, pdl1Reference = 1.0, doidModel = doidModel, evaluateMaxPDL1 = false)
        )
    }

    @Test
    fun `Should evaluate to undetermined when TPS test result is negative and evaluating against min PDL1 of 0,5`() {
        val record = MolecularTestFactory.withIhcTests(pdl1Test.copy(scoreText = "negative", measure = TPS))
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            evaluatePDL1byIhc(record, TPS, pdl1Reference = 0.5, doidModel = doidModel, evaluateMaxPDL1 = false)
        )
    }

    @Test
    fun `Should evaluate to undetermined when CPS test result is negative and evaluating against max PDL1 of 1,0`() {
        val record = MolecularTestFactory.withIhcTests(pdl1Test.copy(scoreText = "negative", measure = CPS))
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            evaluatePDL1byIhc(record, CPS, pdl1Reference = 1.0, doidModel = doidModel, evaluateMaxPDL1 = true)
        )
    }

    @Test
    fun `Should pass when CPS test result is negative and evaluating against max PDL1 of 10,0`() {
        val record = MolecularTestFactory.withIhcTests(pdl1Test.copy(scoreText = "negative", measure = CPS))
        assertEvaluation(
            EvaluationResult.PASS,
            evaluatePDL1byIhc(record, CPS, pdl1Reference = 10.0, doidModel = doidModel, evaluateMaxPDL1 = true)
        )
    }

    @Test
    fun `Should evaluate to undetermined when CPS test result is positive and evaluating against max PDL1 of 10,0`() {
        val record = MolecularTestFactory.withIhcTests(pdl1Test.copy(scoreText = "positive", measure = CPS))
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            evaluatePDL1byIhc(record, CPS, pdl1Reference = 10.0, doidModel = doidModel, evaluateMaxPDL1 = true)
        )
    }

    @Test
    fun `Should pass when CPS test result is positive and evaluating against min PDL1 of 1,0`() {
        val record = MolecularTestFactory.withIhcTests(pdl1Test.copy(scoreText = "positive", measure = CPS))
        assertEvaluation(
            EvaluationResult.PASS,
            evaluatePDL1byIhc(record, CPS, pdl1Reference = 1.0, doidModel = doidModel, evaluateMaxPDL1 = false)
        )
    }

    @Test
    fun `Should evaluate to undetermined when CPS test result is positive and evaluating against min PDL1 of 10,0`() {
        val record = MolecularTestFactory.withIhcTests(pdl1Test.copy(scoreText = "positive", measure = CPS))
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            evaluatePDL1byIhc(record, CPS, pdl1Reference = 10.0, doidModel = doidModel, evaluateMaxPDL1 = false)
        )
    }

    @Test
    fun `Should evaluate to undetermined when CPS test result is negative and evaluating against min PDL1 of 1,0`() {
        val record = MolecularTestFactory.withIhcTests(pdl1Test.copy(scoreText = "negative", measure = CPS))
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            evaluatePDL1byIhc(record, CPS, pdl1Reference = 1.0, doidModel = doidModel, evaluateMaxPDL1 = false)
        )
    }

    @Test
    fun `Should fail when CPS test result is negative and evaluating against min PDL1 of 10,0`() {
        val record = MolecularTestFactory.withIhcTests(pdl1Test.copy(scoreText = "negative", measure = CPS))
        assertEvaluation(
            EvaluationResult.FAIL,
            evaluatePDL1byIhc(record, CPS, pdl1Reference = 10.0, doidModel = doidModel, evaluateMaxPDL1 = false)
        )
    }

    private fun evaluateFunctions(
        expected: EvaluationResult, record: PatientRecord, measure: String? = MEASURE, reference: Double = PDL1_REFERENCE
    ) {
        assertEvaluation(expected, evaluatePDL1byIhc(record, measure, reference, doidModel, evaluateMaxPDL1 = true))
        assertEvaluation(expected, evaluatePDL1byIhc(record, measure, reference, doidModel, evaluateMaxPDL1 = false))
    }

    private fun assertMessage(record: PatientRecord, message: String) {
        val evaluations = listOf(
            evaluatePDL1byIhc(record, MEASURE, PDL1_REFERENCE, doidModel, evaluateMaxPDL1 = true),
            evaluatePDL1byIhc(record, MEASURE, PDL1_REFERENCE, doidModel, evaluateMaxPDL1 = false)
        )
        evaluations.forEach {
            assertThat(it.failMessagesStrings()).containsExactly(message)
        }
    }
}