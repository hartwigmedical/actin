package com.hartwig.actin.algo.soc

import com.hartwig.actin.algo.soc.datamodel.EvaluatedTreatment

internal class EvaluatedTreatmentInterpreter(recommendedTreatments: List<EvaluatedTreatment>) {
    private val recommendedTreatments: List<EvaluatedTreatment>

    init {
        this.recommendedTreatments = recommendedTreatments
    }

    fun summarize(): String {
        return if (recommendedTreatments.isEmpty()) {
            "No treatments available"
        } else {
            val bestScore: Int = recommendedTreatments[0].score
            "Recommended treatments: " + recommendedTreatments.filter { it.score == bestScore }.joinToString { it.treatmentCandidate.name }
        }
    }

    fun csv(): String {
        return "Treatment,Score,Warnings\n" + recommendedTreatments.joinToString("\n") { evaluatedTreatment: EvaluatedTreatment ->
            val warningSummary: String = evaluatedTreatment.evaluations.toSet().flatMap { eval ->
                setOf(eval.failSpecificMessages(), eval.warnSpecificMessages(), eval.undeterminedSpecificMessages()).flatten()
            }.joinToString()
            listOf(evaluatedTreatment.treatmentCandidate.name, evaluatedTreatment.score, warningSummary).joinToString()
        }
    }

    fun listAvailableTreatmentsByScore(): String {
        return availableTreatmentsByScore().entries.sortedByDescending { it.key }
            .joinToString("\n") { (key: Int, value: List<EvaluatedTreatment>) ->
                "Score=$key: " + value.joinToString { it.treatmentCandidate.name }
            }
    }

    private fun availableTreatmentsByScore(): Map<Int, List<EvaluatedTreatment>> {
        return recommendedTreatments.groupBy { it.score }
    }
}