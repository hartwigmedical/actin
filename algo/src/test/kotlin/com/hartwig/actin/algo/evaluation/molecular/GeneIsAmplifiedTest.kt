package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertMolecularEvaluation
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.TestPatientFactory
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.molecular.MolecularHistory
import com.hartwig.actin.datamodel.molecular.TestMolecularFactory
import com.hartwig.actin.datamodel.molecular.driver.CopyNumberType
import com.hartwig.actin.datamodel.molecular.driver.GeneRole
import com.hartwig.actin.datamodel.molecular.driver.ProteinEffect
import com.hartwig.actin.datamodel.molecular.driver.TestCopyNumberFactory
import com.hartwig.actin.datamodel.molecular.driver.TestTranscriptCopyNumberImpactFactory
import org.assertj.core.api.Assertions
import org.junit.Test

private const val PLOIDY = 3.0

class GeneIsAmplifiedTest {
    private val passingAmpOnCanonicalTranscript = TestCopyNumberFactory.createMinimal().copy(
        gene = "gene A",
        geneRole = GeneRole.ONCO,
        proteinEffect = ProteinEffect.GAIN_OF_FUNCTION,
        isReportable = true,
        canonicalImpact = TestTranscriptCopyNumberImpactFactory.createTranscriptCopyNumberImpact(CopyNumberType.FULL_GAIN, 40, 40)
    )
    private val passingAmpOnNonCanonicalTranscript = TestCopyNumberFactory.createMinimal().copy(
        gene = "gene A",
        geneRole = GeneRole.ONCO,
        proteinEffect = ProteinEffect.GAIN_OF_FUNCTION,
        isReportable = true,
        canonicalImpact = TestTranscriptCopyNumberImpactFactory.createTranscriptCopyNumberImpact(),
        otherImpacts = setOf(TestTranscriptCopyNumberImpactFactory.createTranscriptCopyNumberImpact(CopyNumberType.FULL_GAIN, 40, 40))
    )
    private val functionWithMinCopies = GeneIsAmplified("gene A", 5)
    private val functionWithNoMinCopies = GeneIsAmplified("gene A", null)

    @Test
    fun `Should return undetermined when molecular record is empty`() {
        assertEvaluation(EvaluationResult.UNDETERMINED, TestPatientFactory.createEmptyMolecularTestPatientRecord())
    }

    @Test
    fun `Should fail with minimal WGS record`() {
        assertEvaluation(EvaluationResult.FAIL, TestPatientFactory.createMinimalTestWGSPatientRecord())
    }

    @Test
    fun `Should fail with null ploidy`() {
        assertEvaluation(EvaluationResult.FAIL, MolecularTestFactory.withPloidyAndCopyNumber(null, passingAmpOnCanonicalTranscript))
    }

    @Test
    fun `Should warn with non-oncogene`() {
        assertEvaluation(
            EvaluationResult.WARN,
            MolecularTestFactory.withPloidyAndCopyNumber(PLOIDY, passingAmpOnCanonicalTranscript.copy(geneRole = GeneRole.TSG))
        )
    }

    @Test
    fun `Should warn with loss of function effect`() {
        assertEvaluation(
            EvaluationResult.WARN,
            MolecularTestFactory.withPloidyAndCopyNumber(
                PLOIDY,
                passingAmpOnCanonicalTranscript.copy(proteinEffect = ProteinEffect.LOSS_OF_FUNCTION)
            )
        )
    }

    @Test
    fun `Should warn with loss of function predicted effect`() {
        assertEvaluation(
            EvaluationResult.WARN,
            MolecularTestFactory.withPloidyAndCopyNumber(
                PLOIDY,
                passingAmpOnCanonicalTranscript.copy(proteinEffect = ProteinEffect.LOSS_OF_FUNCTION_PREDICTED)
            )
        )
    }

    @Test
    fun `Should warn with unreportable amplification when no min copies requested`() {
        assertMolecularEvaluation(
            EvaluationResult.WARN,
            functionWithNoMinCopies.evaluate(
                MolecularTestFactory.withPloidyAndCopyNumber(
                    PLOIDY,
                    passingAmpOnCanonicalTranscript.copy(isReportable = false)
                )
            )
        )
    }

    @Test
    fun `Should pass with unreportable amplification when min copies requested and satisfied`() {
        assertMolecularEvaluation(
            EvaluationResult.PASS,
            functionWithMinCopies.evaluate(
                MolecularTestFactory.withPloidyAndCopyNumber(
                    PLOIDY,
                    passingAmpOnCanonicalTranscript.copy(isReportable = false)
                )
            )
        )
    }

    @Test
    fun `Should pass with reportable full gain of function`() {
        assertEvaluation(EvaluationResult.PASS, MolecularTestFactory.withPloidyAndCopyNumber(PLOIDY, passingAmpOnCanonicalTranscript))
    }

    @Test
    fun `Should fail when requested min copy number is not satisfied`() {
        assertMolecularEvaluation(
            EvaluationResult.FAIL,
            functionWithMinCopies.evaluate(
                MolecularTestFactory.withPloidyAndCopyNumber(
                    PLOIDY,
                    passingAmpOnCanonicalTranscript.copy(
                        canonicalImpact = TestTranscriptCopyNumberImpactFactory.createTranscriptCopyNumberImpact(
                            CopyNumberType.FULL_GAIN,
                            3,
                            40
                        )
                    )
                )
            )
        )
    }

    @Test
    fun `Should warn with reportable partial amplification`() {
        assertEvaluation(
            EvaluationResult.WARN,
            MolecularTestFactory.withPloidyAndCopyNumber(
                PLOIDY,
                passingAmpOnCanonicalTranscript.copy(
                    canonicalImpact = TestTranscriptCopyNumberImpactFactory.createTranscriptCopyNumberImpact(
                        CopyNumberType.FULL_GAIN,
                        6,
                        40
                    )
                )
            )
        )
    }

    @Test
    fun `Should fail with copy numbers below amplification threshold`() {
        assertEvaluation(
            EvaluationResult.FAIL,
            MolecularTestFactory.withPloidyAndCopyNumber(
                PLOIDY,
                passingAmpOnCanonicalTranscript.copy(
                    canonicalImpact = TestTranscriptCopyNumberImpactFactory.createTranscriptCopyNumberImpact(
                        CopyNumberType.FULL_GAIN,
                        4,
                        4
                    )
                )
            )
        )
    }

    @Test
    fun `Should warn with copy numbers below amplification threshold if requested min copy number is met`() {
        val function = GeneIsAmplified("gene A", 4)
        assertMolecularEvaluation(
            EvaluationResult.WARN,
            function.evaluate(
                MolecularTestFactory.withPloidyAndCopyNumber(
                    PLOIDY,
                    passingAmpOnCanonicalTranscript.copy(
                        canonicalImpact = TestTranscriptCopyNumberImpactFactory.createTranscriptCopyNumberImpact(
                            CopyNumberType.FULL_GAIN,
                            4,
                            4
                        )
                    )
                )
            )
        )
    }

    @Test
    fun `Should warn with full gain on non-canonical transcript and no gain on canonical transcript`() {
        val function = GeneIsAmplified("gene A", 4)
        assertMolecularEvaluation(
            EvaluationResult.WARN,
            function.evaluate(
                MolecularTestFactory.withPloidyAndCopyNumber(
                    PLOIDY,
                    passingAmpOnNonCanonicalTranscript
                )
            )
        )
    }

    @Test
    fun `Should evaluate undetermined with appropriate message when target coverage insufficient`() {
        val result = GeneIsAmplified("gene A", 2).evaluate(
            TestPatientFactory.createMinimalTestWGSPatientRecord().copy(
                molecularHistory = MolecularHistory(molecularTests = listOf(TestMolecularFactory.createMinimalTestPanelRecord()))
            )
        )
        Assertions.assertThat(result.result).isEqualTo(EvaluationResult.UNDETERMINED)
        Assertions.assertThat(result.undeterminedMessages)
            .containsExactly("Amplification of gene gene A undetermined (not tested for amplifications)")
    }

    private fun assertEvaluation(result: EvaluationResult, record: PatientRecord) {
        assertMolecularEvaluation(result, functionWithMinCopies.evaluate(record))
        assertMolecularEvaluation(result, functionWithNoMinCopies.evaluate(record))
    }
}