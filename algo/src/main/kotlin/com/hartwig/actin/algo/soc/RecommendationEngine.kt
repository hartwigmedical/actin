package com.hartwig.actin.algo.soc

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.calendar.ReferenceDateProvider
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.doid.DoidConstants
import com.hartwig.actin.algo.evaluation.EvaluationFunctionFactory
import com.hartwig.actin.algo.evaluation.medication.AtcTree
import com.hartwig.actin.algo.soc.datamodel.EvaluatedTreatment
import com.hartwig.actin.algo.soc.datamodel.TreatmentCandidate
import com.hartwig.actin.doid.DoidModel
import com.hartwig.actin.trial.datamodel.EligibilityFunction

class RecommendationEngine private constructor(
    private val doidModel: DoidModel,
    private val recommendationDatabase: RecommendationDatabase,
    private val evaluationFunctionFactory: EvaluationFunctionFactory
) {

    fun determineAvailableTreatments(patientRecord: PatientRecord): List<EvaluatedTreatment> {
        require(standardOfCareCanBeEvaluatedForPatient(patientRecord, doidModel)) {
            "SOC recommendation only supported for colorectal carcinoma"
        }

        return treatmentCandidateSequence(patientRecord)
            .map { evaluateTreatmentEligibilityForPatient(it, patientRecord) }
            .filter { treatmentHasNoFailedEvaluations(it) }
            .filter { it.score >= 0 }
            .sortedByDescending { it.score }.toList()
    }

    fun determineRequiredTreatments(patientRecord: PatientRecord): List<EvaluatedTreatment> {
        return treatmentCandidateSequence(patientRecord)
            .filterNot(TreatmentCandidate::isOptional)
            .map { evaluateTreatmentRequirementForPatient(it, patientRecord) }
            .filter { it.score >= 0 && treatmentHasNoFailedEvaluations(it) }
            .toList()
    }

    fun provideRecommendations(patientRecord: PatientRecord): String {
        return EvaluatedTreatmentInterpreter(determineAvailableTreatments(patientRecord)).summarize()
    }

    fun patientHasExhaustedStandardOfCare(patientRecord: PatientRecord): Boolean {
        return determineRequiredTreatments(patientRecord).isEmpty()
    }

    private fun treatmentCandidateSequence(patientRecord: PatientRecord) =
        recommendationDatabase.treatmentCandidatesForDoidSet(expandedTumorDoids(patientRecord, doidModel)).asSequence()

    private fun evaluateTreatmentEligibilityForPatient(
        treatmentCandidate: TreatmentCandidate,
        patientRecord: PatientRecord
    ): EvaluatedTreatment {
        return evaluateTreatmentCandidate(treatmentCandidate.eligibilityFunctions, patientRecord, treatmentCandidate)
    }

    private fun evaluateTreatmentRequirementForPatient(
        treatmentCandidate: TreatmentCandidate,
        patientRecord: PatientRecord
    ): EvaluatedTreatment {
        return evaluateTreatmentCandidate(treatmentCandidate.eligibilityFunctionsForRequirement(), patientRecord, treatmentCandidate)
    }

    private fun evaluateTreatmentCandidate(
        eligibilityFunctions: Set<EligibilityFunction>,
        patientRecord: PatientRecord,
        treatmentCandidate: TreatmentCandidate
    ): EvaluatedTreatment {
        val evaluations = eligibilityFunctions.map { evaluationFunctionFactory.create(it).evaluate(patientRecord) }
        return EvaluatedTreatment(treatmentCandidate, evaluations, treatmentCandidate.expectedBenefitScore)
    }

    companion object {
        private val EXCLUDED_TUMOR_DOIDS = setOf("5777", "169", "1800")

        fun create(
            doidModel: DoidModel,
            atcTree: AtcTree,
            recommendationDatabase: RecommendationDatabase,
            referenceDateProvider: ReferenceDateProvider
        ): RecommendationEngine {
            return RecommendationEngine(
                doidModel, recommendationDatabase, EvaluationFunctionFactory.create(
                    doidModel,
                    referenceDateProvider,
                    recommendationDatabase.treatmentDatabase,
                    atcTree
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