package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.clinical.datamodel.treatment.Treatment
import com.hartwig.actin.clinical.datamodel.treatment.history.TreatmentResponse

class HasHadClinicalBenefitFollowingSomeTreatment(private val treatment: Treatment) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val targetTreatments = record.clinical.oncologicalHistory.filter { it.treatments.contains(treatment) }
        val treatmentWithResponse = targetTreatments.any { it.treatmentHistoryDetails?.bestResponse in OBJECTIVE_RESPONSE_SET }
        val stableDisease = targetTreatments.any { it.treatmentHistoryDetails?.bestResponse == TreatmentResponse.STABLE_DISEASE }
        val mixedResponse = targetTreatments.any { it.treatmentHistoryDetails?.bestResponse == TreatmentResponse.MIXED }
        val uncertainResponse = targetTreatments.all { it.treatmentHistoryDetails?.bestResponse == null }

        val benefitMessage = "objective benefit from treatment with ${treatment.name}"
        val bestResponse = if (stableDisease) "best response: stable disease" else "best response: mixed"
        return when {
            targetTreatments.isEmpty() -> {
                EvaluationFactory.fail(
                    "Patient has not received ${treatment.name} treatment",
                    "Has not received ${treatment.name} treatment"
                )
            }

            treatmentWithResponse -> {
                EvaluationFactory.pass("Patient has had $benefitMessage", "Has had $benefitMessage")
            }

            stableDisease || mixedResponse -> {
                EvaluationFactory.warn(
                    "Uncertain if patient has had $benefitMessage ($bestResponse)",
                    "Uncertain $benefitMessage ($bestResponse"
                )
            }

            uncertainResponse -> {
                EvaluationFactory.undetermined("Undetermined $benefitMessage", "Undetermined $benefitMessage")
            }

            else -> {
                EvaluationFactory.fail("Patient does not have $benefitMessage", "No $benefitMessage")
            }
        }
    }

    companion object {
        private val OBJECTIVE_RESPONSE_SET = setOf(
            TreatmentResponse.PARTIAL_RESPONSE, TreatmentResponse.COMPLETE_RESPONSE, TreatmentResponse.REMISSION
        )
    }
}