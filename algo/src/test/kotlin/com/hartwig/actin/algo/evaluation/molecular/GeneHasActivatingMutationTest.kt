package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.TestDataFactory
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.molecular.datamodel.driver.CodingEffect
import com.hartwig.actin.molecular.datamodel.driver.DriverLikelihood
import com.hartwig.actin.molecular.datamodel.driver.GeneRole
import com.hartwig.actin.molecular.datamodel.driver.ProteinEffect
import com.hartwig.actin.molecular.datamodel.driver.TestTranscriptImpactFactory
import com.hartwig.actin.molecular.datamodel.driver.TestVariantFactory
import com.hartwig.actin.molecular.datamodel.driver.Variant
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertMolecularEvaluation
import org.junit.Test

class GeneHasActivatingMutationTest {
    @Test
    fun canEvaluate() {
        val function = GeneHasActivatingMutation("gene A")
        assertMolecularEvaluation(EvaluationResult.FAIL, function.evaluate(TestDataFactory.createMinimalTestPatientRecord()))
        val activatingVariant: Variant = TestVariantFactory.builder()
            .gene("gene A")
            .isReportable(true)
            .driverLikelihood(DriverLikelihood.HIGH)
            .geneRole(GeneRole.ONCO)
            .proteinEffect(ProteinEffect.GAIN_OF_FUNCTION)
            .isHotspot(true)
            .isAssociatedWithDrugResistance(false)
            .clonalLikelihood(0.8)
            .build()
        assertMolecularEvaluation(EvaluationResult.PASS, function.evaluate(MolecularTestFactory.withVariant(activatingVariant)))
        assertMolecularEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(
                MolecularTestFactory.withVariant(
                    TestVariantFactory.builder()
                        .from(activatingVariant)
                        .gene("gene B")
                        .build()
                )
            )
        )
        assertMolecularEvaluation(
            EvaluationResult.WARN,
            function.evaluate(
                MolecularTestFactory.withVariant(
                    TestVariantFactory.builder()
                        .from(activatingVariant)
                        .geneRole(GeneRole.TSG)
                        .build()
                )
            )
        )
        assertMolecularEvaluation(
            EvaluationResult.WARN,
            function.evaluate(
                MolecularTestFactory.withVariant(
                    TestVariantFactory.builder()
                        .from(activatingVariant)
                        .isAssociatedWithDrugResistance(true)
                        .build()
                )
            )
        )
        assertMolecularEvaluation(
            EvaluationResult.WARN,
            function.evaluate(
                MolecularTestFactory.withVariant(
                    TestVariantFactory.builder()
                        .from(activatingVariant)
                        .geneRole(GeneRole.TSG)
                        .build()
                )
            )
        )
        assertMolecularEvaluation(
            EvaluationResult.WARN,
            function.evaluate(
                MolecularTestFactory.withVariant(
                    TestVariantFactory.builder()
                        .from(activatingVariant)
                        .proteinEffect(ProteinEffect.UNKNOWN)
                        .isHotspot(false)
                        .build()
                )
            )
        )
        assertMolecularEvaluation(
            EvaluationResult.WARN,
            function.evaluate(
                MolecularTestFactory.withVariant(
                    TestVariantFactory.builder()
                        .from(activatingVariant)
                        .driverLikelihood(DriverLikelihood.LOW)
                        .build()
                )
            )
        )
        assertMolecularEvaluation(
            EvaluationResult.WARN,
            function.evaluate(
                MolecularTestFactory.withVariant(
                    TestVariantFactory.builder()
                        .from(activatingVariant)
                        .proteinEffect(ProteinEffect.UNKNOWN)
                        .driverLikelihood(DriverLikelihood.LOW)
                        .build()
                )
            )
        )
        assertMolecularEvaluation(
            EvaluationResult.WARN,
            function.evaluate(
                MolecularTestFactory.withVariant(
                    TestVariantFactory.builder()
                        .from(activatingVariant)
                        .isReportable(false)
                        .canonicalImpact(TestTranscriptImpactFactory.builder().codingEffect(CodingEffect.MISSENSE).build())
                        .build()
                )
            )
        )
        assertMolecularEvaluation(
            EvaluationResult.WARN,
            function.evaluate(
                MolecularTestFactory.withVariant(
                    TestVariantFactory.builder()
                        .from(activatingVariant)
                        .isReportable(true)
                        .clonalLikelihood(0.2)
                        .build()
                )
            )
        )

        // high TML and high driver likelihood variant
        assertMolecularEvaluation(
            EvaluationResult.PASS,
            function.evaluate(
                MolecularTestFactory.withHasTumorMutationalLoadAndVariants(
                    true,
                    TestVariantFactory.builder().from(activatingVariant).build()
                )
            )
        )

        // high TML and low driver likelihood variant
        assertMolecularEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(
                MolecularTestFactory.withHasTumorMutationalLoadAndVariants(
                    true,
                    TestVariantFactory.builder()
                        .from(activatingVariant)
                        .proteinEffect(ProteinEffect.UNKNOWN)
                        .driverLikelihood(DriverLikelihood.LOW)
                        .build()
                )
            )
        )

        // low TML and low driver likelihood variant
        assertMolecularEvaluation(
            EvaluationResult.WARN,
            function.evaluate(
                MolecularTestFactory.withHasTumorMutationalLoadAndVariants(
                    false,
                    TestVariantFactory.builder()
                        .from(activatingVariant)
                        .proteinEffect(ProteinEffect.UNKNOWN)
                        .driverLikelihood(DriverLikelihood.LOW)
                        .build()
                )
            )
        )
    }
}