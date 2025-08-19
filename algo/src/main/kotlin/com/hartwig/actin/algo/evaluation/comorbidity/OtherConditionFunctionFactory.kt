package com.hartwig.actin.algo.evaluation.comorbidity

import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.clinical.IcdCode
import com.hartwig.actin.icd.IcdModel
import java.time.LocalDate

object OtherConditionFunctionFactory {

    fun createPriorConditionWithIcdCodeFunction(
        icdModel: IcdModel,
        targetIcdCodes: Set<IcdCode>,
        conditionTerm: String,
        referenceDate: LocalDate
    ): EvaluationFunction {
        return HasHadComorbidityWithIcdCode(
            icdModel, targetIcdCodes, conditionTerm,
            referenceDate
        )
    }
}