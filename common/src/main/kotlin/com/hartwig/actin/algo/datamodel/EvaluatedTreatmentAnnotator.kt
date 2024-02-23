package com.hartwig.actin.algo.datamodel

import com.hartwig.actin.efficacy.EfficacyEntry
import com.hartwig.actin.efficacy.Therapy

class EvaluatedTreatmentAnnotator(
    private val evidenceByTherapyName: Map<String, EfficacyEntry>
) {

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
//        return efficacyEvidence.filter { entry ->
//            namesForTherapies(entry.therapies).any { therapy ->
//                therapy.equals(
//                    treatment.treatmentCandidate.treatment.name,
//                    true
//                )
//            }
//        }
        val rawTreatment = treatment.treatmentCandidate.treatment
        return (rawTreatment.synonyms + rawTreatment.name).mapNotNull { evidenceByTherapyName[it.lowercase()] }.distinct()
    }

    companion object {
        fun create(efficacyEvidence: List<EfficacyEntry>): EvaluatedTreatmentAnnotator {
            val evidenceByTherapyName = efficacyEvidence.flatMap { entry ->
                namesForTherapies(entry.therapies).map { it.lowercase() to entry }
            }.toMap()
            return EvaluatedTreatmentAnnotator(evidenceByTherapyName)
        }

        private fun namesForTherapies(therapies: List<Therapy>): List<String> {
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
}