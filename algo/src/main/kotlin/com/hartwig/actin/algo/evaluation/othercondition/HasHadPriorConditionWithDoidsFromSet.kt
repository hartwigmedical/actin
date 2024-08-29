package com.hartwig.actin.algo.evaluation.othercondition

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.tumor.DoidEvaluationFunctions
import com.hartwig.actin.algo.othercondition.OtherConditionSelector
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.doid.DoidModel

class HasHadPriorConditionWithDoidsFromSet(
    private val doidModel: DoidModel, private val doidsToFind: Set<String>, private val priorOtherConditionTerm: String
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val conditions = OtherConditionSelector.selectClinicallyRelevant(record.priorOtherConditions)
            .filter { DoidEvaluationFunctions.isOfAtLeastOneDoidType(doidModel, it.doids, doidsToFind) }.map { it.name }

        return if (conditions.isNotEmpty()) {
            EvaluationFactory.pass(
                PriorConditionMessages.passSpecific(PriorConditionMessages.Characteristic.CONDITION, conditions, priorOtherConditionTerm),
                PriorConditionMessages.passGeneral(conditions)
            )
        } else EvaluationFactory.fail(
            PriorConditionMessages.failSpecific(priorOtherConditionTerm),
            PriorConditionMessages.failGeneral()
        )
    }
}