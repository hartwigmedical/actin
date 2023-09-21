package com.hartwig.actin.algo.soc

import com.hartwig.actin.algo.soc.datamodel.EvaluatedTreatment

class EvaluatedTreatmentInterpreter(private val recommendedTreatments: List<EvaluatedTreatment>) {

    fun summarize(): String {
        return if (recommendedTreatments.isEmpty()) {
            "No treatments available"
        } else {
            val recommendedTreatmentString = recommendedTreatments.joinToString(", ") { it.treatmentCandidate.treatment.display() }
            val exhaustionString = if (hasExhaustedStandardOfCare()) "" else " not"
            "Recommended treatment(s): $recommendedTreatmentString\nStandard of care has$exhaustionString been exhausted"
        }
    }

    fun csv(): String {
        return "Treatment,Score,Warnings\n" + recommendedTreatments.joinToString("\n") { evaluatedTreatment: EvaluatedTreatment ->
            val warningSummary: String = evaluatedTreatment.evaluations.toSet().flatMap { eval ->
                setOf(eval.failSpecificMessages(), eval.warnSpecificMessages(), eval.undeterminedSpecificMessages()).flatten()
            }.joinToString()
            listOf(evaluatedTreatment.treatmentCandidate.treatment.name(), evaluatedTreatment.score, warningSummary).joinToString()
        }
    }

    fun listAvailableTreatmentsByScore(): String {
        return availableTreatmentsByScore().entries.sortedByDescending { it.key }
            .joinToString("\n") { (key: Int, value: List<EvaluatedTreatment>) ->
                "Score=$key: " + value.joinToString { it.treatmentCandidate.treatment.name() }
            }
    }

    private fun hasExhaustedStandardOfCare() = recommendedTreatments.all { it.treatmentCandidate.isOptional }

    private fun availableTreatmentsByScore(): Map<Int, List<EvaluatedTreatment>> {
        return recommendedTreatments.groupBy { it.score }
    }
}