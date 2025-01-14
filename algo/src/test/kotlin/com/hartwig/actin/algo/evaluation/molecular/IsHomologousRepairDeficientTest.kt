package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertMolecularEvaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.molecular.Variant
import com.hartwig.actin.datamodel.molecular.driver.TestCopyNumberFactory
import com.hartwig.actin.datamodel.molecular.driver.TestDisruptionFactory
import com.hartwig.actin.datamodel.molecular.driver.TestHomozygousDisruptionFactory
import com.hartwig.actin.datamodel.molecular.driver.TestTranscriptCopyNumberImpactFactory
import com.hartwig.actin.datamodel.molecular.driver.TestVariantFactory
import com.hartwig.actin.datamodel.molecular.orange.driver.CopyNumberType
import org.junit.Test

class IsHomologousRepairDeficientTest {
    private val function = IsHomologousRepairDeficient()
    private val hrdGene = MolecularConstants.HRD_GENES.first()

    @Test
    fun canEvaluate() {
        assertMolecularEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(
                MolecularTestFactory.withHomologousRepairDeficiencyAndVariant(null, hrdVariant())
            )
        )
        assertMolecularEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(
                MolecularTestFactory.withHomologousRepairDeficiencyAndVariant(null, hrdVariant(isReportable = true, isBiallelic = true))
            )
        )
        assertMolecularEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(
                MolecularTestFactory.withHomologousRepairDeficiencyAndVariant(null, hrdVariant(isReportable = true, isBiallelic = false))
            )
        )
        assertMolecularEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(
                MolecularTestFactory.withHomologousRepairDeficiencyAndVariant(
                    null,
                    TestVariantFactory.createMinimal().copy(isReportable = true, gene = hrdGene)
                )
            )
        )
        assertMolecularEvaluation(
            EvaluationResult.WARN,
            function.evaluate(
                MolecularTestFactory.withHomologousRepairDeficiencyAndVariant(true, hrdVariant(isReportable = true, isBiallelic = false))
            )
        )
        assertMolecularEvaluation(
            EvaluationResult.PASS,
            function.evaluate(
                MolecularTestFactory.withHomologousRepairDeficiencyAndVariant(true, hrdVariant(isReportable = true, isBiallelic = true))
            )
        )
        assertMolecularEvaluation(
            EvaluationResult.PASS,
            function.evaluate(
                MolecularTestFactory.withHomologousRepairDeficiencyAndLoss(
                    true,
                    TestCopyNumberFactory.createMinimal().copy(
                        canonicalImpact = TestTranscriptCopyNumberImpactFactory.createTranscriptCopyNumberImpact(CopyNumberType.LOSS),
                        gene = hrdGene
                    )
                )
            )
        )
        assertMolecularEvaluation(
            EvaluationResult.PASS,
            function.evaluate(
                MolecularTestFactory.withHomologousRepairDeficiencyAndHomozygousDisruption(
                    true, TestHomozygousDisruptionFactory.createMinimal().copy(gene = hrdGene)
                )
            )
        )
        assertMolecularEvaluation(
            EvaluationResult.WARN,
            function.evaluate(
                MolecularTestFactory.withHomologousRepairDeficiencyAndDisruption(
                    true, TestDisruptionFactory.createMinimal().copy(gene = hrdGene)
                )
            )
        )
        assertMolecularEvaluation(
            EvaluationResult.WARN,
            function.evaluate(MolecularTestFactory.withHomologousRepairDeficiencyAndVariant(true, hrdVariant(isReportable = false)))
        )
        assertMolecularEvaluation(
            EvaluationResult.WARN,
            function.evaluate(
                MolecularTestFactory.withHomologousRepairDeficiencyAndVariant(
                    true,
                    TestVariantFactory.createMinimal().copy(
                        gene = "other gene",
                        isReportable = true,
                        extendedVariantDetails = TestVariantFactory.createMinimalExtended().copy(isBiallelic = false)
                    )
                )
            )
        )
        assertMolecularEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(MolecularTestFactory.withHomologousRepairDeficiencyAndVariant(false, hrdVariant(isReportable = true)))
        )
    }

    private fun hrdVariant(isReportable: Boolean = false, isBiallelic: Boolean = false): Variant {
        return TestVariantFactory.createMinimal().copy(
            gene = hrdGene,
            isReportable = isReportable,
            extendedVariantDetails = TestVariantFactory.createMinimalExtended().copy(isBiallelic = isBiallelic)
        )
    }
}