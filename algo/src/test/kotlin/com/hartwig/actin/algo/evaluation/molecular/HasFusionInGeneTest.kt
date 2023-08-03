package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.TestDataFactory
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertMolecularEvaluation
import com.hartwig.actin.molecular.datamodel.driver.DriverLikelihood
import com.hartwig.actin.molecular.datamodel.driver.FusionDriverType
import com.hartwig.actin.molecular.datamodel.driver.ImmutableFusion
import com.hartwig.actin.molecular.datamodel.driver.ProteinEffect
import com.hartwig.actin.molecular.datamodel.driver.TestFusionFactory
import org.junit.Test

class HasFusionInGeneTest {

    val function = HasFusionInGene(MATCHING_GENE)

    @Test
    fun shouldFailOnMinimalTestPatientRecordEvaluate() {
        assertMolecularEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(TestDataFactory.createMinimalTestPatientRecord())
        )
    }

    @Test
    fun shouldPassOnHighDriverReportableGainOfFunctionMatchingFusion() {
        assertMolecularEvaluation(
            EvaluationResult.PASS,
            function.evaluate(MolecularTestFactory.withFusion(matchingFusionBuilder().build()))
        )
    }

    @Test
    fun shouldFailOnThreeGeneMatchWhenTypeFivePromiscuous() {
        assertMolecularEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(
                MolecularTestFactory.withFusion(
                    matchingFusionBuilder().geneStart("gene B").geneEnd("gene A").build()
                )
            )
        )
    }

    @Test
    fun shouldFailIfExonDelDupOnDifferentGene() {
        assertMolecularEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(MolecularTestFactory.withFusion(matchingFusionBuilder().geneStart("gene B").geneEnd("gene B").build()))
        )
    }

    @Test
    fun shouldFailOnFiveGeneMatchWhenTypeIsThreePromiscuous() {
        assertMolecularEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(
                MolecularTestFactory.withFusion(
                    matchingFusionBuilder().driverType(FusionDriverType.PROMISCUOUS_3).build()
                )
            )
        )
    }

    @Test
    fun shouldWarnOnUnreportableGainOfFunctionMatch() {
        assertMolecularEvaluation(
            EvaluationResult.WARN,
            function.evaluate(MolecularTestFactory.withFusion(matchingFusionBuilder().isReportable(false).build()))
        )
    }

    @Test
    fun shouldFailOnUnreportableFusionWithNoEffect() {
        assertMolecularEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(
                MolecularTestFactory.withFusion(
                    matchingFusionBuilder().isReportable(false).proteinEffect(ProteinEffect.NO_EFFECT).build()
                )
            )
        )
    }

    @Test
    fun shouldWarnOnLowDriverGainOfFunctionFusion() {
        assertMolecularEvaluation(
            EvaluationResult.WARN,
            function.evaluate(
                MolecularTestFactory.withFusion(
                    matchingFusionBuilder().driverLikelihood(DriverLikelihood.LOW).build()
                )
            )
        )
    }

    @Test
    fun shouldWarnOnHighDriverFusionWithNoEffect() {
        assertMolecularEvaluation(
            EvaluationResult.WARN,
            function.evaluate(
                MolecularTestFactory.withFusion(
                    matchingFusionBuilder().proteinEffect(ProteinEffect.NO_EFFECT).build()
                )
            )
        )
    }


    companion object {
        private const val MATCHING_GENE = "gene A"

        private fun matchingFusionBuilder(): ImmutableFusion.Builder {
            return TestFusionFactory.builder()
                .geneStart(MATCHING_GENE)
                .isReportable(true)
                .driverLikelihood(DriverLikelihood.HIGH)
                .proteinEffect(ProteinEffect.GAIN_OF_FUNCTION)
                .driverType(FusionDriverType.PROMISCUOUS_5)
        }
    }
}