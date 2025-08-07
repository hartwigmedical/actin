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

class HasHadTreatmentResponseFollowingSomeTreatmentOrCategoryOfTypes(
    private val treatmentResponses: Set<TreatmentResponse>,
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
                    historyEntry.matchesTypeFromSet(target.types()) != false &&
                            historyEntry.categories().intersect(target.categories()).isNotEmpty()
                }
            }
        }

        val evaluateClinicalBenefit = treatmentResponses == TreatmentResponse.BENEFIT_RESPONSES

        val treatmentsWithResponse = targetTreatmentsToResponseMap.filterKeys { it in treatmentResponses }.values.flatten()
        val otherResponses = targetTreatmentsToResponseMap.filterKeys { it !in treatmentResponses }.keys.filterNotNull()

        val responseMessage =
            if (evaluateClinicalBenefit) " objective benefit from treatment" else " ${Format.concatWithCommaAndOr(treatmentResponses.map { it.display() })} from treatment"
        val similarDrugMessage = "receive exact treatment but received similar drugs " +
                "(${treatmentsSimilarToTargetTreatment?.joinToString(",") { it.treatmentDisplay() }})"
        val hadSimilarTreatmentsWithPD = treatmentsSimilarToTargetTreatment.takeIf { !it.isNullOrEmpty() }
            ?.any { ProgressiveDiseaseFunctions.treatmentResultedInPD(it) == true }

        return when {
            !evaluateClinicalBenefit && otherResponses.isNotEmpty() && treatmentsWithResponse.isNotEmpty() -> {
                EvaluationFactory.warn(
                    "Uncertain$responseMessage${treatmentDisplay()} - also had ${
                        Format.concatLowercaseWithCommaAndAnd(
                            otherResponses.map { it.display() })
                    }"
                )
            }

            evaluateClinicalBenefit && targetTreatmentsToResponseMap.isEmpty() && hadSimilarTreatmentsWithPD == false -> {
                EvaluationFactory.undetermined("Clinical benefit from treatment${treatmentDisplay()} undetermined - did not $similarDrugMessage")
            }

            evaluateClinicalBenefit && targetTreatmentsToResponseMap.isEmpty() && hadSimilarTreatmentsWithPD == true -> {
                EvaluationFactory.fail("Did not $similarDrugMessage with PD as best response")
            }

            targetTreatmentsToResponseMap.isEmpty() -> {
                EvaluationFactory.fail("Has not received treatment${treatmentDisplay()}")
            }

            treatmentsWithResponse.isNotEmpty() -> {
                EvaluationFactory.pass("Has had$responseMessage${treatmentDisplay(treatmentsInHistory(treatmentsWithResponse))}")
            }

            evaluateClinicalBenefit && TreatmentResponse.STABLE_DISEASE in targetTreatmentsToResponseMap -> {
                EvaluationFactory.warn(
                    "Uncertain$responseMessage" +
                            "${treatmentDisplay(treatmentsInHistory(targetTreatmentsToResponseMap[TreatmentResponse.STABLE_DISEASE]))} " +
                            "(best response: stable disease)"
                )
            }

            evaluateClinicalBenefit && TreatmentResponse.MIXED in targetTreatmentsToResponseMap -> {
                EvaluationFactory.warn(
                    "Uncertain$responseMessage" +
                            "${treatmentDisplay(treatmentsInHistory(targetTreatmentsToResponseMap[TreatmentResponse.MIXED]))} " +
                            "(best response: mixed)"
                )
            }

            targetTreatmentsToResponseMap.containsKey(null) -> {
                EvaluationFactory.undetermined(
                    "Undetermined$responseMessage${treatmentDisplay(treatmentsInHistory(targetTreatmentsToResponseMap[null]))}"
                )
            }

            else -> {
                EvaluationFactory.fail("No$responseMessage${treatmentDisplay()}")
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

    private fun treatmentsInHistory(history: List<TreatmentHistoryEntry>?) = history?.flatMap { it.allTreatments() }
}