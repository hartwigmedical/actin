package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.Format
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.treatment.Treatment
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentType
import com.hartwig.actin.datamodel.clinical.treatment.history.TreatmentHistoryEntry
import com.hartwig.actin.datamodel.clinical.treatment.history.TreatmentResponse

class HasHadClinicalBenefitFollowingSomeTreatmentOrCategoryOfTypes(
    private val targetTreatments: List<Treatment>? = null,
    private val category: TreatmentCategory? = null,
    private val types: Set<TreatmentType>? = null
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val history = record.oncologicalHistory
        val isMatchingTreatment: (Treatment) -> Boolean = when {
            targetTreatments != null -> { treatmentInHistory ->
                targetTreatments.any { it.name.equals(treatmentInHistory.name, ignoreCase = true) }
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
        val targetTreatmentsInHistory = history.filter { it.allTreatments().any(isMatchingTreatment::invoke) }
        val targetTreatmentsToResponseMap = targetTreatmentsInHistory.groupBy { it.treatmentHistoryDetails?.bestResponse }

        val treatmentsSimilarToTargetTreatment = targetTreatments?.let {
            history.filter { historyEntry ->
                targetTreatments.any { target ->
                    (historyEntry.matchesTypeFromSet(target.types()) != false) &&
                            historyEntry.categories().intersect(target.categories()).isNotEmpty()
                }
            }
        }

        val (treatmentsWithResponse, treatmentsWithStableDisease, treatmentsWithMixedResponse, treatmentsWithUncertainResponse) =
            listOf(BENEFIT_RESPONSE_SET, setOf(TreatmentResponse.STABLE_DISEASE), setOf(TreatmentResponse.MIXED), setOf(null))
                .map { responseSet -> targetTreatmentsToResponseMap.filterKeys { it in responseSet } }

        val benefitMessage = " objective benefit from treatment"
        val similarDrugMessage = "receive exact treatment but received similar drugs " +
                "(${treatmentsSimilarToTargetTreatment?.joinToString(",") { it.treatmentDisplay() }})"
        val hadSimilarTreatmentsWithPD = treatmentsSimilarToTargetTreatment.takeIf { !it.isNullOrEmpty() }
            ?.any { ProgressiveDiseaseFunctions.treatmentResultedInPD(it) == true }

        return when {
            targetTreatmentsToResponseMap.isEmpty() && hadSimilarTreatmentsWithPD == false -> {
                EvaluationFactory.undetermined("Clinical benefit from treatment${treatmentDisplay()} undetermined - did not $similarDrugMessage")
            }

            targetTreatmentsToResponseMap.isEmpty() && hadSimilarTreatmentsWithPD == true -> {
                EvaluationFactory.fail("Did not $similarDrugMessage with PD as best response")
            }

            targetTreatmentsToResponseMap.isEmpty() -> {
                EvaluationFactory.fail("Has not received treatment${treatmentDisplay()}")
            }

            treatmentsWithResponse.isNotEmpty() -> {
                EvaluationFactory.pass("Has had$benefitMessage${treatmentDisplay(treatmentsFromResponseMap(treatmentsWithResponse))}")
            }

            treatmentsWithStableDisease.isNotEmpty() -> {
                EvaluationFactory.warn("Uncertain$benefitMessage${treatmentDisplay(treatmentsFromResponseMap(treatmentsWithStableDisease))} " +
                        "(best response: stable disease)"
                )
            }

            treatmentsWithMixedResponse.isNotEmpty() -> {
                EvaluationFactory.warn("Uncertain$benefitMessage${treatmentDisplay(treatmentsFromResponseMap(treatmentsWithMixedResponse))} " +
                        "(best response: mixed)"
                )
            }

            treatmentsWithUncertainResponse.isNotEmpty() -> {
                EvaluationFactory.undetermined(
                    "Undetermined$benefitMessage${treatmentDisplay(treatmentsFromResponseMap(treatmentsWithUncertainResponse))}"
                )
            }

            else -> {
                EvaluationFactory.fail("No$benefitMessage${treatmentDisplay()}")
            }
        }
    }

    private fun treatmentDisplay(treatments: List<Treatment>? = targetTreatments): String {
        return when {
            targetTreatments != null && treatments != null -> " with ${Format.concatItemsWithOr(treatments)}"
            category != null && types != null -> " of category ${category.display()} and type(s) ${Format.concatItemsWithOr(types)}"
            category != null -> " of category ${category.display()}"
            else -> ""
        }
    }

    private fun treatmentsFromResponseMap(responseMap: Map<TreatmentResponse?, List<TreatmentHistoryEntry>>): List<Treatment> {
        return responseMap.values.flatten().flatMap { it.allTreatments() }
    }

    companion object {
        private val BENEFIT_RESPONSE_SET = setOf(
            TreatmentResponse.PARTIAL_RESPONSE, TreatmentResponse.COMPLETE_RESPONSE, TreatmentResponse.REMISSION
        )
    }
}