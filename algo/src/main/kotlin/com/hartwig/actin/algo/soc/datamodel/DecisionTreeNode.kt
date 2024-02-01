package com.hartwig.actin.algo.soc.datamodel

interface DecisionTreeNode {

    fun treatmentCandidates(): List<TreatmentCandidate>
}