package com.hartwig.actin.algo.datamodel

import com.hartwig.actin.efficacy.ExtendedEvidenceEntry

class EvaluatedTreatmentAnnotator(private val efficacyEvidence: List<ExtendedEvidenceEntry>) {

    fun annotate(evaluatedTreatments: List<EvaluatedTreatment>): List<StandardOfCareMatch> {
        return evaluatedTreatments.map { evaluatedTreatment ->
            StandardOfCareMatch(
                treatmentCandidate = evaluatedTreatment.treatmentCandidate,
                evaluations = evaluatedTreatment.evaluations,
                annotations = lookUp(evaluatedTreatment)
            )
        }
    }

    private fun lookUp(treatment: EvaluatedTreatment): List<ExtendedEvidenceEntry> {
        return efficacyEvidence.filter { entry -> entry.therapies.any { therapy -> therapy == treatment.treatmentCandidate.treatment.name } }
    }
}