package com.hartwig.actin.algo.soc

import com.hartwig.actin.algo.doid.DoidConstants
import com.hartwig.actin.algo.evaluation.EvaluationFunctionFactory
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.EvaluatedTreatment
import com.hartwig.actin.datamodel.algo.TreatmentCandidate
import com.hartwig.actin.datamodel.trial.EligibilityFunction
import com.hartwig.actin.doid.DoidModel

private val EXCLUDED_TUMOR_DOIDS = setOf(
    DoidConstants.RECTUM_NEUROENDOCRINE_NEOPLASM_DOID,
    DoidConstants.NEUROENDOCRINE_TUMOR_DOID,
    DoidConstants.NEUROENDOCRINE_CARCINOMA_DOID
)

class StandardOfCareEvaluator(
    private val doidModel: DoidModel,
    private val treatmentCandidateDatabase: TreatmentCandidateDatabase,
    private val evaluationFunctionFactory: EvaluationFunctionFactory
) {

    fun standardOfCareEvaluatedTreatments(patientRecord: PatientRecord): StandardOfCareEvaluation {
        require(standardOfCareCanBeEvaluatedForPatient(patientRecord)) {
            "SOC recommendation only supported for colorectal carcinoma"
        }
        return StandardOfCareEvaluation(treatmentCandidates().map { evaluateTreatmentEligibilityForPatient(it, patientRecord) })
    }

    fun standardOfCareCanBeEvaluatedForPatient(patientRecord: PatientRecord): Boolean {
        val tumorDoids = expandedTumorDoids(patientRecord, doidModel)
        return DoidConstants.COLORECTAL_CANCER_DOID in tumorDoids && (EXCLUDED_TUMOR_DOIDS intersect tumorDoids).isEmpty()
    }

    fun evaluateRequiredTreatments(patientRecord: PatientRecord): StandardOfCareEvaluation {
        val evaluatedTreatments = treatmentCandidates().filterNot(TreatmentCandidate::optional)
            .map { evaluateTreatmentRequirementForPatient(it, patientRecord) }
        return StandardOfCareEvaluation(evaluatedTreatments)
    }

    fun summarizeAvailableTreatments(patientRecord: PatientRecord): String {
        return EvaluatedTreatmentInterpreter(determineAvailableTreatments(patientRecord)).summarize()
    }

    fun patientHasExhaustedStandardOfCare(patientRecord: PatientRecord): Boolean {
        return evaluateRequiredTreatments(patientRecord).potentiallyEligibleTreatments().isEmpty()
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

    private fun determineAvailableTreatments(patientRecord: PatientRecord): List<EvaluatedTreatment> {
        return standardOfCareEvaluatedTreatments(patientRecord).potentiallyEligibleTreatments()
    }

    private fun expandedTumorDoids(patientRecord: PatientRecord, doidModel: DoidModel): Set<String> {
        return patientRecord.tumor.doids?.flatMap { doidModel.doidWithParents(it) }?.toSet() ?: emptySet()
    }
}