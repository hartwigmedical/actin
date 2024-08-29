package com.hartwig.actin.datamodel.algo

data class EvaluatedTreatment(
    val treatmentCandidate: TreatmentCandidate,
    val evaluations: List<Evaluation>
) {

    fun eligible() = evaluations.none { it.result == EvaluationResult.FAIL }
}