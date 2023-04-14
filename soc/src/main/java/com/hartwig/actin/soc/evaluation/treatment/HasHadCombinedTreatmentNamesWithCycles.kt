package com.hartwig.actin.soc.evaluation.treatment

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import java.util.*
import java.util.function.Function
import java.util.function.Predicate

internal class HasHadCombinedTreatmentNamesWithCycles(private val treatmentNames: List<String>, private val minCycles: Int, private val maxCycles: Int) : EvaluationFunction {
    fun evaluate(record: PatientRecord): Evaluation {
        val evaluationsByResult: Map<EvaluationResult, List<Evaluation>> = treatmentNames.stream()
                .map { treatmentName: String -> evaluatePriorTreatmentsMatchingName(record.clinical().priorTumorTreatments(), treatmentName) }
                .collect(Collectors.groupingBy<Evaluation, EvaluationResult>(Function { obj: Evaluation -> obj.result() }))
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
        val query = treatmentName.lowercase(Locale.getDefault())
        val matchingPriorTreatments: Map<EvaluationResult, List<PriorTumorTreatment>> = priorTumorTreatments.stream()
                .filter(Predicate<PriorTumorTreatment> { prior: PriorTumorTreatment -> prior.name().lowercase(Locale.getDefault()).contains(query) })
                .collect(Collectors.groupingBy<PriorTumorTreatment, EvaluationResult>(Function<PriorTumorTreatment, EvaluationResult> { prior: PriorTumorTreatment ->
                    Optional.ofNullable<Int>(prior.cycles())
                            .map<EvaluationResult>(Function<Int, EvaluationResult> { cycles: Int -> if (cycles >= minCycles && cycles <= maxCycles) EvaluationResult.PASS else EvaluationResult.FAIL })
                            .orElse(EvaluationResult.UNDETERMINED)
                }))
        return if (matchingPriorTreatments.isEmpty()) {
            EvaluationFactory.fail("No prior treatments found matching $treatmentName", GENERAL_FAIL_MESSAGE)
        } else if (matchingPriorTreatments.containsKey(EvaluationResult.PASS)) {
            EvaluationFactory.pass(
                    "Found matching treatments: " + formatTreatmentList(matchingPriorTreatments[EvaluationResult.PASS]!!, true),
                    "Found matching treatments")
        } else if (matchingPriorTreatments.containsKey(EvaluationResult.UNDETERMINED)) {
            EvaluationFactory.undetermined("Unknown cycles for matching prior treatments: " + formatTreatmentList(
                    matchingPriorTreatments[EvaluationResult.UNDETERMINED]!!,
                    false), "Unknown treatment cycles")
        } else {
            EvaluationFactory.fail(String.format("Matching prior treatments did not have between %d and %d cycles: %s",
                    minCycles,
                    maxCycles,
                    formatTreatmentList(matchingPriorTreatments[EvaluationResult.FAIL]!!, true)), GENERAL_FAIL_MESSAGE)
        }
    }

    companion object {
        private const val GENERAL_FAIL_MESSAGE = "No treatments with cycles"
        private fun formatTreatmentList(treatments: List<PriorTumorTreatment>, includeCycles: Boolean): String {
            return treatments.stream()
                    .map(Function<PriorTumorTreatment, String> { prior: PriorTumorTreatment -> prior.name() + if (includeCycles) String.format(" (%d cycles)", prior.cycles()) else "" })
                    .collect(Collectors.joining(", "))
        }

        private fun getMessagesForEvaluations(evaluations: List<Evaluation>, messageExtractor: Function<Evaluation, Set<String>>): Set<String> {
            return evaluations.stream().map(messageExtractor).flatMap { obj: Set<String> -> obj.stream() }.collect(Collectors.toSet<String>())
        }
    }
}