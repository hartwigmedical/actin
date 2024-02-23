package com.hartwig.actin.algo.interpretation

import com.hartwig.actin.algo.datamodel.AnnotatedTreatmentMatch
import com.hartwig.actin.algo.datamodel.EvaluatedTreatment
import com.hartwig.actin.efficacy.EfficacyEntry
import com.hartwig.actin.efficacy.Therapy
import java.util.Collections

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
            return therapies.flatMap { generateOptions((it.synonyms?.split("|") ?: emptyList()) + it.therapyName) }
        }

        private fun generateOptions(therapies: List<String>): List<String> {
            return therapies.flatMap { item ->
                if (item.contains(" + ")) {
                    createPermutations(item)
                } else {
                    listOf(item)
                }
            }
        }

        private fun createPermutations(treatment: String): List<String> {
            val drugs = treatment.split(" + ")
            val permutations = mutableListOf<String>()
            generatePermutations(drugs, permutations, 0, drugs.size - 1)
            return permutations
        }

        private fun generatePermutations(drugs: List<String>, permutations: MutableList<String>, left: Int, right: Int) {
            if (left == right) {
                permutations.add(drugs.joinToString("+"))
            } else {
                for (i in left..right) {
                    Collections.swap(drugs, left, i)
                    generatePermutations(drugs, permutations, left + 1, right)
                    Collections.swap(drugs, left, i)
                }
            }
        }
    }
}