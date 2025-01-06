package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertMolecularEvaluation
import com.hartwig.actin.datamodel.TestPatientFactory
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.molecular.Variant
import com.hartwig.actin.datamodel.molecular.driver.TestCopyNumberFactory
import com.hartwig.actin.datamodel.molecular.driver.TestDisruptionFactory
import com.hartwig.actin.datamodel.molecular.driver.TestHomozygousDisruptionFactory
import com.hartwig.actin.datamodel.molecular.driver.TestTranscriptCopyNumberImpactFactory
import com.hartwig.actin.datamodel.molecular.driver.TestVariantFactory
import com.hartwig.actin.datamodel.molecular.orange.driver.CopyNumberType
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class IsMicrosatelliteUnstableTest {
    private val msiGene = MolecularConstants.MSI_GENES.first()
    private val function = IsMicrosatelliteUnstable()

    @Test
    fun `Should fail with unknown MSI and no MSI alteration`() {
        assertMolecularEvaluation(
            EvaluationResult.FAIL, function.evaluate(MolecularTestFactory.withMicrosatelliteInstabilityAndVariant(null, msiVariant()))
        )
    }

    @Test
    fun `Should pass with reportable biallelic MSI variant`() {
        assertMolecularEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(
                MolecularTestFactory.withMicrosatelliteInstabilityAndVariant(null, msiVariant(isReportable = true, isBiallelic = true))
            )
        )
    }

    @Test
    fun `Should be undetermined with reportable non-biallelic MSI variant`() {
        assertMolecularEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(MolecularTestFactory.withMicrosatelliteInstabilityAndVariant(null, msiVariant(isReportable = true)))
        )
    }

    @Test
    fun `Should warn with MSI true and reportable non-biallelic MSI variant`() {
        assertMolecularEvaluation(
            EvaluationResult.WARN,
            function.evaluate(MolecularTestFactory.withMicrosatelliteInstabilityAndVariant(true, msiVariant(isReportable = true)))
        )
    }

    @Test
    fun `Should pass with MSI true and reportable biallelic MSI variant`() {
        assertMolecularEvaluation(
            EvaluationResult.PASS,
            function.evaluate(
                MolecularTestFactory.withMicrosatelliteInstabilityAndVariant(true, msiVariant(isReportable = true, isBiallelic = true))
            )
        )
    }

    @Test
    fun `Should pass with MSI true and MSI copy loss`() {
        assertMolecularEvaluation(
            EvaluationResult.PASS,
            function.evaluate(
                MolecularTestFactory.withMicrosatelliteInstabilityAndLoss(
                    true,
                    TestCopyNumberFactory.createMinimal().copy(
                        canonicalImpact = TestTranscriptCopyNumberImpactFactory.createTranscriptCopyNumberImpact(CopyNumberType.LOSS),
                        gene = msiGene
                    )
                )
            )
        )
    }

    @Test
    fun `Should pass with MSI true and MSI homozygous disruption`() {
        assertMolecularEvaluation(
            EvaluationResult.PASS,
            function.evaluate(
                MolecularTestFactory.withMicrosatelliteInstabilityAndHomozygousDisruption(
                    true, TestHomozygousDisruptionFactory.createMinimal().copy(gene = msiGene)
                )
            )
        )
    }

    @Test
    fun `Should warn with MSI true and MSI disruption`() {
        assertMolecularEvaluation(
            EvaluationResult.WARN, function.evaluate(
                MolecularTestFactory.withMicrosatelliteInstabilityAndDisruption(
                    true, TestDisruptionFactory.createMinimal().copy(gene = msiGene)
                )
            )
        )
    }

    @Test
    fun `Should warn with MSI true and non-reportable non-biallelic MSI variant`() {
        assertMolecularEvaluation(
            EvaluationResult.WARN, function.evaluate(MolecularTestFactory.withMicrosatelliteInstabilityAndVariant(true, msiVariant()))
        )
    }

    @Test
    fun `Should warn with MSI true and variant in non-MSI gene`() {
        assertMolecularEvaluation(
            EvaluationResult.WARN, function.evaluate(
                MolecularTestFactory.withMicrosatelliteInstabilityAndVariant(
                    true,
                    TestVariantFactory.createMinimal().copy(
                        gene = "other gene",
                        isReportable = true,
                        extendedVariantDetails = TestVariantFactory.createMinimalExtended().copy(isBiallelic = false)
                    )
                )
            )
        )
    }

    @Test
    fun `Should fail with MSI false and reportable MSI variant`() {
        assertMolecularEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(MolecularTestFactory.withMicrosatelliteInstabilityAndVariant(false, msiVariant(isReportable = true)))
        )
    }

    @Test
    fun `Should return undetermined and state that MSI status is unknown when molecular record not available`() {
        val evaluation = function.evaluate(TestPatientFactory.createEmptyMolecularTestPatientRecord())
        assertThat(evaluation.result).isEqualTo(EvaluationResult.UNDETERMINED)
        assertThat(evaluation.undeterminedMessages).containsExactly("MSI status undetermined")
    }

    @Test
    fun `Should return undetermined when MSI variant with allelic status unknown`() {
        val evaluation = function.evaluate(
            MolecularTestFactory.withMicrosatelliteInstabilityAndVariant(
                null,
                TestVariantFactory.createMinimal().copy(gene = msiGene, isReportable = true)
            )
        )
        assertThat(evaluation.result).isEqualTo(EvaluationResult.UNDETERMINED)
        assertThat(evaluation.undeterminedMessages).containsExactly("Unknown MSI status but drivers with unknown allelic status in MSI genes")
    }

    private fun msiVariant(isReportable: Boolean = false, isBiallelic: Boolean = false): Variant {
        return TestVariantFactory.createMinimal().copy(
            gene = msiGene,
            isReportable = isReportable,
            extendedVariantDetails = TestVariantFactory.createMinimalExtended().copy(isBiallelic = isBiallelic)
        )
    }
}