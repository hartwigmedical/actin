package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.clinical.datamodel.treatment.Treatment
import com.hartwig.actin.clinical.datamodel.treatment.history.TreatmentResponse

class HasHadClinicalBenefitFollowingSomeTreatment(private val treatment: Treatment) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val targetTreatmentsToResponseMap = record.clinical.oncologicalHistory.filter {
            it.allTreatments().any { t -> t.name.equals(treatment.name, ignoreCase = true) }
        }.groupBy { it.treatmentHistoryDetails?.bestResponse }

        val treatmentsSimilarToTargetTreatment = record.clinical.oncologicalHistory.filter {
            (it.matchesTypeFromSet(treatment.types()) != false) &&
                    it.categories().intersect(treatment.categories()).isNotEmpty()
        }

        val treatmentWithResponse = targetTreatmentsToResponseMap.keys.intersect(BENEFIT_RESPONSE_SET).isNotEmpty()
        val stableDisease = targetTreatmentsToResponseMap.containsKey(TreatmentResponse.STABLE_DISEASE)
        val mixedResponse = targetTreatmentsToResponseMap.containsKey(TreatmentResponse.MIXED)
        val uncertainResponse = targetTreatmentsToResponseMap.keys == setOf(null)

        val benefitMessage = "objective benefit from treatment with ${treatment.name}"
        val bestResponse = if (stableDisease) "best response: stable disease" else "best response: mixed"
        return when {
            targetTreatmentsToResponseMap.isEmpty() -> {
                if (treatmentsSimilarToTargetTreatment.isNotEmpty()){
                    val similarDrugMessage = "receive exact treatment but received similar drugs (${
                        treatmentsSimilarToTargetTreatment.joinToString(",") { it.treatmentDisplay() }
                    })"
                    if (treatmentsSimilarToTargetTreatment.none {
                        ProgressiveDiseaseFunctions.treatmentResultedInPD(it) == true
                    }) {
                    EvaluationFactory.undetermined(
                        "Undetermined clinical benefit from ${treatment.name} - patient did not $similarDrugMessage",
                        "Undetermined clinical benefit from ${treatment.name} - did not $similarDrugMessage"
                    ) } else {
                        EvaluationFactory.fail(
                            "Patient did not $similarDrugMessage with PD as best response",
                            "Did not $similarDrugMessage with PD as best response"
                        )
                    }
                } else {
                    EvaluationFactory.fail(
                        "Patient has not received ${treatment.name} treatment",
                        "Has not received ${treatment.name} treatment"
                    )
                }
            }

            treatmentWithResponse -> {
                EvaluationFactory.pass("Patient has had $benefitMessage", "Has had $benefitMessage")
            }

            stableDisease || mixedResponse -> {
                EvaluationFactory.warn(
                    "Uncertain if patient has had $benefitMessage ($bestResponse)",
                    "Uncertain $benefitMessage ($bestResponse)"
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
        private val BENEFIT_RESPONSE_SET = setOf(
            TreatmentResponse.PARTIAL_RESPONSE, TreatmentResponse.COMPLETE_RESPONSE, TreatmentResponse.REMISSION
        )
    }
}