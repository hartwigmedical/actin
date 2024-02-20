package com.hartwig.actin.algo.datamodel

import com.hartwig.actin.TreatmentDatabase
import com.hartwig.actin.efficacy.ExtendedEvidenceEntry
import com.hartwig.actin.efficacy.Therapy

class EvaluatedTreatmentAnnotator(
    private val efficacyEvidence: List<ExtendedEvidenceEntry>,
    private val treatmentDatabase: TreatmentDatabase
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
        return efficacyEvidence.filter { entry -> convertTherapies(entry.therapies).any { therapy -> therapy == treatment.treatmentCandidate.treatment.name } }
    }

    fun convertTherapies(therapies: List<Therapy>): List<String?> {
        return therapies.map { therapy -> findTreatment(therapy.synonyms ?: therapy.therapyName) }
    }

    private fun findTreatment(therapy: String): String? {
        val options = generateOptions(therapy)
        val finalOutput = mutableSetOf<String>()
        for (option in options) {
            treatmentDatabase.findTreatmentByName(option)?.let { finalOutput.add(it.name) }
        }

        return if (finalOutput.count() == 1) {
            finalOutput.joinToString("")
        } else if (finalOutput.isEmpty()) {
            null
        } else {
            throw IllegalStateException("Multiple matches found for therapy: $therapy")
        }
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