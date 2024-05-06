package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.TestPatientFactory
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.doid.DoidConstants
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.algo.evaluation.util.ValueComparison
import com.hartwig.actin.clinical.datamodel.TumorDetails
import com.hartwig.actin.molecular.datamodel.MolecularHistory
import org.assertj.core.api.Assertions
import org.junit.Test

private const val MEASURE = "measure"

class HasSufficientPDL1ByIHCTest {
    private val function = HasSufficientPDL1ByIHC(MEASURE, 2.0)

    private val pdl1Test = MolecularTestFactory.priorMolecularTest(test = "IHC", item = "PD-L1", measure = MEASURE)

    @Test
    fun `Should fail with no prior tests`() {
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(MolecularTestFactory.withPriorTests(emptyList())))
    }

    @Test
    fun `Should fail when no test contains result`() {
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(MolecularTestFactory.withPriorTests(listOf(pdl1Test))))
    }

    @Test
    fun `Should fail with specific message when molecular history only contains tests with other measure types `() {
        val molecular = listOf(
            MolecularTestFactory.priorMolecularTest(test = "IHC", item = "PD-L1", measure = "wrong"),
            MolecularTestFactory.priorMolecularTest(test = "IHC", item = "PD-L1", measure = "other wrong")
        )
        val evaluation = function.evaluate(MolecularTestFactory.withPriorTests(molecular))
        assertEvaluation(EvaluationResult.FAIL, evaluation)
        Assertions.assertThat(evaluation.failGeneralMessages).containsExactly("PD-L1 tests not in correct unit ($MEASURE)")
    }

    @Test
    fun `Should use any measurement type when requested measure in function is an empty string`() {
        val function = HasSufficientPDL1ByIHC(null, 2.0)
        val molecular = listOf(
            MolecularTestFactory.priorMolecularTest(test = "IHC", item = "PD-L1", scoreValue = 2.5, measure = "wrong"),
        )
        val evaluation = function.evaluate(MolecularTestFactory.withPriorTests(molecular))
        assertEvaluation(EvaluationResult.PASS, evaluation)
    }

    @Test
    fun `Should fail when test value is too low`() {
        assertEvaluation(
            EvaluationResult.FAIL, function.evaluate(MolecularTestFactory.withPriorTests(listOf(pdl1Test.copy(scoreValue = 1.0))))
        )
    }

    @Test
    fun `Should fail when test value has non-matching prefix`() {
        val priorTests = listOf(pdl1Test.copy(scoreValuePrefix = ValueComparison.SMALLER_THAN, scoreValue = 3.0))
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(MolecularTestFactory.withPriorTests(priorTests)))
    }

    @Test
    fun `Should pass when test value is over limit`() {
        assertEvaluation(
            EvaluationResult.PASS, function.evaluate(MolecularTestFactory.withPriorTests(listOf(pdl1Test.copy(scoreValue = 3.0))))
        )
    }

    @Test
    fun `Should assume that measurement type is TPS if tumor type is non-small cell lung cancer and measurement is null`(){
        val record = TestPatientFactory.createMinimalTestWGSPatientRecord().copy(
            tumor = TumorDetails(
                doids = setOf(DoidConstants.LUNG_NON_SMALL_CELL_CARCINOMA_DOID)
            ), molecularHistory = MolecularHistory.fromInputs(emptyList(), listOf(pdl1Test.copy(measure = null, scoreValue = 3.0)))
        )
        assertEvaluation(EvaluationResult.PASS, HasSufficientPDL1ByIHC("TPS", 2.0).evaluate(record))
    }
}