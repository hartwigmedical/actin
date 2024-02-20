package com.hartwig.actin.algo.datamodel

import com.hartwig.actin.efficacy.ExtendedEvidenceEntry

data class StandardOfCareMatch(
    val treatmentCandidate: TreatmentCandidate,
    val evaluations: List<Evaluation>,
    val annotations: List<ExtendedEvidenceEntry>?
) {

    fun eligible() = evaluations.none { it.result == EvaluationResult.FAIL }
}