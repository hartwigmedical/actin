package com.hartwig.actin.algo.evaluation.othercondition

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.othercondition.OtherConditionSelector
import com.hartwig.actin.clinical.datamodel.PriorOtherCondition
import com.hartwig.actin.doid.DoidModel

class HasHadPriorConditionWithMultipleDoidTerms(
    private val doidModel: DoidModel, private val doidsToFind: Set<String>, private val priorOtherConditionTerm: String
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val conditions = OtherConditionSelector.selectClinicallyRelevant(record.clinical.priorOtherConditions)
            .filter { conditionHasDoid(it, doidsToFind) }.map { it.name }

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

    private fun conditionHasDoid(condition: PriorOtherCondition, doidsToFind: Set<String>): Boolean {
        return condition.doids.flatMap { doidModel.doidWithParents(it) }.any { doidsToFind.contains(it) }
    }
}