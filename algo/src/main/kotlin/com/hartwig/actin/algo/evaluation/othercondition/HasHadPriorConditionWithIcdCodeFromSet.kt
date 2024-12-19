package com.hartwig.actin.algo.evaluation.othercondition

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.othercondition.OtherConditionSelector
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.IcdCode
import com.hartwig.actin.icd.IcdModel

class HasHadPriorConditionWithIcdCodeFromSet(
    private val icdModel: IcdModel, private val targetIcdCodes: Set<IcdCode>, private val priorOtherConditionTerm: String
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val (fullMatches, undeterminedMatches) = icdModel.findInstancesMatchingAnyIcdCode(
            OtherConditionSelector.selectClinicallyRelevant(record.priorOtherConditions),
            targetIcdCodes
        )

        return when {
            fullMatches.isNotEmpty() -> {
                val display = fullMatches.map { it.display() }.toSet()
                EvaluationFactory.pass(
                    PriorConditionMessages.passSpecific(
                        PriorConditionMessages.Characteristic.CONDITION,
                        display,
                        priorOtherConditionTerm
                    ),
                    PriorConditionMessages.passGeneral(display)
                )
            }
            undeterminedMatches.isNotEmpty() -> {
                EvaluationFactory.undetermined(
                    "Has history of ${undeterminedMatches.map { it.display() }} but undetermined if history of $priorOtherConditionTerm"
                )

            } else -> {
                EvaluationFactory.fail(
                    PriorConditionMessages.failSpecific(priorOtherConditionTerm),
                    PriorConditionMessages.failGeneral()
                )
            }
        }
    }
}