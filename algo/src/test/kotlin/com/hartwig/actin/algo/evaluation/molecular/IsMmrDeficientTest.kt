package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertMolecularEvaluation
import com.hartwig.actin.algo.evaluation.IhcTestItemConstants
import com.hartwig.actin.datamodel.TestPatientFactory
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.molecular.driver.CopyNumberType
import com.hartwig.actin.datamodel.molecular.driver.TestCopyNumberFactory
import com.hartwig.actin.datamodel.molecular.driver.TestDisruptionFactory
import com.hartwig.actin.datamodel.molecular.driver.TestHomozygousDisruptionFactory
import com.hartwig.actin.datamodel.molecular.driver.TestTranscriptCopyNumberImpactFactory
import com.hartwig.actin.datamodel.molecular.driver.TestVariantFactory
import com.hartwig.actin.datamodel.molecular.driver.Variant
import com.hartwig.actin.molecular.util.GeneConstants
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

private const val MMR_TERM = IhcTestItemConstants.MMR_TERM

class IsMmrDeficientTest {

    private val mmrGene = GeneConstants.MMR_GENES.first()
    private val function = IsMmrDeficient()

    @Test
    fun `Should evaluate to undetermined with unknown MSI and no MSI alteration`() {
        assertMolecularEvaluation(
            EvaluationResult.UNDETERMINED, function.evaluate(MolecularTestFactory.withMicrosatelliteStabilityAndVariant(null, msiVariant())),
            "No MSI test result"
        )
    }

    @Test
    fun `Should pass with reportable biallelic MSI variant`() {
        assertMolecularEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(
                MolecularTestFactory.withMicrosatelliteStabilityAndVariant(null, msiVariant(isReportable = true, isBiallelic = true))
            ),
            "No MSI test result but biallelic driver event(s) in MMR gene(s) (EPCAM) detected"
        )
    }

    @Test
    fun `Should be undetermined with reportable non-biallelic MSI variant`() {
        assertMolecularEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(MolecularTestFactory.withMicrosatelliteStabilityAndVariant(null, msiVariant(isReportable = true))),
            "No MSI test result but non-biallelic driver event(s) in MMR gene(s) (EPCAM) detected"
        )
    }

    @Test
    fun `Should warn with MSI true and reportable non-biallelic MSI variant`() {
        assertMolecularEvaluation(
            EvaluationResult.WARN,
            function.evaluate(MolecularTestFactory.withMicrosatelliteStabilityAndVariant(true, msiVariant(isReportable = true))),
            "Tumor is MSI but with only non-biallelic driver event(s) in MMR gene(s) (EPCAM)"
        )
    }

    @Test
    fun `Should pass with MSI true and reportable biallelic MSI variant`() {
        assertMolecularEvaluation(
            EvaluationResult.PASS,
            function.evaluate(
                MolecularTestFactory.withMicrosatelliteStabilityAndVariant(true, msiVariant(isReportable = true, isBiallelic = true))
            ),
            "Tumor is MSI with biallelic driver event(s) in MMR gene(s) (EPCAM)"
        )
    }

    @Test
    fun `Should pass with MSI true and MSI copy deletion`() {
        assertMolecularEvaluation(
            EvaluationResult.PASS,
            function.evaluate(
                MolecularTestFactory.withMicrosatelliteStabilityAndDeletion(
                    true,
                    TestCopyNumberFactory.createMinimal().copy(
                        canonicalImpact = TestTranscriptCopyNumberImpactFactory.createTranscriptCopyNumberImpact(CopyNumberType.FULL_DEL),
                        gene = mmrGene
                    )
                )
            ),
            "Tumor is MSI with biallelic driver event(s) in MMR gene(s) (EPCAM)"
        )
    }

    @Test
    fun `Should pass with MSI true and MSI homozygous disruption`() {
        assertMolecularEvaluation(
            EvaluationResult.PASS,
            function.evaluate(
                MolecularTestFactory.withMicrosatelliteStabilityAndHomozygousDisruption(
                    true, TestHomozygousDisruptionFactory.createMinimal().copy(gene = mmrGene)
                )
            ),
            "Tumor is MSI with biallelic driver event(s) in MMR gene(s) (EPCAM)"
        )
    }

    @Test
    fun `Should warn with MSI true and MSI disruption`() {
        assertMolecularEvaluation(
            EvaluationResult.WARN, function.evaluate(
                MolecularTestFactory.withMicrosatelliteStabilityAndDisruption(
                    true, TestDisruptionFactory.createMinimal().copy(gene = mmrGene)
                )
            ),
            "Tumor is MSI but without known driver event(s) in MMR gene(s)"
        )
    }

    @Test
    fun `Should warn with MSI true and non-reportable non-biallelic MSI variant`() {
        assertMolecularEvaluation(
            EvaluationResult.WARN, function.evaluate(MolecularTestFactory.withMicrosatelliteStabilityAndVariant(true, msiVariant())),
            "Tumor is MSI but without known driver event(s) in MMR gene(s)"
        )
    }

    @Test
    fun `Should warn with MSI true and variant in non-MSI gene`() {
        assertMolecularEvaluation(
            EvaluationResult.WARN, function.evaluate(
                MolecularTestFactory.withMicrosatelliteStabilityAndVariant(
                    true,
                    TestVariantFactory.createMinimal().copy(
                        gene = "other gene",
                        isReportable = true,
                        isBiallelic = false
                    )
                )
            ),
            "Tumor is MSI but without known driver event(s) in MMR gene(s)"
        )
    }

    @Test
    fun `Should fail with MSI false and reportable MSI variant`() {
        assertMolecularEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(MolecularTestFactory.withMicrosatelliteStabilityAndVariant(false, msiVariant(isReportable = true))),
            "Tumor is not dMMR"
        )
    }

    @Test
    fun `Should return undetermined when MSI variant with allelic status unknown`() {
        val evaluation = function.evaluate(
            MolecularTestFactory.withMicrosatelliteStabilityAndVariant(
                null,
                TestVariantFactory.createMinimal().copy(gene = mmrGene, isReportable = true)
            )
        )
        assertThat(evaluation.result).isEqualTo(EvaluationResult.UNDETERMINED)
        assertThat(evaluation.undeterminedMessagesStrings()).containsExactly("No MSI test result but driver event(s) in MMR gene(s) (EPCAM) detected")
    }

    @Test
    fun `Should pass with IHC MMR deficient test result`() {
        val result = function.evaluate(MolecularTestFactory.withIhcTests(MolecularTestFactory.ihcTest(MMR_TERM, scoreText = "Deficient")))

        assertMolecularEvaluation(EvaluationResult.PASS, result, "dMMR by IHC")
        assertThat(result.passMessagesStrings()).containsExactly("dMMR by IHC")
    }

    @Test
    fun `Should fail with IHC MMR proficient test result`() {
        assertMolecularEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(MolecularTestFactory.withIhcTests(MolecularTestFactory.ihcTest(MMR_TERM, scoreText = "Proficient"))),
            "Tumor is not dMMR"
        )
    }

    @Test
    fun `Should be undetermined with IHC MMR proficient test result`() {
        val result = function.evaluate(
            MolecularTestFactory.withOnlyIhcTests(
                listOf(
                    MolecularTestFactory.ihcTest(
                        GeneConstants.MMR_GENES.intersect(GeneConstants.IHC_LOSS_EVALUABLE_GENES).first(), scoreText = "loss"
                    )
                )
            )
        )
        assertMolecularEvaluation(
            EvaluationResult.UNDETERMINED,
            result,
            "Undetermined if tumor is dMMR by IHC - but loss detected of MMR gene by IHC"
        )
        assertThat(result.undeterminedMessagesStrings()).containsExactly("Undetermined if tumor is dMMR by IHC - but loss detected of MMR gene by IHC")
    }

    @Test
    fun `Should evaluate to undetermined with unclear IHC MMR test result`() {
        val result = function.evaluate(
            MolecularTestFactory.withOnlyIhcTests(
                listOf(
                    MolecularTestFactory.ihcTest(
                        MMR_TERM,
                        scoreText = "Proficient",
                        impliesIndeterminate = true
                    )
                )
            )
        )
        assertMolecularEvaluation(EvaluationResult.UNDETERMINED, result, "Undetermined dMMR result by IHC")
        assertThat(result.undeterminedMessagesStrings()).containsExactly("Undetermined dMMR result by IHC")
    }

    @Test
    fun `Should warn when MMR proficient by IHC and MSI by molecular test`() {
        assertMolecularEvaluation(
            EvaluationResult.WARN,
            function.evaluate(
                MolecularTestFactory.withIhcTestsMicrosatelliteStabilityAndVariant(
                    listOf(
                        MolecularTestFactory.ihcTest(
                            MMR_TERM,
                            scoreText = "Proficient"
                        )
                    ), true, msiVariant(isReportable = true, isBiallelic = true)
                )
            ),
            "Tumor is MMR proficient by IHC but MSI by molecular test"
        )
    }

    @Test
    fun `Should warn when MMR deficient by IHC and MSS by molecular test`() {
        assertMolecularEvaluation(
            EvaluationResult.WARN,
            function.evaluate(
                MolecularTestFactory.withIhcTestsMicrosatelliteStabilityAndVariant(
                    listOf(
                        MolecularTestFactory.ihcTest(
                            MMR_TERM,
                            scoreText = "Deficient"
                        )
                    ), false, msiVariant(isReportable = true)
                )
            ),
            "Tumor is dMMR by IHC but MSS by molecular test"
        )
    }

    @Test
    fun `Should resolve to undetermined when MMR deficiency test results are missing`() {
        assertMolecularEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(TestPatientFactory.createEmptyMolecularTestPatientRecord()),
            "Undetermined dMMR result by IHC"
        )
    }

    @Test
    fun `Should pass with IHC MMR deficient test result and no molecular test`() {
        assertMolecularEvaluation(
            EvaluationResult.PASS,
            function.evaluate(
                TestPatientFactory.createEmptyMolecularTestPatientRecord()
                    .copy(ihcTests = listOf(MolecularTestFactory.ihcTest(MMR_TERM, scoreText = "Deficient")))
            ),
            "Tumor is dMMR by IHC"
        )
    }

    @Test
    fun `Should fail with IHC MMR proficient test result and no molecular test`() {
        assertMolecularEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(
                TestPatientFactory.createEmptyMolecularTestPatientRecord()
                    .copy(ihcTests = listOf(MolecularTestFactory.ihcTest(MMR_TERM, scoreText = "Proficient")))
            ),
            "Tumor is not dMMR by IHC"
        )
    }

    @Test
    fun `Should resolve to undetermined with IHC MMR test result with indeterminate result and no molecular test`() {
        assertMolecularEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(
                TestPatientFactory.createEmptyMolecularTestPatientRecord()
                    .copy(ihcTests = listOf(MolecularTestFactory.ihcTest(MMR_TERM, scoreText = "Proficient", impliesIndeterminate = true)))
            ),
            "Undetermined dMMR result by IHC"
        )
    }

    private fun msiVariant(isReportable: Boolean = false, isBiallelic: Boolean = false): Variant {
        return TestVariantFactory.createMinimal().copy(
            gene = mmrGene,
            isReportable = isReportable,
            isBiallelic = isBiallelic
        )
    }
}