package com.hartwig.actin.algo.soc

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.calendar.ReferenceDateProvider
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.doid.DoidConstants
import com.hartwig.actin.algo.evaluation.EvaluationFunctionFactory
import com.hartwig.actin.algo.soc.datamodel.EvaluatedTreatment
import com.hartwig.actin.algo.soc.datamodel.TreatmentCandidate
import com.hartwig.actin.doid.DoidModel

class RecommendationEngine private constructor(
    private val doidModel: DoidModel,
    private val recommendationDatabase: RecommendationDatabase,
    private val evaluationFunctionFactory: EvaluationFunctionFactory
) {

    fun determineAvailableTreatments(patientRecord: PatientRecord): List<EvaluatedTreatment> {
        val tumorDoids = expandedTumorDoids(patientRecord, doidModel)
        require(standardOfCareCanBeEvaluatedForPatient(patientRecord, doidModel)) {
            "SOC recommendation only supported for colorectal carcinoma"
        }

        return recommendationDatabase.treatmentCandidatesForDoidSet(tumorDoids).asSequence()
            .map { evaluateTreatmentForPatient(it, patientRecord) }
            .filter { treatmentHasNoFailedEvaluations(it) }
            .filter { it.score >= 0 }
            .sortedByDescending { it.score }.toList()
    }

    fun provideRecommendations(patientRecord: PatientRecord): EvaluatedTreatmentInterpreter {
        return EvaluatedTreatmentInterpreter(determineAvailableTreatments(patientRecord))
    }

    fun patientHasExhaustedStandardOfCare(patientRecord: PatientRecord): Boolean {
        return determineAvailableTreatments(patientRecord).all { evaluatedTreatment: EvaluatedTreatment ->
            evaluatedTreatment.treatmentCandidate.isOptional
        }
    }

    private fun evaluateTreatmentForPatient(treatmentCandidate: TreatmentCandidate, patientRecord: PatientRecord): EvaluatedTreatment {
        val evaluations: List<Evaluation> = treatmentCandidate.eligibilityFunctions.map { eligibilityFunction ->
            evaluationFunctionFactory.create(eligibilityFunction).evaluate(patientRecord)
        }
        return EvaluatedTreatment(treatmentCandidate, evaluations, treatmentCandidate.expectedBenefitScore)
    }

    companion object {
        private val EXCLUDED_TUMOR_DOIDS = setOf("5777", "169", "1800")

        fun create(
            doidModel: DoidModel,
            recommendationDatabase: RecommendationDatabase,
            referenceDateProvider: ReferenceDateProvider
        ): RecommendationEngine {
            return RecommendationEngine(
                doidModel, recommendationDatabase, EvaluationFunctionFactory.create(
                    doidModel,
                    referenceDateProvider,
                    recommendationDatabase.treatmentDatabase
                )
            )
        }

        fun standardOfCareCanBeEvaluatedForPatient(patientRecord: PatientRecord, doidModel: DoidModel): Boolean {
            val tumorDoids = expandedTumorDoids(patientRecord, doidModel)
            return DoidConstants.COLORECTAL_CANCER_DOID in tumorDoids && (EXCLUDED_TUMOR_DOIDS intersect tumorDoids).isEmpty()
        }

        private fun expandedTumorDoids(patientRecord: PatientRecord, doidModel: DoidModel): Set<String> {
            return patientRecord.clinical().tumor().doids()?.flatMap { doidModel.doidWithParents(it) }?.toSet() ?: emptySet()
        }

        private fun treatmentHasNoFailedEvaluations(evaluatedTreatment: EvaluatedTreatment): Boolean {
            return evaluatedTreatment.evaluations.none { it.result() == EvaluationResult.FAIL }
        }
    }
}