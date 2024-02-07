package com.hartwig.actin.algo.soc

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.doid.DoidConstants
import com.hartwig.actin.algo.evaluation.EvaluationFunctionFactory
import com.hartwig.actin.algo.evaluation.RuleMappingResources
import com.hartwig.actin.algo.soc.datamodel.EvaluatedTreatment
import com.hartwig.actin.algo.soc.datamodel.TreatmentCandidate
import com.hartwig.actin.doid.DoidModel
import com.hartwig.actin.trial.datamodel.EligibilityFunction

class RecommendationEngine private constructor(
    private val doidModel: DoidModel,
    private val treatmentCandidateDatabase: TreatmentCandidateDatabase,
    private val evaluationFunctionFactory: EvaluationFunctionFactory
) {

    fun determineAvailableTreatments(patientRecord: PatientRecord): List<EvaluatedTreatment> {
        require(standardOfCareCanBeEvaluatedForPatient(patientRecord)) {
            "SOC recommendation only supported for colorectal carcinoma"
        }

        return treatmentCandidates().asSequence()
            .map { evaluateTreatmentEligibilityForPatient(it, patientRecord) }
            .filter { treatmentHasNoFailedEvaluations(it) }
            .toList()
    }

    fun standardOfCareCanBeEvaluatedForPatient(patientRecord: PatientRecord): Boolean {
        val tumorDoids = expandedTumorDoids(patientRecord, doidModel)
        return DoidConstants.COLORECTAL_CANCER_DOID in tumorDoids && (EXCLUDED_TUMOR_DOIDS intersect tumorDoids).isEmpty()
    }

    private fun determineRequiredTreatments(patientRecord: PatientRecord): List<EvaluatedTreatment> {
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
        return evaluateTreatmentCandidate(treatmentCandidate.eligibilityFunctionsForRequirement(), patientRecord, treatmentCandidate)
    }

    private fun evaluateTreatmentCandidate(
        eligibilityFunctions: Set<EligibilityFunction>,
        patientRecord: PatientRecord,
        treatmentCandidate: TreatmentCandidate
    ): EvaluatedTreatment {
        val evaluations = eligibilityFunctions.map { evaluationFunctionFactory.create(it).evaluate(patientRecord) }
        return EvaluatedTreatment(treatmentCandidate, evaluations)
    }

    companion object {
        private val EXCLUDED_TUMOR_DOIDS = setOf(
            DoidConstants.RECTUM_NEUROENDOCRINE_NEOPLASM_DOID,
            DoidConstants.NEUROENDOCRINE_TUMOR_DOID,
            DoidConstants.NEUROENDOCRINE_CARCINOMA_DOID
        )

        fun create(resources: RuleMappingResources): RecommendationEngine {
            return RecommendationEngine(
                resources.doidModel,
                TreatmentCandidateDatabase(resources.treatmentDatabase),
                EvaluationFunctionFactory.create(resources)
            )
        }

        private fun expandedTumorDoids(patientRecord: PatientRecord, doidModel: DoidModel): Set<String> {
            return patientRecord.clinical.tumor.doids?.flatMap { doidModel.doidWithParents(it) }?.toSet() ?: emptySet()
        }

        private fun treatmentHasNoFailedEvaluations(evaluatedTreatment: EvaluatedTreatment): Boolean {
            return evaluatedTreatment.evaluations.none { it.result == EvaluationResult.FAIL }
        }
    }
}