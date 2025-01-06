package com.hartwig.actin.algo.evaluation.othercondition

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.othercondition.OtherConditionSelector
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation

class HasHadPriorConditionWithName(private val nameToFind: String) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val hasHadPriorConditionWithName = OtherConditionSelector.selectClinicallyRelevant(record.priorOtherConditions)
            .any { it.name.lowercase().contains(nameToFind.lowercase()) }

        if (hasHadPriorConditionWithName) {
            return EvaluationFactory.pass("History of $nameToFind")
        }
        return EvaluationFactory.fail("No history of $nameToFind")
    }
}