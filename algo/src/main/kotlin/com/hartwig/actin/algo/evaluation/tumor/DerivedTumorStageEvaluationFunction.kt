package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.clinical.datamodel.TumorStage

internal class DerivedTumorStageEvaluationFunction(private val originalFunction: EvaluationFunction) : EvaluationFunction {
    override fun evaluate(record: PatientRecord): Evaluation {
        if (record.tumor.stage != null) {
            return originalFunction.evaluate(record)
        }

        val derivedResults = record.tumor.derivedStages
            ?.associateWith { tumorStage -> evaluatedDerivedStage(record, tumorStage) }

        if (derivedResults.isNullOrEmpty()) {
            return originalFunction.evaluate(record)
        }
        if (derivedResults.size == 1) {
            return followResultOfSingleDerivation(derivedResults)
        }
        return if (allDerivedResultsMatch(derivedResults, EvaluationResult.PASS)) {
            createEvaluationForDerivedResult(derivedResults, EvaluationResult.PASS)
        } else if (anyDerivedResultMatches(derivedResults, EvaluationResult.PASS)) {
            createEvaluationForDerivedResult(derivedResults, EvaluationResult.UNDETERMINED)
        } else if (anyDerivedResultMatches(derivedResults, EvaluationResult.WARN)) {
            createEvaluationForDerivedResult(derivedResults, EvaluationResult.WARN)
        } else if (allDerivedResultsMatch(derivedResults, EvaluationResult.NOT_EVALUATED)) {
            createEvaluationForDerivedResult(derivedResults, EvaluationResult.NOT_EVALUATED)
        } else {
            createEvaluationForDerivedResult(derivedResults, EvaluationResult.FAIL)
        }
    }

    private fun evaluatedDerivedStage(record: PatientRecord, newStage: TumorStage): Evaluation {
        return originalFunction.evaluate(
            record.copy(tumor = record.tumor.copy(stage = newStage))
        )
    }

    companion object {
        private fun followResultOfSingleDerivation(derivedResults: Map<TumorStage, Evaluation>): Evaluation {
            val singleDerivedResult = derivedResults.values.iterator().next()
            return if (derivableResult(singleDerivedResult)) createEvaluationForDerivedResult(
                derivedResults,
                singleDerivedResult.result
            ) else singleDerivedResult
        }

        private fun derivableResult(singleDerivedResult: Evaluation): Boolean {
            return singleDerivedResult.result in
                    setOf(EvaluationResult.PASS, EvaluationResult.FAIL, EvaluationResult.UNDETERMINED, EvaluationResult.WARN)
        }

        fun createEvaluationForDerivedResult(derived: Map<TumorStage, Evaluation>, result: EvaluationResult): Evaluation {
            return when (result) {
                EvaluationResult.PASS -> DerivedTumorStageEvaluation.create(derived, EvaluationFactory::pass)

                EvaluationResult.UNDETERMINED -> DerivedTumorStageEvaluation.create(derived, EvaluationFactory::undetermined)

                EvaluationResult.WARN -> DerivedTumorStageEvaluation.create(derived, EvaluationFactory::warn)

                EvaluationResult.FAIL -> DerivedTumorStageEvaluation.create(derived, EvaluationFactory::fail)

                EvaluationResult.NOT_EVALUATED -> DerivedTumorStageEvaluation.create(derived, EvaluationFactory::notEvaluated)

                else -> throw IllegalArgumentException()
            }
        }

        private fun anyDerivedResultMatches(derivedResults: Map<TumorStage, Evaluation>, result: EvaluationResult): Boolean {
            return derivedResults.values.any { it.result == result }
        }

        private fun allDerivedResultsMatch(derivedResults: Map<TumorStage, Evaluation>, result: EvaluationResult): Boolean {
            return derivedResults.values.all { it.result == result }
        }
    }
}