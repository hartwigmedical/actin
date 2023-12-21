package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertMolecularEvaluation
import com.hartwig.actin.molecular.datamodel.driver.CopyNumberType
import com.hartwig.actin.molecular.datamodel.driver.TestCopyNumberFactory
import com.hartwig.actin.molecular.datamodel.driver.TestDisruptionFactory
import com.hartwig.actin.molecular.datamodel.driver.TestHomozygousDisruptionFactory
import com.hartwig.actin.molecular.datamodel.driver.TestVariantFactory
import org.junit.Test

class IsHomologousRepairDeficientTest {
    @Test
    fun canEvaluate() {
        val function = IsHomologousRepairDeficient()
        assertMolecularEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(
                MolecularTestFactory.withHomologousRepairDeficiencyAndVariant(
                    null,
                    TestVariantFactory.createMinimal().gene(MolecularConstants.HRD_GENES.iterator().next()).isReportable(false).build()
                )
            )
        )
        assertMolecularEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(
                MolecularTestFactory.withHomologousRepairDeficiencyAndVariant(
                    null,
                    TestVariantFactory.createMinimal()
                        .gene(MolecularConstants.HRD_GENES.iterator().next())
                        .isReportable(true)
                        .isBiallelic(true)
                        .build()
                )
            )
        )
        assertMolecularEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(
                MolecularTestFactory.withHomologousRepairDeficiencyAndVariant(
                    null,
                    TestVariantFactory.createMinimal()
                        .gene(MolecularConstants.HRD_GENES.iterator().next())
                        .isReportable(true)
                        .isBiallelic(false)
                        .build()
                )
            )
        )
        assertMolecularEvaluation(
            EvaluationResult.WARN,
            function.evaluate(
                MolecularTestFactory.withHomologousRepairDeficiencyAndVariant(
                    true,
                    TestVariantFactory.createMinimal()
                        .gene(MolecularConstants.HRD_GENES.iterator().next())
                        .isReportable(true)
                        .isBiallelic(false)
                        .build()
                )
            )
        )
        assertMolecularEvaluation(
            EvaluationResult.PASS,
            function.evaluate(
                MolecularTestFactory.withHomologousRepairDeficiencyAndVariant(
                    true,
                    TestVariantFactory.createMinimal()
                        .gene(MolecularConstants.HRD_GENES.iterator().next())
                        .isReportable(true)
                        .isBiallelic(true)
                        .build()
                )
            )
        )
        assertMolecularEvaluation(
            EvaluationResult.PASS,
            function.evaluate(
                MolecularTestFactory.withHomologousRepairDeficiencyAndLoss(
                    true,
                    TestCopyNumberFactory.createMinimal()
                        .type(CopyNumberType.LOSS)
                        .gene(MolecularConstants.HRD_GENES.iterator().next())
                        .build()
                )
            )
        )
        assertMolecularEvaluation(
            EvaluationResult.PASS,
            function.evaluate(
                MolecularTestFactory.withHomologousRepairDeficiencyAndHomozygousDisruption(
                    true,
                    TestHomozygousDisruptionFactory.createMinimal().gene(MolecularConstants.HRD_GENES.iterator().next()).build()
                )
            )
        )
        assertMolecularEvaluation(
            EvaluationResult.WARN,
            function.evaluate(
                MolecularTestFactory.withHomologousRepairDeficiencyAndDisruption(
                    true,
                    TestDisruptionFactory.createMinimal().gene(MolecularConstants.HRD_GENES.iterator().next()).build()
                )
            )
        )
        assertMolecularEvaluation(
            EvaluationResult.WARN,
            function.evaluate(
                MolecularTestFactory.withHomologousRepairDeficiencyAndVariant(
                    true,
                    TestVariantFactory.createMinimal().gene(MolecularConstants.HRD_GENES.iterator().next()).isReportable(false).build()
                )
            )
        )
        assertMolecularEvaluation(
            EvaluationResult.WARN,
            function.evaluate(
                MolecularTestFactory.withHomologousRepairDeficiencyAndVariant(
                    true,
                    TestVariantFactory.createMinimal().gene("other gene").isReportable(true).isBiallelic(false).build()
                )
            )
        )
        assertMolecularEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(
                MolecularTestFactory.withHomologousRepairDeficiencyAndVariant(
                    false,
                    TestVariantFactory.createMinimal().gene(MolecularConstants.HRD_GENES.iterator().next()).isReportable(true).build()
                )
            )
        )
    }
}