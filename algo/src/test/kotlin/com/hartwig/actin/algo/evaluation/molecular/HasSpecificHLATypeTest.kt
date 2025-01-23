package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertMolecularEvaluation
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.molecular.immunology.HlaAllele
import org.junit.Test

private const val CORRECT_HLA_GROUP = "A*02"
private val CORRECT_HLA = HlaAllele(name = "$CORRECT_HLA_GROUP:01", tumorCopyNumber = 1.0, hasSomaticMutations = false)

class HasSpecificHLATypeTest {

    private val functionWithSpecificMatch = HasSpecificHLAType(CORRECT_HLA.name)
    private val functionWithGroupMatch = HasSpecificHLAType(CORRECT_HLA_GROUP, matchOnHlaGroup = true)

    @Test
    fun `Should pass if correct HLA allele present`() {
        evaluateFunctions(EvaluationResult.PASS, MolecularTestFactory.withHlaAllele(CORRECT_HLA))
    }

    @Test
    fun `Should pass on HLA group match if matchOnHlaGroup is true`() {
        assertMolecularEvaluation(
            EvaluationResult.PASS,
            functionWithGroupMatch.evaluate(MolecularTestFactory.withHlaAllele(CORRECT_HLA.copy(name = "$CORRECT_HLA_GROUP:02")))
        )
    }

    @Test
    fun `Should fail on HLA group match if matchOnHlaGroup is false`() {
        assertMolecularEvaluation(
            EvaluationResult.FAIL,
            functionWithSpecificMatch.evaluate(MolecularTestFactory.withHlaAllele(CORRECT_HLA.copy(name = "$CORRECT_HLA_GROUP:02")))
        )
    }

    @Test
    fun `Should evaluate to undetermined if immunology results are unreliable`() {
        evaluateFunctions(EvaluationResult.UNDETERMINED, MolecularTestFactory.withUnreliableMolecularImmunology())
    }

    @Test
    fun `Should evaluate to undetermined if no WGS results present`() {
        evaluateFunctions(EvaluationResult.UNDETERMINED, MolecularTestFactory.withIHCTests())
    }

    @Test
    fun `Should evaluate to undetermined if correct HLA allele present but record does not have sufficient quality`() {
        evaluateFunctions(EvaluationResult.UNDETERMINED, MolecularTestFactory.withHlaAlleleAndInsufficientQuality(CORRECT_HLA))
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
        evaluateFunctions(EvaluationResult.FAIL, MolecularTestFactory.withHlaAllele(CORRECT_HLA.copy(name = "other")))
        evaluateFunctions(EvaluationResult.FAIL, MolecularTestFactory.withHlaAlleleAndInsufficientQuality(CORRECT_HLA.copy(name = "other")))
    }

    private fun evaluateFunctions(expected: EvaluationResult, record: PatientRecord) {
        assertMolecularEvaluation(expected, functionWithSpecificMatch.evaluate(record))
        assertMolecularEvaluation(expected, functionWithGroupMatch.evaluate(record))
    }
}