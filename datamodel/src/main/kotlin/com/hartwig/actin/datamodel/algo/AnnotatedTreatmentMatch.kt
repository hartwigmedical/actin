package com.hartwig.actin.datamodel.algo

import com.hartwig.actin.datamodel.efficacy.EfficacyEntry
import com.hartwig.actin.datamodel.personalization.Measurement

data class AnnotatedTreatmentMatch(
    val treatmentCandidate: TreatmentCandidate,
    val evaluations: List<Evaluation>,
    val annotations: List<EfficacyEntry>,
    val generalPfs: Measurement? = null,
    val resistanceEvidence: List<ResistanceEvidence>,
) {

    fun eligible() = evaluations.none { it.result == EvaluationResult.FAIL }
}