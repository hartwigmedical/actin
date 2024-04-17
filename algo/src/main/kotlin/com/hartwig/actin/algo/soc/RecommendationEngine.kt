package com.hartwig.actin.algo.soc

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.EvaluatedTreatment
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.datamodel.TreatmentCandidate
import com.hartwig.actin.algo.doid.DoidConstants
import com.hartwig.actin.algo.evaluation.EvaluationFunctionFactory
import com.hartwig.actin.doid.DoidModel
import com.hartwig.actin.trial.datamodel.EligibilityFunction

class RecommendationEngine(
    private val doidModel: DoidModel,
    private val treatmentCandidateDatabase: TreatmentCandidateDatabase,
    private val evaluationFunctionFactory: EvaluationFunctionFactory
) {

    fun standardOfCareEvaluatedTreatments(patientRecord: PatientRecord): List<EvaluatedTreatment> {
        require(standardOfCareCanBeEvaluatedForPatient(patientRecord)) {
            "SOC recommendation only supported for colorectal carcinoma"
        }
        return treatmentCandidates().map { evaluateTreatmentEligibilityForPatient(it, patientRecord) }
    }

    fun standardOfCareCanBeEvaluatedForPatient(patientRecord: PatientRecord): Boolean {
        val tumorDoids = expandedTumorDoids(patientRecord, doidModel)
        return DoidConstants.COLORECTAL_CANCER_DOID in tumorDoids && (EXCLUDED_TUMOR_DOIDS intersect tumorDoids).isEmpty()
    }

    fun determineRequiredTreatments(patientRecord: PatientRecord): List<EvaluatedTreatment> {
        return treatmentCandidates().asSequence()
            .filterNot(TreatmentCandidate::optional)
            .map { evaluateTreatmentRequirementForPatient(it, patientRecord) }
            .filter(::treatmentHasNoFailedEvaluations)
            .toList()
    }

    fun provideRecommendations(patientRecord: PatientRecord): String {
        return EvaluatedTreatmentInterpreter(determineAvailableTreatments(patientRecord)).summarize()
    }

    fun patientHasExhaustedStandardOfCare(patientRecord: PatientRecord): Boolean {
        return determineRequiredTreatments(patientRecord).isEmpty()
    }

    private fun treatmentCandidates() = CrcDecisionTree(treatmentCandidateDatabase).treatmentCandidates()

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
        return evaluateTreatmentCandidate(treatmentCandidate.eligibilityFunctions, patientRecord, treatmentCandidate)
    }

    private fun evaluateTreatmentCandidate(
        eligibilityFunctions: Set<EligibilityFunction>,
        patientRecord: PatientRecord,
        treatmentCandidate: TreatmentCandidate
    ): EvaluatedTreatment {
        val evaluations = eligibilityFunctions.map { evaluationFunctionFactory.create(it).evaluate(patientRecord) }
        return EvaluatedTreatment(treatmentCandidate, evaluations)
    }

    private fun determineAvailableTreatments(patientRecord: PatientRecord): List<EvaluatedTreatment> {
        return standardOfCareEvaluatedTreatments(patientRecord).filter(::treatmentHasNoFailedEvaluations)
    }

    companion object {
        private val EXCLUDED_TUMOR_DOIDS = setOf(
            DoidConstants.RECTUM_NEUROENDOCRINE_NEOPLASM_DOID,
            DoidConstants.NEUROENDOCRINE_TUMOR_DOID,
            DoidConstants.NEUROENDOCRINE_CARCINOMA_DOID
        )

        private fun expandedTumorDoids(patientRecord: PatientRecord, doidModel: DoidModel): Set<String> {
            return patientRecord.tumor.doids?.flatMap { doidModel.doidWithParents(it) }?.toSet() ?: emptySet()
        }

        fun treatmentHasNoFailedEvaluations(evaluatedTreatment: EvaluatedTreatment): Boolean {
            return evaluatedTreatment.evaluations.none { it.result == EvaluationResult.FAIL }
        }
    }
}