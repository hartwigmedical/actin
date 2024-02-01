package com.hartwig.actin.algo.soc.datamodel

data class DecisionTreeLeaf(val treatmentCandidates: List<TreatmentCandidate>) : DecisionTreeNode {

    override fun treatmentCandidates(): List<TreatmentCandidate> {
        return treatmentCandidates
    }
}
