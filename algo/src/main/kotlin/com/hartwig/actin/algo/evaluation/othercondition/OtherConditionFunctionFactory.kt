package com.hartwig.actin.algo.evaluation.othercondition

import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.doid.DoidModel

object OtherConditionFunctionFactory {

    fun createPriorConditionWithDoidFunction(doidModel: DoidModel, doidToFind: String): EvaluationFunction {
        return HasHadPriorConditionWithDoid(doidModel, doidToFind)
    }
}