package com.hartwig.actin.algo.soc.datamodel

import com.hartwig.actin.algo.datamodel.TreatmentCandidate

data class DecisionTreeLeaf(val treatmentCandidates: List<TreatmentCandidate>) : DecisionTreeNode {

    override fun treatmentCandidates(): List<TreatmentCandidate> {
        return treatmentCandidates
    }
}
