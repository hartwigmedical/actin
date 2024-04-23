package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.TestPatientFactory
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert
import com.hartwig.actin.algo.evaluation.molecular.NsclcDriverGeneStatusesAreAvailable.Companion.NSCLC_DRIVER_GENE_SET
import com.hartwig.actin.clinical.datamodel.PriorMolecularTest
import com.hartwig.actin.molecular.datamodel.ExperimentType
import com.hartwig.actin.molecular.datamodel.MolecularHistory
import com.hartwig.actin.molecular.datamodel.TestMolecularFactory.archerPriorMolecularVariantRecord
import org.assertj.core.api.Assertions
import org.junit.Test

class NsclcDriverGeneStatusesAreAvailableTest {

    @Test
    fun `Should pass if WGS is available and contains tumor cells`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(MolecularTestFactory.withExperimentTypeAndContainingTumorCells(ExperimentType.WHOLE_GENOME, true))
        )
    }

    @Test
    fun `Should pass if targeted panel analysis is available and contains tumor cells`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(MolecularTestFactory.withExperimentTypeAndContainingTumorCells(ExperimentType.TARGETED, true))
        )
    }

    @Test
    fun `Should pass if other panel is available and contains all target genes`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(
                createNonWGSRecordWithOptionalPriorTests(NSCLC_DRIVER_GENE_SET.map { archerPriorMolecularVariantRecord(it, "") })
            )
        )
    }

    @Test
    fun `Should fail if WGS is available but contains no tumor cells`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(MolecularTestFactory.withExperimentTypeAndContainingTumorCells(ExperimentType.WHOLE_GENOME, false))
        )
    }

    @Test
    fun `Should fail if targeted panel analysis is available but contains no tumor cells`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(MolecularTestFactory.withExperimentTypeAndContainingTumorCells(ExperimentType.TARGETED, false))
        )
    }

    @Test
    fun `Should fail if molecular history is empty`() {
        EvaluationAssert.assertEvaluation(EvaluationResult.FAIL, function.evaluate(createNonWGSRecordWithOptionalPriorTests()))
    }

    @Test
    fun `Should fail if molecular history does not contain WGS or targeted panel analysis and other panels do not cover any target gene`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(createNonWGSRecordWithOptionalPriorTests(listOf(archerPriorMolecularVariantRecord("GeneX", ""))))
        )
    }

    @Test
    fun `Should fail with specific message if no WGS or targeted panel analysis in history and other panels only cover part of the target genes`() {
        val evaluation = function.evaluate(
            createNonWGSRecordWithOptionalPriorTests(NSCLC_DRIVER_GENE_SET.drop(1).map { archerPriorMolecularVariantRecord(it, "") })
        )
        EvaluationAssert.assertEvaluation(EvaluationResult.FAIL, evaluation)
        Assertions.assertThat(evaluation.failSpecificMessages).containsExactly(
            "NSCLC driver gene statuses not available (missing: ${NSCLC_DRIVER_GENE_SET.first()})"
        )
    }

    companion object {
        private val function = NsclcDriverGeneStatusesAreAvailable()

        private fun createNonWGSRecordWithOptionalPriorTests(priorTest: List<PriorMolecularTest> = emptyList()): PatientRecord {
            val history = MolecularHistory.fromInputs(emptyList(), priorTest)
            return TestPatientFactory.createMinimalTestWGSPatientRecord().copy(molecularHistory = history)
        }
    }
}