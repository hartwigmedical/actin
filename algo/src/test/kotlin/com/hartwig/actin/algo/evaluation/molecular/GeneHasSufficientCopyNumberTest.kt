package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertMolecularEvaluation
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.TestPatientFactory
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.molecular.MolecularHistory
import com.hartwig.actin.datamodel.molecular.TestMolecularFactory
import com.hartwig.actin.datamodel.molecular.driver.GeneRole
import com.hartwig.actin.datamodel.molecular.driver.ProteinEffect
import com.hartwig.actin.datamodel.molecular.driver.TestCopyNumberFactory
import com.hartwig.actin.datamodel.molecular.driver.TestTranscriptCopyNumberImpactFactory
import com.hartwig.actin.datamodel.molecular.driver.CopyNumberType
import org.assertj.core.api.Assertions
import org.junit.Test

class GeneHasSufficientCopyNumberTest {
    private val passingSufficientCopiesOnCanonicalTranscript = TestCopyNumberFactory.createMinimal().copy(
        gene = "gene A",
        geneRole = GeneRole.ONCO,
        proteinEffect = ProteinEffect.GAIN_OF_FUNCTION,
        isReportable = true,
        canonicalImpact = TestTranscriptCopyNumberImpactFactory.createTranscriptCopyNumberImpact(CopyNumberType.FULL_GAIN, 40, 40)
    )
    private val passingSufficientCopiesOnNonCanonicalTranscript = passingSufficientCopiesOnCanonicalTranscript.copy(
        canonicalImpact = TestTranscriptCopyNumberImpactFactory.createTranscriptCopyNumberImpact(CopyNumberType.NONE, 3, 3),
        otherImpacts = setOf(TestTranscriptCopyNumberImpactFactory.createTranscriptCopyNumberImpact(CopyNumberType.FULL_GAIN, 40, 40))
    )
    private val unknownCopiesOnAllTranscripts = passingSufficientCopiesOnCanonicalTranscript.copy(
        canonicalImpact = TestTranscriptCopyNumberImpactFactory.createTranscriptCopyNumberImpact(CopyNumberType.FULL_GAIN, null, null),
        otherImpacts = setOf(TestTranscriptCopyNumberImpactFactory.createTranscriptCopyNumberImpact(CopyNumberType.FULL_GAIN, null, null))
    )
    private val functionWithMinCopies = GeneHasSufficientCopyNumber("gene A", 5)

    @Test
    fun `Should return undetermined when molecular record is empty`() {
        assertEvaluation(EvaluationResult.UNDETERMINED, TestPatientFactory.createEmptyMolecularTestPatientRecord())
    }

    @Test
    fun `Should fail with minimal WGS record`() {
        assertEvaluation(EvaluationResult.FAIL, TestPatientFactory.createMinimalTestWGSPatientRecord())
    }

    @Test
    fun `Should fail when requested min copy number is not satisfied`() {
        assertEvaluation(
            EvaluationResult.FAIL,
            MolecularTestFactory.withCopyNumber(
                passingSufficientCopiesOnCanonicalTranscript.copy(
                    canonicalImpact = TestTranscriptCopyNumberImpactFactory.createTranscriptCopyNumberImpact(
                        CopyNumberType.FULL_GAIN,
                        3,
                        40
                    )
                )
            )
        )
    }

    @Test
    fun `Should warn with non-oncogene`() {
        assertEvaluation(
            EvaluationResult.WARN,
            MolecularTestFactory.withCopyNumber(passingSufficientCopiesOnCanonicalTranscript.copy(geneRole = GeneRole.TSG))
        )
    }

    @Test
    fun `Should warn with loss of function effect`() {
        assertEvaluation(
            EvaluationResult.WARN,
            MolecularTestFactory.withCopyNumber(passingSufficientCopiesOnCanonicalTranscript.copy(proteinEffect = ProteinEffect.LOSS_OF_FUNCTION))
        )
    }

    @Test
    fun `Should warn with loss of function predicted effect`() {
        assertEvaluation(
            EvaluationResult.WARN,
            MolecularTestFactory.withCopyNumber(passingSufficientCopiesOnCanonicalTranscript.copy(proteinEffect = ProteinEffect.LOSS_OF_FUNCTION_PREDICTED))
        )
    }

    @Test
    fun `Should warn with full gain on non-canonical transcript and no gain on canonical transcript`() {
        assertEvaluation(
            EvaluationResult.WARN,
            MolecularTestFactory.withCopyNumber(passingSufficientCopiesOnNonCanonicalTranscript)
        )
    }

    @Test
    fun `Should pass with unreportable copy number if requested min copy number is met`() {
        assertEvaluation(
            EvaluationResult.PASS,
            MolecularTestFactory.withCopyNumber(passingSufficientCopiesOnCanonicalTranscript.copy(isReportable = false))
        )
    }

    @Test
    fun `Should pass if requested min copy number is met`() {
        assertEvaluation(
            EvaluationResult.PASS,
            MolecularTestFactory.withCopyNumber(
                passingSufficientCopiesOnCanonicalTranscript.copy(
                    canonicalImpact = TestTranscriptCopyNumberImpactFactory.createTranscriptCopyNumberImpact(
                        CopyNumberType.FULL_GAIN,
                        5,
                        5
                    )
                )
            )
        )
    }

    @Test
    fun `Should pass if gene copy nr is null but requested copy nr below assumed min copy nr for amps`() {
        assertEvaluation(
            EvaluationResult.PASS,
            MolecularTestFactory.withCopyNumber(unknownCopiesOnAllTranscripts)
        )
    }


    @Test
    fun `Should fail if gene copy nr is null but type is del`() {
        assertEvaluation(
            EvaluationResult.FAIL,
            MolecularTestFactory.withCopyNumber(
                unknownCopiesOnAllTranscripts.copy(
                    canonicalImpact = TestTranscriptCopyNumberImpactFactory.createTranscriptCopyNumberImpact(
                        CopyNumberType.DEL,
                    ), otherImpacts = emptySet()
                )
            )
        )
    }

    @Test
    fun `Should be undetermined if gene copy nr is null and requested copy nr above assumed min copy nr for amps`() {
        assertMolecularEvaluation(
            EvaluationResult.WARN,
            GeneHasSufficientCopyNumber("gene A", 8).evaluate(MolecularTestFactory.withCopyNumber(unknownCopiesOnAllTranscripts))
        )
    }

    @Test
    fun `Should evaluate undetermined with appropriate message when target coverage insufficient`() {
        val result = functionWithMinCopies.evaluate(
            TestPatientFactory.createMinimalTestWGSPatientRecord().copy(
                molecularHistory = MolecularHistory(molecularTests = listOf(TestMolecularFactory.createMinimalTestPanelRecord()))
            )
        )
        Assertions.assertThat(result.result).isEqualTo(EvaluationResult.UNDETERMINED)
        Assertions.assertThat(result.undeterminedMessages)
            .containsExactly("Sufficient copy number in gene gene A undetermined (not tested for amplifications or mutations)")
    }

    private fun assertEvaluation(result: EvaluationResult, record: PatientRecord) {
        assertMolecularEvaluation(result, functionWithMinCopies.evaluate(record))
    }
}