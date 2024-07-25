package com.hartwig.actin.algo.datamodel

import com.hartwig.actin.efficacy.EfficacyEntry
import com.hartwig.actin.personalized.datamodel.Measurement

data class AnnotatedTreatmentMatch(
    val treatmentCandidate: TreatmentCandidate,
    val evaluations: List<Evaluation>,
    val annotations: List<EfficacyEntry>,
    val generalPfs: Measurement? = null,
    val resistanceEvidence: List<ResistanceEvidence>? = null,
) {

    fun eligible() = evaluations.none { it.result == EvaluationResult.FAIL }
}