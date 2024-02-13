package com.hartwig.actin.algo.datamodel

data class EvaluatedTreatment(
    val treatmentCandidate: TreatmentCandidate,
    val evaluations: List<Evaluation>
) {

    fun eligible() = evaluations.none { it.result == EvaluationResult.FAIL }
}