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
import com.hartwig.actin.datamodel.molecular.driver.TranscriptCopyNumberImpact
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

private const val REQUIRED_COPY_NR = 5
private const val PASSING_COPY_NR = REQUIRED_COPY_NR + 2
private const val NON_PASSING_COPY_NR = REQUIRED_COPY_NR - 2
private const val GENE = "gene A"

class GeneIsAmplifiedTest {

    private val eligibleImpact =
        TestTranscriptCopyNumberImpactFactory.createTranscriptCopyNumberImpact(
            CopyNumberType.FULL_GAIN,
            PASSING_COPY_NR,
            PASSING_COPY_NR
        )
    private val impactAmpWithInsufficientCopyNr = TestTranscriptCopyNumberImpactFactory.createTranscriptCopyNumberImpact(
        CopyNumberType.FULL_GAIN,
        NON_PASSING_COPY_NR,
        NON_PASSING_COPY_NR
    )
    private val impactNoneWithLowCopyNr =
        TestTranscriptCopyNumberImpactFactory.createTranscriptCopyNumberImpact(
            CopyNumberType.NONE,
            NON_PASSING_COPY_NR,
            NON_PASSING_COPY_NR
        )
    private val impactAmpWithUnknownCopyNr =
        TestTranscriptCopyNumberImpactFactory.createTranscriptCopyNumberImpact(CopyNumberType.FULL_GAIN, null, null)

    private val eligibleAmp = TestCopyNumberFactory.createMinimal().copy(
        gene = GENE,
        geneRole = GeneRole.ONCO,
        proteinEffect = ProteinEffect.GAIN_OF_FUNCTION,
        canonicalImpact = eligibleImpact,
        otherImpacts = emptySet()
    )
    private val ampOnNonCanonicalTranscript = TestCopyNumberFactory.createMinimal().copy(
        gene = GENE,
        geneRole = GeneRole.ONCO,
        proteinEffect = ProteinEffect.GAIN_OF_FUNCTION,
        canonicalImpact = impactNoneWithLowCopyNr,
        otherImpacts = setOf(eligibleImpact)
    )
    private val ampOnCanonicalTranscriptWithoutCopies = eligibleAmp.copy(canonicalImpact = impactAmpWithUnknownCopyNr)
    private val ampButInsufficientCopies = eligibleAmp.copy(canonicalImpact = impactAmpWithInsufficientCopyNr)
    private val ineligibleNoneCopyNumber = eligibleAmp.copy(canonicalImpact = impactNoneWithLowCopyNr)

    private val functionWithMinCopies = GeneIsAmplified(GENE, REQUIRED_COPY_NR)
    private val functionWithNoMinCopies = GeneIsAmplified(GENE, null)

    @Test
    fun `Should be undetermined when molecular record is empty`() {
        assertBothFunctions(EvaluationResult.UNDETERMINED, TestPatientFactory.createEmptyMolecularTestPatientRecord())
    }

    @Test
    fun `Should fail with minimal WGS record`() {
        assertBothFunctions(EvaluationResult.FAIL, TestPatientFactory.createMinimalTestWGSPatientRecord())
    }

    @Test
    fun `Should fail when not amplified and ineligible copy number`() {
        assertBothFunctions(EvaluationResult.FAIL, MolecularTestFactory.withCopyNumber(ineligibleNoneCopyNumber))
    }

    @Test
    fun `Should fail when amplified but copies requested and not met`() {
        assertMolecularEvaluation(
            EvaluationResult.FAIL,
            functionWithMinCopies.evaluate(MolecularTestFactory.withCopyNumber(ampButInsufficientCopies))
        )
    }

    @Test
    fun `Should pass with full amp on canonical transcript when copies are null and copies not requested or copies requested and meeting threshold`() {
        assertBothFunctions(EvaluationResult.PASS, MolecularTestFactory.withCopyNumber(eligibleAmp))
    }

    @Test
    fun `Should warn if gene role is TSG`() {
        assertBothFunctions(EvaluationResult.WARN, MolecularTestFactory.withCopyNumber(eligibleAmp.copy(geneRole = GeneRole.TSG)))
    }

    @Test
    fun `Should warn with loss of function effect`() {
        assertBothFunctions(
            EvaluationResult.WARN,
            MolecularTestFactory.withCopyNumber(eligibleAmp.copy(proteinEffect = ProteinEffect.LOSS_OF_FUNCTION))
        )
    }

    @Test
    fun `Should warn with loss of function predicted effect`() {
        assertBothFunctions(
            EvaluationResult.WARN,
            MolecularTestFactory.withCopyNumber(eligibleAmp.copy(proteinEffect = ProteinEffect.LOSS_OF_FUNCTION_PREDICTED))
        )
    }

    @Test
    fun `Should warn with partial amplification when copies requested and met in max copies or copies not requested`() {
        assertBothFunctions(
            EvaluationResult.WARN,
            MolecularTestFactory.withCopyNumber(
                eligibleAmp.copy(
                    eligibleImpact.copy(
                        type = CopyNumberType.PARTIAL_GAIN,
                        minCopies = NON_PASSING_COPY_NR,
                    )
                )
            )
        )
    }

    @Test
    fun `Should fail with partial amplification when copies requested and not met in max copies`() {
        assertMolecularEvaluation(
            EvaluationResult.FAIL,
            functionWithMinCopies.evaluate(
                MolecularTestFactory.withCopyNumber(
                    eligibleAmp.copy(
                        eligibleImpact.copy(
                            type = CopyNumberType.PARTIAL_GAIN,
                            minCopies = NON_PASSING_COPY_NR,
                            maxCopies = NON_PASSING_COPY_NR
                        )
                    )
                )
            )
        )
    }

    @Test
    fun `Should warn with full gain on non-canonical transcript and no gain on canonical transcript`() {
        assertBothFunctions(EvaluationResult.WARN, MolecularTestFactory.withCopyNumber(ampOnNonCanonicalTranscript))
    }

    @Test
    fun `Should warn when not an amp but requested copy nr and min copy nr meets threshold`() {
        assertMolecularEvaluation(
            EvaluationResult.WARN,
            functionWithMinCopies.evaluate(
                MolecularTestFactory.withCopyNumber(
                    eligibleAmp.copy(
                        canonicalImpact = eligibleImpact.copy(type = CopyNumberType.NONE)
                    )
                )
            )
        )
    }

    @Test
    fun `Should warn when not an amp but no requested copy nr and copy nr meets amp cutoff with known ploidy`() {
        val ploidy = 3.00
        assertMolecularEvaluation(
            EvaluationResult.WARN,
            functionWithNoMinCopies.evaluate(
                MolecularTestFactory.withPloidyAndCopyNumber(
                    ploidy = ploidy,
                    eligibleAmp.copy(
                        canonicalImpact = eligibleImpact.copy(
                            type = CopyNumberType.NONE,
                            minCopies = ploidy.toInt() * PASSING_COPY_NR,
                            maxCopies = ploidy.toInt() * PASSING_COPY_NR
                        )
                    )
                )
            )
        )
    }

    @Test
    fun `Should warn when not an amp but no requested copy nr and copy nr meets amp cutoff if ploidy is null`() {
        assertMolecularEvaluation(
            EvaluationResult.WARN,
            functionWithNoMinCopies.evaluate(
                MolecularTestFactory.withPloidyAndCopyNumber(
                    ploidy = null,
                    eligibleAmp.copy(
                        canonicalImpact = eligibleImpact.copy(
                            type = CopyNumberType.NONE,
                            minCopies = 20,
                            maxCopies = 20
                        )
                    )
                )
            )
        )
    }

    @Test
    fun `Should fail when not an amp but no requested copy nr and copy nr does not meet standard ploidy x amp threshold`() {
        assertMolecularEvaluation(
            EvaluationResult.FAIL,
            functionWithNoMinCopies.evaluate(
                MolecularTestFactory.withPloidyAndCopyNumber(
                    ploidy = null,
                    eligibleAmp.copy(
                        canonicalImpact = eligibleImpact.copy(
                            type = CopyNumberType.NONE,
                            minCopies = 5,
                            maxCopies = 5
                        )
                    )
                )
            )
        )
    }

    @Test
    fun `Should warn with copy numbers meeting amplification threshold if not amp but copy nr meets requested copy nr`() {
        val function = GeneIsAmplified(GENE, 4)
        assertMolecularEvaluation(
            EvaluationResult.WARN,
            function.evaluate(
                MolecularTestFactory.withCopyNumber(
                    eligibleAmp.copy(
                        canonicalImpact = TestTranscriptCopyNumberImpactFactory.createTranscriptCopyNumberImpact(
                            CopyNumberType.NONE,
                            4,
                            4
                        )
                    )
                )
            )
        )
    }

    @Test
    fun `Should pass with full amp if copies are null if no requested copy nr or requested copy nr below assumed copy nr`() {
        assertBothFunctions(
            EvaluationResult.PASS,
            MolecularTestFactory.withCopyNumber(ampOnCanonicalTranscriptWithoutCopies)
        )
    }

    @Test
    fun `Should warn with full amp if copies are null and copies requested`() {
        assertMolecularEvaluation(
            EvaluationResult.WARN,
            GeneIsAmplified(GENE, 10).evaluate(
                MolecularTestFactory.withCopyNumber(
                    ampOnCanonicalTranscriptWithoutCopies
                )
            )
        )
    }

    @Test
    fun `Should warn with partial amp if copies are null if no requested copy nr or requested copy nr below assumed copy nr`() {
        assertBothFunctions(
            EvaluationResult.WARN,
            MolecularTestFactory.withCopyNumber(
                ampOnCanonicalTranscriptWithoutCopies.copy(
                    canonicalImpact = impactAmpWithUnknownCopyNr.copy(
                        type = CopyNumberType.PARTIAL_GAIN
                    )
                )
            )
        )
    }

    @Test
    fun `Should fail if not an amp if copies are null and copies not requested`() {
        assertBothFunctions(
            EvaluationResult.FAIL,
            MolecularTestFactory.withCopyNumber(
                ampOnCanonicalTranscriptWithoutCopies.copy(
                    canonicalImpact = TranscriptCopyNumberImpact(
                        type = CopyNumberType.NONE,
                        minCopies = null,
                        maxCopies = null,
                        transcriptId = ""
                    )
                )
            )
        )
    }

    @Test
    fun `Should evaluate undetermined with appropriate message when target coverage insufficient`() {
        val result = GeneIsAmplified(GENE, 2).evaluate(
            TestPatientFactory.createMinimalTestWGSPatientRecord().copy(
                molecularHistory = MolecularHistory(molecularTests = listOf(TestMolecularFactory.createMinimalTestPanelRecord()))
            )
        )
        assertThat(result.result).isEqualTo(EvaluationResult.UNDETERMINED)
        assertThat(result.undeterminedMessages).containsExactly("Amplification of gene gene A undetermined (not tested for amplifications)")
    }

    private fun assertBothFunctions(result: EvaluationResult, record: PatientRecord) {
        assertMolecularEvaluation(result, functionWithMinCopies.evaluate(record))
        assertMolecularEvaluation(result, functionWithNoMinCopies.evaluate(record))
    }
}