package com.hartwig.actin.report.interpretation

import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.trial.CriterionReference
import com.hartwig.actin.trial.sort.CriterionReferenceComparator

private val PLUS_WITHOUT_SURROUNDING_SPACES_REGEX = "(\\S)\\+(\\S)".toRegex()

object EvaluationInterpreter {

    fun interpretForDetailedTrialMatching(
        evaluations: Map<CriterionReference, Evaluation>,
        interpretFailOnly: Boolean
    ): List<EvaluationInterpretation> {
        return if (interpretFailOnly) {
            createInterpretationsOfType(evaluations, EvaluationResult.FAIL)
        } else {
            listOf(
                EvaluationResult.FAIL,
                EvaluationResult.WARN,
                EvaluationResult.UNDETERMINED,
                EvaluationResult.PASS,
                EvaluationResult.NOT_EVALUATED
            ).flatMap { createInterpretationsOfType(evaluations, it) }
        }
    }

    private fun createInterpretationsOfType(
        evaluations: Map<CriterionReference, Evaluation>,
        resultToRender: EvaluationResult
    ): List<EvaluationInterpretation> {
        return evaluations.keys.asSequence().sortedWith(CriterionReferenceComparator()).mapNotNull { key ->
            evaluations.entries.find { it.key == key }
        }.filter { it.value.result == resultToRender }
            .map {
                EvaluationInterpretation(
                    rule = it.key.id,
                    reference = it.key.text,
                    entriesPerResult = createEvaluationInterpretationMap(it.value)
                )
            }.toList()
    }

    private fun createEvaluationInterpretationMap(evaluation: Evaluation): Map<EvaluationResult, EvaluationEntry> {
        return when (evaluation.result) {
            EvaluationResult.PASS, EvaluationResult.NOT_EVALUATED -> {
                mapOf(Pair(evaluation.result, generateEntry(evaluation, evaluation.passMessages)))
            }

            EvaluationResult.WARN -> {
                listOfNotNull(
                    Pair(evaluation.result, generateEntry(evaluation, evaluation.warnMessages)),
                    evaluation.undeterminedMessages.takeIf { it.isNotEmpty() }?.let {
                        Pair(
                            EvaluationResult.UNDETERMINED,
                            generateEntry(EvaluationResult.UNDETERMINED, evaluation.undeterminedMessages)
                        )
                    }
                ).toMap()
            }

            EvaluationResult.UNDETERMINED -> {
                mapOf(Pair(evaluation.result, generateEntry(evaluation, evaluation.undeterminedMessages)))
            }

            EvaluationResult.FAIL -> {
                listOfNotNull(
                    Pair(evaluation.result, generateEntry(evaluation, evaluation.failMessages)),
                    evaluation.warnMessages.takeIf { it.isNotEmpty() && evaluation.recoverable }?.let {
                        Pair(EvaluationResult.WARN, generateEntry(EvaluationResult.WARN, evaluation.warnMessages))
                    },
                    evaluation.undeterminedMessages.takeIf { it.isNotEmpty() && evaluation.recoverable }?.let {
                        Pair(
                            EvaluationResult.UNDETERMINED,
                            generateEntry(EvaluationResult.UNDETERMINED, evaluation.undeterminedMessages)
                        )
                    }
                ).toMap()
            }
        }
    }

    private fun generateEntry(evaluation: Evaluation, messages: Set<String>): EvaluationEntry {
        return createEntry(generateHeader(evaluation.result, evaluation.recoverable), messages)
    }

    private fun generateEntry(result: EvaluationResult, messages: Set<String>): EvaluationEntry {
        return createEntry(generateHeader(result), messages)
    }

    private fun generateHeader(result: EvaluationResult, recoverable: Boolean = false): String {
        val addon = if (result == EvaluationResult.FAIL && recoverable) " (potentially recoverable)" else ""
        return result.toString() + addon
    }

    private fun createEntry(header: String, messages: Set<String>): EvaluationEntry {
        return EvaluationEntry(
            header = header,
            messages = messages.map(::insertSpacesAroundPlus).toSet()
        )
    }

    private fun insertSpacesAroundPlus(input: String): String {
        return input.replace(PLUS_WITHOUT_SURROUNDING_SPACES_REGEX, "$1 + $2")
    }
}