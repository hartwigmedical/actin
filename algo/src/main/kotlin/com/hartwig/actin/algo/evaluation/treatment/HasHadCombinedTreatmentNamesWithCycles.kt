package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.treatment.TreatmentHistoryEntryFunctions.portionOfTreatmentHistoryEntryMatchingPredicate
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.treatment.Treatment
import com.hartwig.actin.datamodel.clinical.treatment.history.TreatmentHistoryEntry

class HasHadCombinedTreatmentNamesWithCycles(
    private val treatments: List<Treatment>,
    private val minCycles: Int,
    private val maxCycles: Int
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
                    in minCycles..maxCycles -> EvaluationResult.PASS
                    else -> EvaluationResult.FAIL
                }
            }
        return if (matchingHistoryEntries.isEmpty()) {
            EvaluationFactory.fail("No prior treatments found matching $treatmentName and between $minCycles and $maxCycles cycles")
        } else if (matchingHistoryEntries.containsKey(EvaluationResult.PASS)) {
            EvaluationFactory.pass(
                "Found matching treatments (${formatTreatmentList(matchingHistoryEntries[EvaluationResult.PASS]!!, true)}" +
                        " and between $minCycles and $maxCycles cycles"
            )
        } else if (matchingHistoryEntries.containsKey(EvaluationResult.UNDETERMINED)) {
            EvaluationFactory.undetermined(
                "Unknown cycles for matching treatments: " + formatTreatmentList(
                    matchingHistoryEntries[EvaluationResult.UNDETERMINED]!!,
                    false
                )
            )
        } else {
            EvaluationFactory.fail(
                String.format(
                    "Matching treatments did not have between %d and %d cycles: %s",
                    minCycles,
                    maxCycles,
                    formatTreatmentList(matchingHistoryEntries[EvaluationResult.FAIL]!!, true)
                )
            )
        }
    }

    companion object {
        private fun formatTreatmentList(treatmentHistoryEntries: List<TreatmentHistoryEntry>, includeCycles: Boolean): String {
            return treatmentHistoryEntries.joinToString(", ") { entry ->
                val cycleString = if (includeCycles) " (${entry.treatmentHistoryDetails?.cycles} cycles)" else ""
                entry.treatments.joinToString("+") { it.display() } + cycleString

            }
        }

        private fun getMessagesForEvaluations(evaluations: List<Evaluation>, messageExtractor: (Evaluation) -> Set<String>): Set<String> {
            return evaluations.flatMap(messageExtractor).toSet()
        }
    }
}