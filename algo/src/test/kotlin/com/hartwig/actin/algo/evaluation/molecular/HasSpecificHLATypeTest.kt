package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertMolecularEvaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.molecular.orange.immunology.HlaAllele
import org.junit.Test

private val CORRECT_HLA = HlaAllele(name = "A*02:01", tumorCopyNumber = 1.0, hasSomaticMutations = false)

class HasSpecificHLATypeTest {

    private val function = HasSpecificHLAType(CORRECT_HLA.name)

    @Test
    fun `Should pass if correct HLA allele present`() {
        assertMolecularEvaluation(EvaluationResult.PASS, function.evaluate(MolecularTestFactory.withHlaAllele(CORRECT_HLA)))
    }

    @Test
    fun `Should evaluate to undetermined if immunology results are unreliable`() {
        assertMolecularEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(MolecularTestFactory.withUnreliableMolecularImmunology())
        )
    }

    @Test
    fun `Should evaluate to undetermined if no WGS results present`() {
        assertMolecularEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(MolecularTestFactory.withIHCTests()))
    }

    @Test
    fun `Should evaluate to undetermined if correct HLA allele present but record does not have sufficient quality`() {
        assertMolecularEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(MolecularTestFactory.withHlaAlleleAndInsufficientQuality(CORRECT_HLA))
        )
    }

    @Test
    fun `Should warn if correct HLA allele present but tumor copy number is less than 0,5`() {
        assertMolecularEvaluation(
            EvaluationResult.WARN,
            function.evaluate(MolecularTestFactory.withHlaAllele(CORRECT_HLA.copy(tumorCopyNumber = 0.0)))
        )
    }

    @Test
    fun `Should warn if correct HLA allele present but also somatic mutations present`() {
        assertMolecularEvaluation(
            EvaluationResult.WARN,
            function.evaluate(MolecularTestFactory.withHlaAllele(CORRECT_HLA.copy(hasSomaticMutations = true)))
        )
    }

    @Test
    fun `Should fail if correct HLA allele not present`() {
        assertMolecularEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(MolecularTestFactory.withHlaAllele(CORRECT_HLA.copy(name = "other")))
        )
        assertMolecularEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(MolecularTestFactory.withHlaAlleleAndInsufficientQuality(CORRECT_HLA.copy(name = "other")))
        )
    }
}