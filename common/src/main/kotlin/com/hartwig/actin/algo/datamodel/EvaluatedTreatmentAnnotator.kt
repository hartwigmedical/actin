package com.hartwig.actin.algo.datamodel

import com.hartwig.actin.efficacy.ExtendedEvidenceEntry
import com.hartwig.actin.efficacy.Therapy

class EvaluatedTreatmentAnnotator(
    private val efficacyEvidence: List<ExtendedEvidenceEntry>
) {

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
        return efficacyEvidence.filter { entry ->
            convertTherapies(entry.therapies).any { therapy ->
                therapy.equals(
                    treatment.treatmentCandidate.treatment.name,
                    true
                )
            }
        }
    }

    private fun convertTherapies(therapies: List<Therapy>): List<String?> {
        return therapies.flatMap { generateOptions(it.synonyms ?: it.therapyName) }
    }

    private fun generateOptions(therapy: String): List<String> {
        return therapy.split("|").flatMap { item ->
            if (item.contains(" + ")) {
                createPermutations(item)
            } else {
                listOf(item)
            }
        }
    }

    private fun createPermutations(treatment: String): List<String> {
        val drugs = treatment.split(" + ")
        return listOf(drugs.joinToString("+"), drugs.reversed().joinToString("+"))
    }
}