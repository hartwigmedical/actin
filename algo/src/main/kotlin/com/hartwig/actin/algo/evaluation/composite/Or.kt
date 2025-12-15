package com.hartwig.actin.algo.evaluation.composite

import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import kotlin.collections.any

class Or(private val functions: List<EvaluationFunction>) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val evaluationsByResult = functions.map { it.evaluate(record) }.distinct().groupBy(Evaluation::result)
        val bestResult = evaluationsByResult.keys.maxOrNull()
            ?: throw IllegalStateException("Could not determine OR result for functions: $functions")

        val finalResult =
            if (bestResult == EvaluationResult.UNDETERMINED && undeterminedWithMissingMolecularResultAndWarnWithMolecularEvent(
                    evaluationsByResult
                )
            ) {
                EvaluationResult.WARN
            } else bestResult

        val additionalEvaluations = if (finalResult != EvaluationResult.PASS) {
            listOf(
                EvaluationResult.WARN,
                EvaluationResult.UNDETERMINED
            ).flatMap { result ->
                evaluationsByResult[result]?.filter { it.exclusionMolecularEvents.isNotEmpty() || it.inclusionMolecularEvents.isNotEmpty() }
                    ?: emptyList()
            }
        } else emptyList()

        val evaluations = evaluationsByResult[finalResult]!! + additionalEvaluations
        val recoverable = evaluations.any(Evaluation::recoverable)
        val filteredEvaluations =
            if (finalResult == EvaluationResult.FAIL && recoverable) evaluations.filter { it.recoverable } else evaluations

        return filteredEvaluations.fold(Evaluation(finalResult, recoverable), Evaluation::addMessagesAndEvents)
    }

    private fun undeterminedWithMissingMolecularResultAndWarnWithMolecularEvent(
        evaluationsByResult: Map<EvaluationResult, List<Evaluation>>
    ): Boolean {
        val undeterminedWithMissingMolecularResult =
            evaluationsByResult[EvaluationResult.UNDETERMINED]?.any { it.isMissingMolecularResultForEvaluation } ?: false
        val warnWithMolecularEvent =
            evaluationsByResult[EvaluationResult.WARN]?.any {
                it.exclusionMolecularEvents.isNotEmpty() ||
                        it.inclusionMolecularEvents.isNotEmpty()
            } ?: false
        return undeterminedWithMissingMolecularResult && warnWithMolecularEvent
    }
}