package com.hartwig.actin.algo.soc.datamodel

import com.hartwig.actin.datamodel.algo.TreatmentCandidate

interface DecisionTreeNode {

    fun treatmentCandidates(): List<TreatmentCandidate>
}