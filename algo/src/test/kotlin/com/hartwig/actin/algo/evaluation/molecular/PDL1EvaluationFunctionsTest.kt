package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.evaluation.molecular.PDL1EvaluationFunctions.evaluateLimitedPDL1byIHC
import com.hartwig.actin.algo.evaluation.molecular.PDL1EvaluationFunctions.evaluateSufficientPDL1byIHC
import com.hartwig.actin.TestPatientFactory
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.doid.DoidConstants
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.algo.evaluation.util.ValueComparison
import com.hartwig.actin.clinical.datamodel.TumorDetails
import com.hartwig.actin.doid.TestDoidModelFactory
import com.hartwig.actin.molecular.datamodel.IHCMolecularTest
import com.hartwig.actin.molecular.datamodel.MolecularHistory
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

private const val MEASURE = "measure"
private const val PDL1_REFERENCE = 2.0
private val doidModel =
    TestDoidModelFactory.createWithOneParentChild(DoidConstants.LUNG_CANCER_DOID, DoidConstants.LUNG_NON_SMALL_CELL_CARCINOMA_DOID)

class PDL1EvaluationFunctionsTest{

    private val pdl1Test = MolecularTestFactory.priorMolecularTest(test = "IHC", item = "PD-L1", measure = MEASURE)

    @Test
    fun `Should evaluate to undetermined if some PD-L1 tests are passing and others failing`(){
        val record = MolecularTestFactory.withMolecularTests(
            listOf(IHCMolecularTest(pdl1Test.copy(scoreValue = 1.0)), IHCMolecularTest(pdl1Test.copy(scoreValue = 3.0)))
        )
        evaluateBoth(EvaluationResult.UNDETERMINED, record)
    }

    @Test
    fun `Should fail with no prior tests`() {
        val record = MolecularTestFactory.withMolecularTests(emptyList())
        evaluateBoth(EvaluationResult.FAIL, record)
    }

    @Test
    fun `Should fail when no test contains result`() {
        val record = MolecularTestFactory.withMolecularTests(listOf(IHCMolecularTest(pdl1Test)))
        evaluateBoth(EvaluationResult.FAIL, record)
    }

    @Test
    fun `Should fail with specific message when molecular history only contains tests with other measure types `() {
        val record = MolecularTestFactory.withMolecularTests(
            listOf(
                IHCMolecularTest(MolecularTestFactory.priorMolecularTest(test = "IHC", item = "PD-L1", measure = "wrong")),
                IHCMolecularTest(MolecularTestFactory.priorMolecularTest(test = "IHC", item = "PD-L1", measure = "other wrong"))
            )
        )
        evaluateBoth(EvaluationResult.FAIL, record)
        assertMessage(record, "PD-L1 tests not in correct unit ($MEASURE)")
    }

    @Test
    fun `Should use any measurement type when requested measure in function is an empty string`() {
        val record = MolecularTestFactory.withMolecularTests(
            listOf(
                IHCMolecularTest(
                    MolecularTestFactory.priorMolecularTest(test = "IHC", item = "PD-L1", scoreValue = PDL1_REFERENCE, measure = "wrong")
                ),
            )
        )
        evaluateBoth(EvaluationResult.PASS, record, measure = null)
    }

    @Test
    fun `Should fail with specific message when measure matches but score value is empty`() {
        val record = MolecularTestFactory.withMolecularTests(listOf(IHCMolecularTest(pdl1Test.copy(scoreValue = null))))
        evaluateBoth(EvaluationResult.FAIL, record)
        assertMessage(record, "No score value available for PD-L1 IHC test")
    }

    @Test
    fun `Should assume that measurement type is TPS if tumor type is non-small cell lung cancer and measurement is null`() {
        val record = TestPatientFactory.createMinimalTestWGSPatientRecord().copy(
            tumor = TumorDetails(
                doids = setOf(DoidConstants.LUNG_NON_SMALL_CELL_CARCINOMA_DOID)
            ), molecularHistory = MolecularHistory(listOf(IHCMolecularTest(pdl1Test.copy(measure = null, scoreValue = 2.0))))
        )
        evaluateBoth(EvaluationResult.PASS, record, measure = "TPS")
    }

    // Tests specific for evaluateLimitedPDL1byIHC
    @Test
    fun `Should pass when test value is below max`() {
        val record =
            MolecularTestFactory.withMolecularTests(listOf(IHCMolecularTest(pdl1Test.copy(scoreValue = PDL1_REFERENCE.minus(0.5)))))
        assertEvaluation(EvaluationResult.PASS, evaluateLimitedPDL1byIHC(record, MEASURE, PDL1_REFERENCE, doidModel))
    }

    @Test
    fun `Should pass when test value is equal to maximum value`() {
        val record =
            MolecularTestFactory.withMolecularTests(listOf(IHCMolecularTest(pdl1Test.copy(scoreValue = PDL1_REFERENCE))))
        assertEvaluation(EvaluationResult.PASS, evaluateLimitedPDL1byIHC(record, MEASURE, PDL1_REFERENCE, doidModel))
    }

    @Test
    fun `Should evaluate to undetermined when it is unclear if test value is below maximum due to its comparator`() {
        val record =
            MolecularTestFactory.withMolecularTests(
                listOf(IHCMolecularTest(pdl1Test.copy(scoreValue = 1.0, scoreValuePrefix = ValueComparison.LARGER_THAN)))
            )
        val evaluation = evaluateLimitedPDL1byIHC(record, MEASURE, PDL1_REFERENCE, doidModel)
        assertEvaluation(EvaluationResult.UNDETERMINED, evaluation)
        assertThat(evaluation.undeterminedGeneralMessages).containsExactly(
            "Undetermined if PD-L1 expression (> 1.0) below maximum of 2.0"
        )
    }

    @Test
    fun `Should fail when test value is above maximum value`() {
        val record =
            MolecularTestFactory.withMolecularTests(listOf(IHCMolecularTest(pdl1Test.copy(scoreValue = PDL1_REFERENCE.plus(1.0)))))
        assertEvaluation(EvaluationResult.FAIL, evaluateLimitedPDL1byIHC(record, MEASURE, PDL1_REFERENCE, doidModel))
    }

    // Tests specific for evaluateSufficientPDL1byIHC
    @Test
    fun `Should pass when test value is above min`() {
        val record =
            MolecularTestFactory.withMolecularTests(listOf(IHCMolecularTest(pdl1Test.copy(scoreValue = PDL1_REFERENCE.plus(0.5)))))
        assertEvaluation(EvaluationResult.PASS, evaluateSufficientPDL1byIHC(record, MEASURE, PDL1_REFERENCE, doidModel))
    }

    @Test
    fun `Should pass when test value is equal to minimum value`() {
        val record =
            MolecularTestFactory.withMolecularTests(listOf(IHCMolecularTest(pdl1Test.copy(scoreValue = PDL1_REFERENCE))))
        assertEvaluation(EvaluationResult.PASS, evaluateSufficientPDL1byIHC(record, MEASURE, PDL1_REFERENCE, doidModel))
    }

    @Test
    fun `Should evaluate to undetermined when it is unclear if test value is above minimum due to its comparator`() {
        val record =
            MolecularTestFactory.withMolecularTests(
                listOf(IHCMolecularTest(pdl1Test.copy(scoreValue = 3.0, scoreValuePrefix = ValueComparison.SMALLER_THAN)))
            )
        val evaluation = evaluateSufficientPDL1byIHC(record, MEASURE, PDL1_REFERENCE, doidModel)
        assertEvaluation(EvaluationResult.UNDETERMINED, evaluation)
        assertThat(evaluation.undeterminedGeneralMessages).containsExactly(
            "Undetermined if PD-L1 expression (< 3.0) above minimum of 2.0"
        )
    }

    @Test
    fun `Should fail when test value is below minimum value`() {
        val record =
            MolecularTestFactory.withMolecularTests(listOf(IHCMolecularTest(pdl1Test.copy(scoreValue = PDL1_REFERENCE.minus(1.0)))))
        assertEvaluation(EvaluationResult.FAIL, evaluateSufficientPDL1byIHC(record, MEASURE, PDL1_REFERENCE, doidModel))
    }

    private fun evaluateBoth(
        expected: EvaluationResult, record: PatientRecord, measure: String? = MEASURE, reference: Double = PDL1_REFERENCE) {
        assertEvaluation(expected, evaluateLimitedPDL1byIHC(record, measure, reference, doidModel))
        assertEvaluation(expected, evaluateSufficientPDL1byIHC(record, measure, reference, doidModel))
    }

    private fun assertMessage(record: PatientRecord, message: String) {
        val evaluations = listOf(
            evaluateLimitedPDL1byIHC(record, MEASURE, PDL1_REFERENCE, doidModel),
            evaluateSufficientPDL1byIHC(record, MEASURE, PDL1_REFERENCE, doidModel)
        )
        evaluations.forEach {
            assertThat(it.failGeneralMessages).containsExactly(message)
        }
    }
}