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
                    failSpecificMessages = getMessagesForEvaluations(failEvaluations, Evaluation::failSpecificMessages),
                    failGeneralMessages = getMessagesForEvaluations(failEvaluations, Evaluation::failGeneralMessages)
                )
            }

            evaluationsByResult.containsKey(EvaluationResult.UNDETERMINED) -> {
                val undeterminedEvaluations = evaluationsByResult[EvaluationResult.UNDETERMINED]!!
                Evaluation(
                    result = EvaluationResult.UNDETERMINED,
                    recoverable = false,
                    undeterminedSpecificMessages = getMessagesForEvaluations(
                        undeterminedEvaluations,
                        Evaluation::undeterminedSpecificMessages
                    ),
                    undeterminedGeneralMessages = getMessagesForEvaluations(
                        undeterminedEvaluations,
                        Evaluation::undeterminedGeneralMessages
                    )
                )
            }

            evaluationsByResult.containsKey(EvaluationResult.PASS) && evaluationsByResult.size == 1 -> {
                val passEvaluations = evaluationsByResult[EvaluationResult.PASS]!!
                Evaluation(
                    result = EvaluationResult.PASS,
                    recoverable = false,
                    passSpecificMessages = getMessagesForEvaluations(passEvaluations, Evaluation::passSpecificMessages),
                    passGeneralMessages = setOf("Found matching treatments")
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
            EvaluationFactory.fail("No prior treatments found matching $treatmentName", GENERAL_FAIL_MESSAGE)
        } else if (matchingHistoryEntries.containsKey(EvaluationResult.PASS)) {
            EvaluationFactory.pass(
                "Found matching treatments: " + formatTreatmentList(matchingHistoryEntries[EvaluationResult.PASS]!!, true),
                "Found matching treatments"
            )
        } else if (matchingHistoryEntries.containsKey(EvaluationResult.UNDETERMINED)) {
            EvaluationFactory.undetermined(
                "Unknown cycles for matching prior treatments: " + formatTreatmentList(
                    matchingHistoryEntries[EvaluationResult.UNDETERMINED]!!,
                    false
                ), "Unknown treatment cycles"
            )
        } else {
            EvaluationFactory.fail(
                String.format(
                    "Matching prior treatments did not have between %d and %d cycles: %s",
                    minCycles,
                    maxCycles,
                    formatTreatmentList(matchingHistoryEntries[EvaluationResult.FAIL]!!, true)
                ), GENERAL_FAIL_MESSAGE
            )
        }
    }

    companion object {
        private const val GENERAL_FAIL_MESSAGE = "No treatments with cycles"

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