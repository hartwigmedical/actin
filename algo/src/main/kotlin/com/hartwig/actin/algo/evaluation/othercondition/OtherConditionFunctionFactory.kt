package com.hartwig.actin.algo.evaluation.othercondition

import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.icd.IcdModel

object OtherConditionFunctionFactory {

    fun createPriorConditionWithIcdCodeFunction(icdModel: IcdModel, targetIcdTitle: String): EvaluationFunction {
        return HasHadPriorConditionWithIcdCode(icdModel, targetIcdTitle)
    }
}