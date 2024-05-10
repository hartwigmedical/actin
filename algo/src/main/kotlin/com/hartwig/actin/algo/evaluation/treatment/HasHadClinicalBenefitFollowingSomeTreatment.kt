package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.Format
import com.hartwig.actin.clinical.datamodel.treatment.Treatment
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentType
import com.hartwig.actin.clinical.datamodel.treatment.history.TreatmentResponse

class HasHadClinicalBenefitFollowingSomeTreatment(
    private val treatment: Treatment? = null, private val category: TreatmentCategory? = null, private val types: Set<TreatmentType>? = null
    ) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val history = record.oncologicalHistory
        val targetTreatments =
            when {
                treatment != null -> {
                    history.filter { it.allTreatments().any { t -> t.name.equals(treatment.name, ignoreCase = true) } }
                }
                category != null && types != null -> {
                    history.filter {
                        it.allTreatments().any { t -> t.categories().contains(category) && t.types().intersect(types).isNotEmpty() }
                    }
                }
                category != null -> {
                    history.filter { it.allTreatments().any { t -> t.categories().contains(category) } }
                }
                else -> history
            }

        val targetTreatmentsToResponseMap = targetTreatments.groupBy { it.treatmentHistoryDetails?.bestResponse }

        val treatmentsSimilarToTargetTreatment = treatment?.let {
            history.filter { (it.matchesTypeFromSet(treatment.types()) != false) &&
                    it.categories().intersect(treatment.categories()).isNotEmpty()
            }
        }

        val treatmentWithResponse = targetTreatmentsToResponseMap.keys.intersect(BENEFIT_RESPONSE_SET).isNotEmpty()
        val stableDisease = targetTreatmentsToResponseMap.containsKey(TreatmentResponse.STABLE_DISEASE)
        val mixedResponse = targetTreatmentsToResponseMap.containsKey(TreatmentResponse.MIXED)
        val uncertainResponse = targetTreatmentsToResponseMap.keys == setOf(null)

        val treatmentDisplay =
            if (treatment != null) {
                " with ${treatment.display()}"
            } else if (category != null && types != null) {
                " of category ${category.display()} + and type(s) ${Format.concatItemsWithOr(types)}"
            } else if (category != null) {
                " of category ${category.display()}"
            } else ""
        val benefitMessage = " objective benefit from treatment$treatmentDisplay"
        val bestResponse = if (stableDisease) "best response: stable disease" else "best response: mixed"
        return when {
            targetTreatmentsToResponseMap.isEmpty() -> {
                if (!treatmentsSimilarToTargetTreatment.isNullOrEmpty()){
                    val similarDrugMessage = "receive exact treatment but received similar drugs (${
                        treatmentsSimilarToTargetTreatment.joinToString(",") { it.treatmentDisplay() }
                    })"
                    if (treatmentsSimilarToTargetTreatment.none {
                        ProgressiveDiseaseFunctions.treatmentResultedInPD(it) == true
                    }) {
                    EvaluationFactory.undetermined(
                        "Undetermined clinical benefit from treatment$treatmentDisplay - patient did not $similarDrugMessage",
                        "Undetermined clinical benefit from treatment$treatmentDisplay - did not $similarDrugMessage"
                    ) } else {
                        EvaluationFactory.fail(
                            "Patient did not $similarDrugMessage with PD as best response",
                            "Did not $similarDrugMessage with PD as best response"
                        )
                    }
                } else {
                    EvaluationFactory.fail(
                        "Patient has not received treatment$treatmentDisplay",
                        "Has not received treatment$treatmentDisplay"
                    )
                }
            }

            treatmentWithResponse -> {
                EvaluationFactory.pass("Patient has had$benefitMessage", "Has had$benefitMessage")
            }

            stableDisease || mixedResponse -> {
                EvaluationFactory.warn(
                    "Uncertain if patient has had$benefitMessage ($bestResponse)",
                    "Uncertain$benefitMessage ($bestResponse)"
                )
            }

            uncertainResponse -> {
                EvaluationFactory.undetermined("Undetermined$benefitMessage", "Undetermined$benefitMessage")
            }

            else -> {
                EvaluationFactory.fail("Patient does not have$benefitMessage", "No$benefitMessage")
            }
        }
    }

    companion object {
        private val BENEFIT_RESPONSE_SET = setOf(
            TreatmentResponse.PARTIAL_RESPONSE, TreatmentResponse.COMPLETE_RESPONSE, TreatmentResponse.REMISSION
        )
    }
}