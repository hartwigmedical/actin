package com.hartwig.actin.soc.evaluation

import com.hartwig.actin.treatment.datamodel.EligibilityFunction

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