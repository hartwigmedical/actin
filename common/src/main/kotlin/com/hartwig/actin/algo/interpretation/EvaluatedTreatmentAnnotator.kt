package com.hartwig.actin.algo.interpretation

import com.hartwig.actin.algo.datamodel.AnnotatedTreatmentMatch
import com.hartwig.actin.algo.datamodel.EvaluatedTreatment
import com.hartwig.actin.efficacy.EfficacyEntry

class EvaluatedTreatmentAnnotator(private val evidenceByTherapyName: Map<String, List<EfficacyEntry>>) {

    fun annotate(evaluatedTreatments: List<EvaluatedTreatment>): List<AnnotatedTreatmentMatch> {
        return evaluatedTreatments.map { evaluatedTreatment ->
            AnnotatedTreatmentMatch(
                treatmentCandidate = evaluatedTreatment.treatmentCandidate,
                evaluations = evaluatedTreatment.evaluations,
                annotations = lookUp(evaluatedTreatment)
            )
        }
    }

    private fun lookUp(treatment: EvaluatedTreatment): List<EfficacyEntry> {
        return evidenceByTherapyName[treatment.treatmentCandidate.treatment.name]?.distinct() ?: emptyList()
    }

    companion object {
        fun create(efficacyEvidence: List<EfficacyEntry>): EvaluatedTreatmentAnnotator {
            val evidenceByTherapyName = efficacyEvidence
                .flatMap { entry ->
                    entry.therapies.map { it.lowercase() to entry }
                }
                .groupBy({ it.first }, { it.second })
            return EvaluatedTreatmentAnnotator(evidenceByTherapyName)
        }
    }
}