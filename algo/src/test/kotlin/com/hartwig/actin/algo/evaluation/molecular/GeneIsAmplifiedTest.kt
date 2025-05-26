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
import org.assertj.core.api.Assertions
import org.junit.Test

private const val REQUIRED_COPIES = 5
private const val PASSING_COPIES = REQUIRED_COPIES + 2
private const val NON_PASSING_COPIES = REQUIRED_COPIES - 2

class GeneIsAmplifiedTest {
    private val impactAmpWithSufficientCopyNr =
        TestTranscriptCopyNumberImpactFactory.createTranscriptCopyNumberImpact(CopyNumberType.FULL_GAIN, PASSING_COPIES, PASSING_COPIES)
    private val impactNoneWithLowCopyNr =
        TestTranscriptCopyNumberImpactFactory.createTranscriptCopyNumberImpact(CopyNumberType.NONE, NON_PASSING_COPIES, NON_PASSING_COPIES)

    private val passingAmpOnCanonicalTranscript = TestCopyNumberFactory.createMinimal().copy(
        gene = "gene A",
        geneRole = GeneRole.ONCO,
        proteinEffect = ProteinEffect.GAIN_OF_FUNCTION,
        canonicalImpact = impactAmpWithSufficientCopyNr
    )
    private val passingAmpOnNonCanonicalTranscript = TestCopyNumberFactory.createMinimal().copy(
        gene = "gene A",
        geneRole = GeneRole.ONCO,
        proteinEffect = ProteinEffect.GAIN_OF_FUNCTION,
        canonicalImpact = impactNoneWithLowCopyNr,
        otherImpacts = setOf(impactAmpWithSufficientCopyNr)
    )
    private val ampOnCanonicalTranscriptWithoutCopies =
        passingAmpOnCanonicalTranscript.copy(canonicalImpact = impactAmpWithSufficientCopyNr.copy(minCopies = null, maxCopies = null))
    private val ampButInsufficientCopies =
        passingAmpOnCanonicalTranscript.copy(
            canonicalImpact = impactAmpWithSufficientCopyNr.copy(
                minCopies = NON_PASSING_COPIES,
                maxCopies = NON_PASSING_COPIES
            )
        )
    private val nonAmp =
        passingAmpOnCanonicalTranscript.copy(
            canonicalImpact = impactAmpWithSufficientCopyNr.copy(
                type = CopyNumberType.NONE,
                minCopies = NON_PASSING_COPIES,
                maxCopies = NON_PASSING_COPIES
            )
        )

    private val functionWithMinCopies = GeneIsAmplified("gene A", REQUIRED_COPIES)
    private val functionWithNoMinCopies = GeneIsAmplified("gene A", null)

    @Test
    fun `Should be undetermined when molecular record is empty`() {
        assertBothFunctions(EvaluationResult.UNDETERMINED, TestPatientFactory.createEmptyMolecularTestPatientRecord())
    }

    @Test
    fun `Should fail with minimal WGS record`() {
        assertBothFunctions(EvaluationResult.FAIL, TestPatientFactory.createMinimalTestWGSPatientRecord())
    }

    @Test
    fun `Should fail when not amplified and no min copy nr requested and copy nr not meeting amp threshold`() {
        assertMolecularEvaluation(
            EvaluationResult.FAIL,
            functionWithNoMinCopies.evaluate(MolecularTestFactory.withCopyNumber(nonAmp))
        )
    }

    @Test
    fun `Should fail when amplified but copies requested and not met`() {
        assertMolecularEvaluation(
            EvaluationResult.FAIL,
            functionWithMinCopies.evaluate(MolecularTestFactory.withCopyNumber(ampButInsufficientCopies))
        )
    }

    @Test
    fun `Should pass with full amp on canonical transcript when copies are null and copies not requested`() {
        assertMolecularEvaluation(
            EvaluationResult.PASS,
            functionWithNoMinCopies.evaluate(MolecularTestFactory.withCopyNumber(passingAmpOnCanonicalTranscript))
        )
    }

    @Test
    fun `Should pass with full amp on canonical transcript and copies requested and meeting threshold`() {
        assertMolecularEvaluation(
            EvaluationResult.PASS,
            functionWithMinCopies.evaluate(MolecularTestFactory.withCopyNumber(passingAmpOnCanonicalTranscript))
        )
    }

    @Test
    fun `Should warn with non-oncogene`() {
        assertBothFunctions(
            EvaluationResult.WARN,
            MolecularTestFactory.withCopyNumber(passingAmpOnCanonicalTranscript.copy(geneRole = GeneRole.TSG))
        )
    }

    @Test
    fun `Should warn with loss of function effect`() {
        assertBothFunctions(
            EvaluationResult.WARN,
            MolecularTestFactory.withCopyNumber(
                passingAmpOnCanonicalTranscript.copy(proteinEffect = ProteinEffect.LOSS_OF_FUNCTION)
            )
        )
    }

    @Test
    fun `Should warn with loss of function predicted effect`() {
        assertBothFunctions(
            EvaluationResult.WARN,
            MolecularTestFactory.withCopyNumber(
                passingAmpOnCanonicalTranscript.copy(proteinEffect = ProteinEffect.LOSS_OF_FUNCTION_PREDICTED)
            )
        )
    }

    @Test
    fun `Should warn with partial amplification when copies requested and met`() {
        assertBothFunctions(
            EvaluationResult.WARN,
            MolecularTestFactory.withCopyNumber(
                passingAmpOnCanonicalTranscript.copy(
                    impactAmpWithSufficientCopyNr.copy(
                        type = CopyNumberType.PARTIAL_GAIN,
                        minCopies = NON_PASSING_COPIES,
                        maxCopies = PASSING_COPIES
                    )
                )
            )
        )
    }

    @Test
    fun `Should fail with partial amplification when copies requested and not met`() {
        assertMolecularEvaluation(
            EvaluationResult.FAIL,
            functionWithMinCopies.evaluate(
                MolecularTestFactory.withCopyNumber(
                    passingAmpOnCanonicalTranscript.copy(
                        impactAmpWithSufficientCopyNr.copy(
                            type = CopyNumberType.PARTIAL_GAIN,
                            minCopies = 2,
                            maxCopies = 2
                        )
                    )
                )
            )
        )
    }

    @Test
    fun `Should warn with full gain on non-canonical transcript and no gain on canonical transcript`() {
        assertBothFunctions(EvaluationResult.WARN, MolecularTestFactory.withCopyNumber(passingAmpOnNonCanonicalTranscript))
    }

    @Test
    fun `Should warn with amp if copies are null and copies requested`() {
        assertMolecularEvaluation(
            EvaluationResult.WARN,
            GeneIsAmplified("gene A", 10).evaluate(
                MolecularTestFactory.withCopyNumber(
                    ampOnCanonicalTranscriptWithoutCopies
                )
            )
        )
    }

    @Test
    fun `Should pass with amp if copies are null but requested copy nr below assumed copy nr`() {
        assertMolecularEvaluation(
            EvaluationResult.PASS,
            functionWithMinCopies.evaluate(MolecularTestFactory.withCopyNumber(ampOnCanonicalTranscriptWithoutCopies))
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
    fun `Should warn when not an amp but requested copy nr and min copy nr meets threshold`() {
        assertMolecularEvaluation(
            EvaluationResult.WARN,
            functionWithMinCopies.evaluate(
                MolecularTestFactory.withCopyNumber(
                    passingAmpOnCanonicalTranscript.copy(
                        canonicalImpact = impactAmpWithSufficientCopyNr.copy(type = CopyNumberType.NONE)
                    )
                )
            )
        )
    }

    @Test
    fun `Should warn when not an amp but no requested copy nr and copy nr meets amp cutoff`() {
        val ploidy = 3.00
        assertMolecularEvaluation(
            EvaluationResult.WARN,
            functionWithNoMinCopies.evaluate(
                MolecularTestFactory.withPloidyAndCopyNumber(
                    ploidy = ploidy,
                    passingAmpOnCanonicalTranscript.copy(
                        canonicalImpact = impactAmpWithSufficientCopyNr.copy(
                            type = CopyNumberType.NONE,
                            minCopies = ploidy.toInt() * PASSING_COPIES,
                            maxCopies = ploidy.toInt() * PASSING_COPIES
                        )
                    )
                )
            )
        )
    }

    @Test
    fun `Should warn when not an amp but no requested copy nr and ploidy but meets cutoff`() {
        assertMolecularEvaluation(
            EvaluationResult.WARN,
            functionWithNoMinCopies.evaluate(
                MolecularTestFactory.withPloidyAndCopyNumber(
                    ploidy = null,
                    passingAmpOnCanonicalTranscript.copy(
                        canonicalImpact = impactAmpWithSufficientCopyNr.copy(
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
    fun `Should fail when not an amp but no requested copy nr and ploidy does not meet cutoff`() {
        assertMolecularEvaluation(
            EvaluationResult.FAIL,
            functionWithNoMinCopies.evaluate(
                MolecularTestFactory.withPloidyAndCopyNumber(
                    ploidy = null,
                    passingAmpOnCanonicalTranscript.copy(
                        canonicalImpact = impactAmpWithSufficientCopyNr.copy(
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
    fun `Should warn with copy numbers meeting amplification threshold if not amp and ploidy is known`() {
        val function = GeneIsAmplified("gene A", 4)
        assertMolecularEvaluation(
            EvaluationResult.WARN,
            function.evaluate(
                MolecularTestFactory.withCopyNumber(
                    passingAmpOnCanonicalTranscript.copy(
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

    private fun assertBothFunctions(result: EvaluationResult, record: PatientRecord) {
        assertMolecularEvaluation(result, functionWithMinCopies.evaluate(record))
        assertMolecularEvaluation(result, functionWithNoMinCopies.evaluate(record))
    }
}