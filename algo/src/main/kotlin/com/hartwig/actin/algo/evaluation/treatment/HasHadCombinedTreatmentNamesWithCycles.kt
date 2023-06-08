package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.datamodel.ImmutableEvaluation
import com.hartwig.actin.clinical.datamodel.treatment.PriorTumorTreatment
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction

internal class HasHadCombinedTreatmentNamesWithCycles(
    private val treatmentNames: List<String>,
    private val minCycles: Int,
    private val maxCycles: Int
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val evaluationsByResult: Map<EvaluationResult, List<Evaluation>> = treatmentNames
            .map { treatmentName: String -> evaluatePriorTreatmentsMatchingName(record.clinical().priorTumorTreatments(), treatmentName) }
            .groupBy { it.result() }
        val builder: ImmutableEvaluation.Builder = EvaluationFactory.unrecoverable()
        return if (evaluationsByResult.containsKey(EvaluationResult.FAIL)) {
            val failEvaluations = evaluationsByResult[EvaluationResult.FAIL]!!
            builder.result(EvaluationResult.FAIL)
                .addAllFailSpecificMessages(getMessagesForEvaluations(failEvaluations) { obj: Evaluation -> obj.failSpecificMessages() })
                .addAllFailGeneralMessages(getMessagesForEvaluations(failEvaluations) { obj: Evaluation -> obj.failGeneralMessages() })
                .build()
        } else if (evaluationsByResult.containsKey(EvaluationResult.UNDETERMINED)) {
            val undeterminedEvaluations = evaluationsByResult[EvaluationResult.UNDETERMINED]!!
            builder.result(EvaluationResult.UNDETERMINED)
                .addAllUndeterminedSpecificMessages(getMessagesForEvaluations(undeterminedEvaluations) { obj: Evaluation -> obj.undeterminedSpecificMessages() })
                .addAllUndeterminedGeneralMessages(getMessagesForEvaluations(undeterminedEvaluations) { obj: Evaluation -> obj.undeterminedGeneralMessages() })
                .build()
        } else if (evaluationsByResult.containsKey(EvaluationResult.PASS) && evaluationsByResult.size == 1) {
            val passEvaluations = evaluationsByResult[EvaluationResult.PASS]!!
            builder.result(EvaluationResult.PASS)
                .addAllPassSpecificMessages(getMessagesForEvaluations(passEvaluations) { obj: Evaluation -> obj.passSpecificMessages() })
                .addPassGeneralMessages("Found matching treatments")
                .build()
        } else {
            throw IllegalStateException("At least one treatment name should be provided, and all results should be PASS, FAIL, or UNDETERMINED")
        }
    }

    private fun evaluatePriorTreatmentsMatchingName(priorTumorTreatments: List<PriorTumorTreatment>, treatmentName: String): Evaluation {
        val query = treatmentName.lowercase()
        val matchingPriorTreatments: Map<EvaluationResult, List<PriorTumorTreatment>> = priorTumorTreatments
            .filter { it.name().lowercase().contains(query) }
            .groupBy { prior: PriorTumorTreatment ->
                when (prior.cycles()) {
                    null -> EvaluationResult.UNDETERMINED
                    in minCycles..maxCycles -> EvaluationResult.PASS
                    else -> EvaluationResult.FAIL
                }
            }
        return if (matchingPriorTreatments.isEmpty()) {
            EvaluationFactory.fail("No prior treatments found matching $treatmentName", GENERAL_FAIL_MESSAGE)
        } else if (matchingPriorTreatments.containsKey(EvaluationResult.PASS)) {
            EvaluationFactory.pass(
                "Found matching treatments: " + formatTreatmentList(matchingPriorTreatments[EvaluationResult.PASS]!!, true),
                "Found matching treatments"
            )
        } else if (matchingPriorTreatments.containsKey(EvaluationResult.UNDETERMINED)) {
            EvaluationFactory.undetermined(
                "Unknown cycles for matching prior treatments: " + formatTreatmentList(
                    matchingPriorTreatments[EvaluationResult.UNDETERMINED]!!,
                    false
                ), "Unknown treatment cycles"
            )
        } else {
            EvaluationFactory.fail(
                String.format(
                    "Matching prior treatments did not have between %d and %d cycles: %s",
                    minCycles,
                    maxCycles,
                    formatTreatmentList(matchingPriorTreatments[EvaluationResult.FAIL]!!, true)
                ), GENERAL_FAIL_MESSAGE
            )
        }
    }

    companion object {
        private const val GENERAL_FAIL_MESSAGE = "No treatments with cycles"
        private fun formatTreatmentList(treatments: List<PriorTumorTreatment>, includeCycles: Boolean): String {
            return treatments.joinToString(", ") { prior: PriorTumorTreatment ->
                prior.name() + if (includeCycles) String.format(
                    " (%d cycles)",
                    prior.cycles()
                ) else ""
            }
        }

        private fun getMessagesForEvaluations(evaluations: List<Evaluation>, messageExtractor: (Evaluation) -> Set<String>): Set<String> {
            return evaluations.flatMap(messageExtractor).toSet()
        }
    }
}