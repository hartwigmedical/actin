package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertMolecularEvaluation
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.TestPatientFactory
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.molecular.MolecularHistory
import com.hartwig.actin.datamodel.molecular.TestMolecularFactory
import com.hartwig.actin.datamodel.molecular.driver.CodingContext
import com.hartwig.actin.datamodel.molecular.driver.RegionType
import com.hartwig.actin.datamodel.molecular.driver.TestDisruptionFactory
import com.hartwig.actin.datamodel.molecular.driver.TestTranscriptVariantImpactFactory
import com.hartwig.actin.datamodel.molecular.driver.TestVariantFactory
import com.hartwig.actin.datamodel.molecular.driver.VariantEffect
import org.assertj.core.api.Assertions
import org.junit.Test

private const val TARGET_GENE = "gene A"

class GeneHasUTR3LossTest {

    private val function = GeneHasUTR3Loss(TARGET_GENE)

    @Test
    fun canEvaluate() {
        assertMolecularEvaluation(EvaluationResult.FAIL, function.evaluate(TestPatientFactory.createMinimalTestWGSPatientRecord()))
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
            EvaluationResult.WARN,
            function.evaluate(patientWithThreePrimeUtrEffect(isReportable = false, isCancerAssociatedVariant = false))
        )
        assertMolecularEvaluation(
            EvaluationResult.WARN, function.evaluate(patientWithThreePrimeUtrEffect(isReportable = false, isCancerAssociatedVariant = true))
        )
        assertMolecularEvaluation(
            EvaluationResult.PASS, function.evaluate(patientWithThreePrimeUtrEffect(isReportable = true, isCancerAssociatedVariant = true))
        )
    }

    @Test
    fun `Should evaluate undetermined with appropriate message when target coverage insufficient`() {
        val result = function.evaluate(
            TestPatientFactory.createMinimalTestWGSPatientRecord().copy(
                molecularTests = MolecularHistory(molecularTests = listOf(TestMolecularFactory.createMinimalPanelTest()))
            )
        )
        Assertions.assertThat(result.result).isEqualTo(EvaluationResult.UNDETERMINED)
        Assertions.assertThat(result.undeterminedMessagesStrings()).containsExactly("3' UTR loss in gene gene A undetermined (not tested for mutations)")
    }

    private fun patientWithThreePrimeUtrEffect(isReportable: Boolean, isCancerAssociatedVariant: Boolean): PatientRecord {
        return MolecularTestFactory.withVariant(
            TestVariantFactory.createMinimal().copy(
                gene = TARGET_GENE,
                isReportable = isReportable,
                isCancerAssociatedVariant = isCancerAssociatedVariant,
                canonicalImpact = TestTranscriptVariantImpactFactory.createMinimal().copy(effects = setOf(VariantEffect.THREE_PRIME_UTR))
            )
        )
    }
}