package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.TestDataFactory
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertMolecularEvaluation
import com.hartwig.actin.molecular.datamodel.driver.CodingContext
import com.hartwig.actin.molecular.datamodel.driver.RegionType
import com.hartwig.actin.molecular.datamodel.driver.TestDisruptionFactory
import com.hartwig.actin.molecular.datamodel.driver.TestTranscriptImpactFactory
import com.hartwig.actin.molecular.datamodel.driver.TestVariantFactory
import com.hartwig.actin.molecular.datamodel.driver.VariantEffect
import org.junit.Test

class GeneHasUTR3LossTest {
    @Test
    fun canEvaluate() {
        val function = GeneHasUTR3Loss("gene A")
        assertMolecularEvaluation(EvaluationResult.FAIL, function.evaluate(TestDataFactory.createMinimalTestPatientRecord()))
        assertMolecularEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(MolecularTestFactory.withDisruption(TestDisruptionFactory.createMinimal().gene("gene A").build()))
        )
        assertMolecularEvaluation(
            EvaluationResult.WARN,
            function.evaluate(
                MolecularTestFactory.withDisruption(
                    TestDisruptionFactory.createMinimal()
                        .gene("gene A")
                        .regionType(RegionType.EXONIC)
                        .codingContext(CodingContext.UTR_3P)
                        .build()
                )
            )
        )
        assertMolecularEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(MolecularTestFactory.withVariant(TestVariantFactory.createMinimal().gene("gene A").build()))
        )
        assertMolecularEvaluation(
            EvaluationResult.WARN,
            function.evaluate(
                MolecularTestFactory.withVariant(
                    TestVariantFactory.createMinimal()
                        .gene("gene A")
                        .isHotspot(false)
                        .canonicalImpact(TestTranscriptImpactFactory.createMinimal().addEffects(VariantEffect.THREE_PRIME_UTR).build())
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
                        .isReportable(false)
                        .isHotspot(true)
                        .canonicalImpact(TestTranscriptImpactFactory.createMinimal().addEffects(VariantEffect.THREE_PRIME_UTR).build())
                        .build()
                )
            )
        )
        assertMolecularEvaluation(
            EvaluationResult.PASS,
            function.evaluate(
                MolecularTestFactory.withVariant(
                    TestVariantFactory.createMinimal()
                        .gene("gene A")
                        .isReportable(true)
                        .isHotspot(true)
                        .canonicalImpact(TestTranscriptImpactFactory.createMinimal().addEffects(VariantEffect.THREE_PRIME_UTR).build())
                        .build()
                )
            )
        )
    }
}