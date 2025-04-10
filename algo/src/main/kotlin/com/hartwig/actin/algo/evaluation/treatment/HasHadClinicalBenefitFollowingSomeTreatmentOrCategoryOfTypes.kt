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
                    historyEntry.matchesTypeFromSet(target.types()) != false &&
                            historyEntry.categories().intersect(target.categories()).isNotEmpty()
                }
            }
        }

        val treatmentsWithResponse = targetTreatmentsToResponseMap.filterKeys { it in BENEFIT_RESPONSE_SET }.values.flatten()
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
                EvaluationFactory.pass("Has had$benefitMessage${treatmentDisplay(treatmentsInHistory(treatmentsWithResponse))}")
            }

            TreatmentResponse.STABLE_DISEASE in targetTreatmentsToResponseMap -> {
                EvaluationFactory.warn(
                    "Uncertain$benefitMessage" +
                            "${treatmentDisplay(treatmentsInHistory(targetTreatmentsToResponseMap[TreatmentResponse.STABLE_DISEASE]))} " +
                            "(best response: stable disease)"
                )
            }

            TreatmentResponse.MIXED in targetTreatmentsToResponseMap -> {
                EvaluationFactory.warn(
                    "Uncertain$benefitMessage" +
                            "${treatmentDisplay(treatmentsInHistory(targetTreatmentsToResponseMap[TreatmentResponse.MIXED]))} " +
                            "(best response: mixed)"
                )
            }

            targetTreatmentsToResponseMap.containsKey(null) -> {
                EvaluationFactory.undetermined(
                    "Undetermined$benefitMessage${treatmentDisplay(treatmentsInHistory(targetTreatmentsToResponseMap[null]))}"
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

    private fun treatmentsInHistory(history: List<TreatmentHistoryEntry>?) = history?.flatMap { it.allTreatments() }

    companion object {
        private val BENEFIT_RESPONSE_SET = setOf(
            TreatmentResponse.PARTIAL_RESPONSE,
            TreatmentResponse.NEAR_COMPLETE_RESPONSE,
            TreatmentResponse.COMPLETE_RESPONSE,
            TreatmentResponse.REMISSION
        )
    }
}