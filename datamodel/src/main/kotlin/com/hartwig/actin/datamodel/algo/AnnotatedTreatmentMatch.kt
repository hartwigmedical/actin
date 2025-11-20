package com.hartwig.actin.datamodel.algo

import com.hartwig.actin.datamodel.efficacy.EfficacyEntry

data class AnnotatedTreatmentMatch(
    val treatmentCandidate: TreatmentCandidate,
    val evaluations: List<Evaluation>,
    val annotations: List<EfficacyEntry>,
    val resistanceEvidence: List<ResistanceEvidence>,
) {

    fun eligible(): Boolean {
        return evaluations.none { it.result == EvaluationResult.FAIL }
    }
}