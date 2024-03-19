package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.TestPatientFactory
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertMolecularEvaluation
import com.hartwig.actin.molecular.datamodel.driver.CodingContext
import com.hartwig.actin.molecular.datamodel.driver.RegionType
import com.hartwig.actin.molecular.datamodel.driver.TestDisruptionFactory
import com.hartwig.actin.molecular.datamodel.driver.TestTranscriptImpactFactory
import com.hartwig.actin.molecular.datamodel.driver.TestVariantFactory
import com.hartwig.actin.molecular.datamodel.driver.VariantEffect
import org.junit.Test

private const val TARGET_GENE = "gene A"

class GeneHasUTR3LossTest {

    @Test
    fun canEvaluate() {
        val function = GeneHasUTR3Loss(TARGET_GENE)
        assertMolecularEvaluation(EvaluationResult.FAIL, function.evaluate(TestPatientFactory.createMinimalTestPatientRecord()))
        assertMolecularEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(MolecularTestFactory.withDisruption(TestDisruptionFactory.createMinimal().copy(gene = TARGET_GENE)))
        )
        assertMolecularEvaluation(
            EvaluationResult.WARN, function.evaluate(
                MolecularTestFactory.withDisruption(
                    TestDisruptionFactory.createMinimal().copy(
                        gene = TARGET_GENE, regionType = RegionType.EXONIC, codingContext = CodingContext.UTR_3P
                )
            )
            )
        )
        assertMolecularEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(MolecularTestFactory.withVariant(TestVariantFactory.createMinimal().copy(gene = TARGET_GENE)))
        )
        assertMolecularEvaluation(
            EvaluationResult.WARN, function.evaluate(patientWithThreePrimeUtrEffect(isReportable = false, isHotspot = false))
        )
        assertMolecularEvaluation(
            EvaluationResult.WARN, function.evaluate(patientWithThreePrimeUtrEffect(isReportable = false, isHotspot = true))
        )
        assertMolecularEvaluation(
            EvaluationResult.PASS, function.evaluate(patientWithThreePrimeUtrEffect(isReportable = true, isHotspot = true))
        )
    }

    private fun patientWithThreePrimeUtrEffect(isReportable: Boolean, isHotspot: Boolean): PatientRecord {
        return MolecularTestFactory.withVariant(
            TestVariantFactory.createMinimal().copy(
                gene = TARGET_GENE,
                isReportable = isReportable,
                isHotspot = isHotspot,
                canonicalImpact = TestTranscriptImpactFactory.createMinimal().copy(effects = setOf(VariantEffect.THREE_PRIME_UTR))
            )
        )
    }
}