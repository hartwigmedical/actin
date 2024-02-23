package com.hartwig.actin.algo.datamodel

import com.hartwig.actin.efficacy.EfficacyEntry

data class AnnotatedTreatmentMatch(
    val treatmentCandidate: TreatmentCandidate,
    val evaluations: List<Evaluation>,
    val annotations: List<EfficacyEntry>
) {

    fun eligible() = evaluations.none { it.result == EvaluationResult.FAIL }
}