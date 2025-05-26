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

private const val REQUIRED_COPIES = 5
private const val PASSING_COPIES = REQUIRED_COPIES + 2
private const val NON_PASSING_COPIES = REQUIRED_COPIES - 2

class GeneHasSufficientCopyNumberTest {
    private val impactAmpWithSufficientCopyNr =
        TestTranscriptCopyNumberImpactFactory.createTranscriptCopyNumberImpact(CopyNumberType.FULL_GAIN, PASSING_COPIES, PASSING_COPIES)
    private val impactAmpWithInsufficientCopyNr = TestTranscriptCopyNumberImpactFactory.createTranscriptCopyNumberImpact(
        CopyNumberType.FULL_GAIN,
        NON_PASSING_COPIES,
        NON_PASSING_COPIES
    )
    private val impactNoneWithLowCopyNr =
        TestTranscriptCopyNumberImpactFactory.createTranscriptCopyNumberImpact(CopyNumberType.NONE, NON_PASSING_COPIES, NON_PASSING_COPIES)
    private val impactAmpWithUnknownCopyNr =
        TestTranscriptCopyNumberImpactFactory.createTranscriptCopyNumberImpact(CopyNumberType.FULL_GAIN, null, null)

    private val ampWithPassingSufficientCopiesOnCanonicalTranscript = TestCopyNumberFactory.createMinimal().copy(
        gene = "gene A",
        geneRole = GeneRole.ONCO,
        proteinEffect = ProteinEffect.GAIN_OF_FUNCTION,
        canonicalImpact = impactAmpWithSufficientCopyNr
    )
    private val ampWithPassingSufficientCopiesOnNonCanonicalTranscript = ampWithPassingSufficientCopiesOnCanonicalTranscript.copy(
        canonicalImpact = impactNoneWithLowCopyNr,
        otherImpacts = setOf(impactAmpWithSufficientCopyNr)
    )
    private val ampWithUnknownCopiesOnCanonicalTranscript = ampWithPassingSufficientCopiesOnCanonicalTranscript.copy(
        canonicalImpact = impactAmpWithUnknownCopyNr,
        otherImpacts = emptySet()
    )
    private val function = GeneHasSufficientCopyNumber("gene A", REQUIRED_COPIES)

    @Test
    fun `Should be undetermined when molecular record is empty`() {
        assertEvaluation(EvaluationResult.UNDETERMINED, TestPatientFactory.createEmptyMolecularTestPatientRecord())
    }

    @Test
    fun `Should fail with minimal WGS record`() {
        assertEvaluation(EvaluationResult.FAIL, TestPatientFactory.createMinimalTestWGSPatientRecord())
    }

    @Test
    fun `Should fail when requested min copy number is not met`() {
        assertEvaluation(
            EvaluationResult.FAIL,
            MolecularTestFactory.withCopyNumber(ampWithPassingSufficientCopiesOnCanonicalTranscript.copy(canonicalImpact = impactAmpWithInsufficientCopyNr))
        )
    }

    @Test
    fun `Should pass if requested min copy number is met on canonical transcript`() {
        assertEvaluation(
            EvaluationResult.PASS,
            MolecularTestFactory.withCopyNumber(ampWithPassingSufficientCopiesOnCanonicalTranscript)
        )
    }

    @Test
    fun `Should pass if requested min copy number is met on canonical transcript also if not an amp`() {
        assertEvaluation(
            EvaluationResult.PASS,
            MolecularTestFactory.withCopyNumber(
                ampWithPassingSufficientCopiesOnCanonicalTranscript.copy(
                    canonicalImpact = impactAmpWithSufficientCopyNr.copy(
                        type = CopyNumberType.NONE
                    )
                )
            )
        )
    }

    @Test
    fun `Should warn with non-oncogene`() {
        assertEvaluation(
            EvaluationResult.WARN,
            MolecularTestFactory.withCopyNumber(ampWithPassingSufficientCopiesOnCanonicalTranscript.copy(geneRole = GeneRole.TSG))
        )
    }

    @Test
    fun `Should warn with loss of function effect`() {
        assertEvaluation(
            EvaluationResult.WARN,
            MolecularTestFactory.withCopyNumber(ampWithPassingSufficientCopiesOnCanonicalTranscript.copy(proteinEffect = ProteinEffect.LOSS_OF_FUNCTION))
        )
    }

    @Test
    fun `Should warn with loss of function predicted effect`() {
        assertEvaluation(
            EvaluationResult.WARN,
            MolecularTestFactory.withCopyNumber(ampWithPassingSufficientCopiesOnCanonicalTranscript.copy(proteinEffect = ProteinEffect.LOSS_OF_FUNCTION_PREDICTED))
        )
    }

    @Test
    fun `Should warn when requested min copy number is not satisfied but max copy number is`() {
        assertEvaluation(
            EvaluationResult.WARN,
            MolecularTestFactory.withCopyNumber(
                ampWithPassingSufficientCopiesOnCanonicalTranscript.copy(
                    canonicalImpact = impactAmpWithSufficientCopyNr.copy(
                        type = CopyNumberType.FULL_GAIN,
                        minCopies = NON_PASSING_COPIES,
                        maxCopies = PASSING_COPIES
                    )
                )
            )
        )
    }

    @Test
    fun `Should warn with full gain on non-canonical transcript and no gain on canonical transcript`() {
        assertEvaluation(
            EvaluationResult.WARN,
            MolecularTestFactory.withCopyNumber(ampWithPassingSufficientCopiesOnNonCanonicalTranscript)
        )
    }

    @Test
    fun `Should pass if amp with unknown min copy nr is null but requested copy nr below assumed min copy nr for amps`() {
        assertEvaluation(
            EvaluationResult.PASS,
            MolecularTestFactory.withCopyNumber(ampWithUnknownCopiesOnCanonicalTranscript)
        )
    }

    @Test
    fun `Should warn if amp with unknown min copy nr and requested copy nr above assumed min copy nr for amps`() {
        assertMolecularEvaluation(
            EvaluationResult.WARN,
            GeneHasSufficientCopyNumber(
                "gene A",
                8
            ).evaluate(MolecularTestFactory.withCopyNumber(ampWithUnknownCopiesOnCanonicalTranscript))
        )
    }

    @Test
    fun `Should fail if gene copy nr is unknown and type is not an amp`() {
        assertEvaluation(
            EvaluationResult.FAIL,
            MolecularTestFactory.withCopyNumber(
                ampWithUnknownCopiesOnCanonicalTranscript.copy(
                    canonicalImpact = TestTranscriptCopyNumberImpactFactory.createTranscriptCopyNumberImpact(
                        CopyNumberType.NONE,
                    )
                )
            )
        )
    }

    @Test
    fun `Should evaluate undetermined with appropriate message when target coverage insufficient`() {
        val result = function.evaluate(
            TestPatientFactory.createMinimalTestWGSPatientRecord().copy(
                molecularHistory = MolecularHistory(molecularTests = listOf(TestMolecularFactory.createMinimalTestPanelRecord()))
            )
        )
        Assertions.assertThat(result.result).isEqualTo(EvaluationResult.UNDETERMINED)
        Assertions.assertThat(result.undeterminedMessages)
            .containsExactly("Sufficient copy number in gene gene A undetermined (not tested for amplifications or mutations)")
    }

    private fun assertEvaluation(result: EvaluationResult, record: PatientRecord) {
        assertMolecularEvaluation(result, function.evaluate(record))
    }
}