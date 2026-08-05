package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.EvaluationLabels
import com.hartwig.actin.algo.evaluation.util.Format
import com.hartwig.actin.clinical.interpretation.ProgressiveDiseaseFunctions
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
    private val types: Set<TreatmentType>? = null,
    private val labels: EvaluationLabels.Treatment
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

        val (treatmentsWithResponse, otherResponses) = targetTreatmentsToResponseMap.entries.partition { it.key in treatmentResponses }
            .let { (withResponse, otherResponse) -> withResponse.flatMap { it.value } to otherResponse.mapNotNull { it.key } }

        val responseMessage =
            if (evaluateClinicalBenefit) {
                labels.hasHadTreatmentResponseFollowingSomeTreatmentOrCategoryOfTypesResponseMessageObjectiveBenefit()
            } else {
                labels.hasHadTreatmentResponseFollowingSomeTreatmentOrCategoryOfTypesResponseMessageOther(
                    Format.concatWithCommaAndOr(treatmentResponses.map { it.display() })
                )
            }
        val similarTreatmentsDisplay = treatmentsSimilarToTargetTreatment?.joinToString(",") { it.treatmentDisplay() }.toString()
        val similarDrugMessage =
            labels.hasHadTreatmentResponseFollowingSomeTreatmentOrCategoryOfTypesSimilarDrugMessage(similarTreatmentsDisplay)
        val hadSimilarTreatmentsWithPD = treatmentsSimilarToTargetTreatment.takeIf { !it.isNullOrEmpty() }
            ?.any { ProgressiveDiseaseFunctions.treatmentResultedInPD(it) == true }

        return when {
            !evaluateClinicalBenefit && otherResponses.isNotEmpty() && treatmentsWithResponse.isNotEmpty() -> {
                EvaluationFactory.warn(
                    labels.hasHadTreatmentResponseFollowingSomeTreatmentOrCategoryOfTypesWarnUncertainWithOther(
                        responseMessage, treatmentDisplay(), Format.concatLowercaseWithCommaAndAnd(otherResponses.map { it.display() })
                    )
                )
            }

            evaluateClinicalBenefit && targetTreatmentsToResponseMap.isEmpty() && hadSimilarTreatmentsWithPD == false -> {
                EvaluationFactory.undetermined(
                    labels.hasHadTreatmentResponseFollowingSomeTreatmentOrCategoryOfTypesUndeterminedClinicalBenefit(
                        treatmentDisplay(), similarDrugMessage
                    )
                )
            }

            evaluateClinicalBenefit && targetTreatmentsToResponseMap.isEmpty() && hadSimilarTreatmentsWithPD == true -> {
                EvaluationFactory.fail(
                    labels.hasHadTreatmentResponseFollowingSomeTreatmentOrCategoryOfTypesFailSimilarWithPd(similarDrugMessage)
                )
            }

            targetTreatmentsToResponseMap.isEmpty() -> {
                EvaluationFactory.fail(
                    labels.hasHadTreatmentResponseFollowingSomeTreatmentOrCategoryOfTypesFailNotReceived(treatmentDisplay())
                )
            }

            treatmentsWithResponse.isNotEmpty() -> {
                EvaluationFactory.pass(
                    labels.hasHadTreatmentResponseFollowingSomeTreatmentOrCategoryOfTypesPass(
                        responseMessage, treatmentDisplay(treatmentsInHistory(treatmentsWithResponse))
                    )
                )
            }

            evaluateClinicalBenefit && TreatmentResponse.STABLE_DISEASE in targetTreatmentsToResponseMap -> {
                EvaluationFactory.warn(
                    labels.hasHadTreatmentResponseFollowingSomeTreatmentOrCategoryOfTypesWarnStableDisease(
                        responseMessage, treatmentDisplay(treatmentsInHistory(targetTreatmentsToResponseMap[TreatmentResponse.STABLE_DISEASE]))
                    )
                )
            }

            evaluateClinicalBenefit && TreatmentResponse.MIXED in targetTreatmentsToResponseMap -> {
                EvaluationFactory.warn(
                    labels.hasHadTreatmentResponseFollowingSomeTreatmentOrCategoryOfTypesWarnMixed(
                        responseMessage, treatmentDisplay(treatmentsInHistory(targetTreatmentsToResponseMap[TreatmentResponse.MIXED]))
                    )
                )
            }

            targetTreatmentsToResponseMap.containsKey(null) -> {
                EvaluationFactory.undetermined(
                    labels.hasHadTreatmentResponseFollowingSomeTreatmentOrCategoryOfTypesUndeterminedUnknownResponse(
                        responseMessage, treatmentDisplay(treatmentsInHistory(targetTreatmentsToResponseMap[null]))
                    )
                )
            }

            else -> {
                EvaluationFactory.fail(
                    labels.hasHadTreatmentResponseFollowingSomeTreatmentOrCategoryOfTypesFailNoResponse(responseMessage, treatmentDisplay())
                )
            }
        }
    }

    private fun treatmentDisplay(treatments: List<Treatment>? = targetTreatments): String {
        return when {
            targetTreatments != null && treatments != null ->
                labels.hasHadTreatmentResponseFollowingSomeTreatmentOrCategoryOfTypesDisplayWithTreatments(Format.concatItemsWithOr(treatments))

            category != null && types != null ->
                labels.hasHadTreatmentResponseFollowingSomeTreatmentOrCategoryOfTypesDisplayOfCategoryAndTypes(
                    category.display(), Format.concatItemsWithOr(types)
                )

            category != null -> labels.hasHadTreatmentResponseFollowingSomeTreatmentOrCategoryOfTypesDisplayOfCategory(category.display())
            else -> ""
        }
    }

    private fun treatmentsInHistory(history: List<TreatmentHistoryEntry>?) = history?.flatMap { it.allTreatments() }
}