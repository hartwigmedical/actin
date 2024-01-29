package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertMolecularEvaluation
import com.hartwig.actin.molecular.datamodel.driver.CopyNumberType
import com.hartwig.actin.molecular.datamodel.driver.TestCopyNumberFactory
import com.hartwig.actin.molecular.datamodel.driver.TestDisruptionFactory
import com.hartwig.actin.molecular.datamodel.driver.TestHomozygousDisruptionFactory
import com.hartwig.actin.molecular.datamodel.driver.TestVariantFactory
import com.hartwig.actin.molecular.datamodel.driver.Variant
import org.junit.Test

class IsMicrosatelliteUnstableTest {
    private val msiGene = MolecularConstants.MSI_GENES.first()
    private val function = IsMicrosatelliteUnstable()

    @Test
    fun canEvaluate() {
        assertMolecularEvaluation(
            EvaluationResult.FAIL, function.evaluate(MolecularTestFactory.withMicrosatelliteInstabilityAndVariant(null, msiVariant()))
        )
        assertMolecularEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(
                MolecularTestFactory.withMicrosatelliteInstabilityAndVariant(null, msiVariant(isReportable = true, isBiallelic = true))
            )
        )
        assertMolecularEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(MolecularTestFactory.withMicrosatelliteInstabilityAndVariant(null, msiVariant(isReportable = true)))
        )
        assertMolecularEvaluation(
            EvaluationResult.WARN,
            function.evaluate(MolecularTestFactory.withMicrosatelliteInstabilityAndVariant(true, msiVariant(isReportable = true)))
        )
        assertMolecularEvaluation(
            EvaluationResult.PASS,
            function.evaluate(
                MolecularTestFactory.withMicrosatelliteInstabilityAndVariant(true, msiVariant(isReportable = true, isBiallelic = true))
            )
        )
        assertMolecularEvaluation(
            EvaluationResult.PASS,
            function.evaluate(
                MolecularTestFactory.withMicrosatelliteInstabilityAndLoss(
                    true, TestCopyNumberFactory.createMinimal().copy(type = CopyNumberType.LOSS, gene = msiGene)
                )
            )
        )
        assertMolecularEvaluation(
            EvaluationResult.PASS,
            function.evaluate(
                MolecularTestFactory.withMicrosatelliteInstabilityAndHomozygousDisruption(
                    true, TestHomozygousDisruptionFactory.createMinimal().copy(gene = msiGene)
                )
            )
        )
        assertMolecularEvaluation(
            EvaluationResult.WARN,
            function.evaluate(
                MolecularTestFactory.withMicrosatelliteInstabilityAndDisruption(
                    true, TestDisruptionFactory.createMinimal().copy(gene = msiGene)
                )
            )
        )
        assertMolecularEvaluation(
            EvaluationResult.WARN, function.evaluate(MolecularTestFactory.withMicrosatelliteInstabilityAndVariant(true, msiVariant()))
        )
        assertMolecularEvaluation(
            EvaluationResult.WARN,
            function.evaluate(
                MolecularTestFactory.withMicrosatelliteInstabilityAndVariant(
                    true, TestVariantFactory.createMinimal().copy(gene = "other gene", isReportable = true, isBiallelic = false)
                )
            )
        )
        assertMolecularEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(MolecularTestFactory.withMicrosatelliteInstabilityAndVariant(false, msiVariant(isReportable = true)))
        )
    }

    private fun msiVariant(isReportable: Boolean = false, isBiallelic: Boolean = false): Variant {
        return TestVariantFactory.createMinimal().copy(
            gene = msiGene, isReportable = isReportable, isBiallelic = isBiallelic
        )
    }
}