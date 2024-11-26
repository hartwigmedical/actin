package com.hartwig.actin.algo.matchcomparison

import com.hartwig.actin.algo.matchcomparison.DifferenceExtractionUtil.extractDifferences
import com.hartwig.actin.algo.matchcomparison.DifferenceExtractionUtil.mapKeyDifferences
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.trial.CriterionReference
import com.hartwig.actin.datamodel.trial.Eligibility
import com.hartwig.actin.datamodel.trial.EligibilityFunction
import org.apache.logging.log4j.LogManager

object EvaluationComparison {

    private const val INDENT_WIDTH = 2
    private val LOGGER = LogManager.getLogger(EvaluationComparison::class.java)

    fun determineEvaluationDifferences(
        oldEvaluations: Map<Eligibility, Evaluation>, newEvaluations: Map<Eligibility, Evaluation>, id: String, indent: Int
    ): EvaluationDifferences {
        val oldEvaluationsByCriteria = evaluationsByCriteria(oldEvaluations)
        val newEvaluationsByCriteria = evaluationsByCriteria(newEvaluations)
        val detailIndent = indent + INDENT_WIDTH
        val modifiedKeys = mapKeyDifferences(oldEvaluationsByCriteria, newEvaluationsByCriteria, "evaluations") { it.toString() }

        return oldEvaluationsByCriteria.map { (references, oldFunctionAndEvaluation) ->
            val (oldFunction, oldEvaluation) = oldFunctionAndEvaluation
            val newFunctionAndEvaluation = newEvaluationsByCriteria[references]
            if (newFunctionAndEvaluation == null) {
                EvaluationDifferences.create()
            } else {
                val (newFunction, newEvaluation) = newFunctionAndEvaluation
                val criteriaId = "ID $id, Criteria ${references.joinToString(", ") { it.id }}"

                val resultDifferences = extractDifferences(
                    oldEvaluation, newEvaluation, mapOf("result for $criteriaId" to Evaluation::result)
                )
                if (resultDifferences.isNotEmpty()) {
                    resultDifferences.forEach(LOGGER::warn)
                    LOGGER.warn("  Old function: $oldFunction")
                    LOGGER.warn("  New function: $newFunction")
                }
                val recoverableDifferences = extractDifferences(oldEvaluation, newEvaluation, mapOf("recoverable" to Evaluation::recoverable))
                val messageDifferences = extractMessageDifferences(oldEvaluation, newEvaluation)
                val functionDifferences = EligibilityFunctionComparison.determineEligibilityFunctionDifferences(oldFunction, newFunction)

                if (resultDifferences.isNotEmpty() || recoverableDifferences.isNotEmpty() || messageDifferences.isNotEmpty()
                    || functionDifferences.isNotEmpty()
                ) {
                    logDebug("Differences found for $criteriaId:", indent)
                    listOf(resultDifferences, recoverableDifferences, messageDifferences, functionDifferences.asList()).flatten()
                        .forEach { logDebug(it, detailIndent) }
                }
                EvaluationDifferences.create(
                    resultDifferences = resultDifferences,
                    recoverableDifferences = recoverableDifferences,
                    messageDifferences = messageDifferences,
                    functionDifferences = functionDifferences
                )
            }
        }.fold(EvaluationDifferences.create(mapKeyDifferences = modifiedKeys)) { acc, other -> acc + other }
    }

    private fun evaluationsByCriteria(evaluations: Map<Eligibility, Evaluation>): Map<Set<CriterionReference>, Pair<EligibilityFunction, Evaluation>> {
        return evaluations.map { (eligibility, evaluation) -> eligibility.references to Pair(eligibility.function, evaluation) }.toMap()
    }

    private fun extractMessageDifferences(old: Evaluation, new: Evaluation): List<String> {
        val oldMessages = getGeneralMessagesForEvaluation(old)
        val newMessages = getGeneralMessagesForEvaluation(new)
        val removedMessages = oldMessages - newMessages
        val addedMessages = newMessages - oldMessages
        return if (removedMessages.map(String::lowercase) == addedMessages.map(String::lowercase)) {
            emptyList()
        } else {
            removedMessages.map { "- $it" } + addedMessages.map { "+ $it" }
        }
    }

    private fun getGeneralMessagesForEvaluation(evaluation: Evaluation): Set<String> {
        return when (evaluation.result) {
            EvaluationResult.PASS -> evaluation.passGeneralMessages
            EvaluationResult.NOT_EVALUATED -> evaluation.passGeneralMessages
            EvaluationResult.WARN -> evaluation.warnGeneralMessages
            EvaluationResult.UNDETERMINED -> evaluation.undeterminedGeneralMessages
            EvaluationResult.FAIL -> evaluation.failGeneralMessages
            else -> emptySet()
        }
    }

    private fun logDebug(message: String, indent: Int = 0) {
        LOGGER.debug(" ".repeat(indent) + message)
    }
}