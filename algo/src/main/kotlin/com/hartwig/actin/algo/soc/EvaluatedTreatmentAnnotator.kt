package com.hartwig.actin.algo.soc

import com.hartwig.actin.datamodel.algo.AnnotatedTreatmentMatch
import com.hartwig.actin.datamodel.algo.EvaluatedTreatment
import com.hartwig.actin.datamodel.efficacy.EfficacyEntry
import com.hartwig.actin.datamodel.personalization.MeasurementType
import com.hartwig.actin.datamodel.personalization.TreatmentAnalysis
import com.hartwig.actin.personalization.similarity.population.ALL_PATIENTS_POPULATION_NAME

class EvaluatedTreatmentAnnotator(
    private val evidenceByTreatmentName: Map<String, List<EfficacyEntry>>,
    private val resistanceEvidenceMatcher: ResistanceEvidenceMatcher
) {

    fun annotate(
        evaluatedTreatments: List<EvaluatedTreatment>, treatmentAnalyses: List<TreatmentAnalysis>? = null
    ): List<AnnotatedTreatmentMatch> {
        val pfsByTreatmentName = treatmentAnalyses?.flatMap { (treatmentGroup, measurementsByType) ->
            treatmentGroup.memberTreatmentNames.map { treatmentName ->
                treatmentName to measurementsByType[MeasurementType.PROGRESSION_FREE_SURVIVAL]!![ALL_PATIENTS_POPULATION_NAME]
            }
        }?.toMap()

        return evaluatedTreatments.map { evaluatedTreatment ->
            AnnotatedTreatmentMatch(
                treatmentCandidate = evaluatedTreatment.treatmentCandidate,
                evaluations = evaluatedTreatment.evaluations,
                annotations = lookUp(evaluatedTreatment),
                generalPfs = pfsByTreatmentName?.get(evaluatedTreatment.treatmentCandidate.treatment.name.lowercase()),
                resistanceEvidence = resistanceEvidenceMatcher.match(evaluatedTreatment.treatmentCandidate.treatment)
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