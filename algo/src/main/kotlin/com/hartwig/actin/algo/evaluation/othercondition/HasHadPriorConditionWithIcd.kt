package com.hartwig.actin.algo.evaluation.othercondition

import com.hartwig.actin.algo.evaluation.EvaluationFactory.fail
import com.hartwig.actin.algo.evaluation.EvaluationFactory.pass
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.othercondition.PriorConditionMessages.Characteristic
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.IcdCode
import com.hartwig.actin.icd.IcdModel

class HasHadPriorConditionWithIcd(private val icdModel: IcdModel, private val targetIcdCode: IcdCode) : EvaluationFunction {
    
    override fun evaluate(record: PatientRecord): Evaluation {
        val conditions =
            PriorOtherConditionFunctions.findPriorOtherConditionsMatchingAnyIcdCode(icdModel, record, setOf(targetIcdCode)).fullMatches
                .map { it.name }

        val targetTitle = icdModel.resolveTitleForCode(targetIcdCode)

        return if (conditions.isNotEmpty()) {
            pass(
                PriorConditionMessages.passSpecific(Characteristic.CONDITION, conditions, targetTitle),
                PriorConditionMessages.passGeneral(conditions)
            )
        } else fail(
            PriorConditionMessages.failSpecific(targetTitle),
            PriorConditionMessages.failGeneral()
        )
    }
}