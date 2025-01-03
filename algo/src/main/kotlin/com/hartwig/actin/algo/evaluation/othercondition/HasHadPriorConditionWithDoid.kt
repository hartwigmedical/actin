package com.hartwig.actin.algo.evaluation.othercondition

import com.hartwig.actin.algo.evaluation.EvaluationFactory.fail
import com.hartwig.actin.algo.evaluation.EvaluationFactory.pass
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.othercondition.PriorConditionMessages.Characteristic
import com.hartwig.actin.algo.othercondition.OtherConditionSelector
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.doid.DoidModel

//TODO (CB)!
class HasHadPriorConditionWithDoid(private val doidModel: DoidModel, private val doidToFind: String) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val doidTerm = doidModel.resolveTermForDoid(doidToFind)
        val conditions =
            OtherConditionSelector.selectConditionsMatchingDoid(record.priorOtherConditions, doidToFind, doidModel)
        return if (conditions.isNotEmpty()) {
            pass(
                PriorConditionMessages.passSpecific(Characteristic.CONDITION, conditions, doidTerm),
                PriorConditionMessages.pass(conditions)
            )
        } else fail(
            PriorConditionMessages.failSpecific(doidTerm),
            PriorConditionMessages.failGeneral()
        )
    }
}