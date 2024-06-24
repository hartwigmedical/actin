package com.hartwig.actin.algo.datamodel

import com.hartwig.actin.efficacy.EfficacyEntry
import com.hartwig.actin.personalization.datamodel.Measurement

data class AnnotatedTreatmentMatch(
    val treatmentCandidate: TreatmentCandidate,
    val evaluations: List<Evaluation>,
    val annotations: List<EfficacyEntry>,
    val generalPfs: Measurement? = null
) {

    fun eligible() = evaluations.none { it.result == EvaluationResult.FAIL }
}