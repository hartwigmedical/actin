package com.hartwig.actin.algo.evaluation.othercondition

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.othercondition.PriorConditionMessages.Characteristic
import com.hartwig.actin.algo.othercondition.OtherConditionSelector
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.IcdCode
import com.hartwig.actin.icd.IcdModel

class HasHadPriorConditionWithIcd(private val icdModel: IcdModel, private val targetIcdCode: IcdCode) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val (fullMatches, undeterminedMatches) = icdModel.findInstancesMatchingAnyIcdCode(
            OtherConditionSelector.selectClinicallyRelevant(record.priorOtherConditions),
            setOf(targetIcdCode)
        )

        val targetTitle = icdModel.resolveTitleForCode(targetIcdCode)

        return when {
            fullMatches.isNotEmpty() -> {
                EvaluationFactory.pass(
                    PriorConditionMessages.passSpecific(Characteristic.CONDITION, fullMatches.map { it.display() }, targetTitle),
                    PriorConditionMessages.passGeneral(fullMatches.map { it.display() })
                )
            }

            undeterminedMatches.isNotEmpty() -> EvaluationFactory.undetermined(
                "Has history of ${undeterminedMatches.map { it.display() }} but undetermined if history of $targetTitle"
            )

            else -> EvaluationFactory.fail(
                PriorConditionMessages.failSpecific(targetTitle),
                PriorConditionMessages.failGeneral()
            )
        }
    }
}