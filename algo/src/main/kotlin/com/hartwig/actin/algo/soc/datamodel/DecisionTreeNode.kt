package com.hartwig.actin.algo.soc.datamodel

import com.hartwig.actin.algo.datamodel.TreatmentCandidate

interface DecisionTreeNode {

    fun treatmentCandidates(): List<TreatmentCandidate>
}