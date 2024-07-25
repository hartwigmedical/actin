package com.hartwig.actin.algo.datamodel

import com.hartwig.actin.efficacy.EfficacyEntry
import com.hartwig.actin.personalized.datamodel.Measurement
import com.hartwig.serve.datamodel.ActionableEvent

data class AnnotatedTreatmentMatch(
    val treatmentCandidate: TreatmentCandidate,
    val evaluations: List<Evaluation>,
    val annotations: List<EfficacyEntry>,
    val generalPfs: Measurement? = null,
    val resistanceEvidence: List<ResistanceEvidence>,
) {

    fun eligible() = evaluations.none { it.result == EvaluationResult.FAIL }
}


// TreatmentMatcherApplication serve inlezen
//