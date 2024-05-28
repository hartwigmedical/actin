package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.TestPatientFactory
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert
import com.hartwig.actin.algo.evaluation.molecular.NsclcDriverGeneStatusesAreAvailable.Companion.NSCLC_DRIVER_GENE_SET
import com.hartwig.actin.molecular.datamodel.ExperimentType
import com.hartwig.actin.molecular.datamodel.MolecularHistory
import com.hartwig.actin.molecular.datamodel.MolecularTest
import com.hartwig.actin.molecular.datamodel.panel.archer.ArcherPanelExtraction
import com.hartwig.actin.molecular.datamodel.panel.archer.ArcherSmallVariant
import org.assertj.core.api.Assertions
import org.junit.Test

private const val HGVS_VARIANT = "c.123C>T"

class NsclcDriverGeneStatusesAreAvailableTest {

    private val function = NsclcDriverGeneStatusesAreAvailable()

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
                createNonWGSRecordWithOptionalPriorTests(NSCLC_DRIVER_GENE_SET.map { archerPanelWithVarientForGene(it) })
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
            function.evaluate(createNonWGSRecordWithOptionalPriorTests(listOf(archerPanelWithVarientForGene("GeneX"))))
        )
    }

    @Test
    fun `Should fail with specific message if no WGS or targeted panel analysis in history and other panels only cover part of the target genes`() {
        val evaluation = function.evaluate(
            createNonWGSRecordWithOptionalPriorTests(
                NSCLC_DRIVER_GENE_SET.drop(1).map { archerPanelWithVarientForGene(it) })
        )
        EvaluationAssert.assertEvaluation(EvaluationResult.FAIL, evaluation)
        Assertions.assertThat(evaluation.failSpecificMessages).containsExactly(
            "NSCLC driver gene statuses not available (missing: ${NSCLC_DRIVER_GENE_SET.first()})"
        )
    }

    private fun archerPanelWithVarientForGene(it: String) = TestPanelRecordFactory.empty()
        .copy(archerPanelExtraction = ArcherPanelExtraction(variants = listOf(ArcherSmallVariant(it, HGVS_VARIANT))))

    private fun createNonWGSRecordWithOptionalPriorTests(priorTest: List<MolecularTest<*>> = emptyList()): PatientRecord {
        val history = MolecularHistory(priorTest)
        return TestPatientFactory.createMinimalTestWGSPatientRecord().copy(molecularHistory = history)
    }
}