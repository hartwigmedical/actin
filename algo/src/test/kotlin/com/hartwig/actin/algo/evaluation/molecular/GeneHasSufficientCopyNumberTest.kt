package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertMolecularEvaluation
import com.hartwig.actin.datamodel.TestPatientFactory
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.molecular.TestMolecularFactory
import com.hartwig.actin.datamodel.molecular.driver.CopyNumberType
import com.hartwig.actin.datamodel.molecular.driver.GeneRole
import com.hartwig.actin.datamodel.molecular.driver.ProteinEffect
import com.hartwig.actin.datamodel.molecular.driver.TestCopyNumberFactory
import com.hartwig.actin.datamodel.molecular.driver.TestTranscriptCopyNumberImpactFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

private const val REQUIRED_COPY_NR = 5
private const val PASSING_COPY_NR = REQUIRED_COPY_NR + 2
private const val NON_PASSING_COPY_NR = REQUIRED_COPY_NR - 2
private const val GENE = "gene A"

class GeneHasSufficientCopyNumberTest {

    private val eligibleImpact =
        TestTranscriptCopyNumberImpactFactory.createTranscriptCopyNumberImpact(CopyNumberType.FULL_GAIN, PASSING_COPY_NR, PASSING_COPY_NR)
    private val impactAmpWithInsufficientCopyNr = TestTranscriptCopyNumberImpactFactory.createTranscriptCopyNumberImpact(
        CopyNumberType.FULL_GAIN,
        NON_PASSING_COPY_NR,
        NON_PASSING_COPY_NR
    )
    private val impactAmpWithUnknownCopyNr =
        TestTranscriptCopyNumberImpactFactory.createTranscriptCopyNumberImpact(CopyNumberType.FULL_GAIN, null, null)
    private val impactNoneWithLowCopyNr =
        TestTranscriptCopyNumberImpactFactory.createTranscriptCopyNumberImpact(
            CopyNumberType.NONE,
            NON_PASSING_COPY_NR,
            NON_PASSING_COPY_NR
        )

    private val eligibleAmp = TestCopyNumberFactory.createMinimal().copy(
        gene = GENE,
        geneRole = GeneRole.ONCO,
        proteinEffect = ProteinEffect.GAIN_OF_FUNCTION,
        canonicalImpact = eligibleImpact,
        otherImpacts = emptySet()
    )
    private val ampWithSufficientCopiesOnNonCanonicalTranscript = eligibleAmp.copy(
        canonicalImpact = impactNoneWithLowCopyNr,
        otherImpacts = setOf(eligibleImpact)
    )
    private val ampWithUnknownCopiesOnCanonicalTranscript = eligibleAmp.copy(
        canonicalImpact = impactAmpWithUnknownCopyNr,
        otherImpacts = emptySet()
    )
    private val function = GeneHasSufficientCopyNumber(GENE, REQUIRED_COPY_NR)

    @Test
    fun `Should be undetermined when molecular record is empty`() {
        assertMolecularEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(TestPatientFactory.createEmptyMolecularTestPatientRecord()),
            "No molecular results of sufficient quality"
        )
    }

    @Test
    fun `Should fail with minimal WGS record`() {
        assertMolecularEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(TestPatientFactory.createMinimalTestWGSPatientRecord()),
            "gene A does not have at least 5 copies"
        )
    }

    @Test
    fun `Should fail when requested min copy number is not met`() {
        assertMolecularEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(MolecularTestFactory.withCopyNumber(eligibleAmp.copy(canonicalImpact = impactAmpWithInsufficientCopyNr))),
            "gene A does not have at least 5 copies"
        )
    }

    @Test
    fun `Should pass if requested min copy number is met on canonical transcript`() {
        assertMolecularEvaluation(
            EvaluationResult.PASS,
            function.evaluate(
                MolecularTestFactory.withCopyNumber(eligibleAmp)
            ),
            "gene A copy number is above 5"
        )
    }

    @Test
    fun `Should pass if requested min copy number is met on canonical transcript also if not an amp`() {
        assertMolecularEvaluation(
            EvaluationResult.PASS,
            function.evaluate(
                MolecularTestFactory.withCopyNumber(
                    eligibleAmp.copy(
                        canonicalImpact = eligibleImpact.copy(
                            type = CopyNumberType.NONE
                        )
                    )
                )
            ),
            "gene A copy number is above 5"
        )
    }

    @Test
    fun `Should warn if gene role is TSG`() {
        assertMolecularEvaluation(
            EvaluationResult.WARN,
            function.evaluate(
                MolecularTestFactory.withCopyNumber(eligibleAmp.copy(geneRole = GeneRole.TSG))
            ),
            "gene A has at least 5 copies but gene known as TSG in "
        )
    }

    @Test
    fun `Should warn with loss of function effect`() {
        assertMolecularEvaluation(
            EvaluationResult.WARN,
            function.evaluate(
                MolecularTestFactory.withCopyNumber(eligibleAmp.copy(proteinEffect = ProteinEffect.LOSS_OF_FUNCTION))
            ),
            "gene A has at least 5 copies but gene associated with loss-of-function protein impact in "
        )
    }

    @Test
    fun `Should warn with loss of function predicted effect`() {
        assertMolecularEvaluation(
            EvaluationResult.WARN,
            function.evaluate(
                MolecularTestFactory.withCopyNumber(eligibleAmp.copy(proteinEffect = ProteinEffect.LOSS_OF_FUNCTION_PREDICTED))
            ),
            "gene A has at least 5 copies but gene associated with loss-of-function protein impact in "
        )
    }

    @Test
    fun `Should warn when requested min copy number is not satisfied but max copy number is`() {
        assertMolecularEvaluation(
            EvaluationResult.WARN,
            function.evaluate(
                MolecularTestFactory.withCopyNumber(
                    eligibleAmp.copy(
                        canonicalImpact = eligibleImpact.copy(
                            minCopies = NON_PASSING_COPY_NR,
                        )
                    )
                )
            ),
            "gene A has at least 5 copies but only partially"
        )
    }

    @Test
    fun `Should warn with full gain on non-canonical transcript and no gain on canonical transcript`() {
        assertMolecularEvaluation(
            EvaluationResult.WARN,
            function.evaluate(
                MolecularTestFactory.withCopyNumber(ampWithSufficientCopiesOnNonCanonicalTranscript)
            ),
            "gene A has at least 5 copies but on non-canonical transcript"
        )
    }

    @Test
    fun `Should pass if amp with unknown min copy nr but requested copy nr below assumed min copy nr for amps`() {
        assertMolecularEvaluation(
            EvaluationResult.PASS,
            function.evaluate(
                MolecularTestFactory.withCopyNumber(ampWithUnknownCopiesOnCanonicalTranscript)
            ),
            "gene A is amplified hence assumed gene has a copy number >= 5 copies"
        )
    }

    @Test
    fun `Should warn if amp with unknown min copy nr and requested copy nr above assumed min copy nr for amps`() {
        assertMolecularEvaluation(
            EvaluationResult.WARN,
            GeneHasSufficientCopyNumber(
                GENE,
                10
            ).evaluate(MolecularTestFactory.withCopyNumber(ampWithUnknownCopiesOnCanonicalTranscript)),
            "gene A is amplified but undetermined if gene has a copy number >= 10 copies"
        )
    }

    @Test
    fun `Should fail if gene copy nr is unknown and type is not an amp`() {
        assertMolecularEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(
                MolecularTestFactory.withCopyNumber(
                    ampWithUnknownCopiesOnCanonicalTranscript.copy(
                        canonicalImpact = impactAmpWithUnknownCopyNr.copy(type = CopyNumberType.NONE)
                    )
                )
            ),
            "gene A does not have at least 5 copies"
        )
    }

    @Test
    fun `Should warn with partial amp if copies are null if no requested copy nr or requested copy nr below assumed copy nr`() {
        assertMolecularEvaluation(
            EvaluationResult.WARN,
            function.evaluate(
                MolecularTestFactory.withCopyNumber(
                    ampWithUnknownCopiesOnCanonicalTranscript.copy(
                        canonicalImpact = impactAmpWithUnknownCopyNr.copy(type = CopyNumberType.PARTIAL_GAIN)
                    )
                )
            ),
            "gene A is amplified but partially and undetermined if copy nr meets threshold of >= 5 copies"
        )
    }

    @Test
    fun `Should evaluate undetermined with appropriate message when target coverage insufficient`() {
        val result = function.evaluate(
            TestPatientFactory.createMinimalTestWGSPatientRecord().copy(
                molecularTests = listOf(TestMolecularFactory.createMinimalPanelTest())
            )
        )
        assertThat(result.result).isEqualTo(EvaluationResult.UNDETERMINED)
        assertThat(result.undeterminedMessagesStrings())
            .containsExactly("Sufficient copy number in gene gene A undetermined (not tested for amplifications or mutations)")
    }
}