package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertMolecularEvaluation
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.molecular.immunology.TestHlaAlleleFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

private const val GENE = "A"
private const val ALLELE_GROUP = "02"
private const val HLA_PROTEIN = "01"
private val CORRECT_HLA = TestHlaAlleleFactory.createMinimal().copy(gene = "HLA-$GENE", alleleGroup = ALLELE_GROUP, hlaProtein = HLA_PROTEIN, event = "HLA-${GENE}*${ALLELE_GROUP}:${HLA_PROTEIN}")

class HasAnyHLATypeTest {

    private val functionWithSpecificMatch = HasAnyHLAType(setOf("$GENE*$ALLELE_GROUP:$HLA_PROTEIN", "A*02:07"))
    private val functionWithGroupMatch = HasAnyHLAType(setOf("$GENE*$ALLELE_GROUP"), matchOnHlaGroup = true)

    @Test
    fun `Should pass if correct HLA allele present`() {
        evaluateFunctions(EvaluationResult.PASS, MolecularTestFactory.withHlaAllele(CORRECT_HLA))
    }

    @Test
    fun `Should pass on HLA group match if matchOnHlaGroup is true`() {
        assertMolecularEvaluation(
            EvaluationResult.PASS,
            functionWithGroupMatch.evaluate(MolecularTestFactory.withHlaAllele(CORRECT_HLA.copy(hlaProtein = "02")))
        )
    }

    @Test
    fun `Should fail on HLA group match if matchOnHlaGroup is false`() {
        assertMolecularEvaluation(
            EvaluationResult.FAIL,
            functionWithSpecificMatch.evaluate(MolecularTestFactory.withHlaAllele(CORRECT_HLA.copy(hlaProtein = "02")))
        )
    }

    @Test
    fun `Should evaluate to undetermined if immunology results are unreliable`() {
        evaluateFunctions(EvaluationResult.UNDETERMINED, MolecularTestFactory.withUnreliableMolecularImmunology())
    }

    @Test
    fun `Should evaluate to undetermined if no WGS results present`() {
        evaluateFunctions(EvaluationResult.UNDETERMINED, MolecularTestFactory.withIhcTests())
    }

    @Test
    fun `Should warn if correct HLA allele present but record does not have sufficient quality`() {
        val record = MolecularTestFactory.withHlaAlleleAndInsufficientQuality(CORRECT_HLA)
        evaluateFunctions(EvaluationResult.WARN, record)
        val evaluation = functionWithSpecificMatch.evaluate(record)
        assertThat(evaluation.inclusionMolecularEvents).isEqualTo(setOf("HLA-A*02:01"))
    }

    @Test
    fun `Should warn if correct HLA allele present but tumor copy number is less than 0,5`() {
        evaluateFunctions(EvaluationResult.WARN, MolecularTestFactory.withHlaAllele(CORRECT_HLA.copy(tumorCopyNumber = 0.0)))
    }

    @Test
    fun `Should warn if correct HLA allele present but also somatic mutations present`() {
        evaluateFunctions(EvaluationResult.WARN, MolecularTestFactory.withHlaAllele(CORRECT_HLA.copy(hasSomaticMutations = true)))
    }

    @Test
    fun `Should fail if correct HLA allele not present`() {
        evaluateFunctions(EvaluationResult.FAIL, MolecularTestFactory.withHlaAllele(CORRECT_HLA.copy(gene = "other gene")))
        evaluateFunctions(EvaluationResult.FAIL, MolecularTestFactory.withHlaAlleleAndInsufficientQuality(CORRECT_HLA.copy(gene = "other gene")))
    }

    private fun evaluateFunctions(expected: EvaluationResult, record: PatientRecord) {
        assertMolecularEvaluation(expected, functionWithSpecificMatch.evaluate(record))
        assertMolecularEvaluation(expected, functionWithGroupMatch.evaluate(record))
    }
}