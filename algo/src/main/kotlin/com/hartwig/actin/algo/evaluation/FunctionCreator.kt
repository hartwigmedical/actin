package com.hartwig.actin.algo.evaluation

import com.hartwig.actin.trial.datamodel.EligibilityFunction

interface FunctionCreator {

    fun create(eligibilityFunction: EligibilityFunction): EvaluationFunction

    companion object {
        operator fun invoke(createEvaluationFunction: (EligibilityFunction) -> EvaluationFunction): FunctionCreator =
            object : FunctionCreator {
                override fun create(eligibilityFunction: EligibilityFunction): EvaluationFunction =
                    createEvaluationFunction(eligibilityFunction)
            }
    }
}