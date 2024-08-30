package com.hartwig.actin.algo.soc

import com.hartwig.actin.datamodel.algo.EvaluatedTreatment

class EvaluatedTreatmentInterpreter(private val recommendedTreatments: List<EvaluatedTreatment>) {

    fun summarize(): String {
        return if (recommendedTreatments.isEmpty()) {
            "No treatments available"
        } else {
            "Available treatment(s): " + recommendedTreatments.map(EvaluatedTreatment::treatmentCandidate).distinct().joinToString("\n")
        }
    }
}