package com.hartwig.actin.algo.soc.datamodel

import com.hartwig.actin.datamodel.algo.TreatmentCandidate

data class DecisionTreeLeaf(val treatmentCandidates: List<TreatmentCandidate>) : DecisionTreeNode {

    override fun treatmentCandidates(): List<TreatmentCandidate> {
        return treatmentCandidates
    }
}
