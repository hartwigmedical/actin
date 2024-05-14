package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.TestPatientFactory
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertMolecularEvaluation
import com.hartwig.actin.molecular.datamodel.driver.CopyNumberType
import com.hartwig.actin.molecular.datamodel.driver.TestCopyNumberFactory
import com.hartwig.actin.molecular.datamodel.driver.TestDisruptionFactory
import com.hartwig.actin.molecular.datamodel.driver.TestHomozygousDisruptionFactory
import com.hartwig.actin.molecular.datamodel.driver.TestVariantFactory
import com.hartwig.actin.molecular.datamodel.driver.Variant
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class IsMicrosatelliteUnstableTest {
    private val msiGene = MolecularConstants.MSI_GENES.first()
    private val function = IsMicrosatelliteUnstable()

    @Test
    fun `Should fail with unknown MSI and no MSI alteration`() {
        assertMolecularEvaluation(
            EvaluationResult.FAIL, function.evaluate(TestMolecularTestFactory.withMicrosatelliteInstabilityAndVariant(null, msiVariant()))
        )
    }

    @Test
    fun `Should pass with reportable biallelic MSI variant`() {
        assertMolecularEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(
                TestMolecularTestFactory.withMicrosatelliteInstabilityAndVariant(null, msiVariant(isReportable = true, isBiallelic = true))
            )
        )
    }

    @Test
    fun `Should be undetermined with reportable non-biallelic MSI variant`() {
        assertMolecularEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(TestMolecularTestFactory.withMicrosatelliteInstabilityAndVariant(null, msiVariant(isReportable = true)))
        )
    }

    @Test
    fun `Should warn with MSI true and reportable non-biallelic MSI variant`() {
        assertMolecularEvaluation(
            EvaluationResult.WARN,
            function.evaluate(TestMolecularTestFactory.withMicrosatelliteInstabilityAndVariant(true, msiVariant(isReportable = true)))
        )
    }

    @Test
    fun `Should pass with MSI true and reportable biallelic MSI variant`() {
        assertMolecularEvaluation(
            EvaluationResult.PASS,
            function.evaluate(
                TestMolecularTestFactory.withMicrosatelliteInstabilityAndVariant(true, msiVariant(isReportable = true, isBiallelic = true))
            )
        )
    }

    @Test
    fun `Should pass with MSI true and MSI copy loss`() {
        assertMolecularEvaluation(
            EvaluationResult.PASS,
            function.evaluate(
                TestMolecularTestFactory.withMicrosatelliteInstabilityAndLoss(
                    true, TestCopyNumberFactory.createMinimal().copy(type = CopyNumberType.LOSS, gene = msiGene)
                )
            )
        )
    }

    @Test
    fun `Should pass with MSI true and MSI homozygous disruption`() {
        assertMolecularEvaluation(
            EvaluationResult.PASS,
            function.evaluate(
                TestMolecularTestFactory.withMicrosatelliteInstabilityAndHomozygousDisruption(
                    true, TestHomozygousDisruptionFactory.createMinimal().copy(gene = msiGene)
                )
            )
        )
    }

    @Test
    fun `Should warn with MSI true and MSI disruption`() {
        assertMolecularEvaluation(
            EvaluationResult.WARN, function.evaluate(
                TestMolecularTestFactory.withMicrosatelliteInstabilityAndDisruption(
                    true, TestDisruptionFactory.createMinimal().copy(gene = msiGene)
                )
            )
        )
    }

    @Test
    fun `Should warn with MSI true and non-reportable non-biallelic MSI variant`() {
        assertMolecularEvaluation(
            EvaluationResult.WARN, function.evaluate(TestMolecularTestFactory.withMicrosatelliteInstabilityAndVariant(true, msiVariant()))
        )
    }

    @Test
    fun `Should warn with MSI true and variant in non-MSI gene`() {
        assertMolecularEvaluation(
            EvaluationResult.WARN, function.evaluate(
                TestMolecularTestFactory.withMicrosatelliteInstabilityAndVariant(
                    true, TestVariantFactory.createMinimal().copy(gene = "other gene", isReportable = true, isBiallelic = false)
                )
            )
        )
    }

    @Test
    fun `Should fail with MSI false and reportable MSI variant`() {
        assertMolecularEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(TestMolecularTestFactory.withMicrosatelliteInstabilityAndVariant(false, msiVariant(isReportable = true)))
        )
    }

    @Test
    fun `Should return undetermined and state that MSI status is unknown when molecular record not available`() {
        val evaluation = function.evaluate(TestPatientFactory.createEmptyMolecularTestPatientRecord())
        assertThat(evaluation.result).isEqualTo(EvaluationResult.UNDETERMINED)
        assertThat(evaluation.undeterminedSpecificMessages).containsExactly("Undetermined if tumor is MSI")
        assertThat(evaluation.undeterminedGeneralMessages).containsExactly("Undetermined MSI status")
    }

    private fun msiVariant(isReportable: Boolean = false, isBiallelic: Boolean = false): Variant {
        return TestVariantFactory.createMinimal().copy(
            gene = msiGene, isReportable = isReportable, isBiallelic = isBiallelic
        )
    }
}