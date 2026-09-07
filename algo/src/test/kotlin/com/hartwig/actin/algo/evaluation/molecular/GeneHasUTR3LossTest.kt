package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertMolecularEvaluation
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.TestPatientFactory
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.molecular.TestMolecularFactory
import com.hartwig.actin.datamodel.molecular.driver.CodingContext
import com.hartwig.actin.datamodel.molecular.driver.RegionType
import com.hartwig.actin.datamodel.molecular.driver.TestDisruptionFactory
import com.hartwig.actin.datamodel.molecular.driver.TestTranscriptVariantImpactFactory
import com.hartwig.actin.datamodel.molecular.driver.TestVariantFactory
import com.hartwig.actin.datamodel.molecular.driver.VariantEffect
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test

private const val TARGET_GENE = "gene A"

class GeneHasUTR3LossTest {

    private val function = GeneHasUTR3Loss(TARGET_GENE)

    @Test
    fun canEvaluate() {
        assertMolecularEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(TestPatientFactory.createMinimalTestWGSPatientRecord()),
            "No 3' UTR loss of gene A"
        )
        assertMolecularEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(MolecularTestFactory.withDisruption(TestDisruptionFactory.createMinimal().copy(gene = TARGET_GENE))),
            "No 3' UTR loss of gene A"
        )
        assertMolecularEvaluation(
            EvaluationResult.WARN, function.evaluate(
                MolecularTestFactory.withDisruption(
                    TestDisruptionFactory.createMinimal().copy(
                        gene = TARGET_GENE, regionType = RegionType.EXONIC, codingContext = CodingContext.UTR_3P, event = "event"
                    )
                )
            ),
            "Disruption(s) event in 3' UTR region of gene A which may lead to 3' UTR loss"
        )
        assertMolecularEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(MolecularTestFactory.withVariant(TestVariantFactory.createMinimal().copy(gene = TARGET_GENE))),
            "No 3' UTR loss of gene A"
        )
        assertMolecularEvaluation(
            EvaluationResult.WARN,
            function.evaluate(patientWithThreePrimeUtrEffect(isReportable = false, isCancerAssociatedVariant = false)),
            "Cancer-associated variant(s) event in 3' UTR region of gene A which may lead to 3' UTR loss but mutation is not considered reportable"
        )
        assertMolecularEvaluation(
            EvaluationResult.WARN, function.evaluate(patientWithThreePrimeUtrEffect(isReportable = false, isCancerAssociatedVariant = true)),
            "VUS mutation(s) event in 3' UTR region of gene A which may lead to 3' UTR loss"
        )
        assertMolecularEvaluation(
            EvaluationResult.PASS, function.evaluate(patientWithThreePrimeUtrEffect(isReportable = true, isCancerAssociatedVariant = true)),
            "3' UTR cancer-associated variant(s) event in gene A should lead to 3' UTR loss"
        )
    }

    @Test
    fun `Should evaluate undetermined with appropriate message when target coverage insufficient`() {
        val result = function.evaluate(
            TestPatientFactory.createMinimalTestWGSPatientRecord().copy(
                molecularTests = listOf(TestMolecularFactory.createMinimalPanelTest())
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
                canonicalImpact = TestTranscriptVariantImpactFactory.createMinimal().copy(effects = setOf(VariantEffect.THREE_PRIME_UTR)),
                event = "event"
            )
        )
    }
}