package com.hartwig.actin.algo.soc.datamodel

import com.hartwig.actin.algo.datamodel.TreatmentCandidate
import com.hartwig.actin.clinical.datamodel.TreatmentTestFactory
import com.hartwig.actin.trial.datamodel.EligibilityFunction
import com.hartwig.actin.trial.datamodel.EligibilityRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class DecisionTreeNodeTest {

    @Test
    fun `Should simply return treatment candidates for leaf nodes`() {
        val candidates = listOf(treatmentCandidate("treatment1"), treatmentCandidate("treatment2"))
        val tree: DecisionTreeNode = DecisionTreeLeaf(candidates)
        assertThat(tree.treatmentCandidates()).isEqualTo(candidates)
    }

    @Test
    fun `Should return empty list for empty leaf nodes`() {
        val tree: DecisionTreeNode = DecisionTreeLeaf(emptyList())
        assertThat(tree.treatmentCandidates()).isEmpty()
    }

    @Test
    fun `Should add inclusion criteria to treatment candidates from decision nodes`() {
        val candidatesIfTrue = listOf(treatmentCandidate("treatment1"), treatmentCandidate("treatment2"))
        val candidatesIfFalse = listOf(treatmentCandidate("treatment3"), treatmentCandidate("treatment4"))
        val decision = EligibilityFunction(EligibilityRule.MUTATION_IN_GENE_X_OF_ANY_PROTEIN_IMPACTS_Y, listOf("BRAF", "V600E"))
        val tree: DecisionTreeNode = DecisionTree(decision, DecisionTreeLeaf(candidatesIfTrue), DecisionTreeLeaf(candidatesIfFalse))
        assertThat(tree.treatmentCandidates()).containsExactlyInAnyOrder(
            treatmentCandidate("treatment1", setOf(decision)),
            treatmentCandidate("treatment2", setOf(decision)),
            treatmentCandidate("treatment3", setOf(not(decision))),
            treatmentCandidate("treatment4", setOf(not(decision)))
        )
    }

    @Test
    fun `Should combine inclusion criteria from multiple decision node levels`() {
        val brafV600EMut = EligibilityFunction(EligibilityRule.MUTATION_IN_GENE_X_OF_ANY_PROTEIN_IMPACTS_Y, listOf("BRAF", "V600E"))
        val krasMut = EligibilityFunction(EligibilityRule.ACTIVATING_MUTATION_IN_ANY_GENES_X, listOf("KRAS"))
        val brafV600ECandidates = listOf(treatmentCandidate("treatment1"), treatmentCandidate("treatment2"))
        val brafV600EWtKrasMutCandidates = listOf(treatmentCandidate("treatment3"), treatmentCandidate("treatment4"))
        val brafV600EWtKrasWtCandidates = listOf(treatmentCandidate("treatment5"), treatmentCandidate("treatment6"))
        val tree: DecisionTreeNode = DecisionTree(
            brafV600EMut,
            DecisionTreeLeaf(brafV600ECandidates),
            DecisionTree(
                krasMut,
                DecisionTreeLeaf(brafV600EWtKrasMutCandidates),
                DecisionTreeLeaf(brafV600EWtKrasWtCandidates)
            )
        )

        assertThat(tree.treatmentCandidates()).containsExactlyInAnyOrder(
            treatmentCandidate("treatment1", setOf(brafV600EMut)),
            treatmentCandidate("treatment2", setOf(brafV600EMut)),
            treatmentCandidate("treatment3", setOf(not(brafV600EMut), krasMut)),
            treatmentCandidate("treatment4", setOf(not(brafV600EMut), krasMut)),
            treatmentCandidate("treatment5", setOf(not(brafV600EMut), not(krasMut))),
            treatmentCandidate("treatment6", setOf(not(brafV600EMut), not(krasMut)))
        )
    }

    private fun not(function: EligibilityFunction) = EligibilityFunction(EligibilityRule.NOT, listOf(function))

    private fun treatmentCandidate(name: String, additionalCriteria: Set<EligibilityFunction> = emptySet()) = TreatmentCandidate(
        treatment = TreatmentTestFactory.treatment(name, true),
        optional = false,
        eligibilityFunctions = setOf(EligibilityFunction(EligibilityRule.IS_ELIGIBLE_FOR_ON_LABEL_TREATMENT_X, listOf(name)))
                + additionalCriteria
    )
}