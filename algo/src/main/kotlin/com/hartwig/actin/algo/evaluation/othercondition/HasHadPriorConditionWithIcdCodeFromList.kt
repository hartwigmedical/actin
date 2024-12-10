package com.hartwig.actin.algo.evaluation.othercondition

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.othercondition.OtherConditionSelector
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.icd.IcdModel

class HasHadPriorConditionWithIcdCodeFromList(
    private val icdModel: IcdModel, private val targetIcdCodes: List<String>, private val priorOtherConditionTerm: String
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val matchingConditions = OtherConditionSelector.selectClinicallyRelevant(record.priorOtherConditions)
            .flatMap { PriorOtherConditionFunctions.findPriorOtherConditionsMatchingAnyIcdCode(icdModel, record, targetIcdCodes).fullMatches }
            .map { it.name }

        return if (matchingConditions.isNotEmpty()) {
            EvaluationFactory.pass(
                PriorConditionMessages.passSpecific(PriorConditionMessages.Characteristic.CONDITION, matchingConditions, priorOtherConditionTerm),
                PriorConditionMessages.passGeneral(matchingConditions)
            )
        } else EvaluationFactory.fail(
            PriorConditionMessages.failSpecific(priorOtherConditionTerm),
            PriorConditionMessages.failGeneral()
        )
    }
}