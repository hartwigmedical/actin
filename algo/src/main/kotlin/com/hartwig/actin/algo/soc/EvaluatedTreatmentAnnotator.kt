package com.hartwig.actin.algo.soc

import com.hartwig.actin.datamodel.algo.AnnotatedTreatmentMatch
import com.hartwig.actin.datamodel.algo.EvaluatedTreatment
import com.hartwig.actin.datamodel.efficacy.EfficacyEntry

class EvaluatedTreatmentAnnotator(
    private val evidenceByTreatmentName: Map<String, List<EfficacyEntry>>,
    private val resistanceEvidenceMatcher: ResistanceEvidenceMatcher
) {

    fun annotate(evaluatedTreatments: List<EvaluatedTreatment>): List<AnnotatedTreatmentMatch> {
        return evaluatedTreatments.map { evaluatedTreatment ->
            val treatment = evaluatedTreatment.treatmentCandidate.treatment

            AnnotatedTreatmentMatch(
                treatmentCandidate = evaluatedTreatment.treatmentCandidate,
                evaluations = evaluatedTreatment.evaluations,
                annotations = lookUp(evaluatedTreatment),
                resistanceEvidence = resistanceEvidenceMatcher.match(treatment)
            )
        }
    }

    private fun lookUp(treatment: EvaluatedTreatment): List<EfficacyEntry> {
        return evidenceByTreatmentName[treatment.treatmentCandidate.treatment.name.lowercase()]?.distinct() ?: emptyList()
    }

    companion object {
        fun create(
            efficacyEvidence: List<EfficacyEntry>,
            resistanceEvidenceMatcher: ResistanceEvidenceMatcher
        ): EvaluatedTreatmentAnnotator {
            val evidenceByTreatmentName = efficacyEvidence
                .flatMap { entry ->
                    entry.trialReferences
                        .flatMap { it.patientPopulations }
                        .mapNotNull { it.treatment }.map { it.name.lowercase() to entry }
                }
                .groupBy({ it.first }, { it.second })

            return EvaluatedTreatmentAnnotator(evidenceByTreatmentName, resistanceEvidenceMatcher)
        }
    }
}