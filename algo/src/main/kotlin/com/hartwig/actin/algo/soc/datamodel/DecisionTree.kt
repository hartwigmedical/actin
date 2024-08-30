package com.hartwig.actin.algo.soc.datamodel

import com.hartwig.actin.datamodel.algo.TreatmentCandidate
import com.hartwig.actin.datamodel.trial.EligibilityFunction
import com.hartwig.actin.datamodel.trial.EligibilityRule

data class DecisionTree(
    private val decision: EligibilityFunction,
    private val trueBranch: DecisionTreeNode,
    private val falseBranch: DecisionTreeNode
) : DecisionTreeNode {

    override fun treatmentCandidates(): List<TreatmentCandidate> {
        val negatedDecision = EligibilityFunction(EligibilityRule.NOT, listOf(decision))
        return candidatesForChild(trueBranch, decision) + candidatesForChild(falseBranch, negatedDecision)
    }

    private fun candidatesForChild(branch: DecisionTreeNode, additionalCriteria: EligibilityFunction) = branch.treatmentCandidates().map {
        it.copy(eligibilityFunctions = it.eligibilityFunctions + additionalCriteria)
    }
}