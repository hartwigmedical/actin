package com.hartwig.actin.algo.interpretation

import com.hartwig.actin.algo.datamodel.AnnotatedTreatmentMatch
import com.hartwig.actin.algo.datamodel.EvaluatedTreatment
import com.hartwig.actin.efficacy.EfficacyEntry

class EvaluatedTreatmentAnnotator(private val evidenceByTreatmentName: Map<String, List<EfficacyEntry>>) {

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
        return evidenceByTreatmentName[treatment.treatmentCandidate.treatment.name.lowercase()]?.distinct() ?: emptyList()
    }

    companion object {
        fun create(efficacyEvidence: List<EfficacyEntry>): EvaluatedTreatmentAnnotator {
            val evidenceByTreatmentName = efficacyEvidence
                .flatMap { entry ->
                    entry.treatments.map { it.name.lowercase() to entry }
                }
                .groupBy({ it.first }, { it.second })
            return EvaluatedTreatmentAnnotator(evidenceByTreatmentName)
        }
    }
}