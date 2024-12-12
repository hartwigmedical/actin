package com.hartwig.actin.algo.evaluation.othercondition

import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.clinical.IcdCode
import com.hartwig.actin.icd.IcdModel

object OtherConditionFunctionFactory {

    fun createPriorConditionWithIcdCodeFunction(icdModel: IcdModel, targetIcdCode: IcdCode): EvaluationFunction {
        return HasHadPriorConditionWithIcd(icdModel, targetIcdCode)
    }
}