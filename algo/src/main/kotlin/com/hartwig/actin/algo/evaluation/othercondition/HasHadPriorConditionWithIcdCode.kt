package com.hartwig.actin.algo.evaluation.othercondition

import com.hartwig.actin.algo.evaluation.EvaluationFactory.fail
import com.hartwig.actin.algo.evaluation.EvaluationFactory.pass
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.othercondition.PriorConditionMessages.Characteristic
import com.hartwig.actin.algo.othercondition.OtherConditionSelector
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.icd.IcdModel

class HasHadPriorConditionWithIcdCode(private val icdModel: IcdModel, private val targetIcdTitle: String) : EvaluationFunction {
    
    override fun evaluate(record: PatientRecord): Evaluation {
        val icdCode = icdModel.titleToCodeMap[targetIcdTitle]!!
        val conditions =
            OtherConditionSelector.selectConditionsMatchingIcdCode(record.priorOtherConditions, listOf(icdCode), icdModel)
        return if (conditions.isNotEmpty()) {
            pass(
                PriorConditionMessages.passSpecific(Characteristic.CONDITION, conditions, targetIcdTitle),
                PriorConditionMessages.passGeneral(conditions)
            )
        } else fail(
            PriorConditionMessages.failSpecific(targetIcdTitle),
            PriorConditionMessages.failGeneral()
        )
    }
}