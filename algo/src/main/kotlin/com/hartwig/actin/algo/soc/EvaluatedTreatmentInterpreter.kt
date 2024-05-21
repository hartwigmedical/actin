package com.hartwig.actin.algo.soc

import com.hartwig.actin.algo.datamodel.EvaluatedTreatment

class EvaluatedTreatmentInterpreter(private val recommendedTreatments: List<EvaluatedTreatment>) {

    fun summarize(): String {
        return if (recommendedTreatments.isEmpty()) {
            "No treatments available"
        } else {
            "Available treatment(s): " + recommendedTreatments.map(EvaluatedTreatment::treatmentCandidate).distinct().joinToString("\n")
        }
    }

    fun csv(): String {
        return "Treatment,Optional,Warnings\n" + recommendedTreatments.joinToString("\n") { evaluatedTreatment: EvaluatedTreatment ->
            val warningSummary: String = evaluatedTreatment.evaluations.toSet().flatMap { eval ->
                setOf(eval.failSpecificMessages, eval.warnSpecificMessages, eval.undeterminedSpecificMessages).flatten()
            }.joinToString()
            listOf(
                evaluatedTreatment.treatmentCandidate.treatment.name,
                evaluatedTreatment.treatmentCandidate.optional,
                warningSummary
            ).joinToString(",")
        }
    }
}