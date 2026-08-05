package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.EvaluationLabels
import com.hartwig.actin.algo.evaluation.treatment.TreatmentHistoryEntryFunctions.portionOfTreatmentHistoryEntryMatchingPredicate
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.algo.EvaluationMessage
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.treatment.Treatment
import com.hartwig.actin.datamodel.clinical.treatment.history.TreatmentHistoryEntry

class HasHadCombinedTreatmentNamesWithCycles(
    private val treatments: List<Treatment>,
    private val minCycles: Int,
    private val maxCycles: Int?,
    private val labels: EvaluationLabels.Treatment
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val evaluationsByResult: Map<EvaluationResult, List<Evaluation>> = treatments
            .map { treatment -> evaluatePriorTreatmentsMatchingName(record.oncologicalHistory, treatment.name) }
            .groupBy { it.result }

        return when {
            evaluationsByResult.containsKey(EvaluationResult.FAIL) -> {
                val failEvaluations = evaluationsByResult[EvaluationResult.FAIL]!!
                Evaluation(
                    result = EvaluationResult.FAIL,
                    recoverable = false,
                    failMessages = getMessagesForEvaluations(failEvaluations, Evaluation::failMessages)
                )
            }

            evaluationsByResult.containsKey(EvaluationResult.UNDETERMINED) -> {
                val undeterminedEvaluations = evaluationsByResult[EvaluationResult.UNDETERMINED]!!
                Evaluation(
                    result = EvaluationResult.UNDETERMINED,
                    recoverable = false,
                    undeterminedMessages = getMessagesForEvaluations(
                        undeterminedEvaluations,
                        Evaluation::undeterminedMessages
                    )
                )
            }

            evaluationsByResult.containsKey(EvaluationResult.WARN) -> {
                val warnEvaluations = evaluationsByResult[EvaluationResult.WARN]!!
                Evaluation(
                    result = EvaluationResult.WARN,
                    recoverable = false,
                    warnMessages = getMessagesForEvaluations(
                        warnEvaluations,
                        Evaluation::warnMessages
                    )
                )
            }

            evaluationsByResult.containsKey(EvaluationResult.PASS) && evaluationsByResult.size == 1 -> {
                val passEvaluations = evaluationsByResult[EvaluationResult.PASS]!!
                Evaluation(
                    result = EvaluationResult.PASS,
                    recoverable = false,
                    passMessages = getMessagesForEvaluations(passEvaluations, Evaluation::passMessages)
                )
            }

            else -> {
                throw IllegalStateException("At least one treatment name should be provided, and all results should be PASS, FAIL, or UNDETERMINED")
            }
        }
    }

    private fun evaluatePriorTreatmentsMatchingName(treatmentHistory: List<TreatmentHistoryEntry>, treatmentName: String): Evaluation {
        val query = treatmentName.lowercase()
        val matchingHistoryEntries: Map<EvaluationResult, List<TreatmentHistoryEntry>> = treatmentHistory.mapNotNull { entry ->
            portionOfTreatmentHistoryEntryMatchingPredicate(entry) { treatment ->
                (treatment.synonyms + treatment.name).any { it.lowercase() == query }
            }
        }
            .groupBy {
                when (it.treatmentHistoryDetails?.cycles) {
                    null -> EvaluationResult.UNDETERMINED
                    in minCycles..(maxCycles ?: Int.MAX_VALUE) -> EvaluationResult.PASS
                    else -> EvaluationResult.WARN
                }
            }

        return if (matchingHistoryEntries.isEmpty()) {
            EvaluationFactory.fail(labels.hasHadCombinedTreatmentNamesWithCyclesFailNoMatching(treatmentName, cyclesRequirementDescription()))
        } else if (matchingHistoryEntries.containsKey(EvaluationResult.PASS)) {
            EvaluationFactory.pass(
                labels.hasHadCombinedTreatmentNamesWithCyclesPass(
                    formatTreatmentList(matchingHistoryEntries[EvaluationResult.PASS]!!, true, labels),
                    cyclesRequirementDescription()
                )
            )
        } else if (matchingHistoryEntries.containsKey(EvaluationResult.UNDETERMINED)) {
            EvaluationFactory.undetermined(
                labels.hasHadCombinedTreatmentNamesWithCyclesUndetermined(
                    formatTreatmentList(matchingHistoryEntries[EvaluationResult.UNDETERMINED]!!, false, labels)
                )
            )
        } else {
            EvaluationFactory.warn(
                labels.hasHadCombinedTreatmentNamesWithCyclesWarn(
                    cyclesRequirementDescription(),
                    formatTreatmentList(matchingHistoryEntries[EvaluationResult.WARN]!!, true, labels)
                )
            )
        }
    }

    private fun cyclesRequirementDescription(): String {
        return if (maxCycles != null) {
            labels.hasHadCombinedTreatmentNamesWithCyclesDescriptionBetween(minCycles, maxCycles)
        } else {
            labels.hasHadCombinedTreatmentNamesWithCyclesDescriptionAtLeast(minCycles)
        }
    }

    companion object {
        private fun formatTreatmentList(
            treatmentHistoryEntries: List<TreatmentHistoryEntry>,
            includeCycles: Boolean,
            labels: EvaluationLabels.Treatment
        ): String {
            return treatmentHistoryEntries.joinToString(", ") { entry ->
                val cycleString = if (includeCycles) {
                    labels.hasHadCombinedTreatmentNamesWithCyclesCycleSuffix(entry.treatmentHistoryDetails?.cycles.toString())
                } else ""
                entry.treatments.joinToString("+") { it.display() } + cycleString

            }
        }

        private fun getMessagesForEvaluations(
            evaluations: List<Evaluation>,
            messageExtractor: (Evaluation) -> Set<EvaluationMessage>
        ): Set<EvaluationMessage> {
            return evaluations.flatMap(messageExtractor).toSet()
        }
    }
}