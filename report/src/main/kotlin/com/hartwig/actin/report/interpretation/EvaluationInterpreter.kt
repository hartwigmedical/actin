package com.hartwig.actin.report.interpretation

import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.trial.CriterionReference
import com.hartwig.actin.trial.sort.CriterionReferenceComparator
import java.util.stream.Collectors

object EvaluationInterpreter {

    fun interpretForDetailedTrialMatching(
        evaluations: Map<CriterionReference, Evaluation>,
        interpretFailOnly: Boolean
    ): List<EvaluationInterpretation> {
        val references = evaluations.keys.sortedWith(CriterionReferenceComparator()).distinct()
        return if (interpretFailOnly) {
            createInterpretationsOfType(references, evaluations, EvaluationResult.FAIL)
        } else {
            createInterpretationsOfType(references, evaluations, EvaluationResult.FAIL) +
                    createInterpretationsOfType(references, evaluations, EvaluationResult.WARN) +
                    createInterpretationsOfType(references, evaluations, EvaluationResult.UNDETERMINED) +
                    createInterpretationsOfType(references, evaluations, EvaluationResult.PASS) +
                    createInterpretationsOfType(references, evaluations, EvaluationResult.NOT_EVALUATED)
        }
    }

    private fun createInterpretationsOfType(
        references: Iterable<CriterionReference>,
        evaluations: Map<CriterionReference, Evaluation>,
        resultToRender: EvaluationResult
    ): List<EvaluationInterpretation> {
        val interpretations: MutableList<EvaluationInterpretation> = mutableListOf()
        for (reference in references) {
            val evaluation = evaluations[reference]!!
            if (evaluation.result == resultToRender) {
                val entriesPerResult: MutableMap<EvaluationResult, EvaluationEntry> = mutableMapOf()

                when (evaluation.result) {
                    EvaluationResult.PASS, EvaluationResult.NOT_EVALUATED -> {
                        entriesPerResult[evaluation.result] = generateEntry(evaluation, evaluation.passSpecificMessages)
                    }

                    EvaluationResult.WARN -> {
                        entriesPerResult[evaluation.result] = generateEntry(evaluation, evaluation.warnSpecificMessages)

                        if (evaluation.undeterminedSpecificMessages.isNotEmpty()) {
                            entriesPerResult[EvaluationResult.UNDETERMINED] =
                                generateEntry(EvaluationResult.UNDETERMINED, evaluation.undeterminedSpecificMessages)

                        }
                    }

                    EvaluationResult.UNDETERMINED -> {
                        entriesPerResult[evaluation.result] = generateEntry(evaluation, evaluation.undeterminedSpecificMessages)
                    }

                    EvaluationResult.FAIL -> {
                        entriesPerResult[evaluation.result] = generateEntry(evaluation, evaluation.failSpecificMessages)

                        if (evaluation.recoverable) {
                            if (evaluation.warnSpecificMessages.isNotEmpty()) {
                                entriesPerResult[EvaluationResult.WARN] =
                                    generateEntry(EvaluationResult.WARN, evaluation.warnSpecificMessages)
                            }

                            if (evaluation.undeterminedSpecificMessages.isNotEmpty()) {
                                entriesPerResult[EvaluationResult.UNDETERMINED] =
                                    generateEntry(EvaluationResult.UNDETERMINED, evaluation.undeterminedSpecificMessages)
                            }
                        }
                    }
                }
                interpretations.add(
                    EvaluationInterpretation(
                        rule = reference.id,
                        reference = reference.text,
                        entriesPerResult = entriesPerResult
                    )
                )
            }
        }
        return interpretations
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
            messages = insertSpacesAroundPlus(messages)
        )
    }

    private fun insertSpacesAroundPlus(inputs: Set<String>): Set<String> {
        val regex = "(\\S)\\+(\\S)".toRegex()

        return inputs.stream().map { s -> s.replace(regex, "$1 + $2") }.collect(Collectors.toSet())
    }
}