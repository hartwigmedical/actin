package com.hartwig.actin.algo.soc

import com.hartwig.actin.algo.datamodel.AnnotatedTreatmentMatch
import com.hartwig.actin.algo.datamodel.EvaluatedTreatment
import com.hartwig.actin.efficacy.EfficacyEntry
import com.hartwig.actin.personalization.datamodel.Measurement

class EvaluatedTreatmentAnnotator(private val evidenceByTreatmentName: Map<String, List<EfficacyEntry>>) {

    fun annotate(
        evaluatedTreatments: List<EvaluatedTreatment>, pfsByTreatmentName: Map<String, Measurement>? = null
    ): List<AnnotatedTreatmentMatch> {
        return evaluatedTreatments.map { evaluatedTreatment ->
            AnnotatedTreatmentMatch(
                treatmentCandidate = evaluatedTreatment.treatmentCandidate,
                evaluations = evaluatedTreatment.evaluations,
                annotations = lookUp(evaluatedTreatment),
                generalPfs = pfsByTreatmentName?.get(evaluatedTreatment.treatmentCandidate.treatment.name.lowercase())
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
                    entry.trialReferences
                        .flatMap { it.patientPopulations }
                        .mapNotNull { it.treatment }.map { it.name.lowercase() to entry }
                }
                .groupBy({ it.first }, { it.second })

            return EvaluatedTreatmentAnnotator(evidenceByTreatmentName)
        }
    }
}