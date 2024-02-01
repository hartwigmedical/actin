package com.hartwig.actin.algo.soc.datamodel

import com.hartwig.actin.algo.datamodel.Evaluation

data class EvaluatedTreatment(
    val treatmentCandidate: TreatmentCandidate,
    val evaluations: List<Evaluation>
)