package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.Format
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.treatment.Treatment
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentType
import com.hartwig.actin.datamodel.clinical.treatment.history.TreatmentResponse

class HasHadClinicalBenefitFollowingSomeTreatmentOrCategoryOfTypes(
    private val treatment: Treatment? = null, private val category: TreatmentCategory? = null, private val types: Set<TreatmentType>? = null
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val history = record.oncologicalHistory
        val isMatchingTreatment: (Treatment) -> Boolean = when {
            treatment != null -> {
                { it.name.equals(treatment.name, ignoreCase = true) }
            }

            category != null && types != null -> {
                { it.categories().contains(category) && it.types().intersect(types).isNotEmpty() }
            }

            category != null -> {
                { it.categories().contains(category) }
            }

            else -> {
                throw IllegalStateException("Treatment or category must be provided")
            }
        }
        val targetTreatmentsToResponseMap = history.filter {
            it.allTreatments().any(isMatchingTreatment::invoke)
        }
            .groupBy { it.treatmentHistoryDetails?.bestResponse }

        val treatmentsSimilarToTargetTreatment = treatment?.let {
            history.filter {
                (it.matchesTypeFromSet(treatment.types()) != false) &&
                        it.categories().intersect(treatment.categories()).isNotEmpty()
            }
        }

        val treatmentWithResponse = targetTreatmentsToResponseMap.keys.intersect(BENEFIT_RESPONSE_SET).isNotEmpty()
        val stableDisease = targetTreatmentsToResponseMap.containsKey(TreatmentResponse.STABLE_DISEASE)
        val mixedResponse = targetTreatmentsToResponseMap.containsKey(TreatmentResponse.MIXED)
        val uncertainResponse = targetTreatmentsToResponseMap.keys == setOf(null)

        val treatmentDisplay =
            when {
                treatment != null -> " with ${treatment.display()}"
                category != null && types != null -> " of category ${category.display()} and type(s) ${Format.concatItemsWithOr(types)}"
                category != null -> " of category ${category.display()}"
                else -> ""
            }
        val benefitMessage = " objective benefit from treatment$treatmentDisplay"
        val bestResponse = if (stableDisease) "best response: stable disease" else "best response: mixed"
        return when {
            targetTreatmentsToResponseMap.isEmpty() -> {
                if (!treatmentsSimilarToTargetTreatment.isNullOrEmpty()) {
                    val similarDrugMessage = "receive exact treatment but received similar drugs (${
                        treatmentsSimilarToTargetTreatment.joinToString(",") { it.treatmentDisplay() }
                    })"
                    if (treatmentsSimilarToTargetTreatment.none {
                            ProgressiveDiseaseFunctions.treatmentResultedInPD(it) == true
                        }) {
                        EvaluationFactory.undetermined("Undetermined clinical benefit from treatment$treatmentDisplay - did not $similarDrugMessage")
                    } else {
                        EvaluationFactory.fail("Did not $similarDrugMessage with PD as best response")
                    }
                } else {
                    EvaluationFactory.fail("Has not received treatment$treatmentDisplay")
                }
            }

            treatmentWithResponse -> {
                EvaluationFactory.pass("Has had$benefitMessage")
            }

            stableDisease || mixedResponse -> {
                EvaluationFactory.warn("Uncertain$benefitMessage ($bestResponse)")
            }

            uncertainResponse -> {
                EvaluationFactory.undetermined("Undetermined$benefitMessage")
            }

            else -> {
                EvaluationFactory.fail("No$benefitMessage")
            }
        }
    }

    companion object {
        private val BENEFIT_RESPONSE_SET = setOf(
            TreatmentResponse.PARTIAL_RESPONSE, TreatmentResponse.COMPLETE_RESPONSE, TreatmentResponse.REMISSION
        )
    }
}