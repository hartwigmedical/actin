package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.TestDataFactory
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertMolecularEvaluation
import com.hartwig.actin.molecular.datamodel.driver.DriverLikelihood
import com.hartwig.actin.molecular.datamodel.driver.GeneRole
import com.hartwig.actin.molecular.datamodel.driver.ProteinEffect
import com.hartwig.actin.molecular.datamodel.driver.TestCopyNumberFactory
import com.hartwig.actin.molecular.datamodel.driver.TestDisruptionFactory
import com.hartwig.actin.molecular.datamodel.driver.TestFusionFactory
import com.hartwig.actin.molecular.datamodel.driver.TestHomozygousDisruptionFactory
import com.hartwig.actin.molecular.datamodel.driver.TestVariantFactory
import org.junit.Test

class GeneIsWildTypeTest {

    @Test
    fun canEvaluateVariants() {
        val function = GeneIsWildType("gene A")
        assertMolecularEvaluation(EvaluationResult.PASS, function.evaluate(TestDataFactory.createMinimalTestPatientRecord()))
        assertMolecularEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(
                MolecularTestFactory.withVariant(
                    TestVariantFactory.createMinimal()
                        .gene("gene A")
                        .isReportable(true)
                        .driverLikelihood(DriverLikelihood.HIGH)
                        .proteinEffect(ProteinEffect.GAIN_OF_FUNCTION)
                        .build()
                )
            )
        )
        assertMolecularEvaluation(
            EvaluationResult.WARN,
            function.evaluate(
                MolecularTestFactory.withVariant(
                    TestVariantFactory.createMinimal()
                        .gene("gene A")
                        .isReportable(true)
                        .driverLikelihood(DriverLikelihood.HIGH)
                        .proteinEffect(ProteinEffect.NO_EFFECT)
                        .build()
                )
            )
        )
        assertMolecularEvaluation(
            EvaluationResult.WARN,
            function.evaluate(
                MolecularTestFactory.withVariant(
                    TestVariantFactory.createMinimal()
                        .gene("gene A")
                        .isReportable(true)
                        .driverLikelihood(DriverLikelihood.LOW)
                        .proteinEffect(ProteinEffect.GAIN_OF_FUNCTION)
                        .build()
                )
            )
        )
    }

    @Test
    fun canEvaluateCopyNumbers() {
        val function = GeneIsWildType("gene A")
        assertMolecularEvaluation(EvaluationResult.PASS, function.evaluate(TestDataFactory.createMinimalTestPatientRecord()))
        assertMolecularEvaluation(
            EvaluationResult.WARN,
            function.evaluate(
                MolecularTestFactory.withCopyNumber(
                    TestCopyNumberFactory.createMinimal()
                        .gene("gene A")
                        .isReportable(true)
                        .proteinEffect(ProteinEffect.NO_EFFECT)
                        .build()
                )
            )
        )
        assertMolecularEvaluation(
            EvaluationResult.WARN,
            function.evaluate(
                MolecularTestFactory.withCopyNumber(
                    TestCopyNumberFactory.createMinimal()
                        .gene("gene A")
                        .isReportable(true)
                        .proteinEffect(ProteinEffect.GAIN_OF_FUNCTION)
                        .build()
                )
            )
        )
    }

    @Test
    fun canEvaluateHomozygousDisruptions() {
        val function = GeneIsWildType("gene A")
        assertMolecularEvaluation(EvaluationResult.PASS, function.evaluate(TestDataFactory.createMinimalTestPatientRecord()))
        assertMolecularEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(
                MolecularTestFactory.withHomozygousDisruption(
                    TestHomozygousDisruptionFactory.createMinimal()
                        .gene("gene A")
                        .isReportable(true)
                        .proteinEffect(ProteinEffect.LOSS_OF_FUNCTION)
                        .geneRole(GeneRole.TSG)
                        .build()
                )
            )
        )
        assertMolecularEvaluation(
            EvaluationResult.WARN,
            function.evaluate(
                MolecularTestFactory.withHomozygousDisruption(
                    TestHomozygousDisruptionFactory.createMinimal()
                        .gene("gene A")
                        .isReportable(true)
                        .proteinEffect(ProteinEffect.NO_EFFECT)
                        .geneRole(GeneRole.TSG)
                        .build()
                )
            )
        )
        assertMolecularEvaluation(
            EvaluationResult.PASS,
            function.evaluate(
                MolecularTestFactory.withHomozygousDisruption(
                    TestHomozygousDisruptionFactory.createMinimal()
                        .gene("gene A")
                        .isReportable(true)
                        .proteinEffect(ProteinEffect.LOSS_OF_FUNCTION)
                        .geneRole(GeneRole.ONCO)
                        .build()
                )
            )
        )
    }

    @Test
    fun canEvaluateDisruptions() {
        val function = GeneIsWildType("gene A")
        assertMolecularEvaluation(EvaluationResult.PASS, function.evaluate(TestDataFactory.createMinimalTestPatientRecord()))
        assertMolecularEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(
                MolecularTestFactory.withDisruption(
                    TestDisruptionFactory.createMinimal()
                        .gene("gene A")
                        .isReportable(true)
                        .proteinEffect(ProteinEffect.LOSS_OF_FUNCTION)
                        .geneRole(GeneRole.TSG)
                        .build()
                )
            )
        )
        assertMolecularEvaluation(
            EvaluationResult.WARN,
            function.evaluate(
                MolecularTestFactory.withDisruption(
                    TestDisruptionFactory.createMinimal()
                        .gene("gene A")
                        .isReportable(true)
                        .proteinEffect(ProteinEffect.NO_EFFECT)
                        .geneRole(GeneRole.TSG)
                        .build()
                )
            )
        )
        assertMolecularEvaluation(
            EvaluationResult.PASS,
            function.evaluate(
                MolecularTestFactory.withDisruption(
                    TestDisruptionFactory.createMinimal()
                        .gene("gene A")
                        .isReportable(true)
                        .proteinEffect(ProteinEffect.LOSS_OF_FUNCTION)
                        .geneRole(GeneRole.ONCO)
                        .build()
                )
            )
        )
    }

    @Test
    fun canEvaluateFusions() {
        val function = GeneIsWildType("gene A")
        assertMolecularEvaluation(EvaluationResult.PASS, function.evaluate(TestDataFactory.createMinimalTestPatientRecord()))
        assertMolecularEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(
                MolecularTestFactory.withFusion(
                    TestFusionFactory.createMinimal()
                        .geneStart("gene A")
                        .isReportable(true)
                        .proteinEffect(ProteinEffect.GAIN_OF_FUNCTION)
                        .build()
                )
            )
        )
        assertMolecularEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(
                MolecularTestFactory.withFusion(
                    TestFusionFactory.createMinimal()
                        .geneEnd("gene A")
                        .isReportable(true)
                        .proteinEffect(ProteinEffect.GAIN_OF_FUNCTION)
                        .build()
                )
            )
        )
        assertMolecularEvaluation(
            EvaluationResult.WARN,
            function.evaluate(
                MolecularTestFactory.withFusion(
                    TestFusionFactory.createMinimal()
                        .geneStart("gene A")
                        .isReportable(true)
                        .proteinEffect(ProteinEffect.NO_EFFECT)
                        .build()
                )
            )
        )
    }
}