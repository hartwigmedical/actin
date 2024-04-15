package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.TestPatientFactory
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertMolecularEvaluation
import com.hartwig.actin.molecular.datamodel.driver.CodingEffect
import com.hartwig.actin.molecular.datamodel.driver.Fusion
import com.hartwig.actin.molecular.datamodel.driver.TestFusionFactory
import com.hartwig.actin.molecular.datamodel.driver.TestTranscriptImpactFactory
import com.hartwig.actin.molecular.datamodel.driver.TestVariantFactory
import com.hartwig.actin.molecular.datamodel.driver.Variant
import org.junit.Test

class GeneHasSpecificExonSkippingTest {

    @Test
    fun canEvaluate() {
        val function = GeneHasSpecificExonSkipping("gene A", 2)
        assertMolecularEvaluation(EvaluationResult.FAIL, function.evaluate(TestPatientFactory.createMinimalTestWGSPatientRecord()))
        val spliceVariant: Variant = TestVariantFactory.createMinimal().copy(
            gene = "gene A",
            isReportable = true,
            canonicalImpact = TestTranscriptImpactFactory.createMinimal().copy(affectedExon = 2, isSpliceRegion = true)
        )
        assertMolecularEvaluation(EvaluationResult.WARN, function.evaluate(MolecularTestFactory.withVariant(spliceVariant)))
        assertMolecularEvaluation(
            EvaluationResult.WARN,
            function.evaluate(
                MolecularTestFactory.withVariant(
                    spliceVariant.copy(
                        canonicalImpact = TestTranscriptImpactFactory.createMinimal().copy(
                            affectedExon = 2, codingEffect = CodingEffect.SPLICE
                        )
                    )
                )
            )
        )
        assertMolecularEvaluation(
            EvaluationResult.FAIL, function.evaluate(MolecularTestFactory.withVariant(spliceVariant.copy(isReportable = false)))
        )

        val exonSkippingFusion: Fusion = TestFusionFactory.createMinimal().copy(
            isReportable = true,
            geneStart = "gene A",
            fusedExonUp = 1,
            geneEnd = "gene A",
            fusedExonDown = 3
        )
        assertMolecularEvaluation(EvaluationResult.PASS, function.evaluate(MolecularTestFactory.withFusion(exonSkippingFusion)))
        assertMolecularEvaluation(
            EvaluationResult.FAIL, function.evaluate(MolecularTestFactory.withFusion(exonSkippingFusion.copy(isReportable = false)))
        )
        assertMolecularEvaluation(
            EvaluationResult.FAIL, function.evaluate(MolecularTestFactory.withFusion(exonSkippingFusion.copy(fusedExonDown = 5)))
        )
    }
}