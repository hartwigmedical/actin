package com.hartwig.actin.algo.datamodel

import com.hartwig.actin.efficacy.EfficacyEntry
import com.hartwig.serve.datamodel.ActionableEvent
import com.hartwig.actin.personalized.datamodel.Measurement

data class AnnotatedTreatmentMatch(
    val treatmentCandidate: TreatmentCandidate,
    val evaluations: List<Evaluation>,
    val annotations: List<EfficacyEntry>,
    val resistanceEvidence: List<ResistanceEvidence>,
    val generalPfs: Measurement? = null
) {

    fun eligible() = evaluations.none { it.result == EvaluationResult.FAIL }
}


// TreatmentMatcherApplication serve inlezen
//