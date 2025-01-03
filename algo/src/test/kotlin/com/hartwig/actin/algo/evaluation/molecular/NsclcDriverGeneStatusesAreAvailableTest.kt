package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationAssert
import com.hartwig.actin.algo.evaluation.molecular.NsclcDriverGeneStatusesAreAvailable.Companion.NSCLC_DRIVER_GENE_SET
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.TestPatientFactory
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.molecular.ExperimentType
import com.hartwig.actin.datamodel.molecular.MolecularHistory
import com.hartwig.actin.datamodel.molecular.MolecularTest
import com.hartwig.actin.datamodel.molecular.TestPanelRecordFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class NsclcDriverGeneStatusesAreAvailableTest {

    private val function = NsclcDriverGeneStatusesAreAvailable()

    @Test
    fun `Should pass if WGS is available and contains tumor cells`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(MolecularTestFactory.withExperimentTypeAndContainingTumorCells(ExperimentType.HARTWIG_WHOLE_GENOME, true))
        )
    }

    @Test
    fun `Should pass if targeted panel analysis is available and contains tumor cells`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(MolecularTestFactory.withExperimentTypeAndContainingTumorCells(ExperimentType.HARTWIG_TARGETED, true))
        )
    }

    @Test
    fun `Should pass if other panel is available and contains all target genes`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(
                createNonWGSRecordWithOptionalPriorTests(NSCLC_DRIVER_GENE_SET.map { panelWithTestForGene(it) })
            )
        )
    }

    @Test
    fun `Should fail if WGS is available but contains no tumor cells`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(MolecularTestFactory.withExperimentTypeAndContainingTumorCells(ExperimentType.HARTWIG_WHOLE_GENOME, false))
        )
    }

    @Test
    fun `Should fail if targeted panel analysis is available but contains no tumor cells`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(MolecularTestFactory.withExperimentTypeAndContainingTumorCells(ExperimentType.HARTWIG_TARGETED, false))
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
            function.evaluate(createNonWGSRecordWithOptionalPriorTests(listOf(panelWithTestForGene("GeneX"))))
        )
    }

    @Test
    fun `Should fail with specific message if no WGS or targeted panel analysis in history and other panels only cover part of the target genes`() {
        val evaluation = function.evaluate(
            createNonWGSRecordWithOptionalPriorTests(
                NSCLC_DRIVER_GENE_SET.drop(1).map { panelWithTestForGene(it) })
        )
        EvaluationAssert.assertEvaluation(EvaluationResult.FAIL, evaluation)
        assertThat(evaluation.failMessages).containsExactly(
            "NSCLC driver gene statuses not available (missing: ${NSCLC_DRIVER_GENE_SET.first()})"
        )
    }

    private fun panelWithTestForGene(it: String) = TestPanelRecordFactory.empty()
        .copy(testedGenes = setOf(it))

    private fun createNonWGSRecordWithOptionalPriorTests(priorTest: List<MolecularTest> = emptyList()): PatientRecord {
        val history = MolecularHistory(priorTest)
        return TestPatientFactory.createMinimalTestWGSPatientRecord().copy(molecularHistory = history)
    }
}