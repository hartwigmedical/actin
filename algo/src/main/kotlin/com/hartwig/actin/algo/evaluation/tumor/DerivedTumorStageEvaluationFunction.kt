package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.TumorStage

internal class DerivedTumorStageEvaluationFunction(private val originalFunction: EvaluationFunction, private val messageEnd: String) :
    EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        if (record.tumor.stage != null) {
            return originalFunction.evaluate(record)
        }

        val derivedResults = record.tumor.derivedStages?.associateWith { tumorStage -> evaluatedDerivedStage(record, tumorStage) }

        if (derivedResults.isNullOrEmpty()) {
            return originalFunction.evaluate(record)
        }
        if (derivedResults.size == 1) {
            return followResultOfSingleDerivation(derivedResults)
        }

        val uniqueResults = derivedResults.values.toSet()

        return if (uniqueResults.size == 1) {
            createEvaluationForDerivedResult(derivedResults, uniqueResults.first().result)
        } else {
            EvaluationFactory.undetermined("Undetermined if patient has $messageEnd")
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
    }
}