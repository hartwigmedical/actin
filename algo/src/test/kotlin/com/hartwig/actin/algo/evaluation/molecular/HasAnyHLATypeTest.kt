package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertMolecularEvaluation
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.algo.MolecularEvent
import com.hartwig.actin.datamodel.molecular.immunology.TestHlaAlleleFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

private const val GENE = "A"
private const val ALLELE_GROUP = "02"
private const val HLA_PROTEIN = "01"
private val CORRECT_HLA = TestHlaAlleleFactory.createMinimal().copy(gene = "HLA-$GENE", alleleGroup = ALLELE_GROUP, hlaProtein = HLA_PROTEIN, event = "HLA-${GENE}*${ALLELE_GROUP}:${HLA_PROTEIN}")

class HasAnyHLATypeTest {

    private val functionWithSpecificMatch = HasAnyHLAType(setOf("$GENE*$ALLELE_GROUP:$HLA_PROTEIN", "A*02:07"))
    private val functionWithGroupMatch = HasAnyHLAType(setOf("$GENE*$ALLELE_GROUP"), matchOnHlaGroup = true)

    @Test
    fun `Should pass if correct HLA allele present`() {
        evaluateFunctions(
            EvaluationResult.PASS,
            MolecularTestFactory.withHlaAllele(CORRECT_HLA),
            "Has HLA type hla-a*02:01 (allele present without somatic variants in tumor)"
        )
    }

    @Test
    fun `Should pass on HLA group match if matchOnHlaGroup is true`() {
        assertMolecularEvaluation(
            EvaluationResult.PASS,
            functionWithGroupMatch.evaluate(MolecularTestFactory.withHlaAllele(CORRECT_HLA.copy(hlaProtein = "02"))),
            "Has HLA type hla-a*02:01 (allele present without somatic variants in tumor)"
        )
    }

    @Test
    fun `Should fail on HLA group match if matchOnHlaGroup is false`() {
        assertMolecularEvaluation(
            EvaluationResult.FAIL,
            functionWithSpecificMatch.evaluate(MolecularTestFactory.withHlaAllele(CORRECT_HLA.copy(hlaProtein = "02"))),
            "Does not have HLA type a*02:01 or a*02:07"
        )
    }

    @Test
    fun `Should evaluate to undetermined if immunology results are unreliable`() {
        evaluateFunctions(EvaluationResult.UNDETERMINED, MolecularTestFactory.withUnreliableMolecularImmunology(), "HLA typing unreliable")
    }

    @Test
    fun `Should evaluate to undetermined if no WGS results present`() {
        evaluateFunctions(EvaluationResult.UNDETERMINED, MolecularTestFactory.withIhcTests(), "HLA typing unreliable")
    }

    @Test
    fun `Should warn if correct HLA allele present but record does not have sufficient quality`() {
        val record = MolecularTestFactory.withHlaAlleleAndInsufficientQuality(CORRECT_HLA)
        evaluateFunctions(
            EvaluationResult.WARN, record, "Has required HLA type hla-a*02:01 however undetermined whether allele is present in tumor"
        )
        val evaluation = functionWithSpecificMatch.evaluate(record)
        assertThat(evaluation.inclusionMolecularEvents).isEqualTo(setOf(MolecularEvent("HLA-A*02:01")))
    }

    @Test
    fun `Should warn if correct HLA allele present but tumor copy number is less than 0,5`() {
        evaluateFunctions(
            EvaluationResult.WARN,
            MolecularTestFactory.withHlaAllele(CORRECT_HLA.copy(tumorCopyNumber = 0.0)),
            "Has required HLA type hla-a*02:01 but allele has low copy number in tumor"
        )
    }

    @Test
    fun `Should warn if correct HLA allele present but tumor copy number is less than 0,5 even when somatic mutation status missing`() {
        evaluateFunctions(
            EvaluationResult.WARN,
            MolecularTestFactory.withHlaAllele(CORRECT_HLA.copy(tumorCopyNumber = 0.0, hasSomaticMutations = null)),
            "Has required HLA type hla-a*02:01 but allele has low copy number in tumor"
        )
    }

    @Test
    fun `Should warn if correct HLA allele present but also somatic mutations present`() {
        evaluateFunctions(
            EvaluationResult.WARN,
            MolecularTestFactory.withHlaAllele(CORRECT_HLA.copy(hasSomaticMutations = true)),
            "Has required HLA type hla-a*02:01 but somatic mutation present in this allele in tumor"
        )
    }

    @Test
    fun `Should warn if correct HLA allele present but somatic mutation is present even when tumor copy number is missing`() {
        evaluateFunctions(
            EvaluationResult.WARN,
            MolecularTestFactory.withHlaAllele(CORRECT_HLA.copy(tumorCopyNumber = null, hasSomaticMutations = true)),
            "Has required HLA type hla-a*02:01 but somatic mutation present in this allele in tumor"
        )
    }

    @Test
    fun `Should pass if correct HLA allele present but tumor copy number or somatic mutation status or both are missing`() {
        evaluateFunctions(
            EvaluationResult.PASS,
            MolecularTestFactory.withHlaAllele(CORRECT_HLA.copy(tumorCopyNumber = 1.0, hasSomaticMutations = null)),
            "Has HLA type hla-a*02:01"
        )
        evaluateFunctions(
            EvaluationResult.PASS,
            MolecularTestFactory.withHlaAllele(CORRECT_HLA.copy(tumorCopyNumber = null, hasSomaticMutations = false)),
            "Has HLA type hla-a*02:01"
        )
        evaluateFunctions(
            EvaluationResult.PASS,
            MolecularTestFactory.withHlaAllele(CORRECT_HLA.copy(tumorCopyNumber = null, hasSomaticMutations = null)),
            "Has HLA type hla-a*02:01"
        )
    }

    @Test
    fun `Should fail if correct HLA allele not present`() {
        evaluateFunctions(
            EvaluationResult.FAIL,
            MolecularTestFactory.withHlaAllele(CORRECT_HLA.copy(gene = "other gene")),
            "Does not have HLA type a*02:01 or a*02:07",
            "Does not have HLA type a*02"
        )
        evaluateFunctions(
            EvaluationResult.FAIL,
            MolecularTestFactory.withHlaAlleleAndInsufficientQuality(CORRECT_HLA.copy(gene = "other gene")),
            "Does not have HLA type a*02:01 or a*02:07",
            "Does not have HLA type a*02"
        )
    }

    private fun evaluateFunctions(
        expected: EvaluationResult,
        record: PatientRecord,
        expectedMessageForSpecificMatch: String,
        expectedMessageForGroupMatch: String = expectedMessageForSpecificMatch
    ) {
        assertMolecularEvaluation(expected, functionWithSpecificMatch.evaluate(record), expectedMessageForSpecificMatch)
        assertMolecularEvaluation(expected, functionWithGroupMatch.evaluate(record), expectedMessageForGroupMatch)
    }
}