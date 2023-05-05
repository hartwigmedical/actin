package com.hartwig.actin.algo.soc.datamodel

import com.hartwig.actin.algo.datamodel.Evaluation

data class EvaluatedTreatment(
    val treatment: Treatment,
    val evaluations: List<Evaluation>,
    val score: Int,
)