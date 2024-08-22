package com.hartwig.actin.algo.evaluation.othercondition

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.othercondition.OtherConditionSelector

class HasHadPriorConditionWithName(private val nameToFind: String) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val hasHadPriorConditionWithName = OtherConditionSelector.selectClinicallyRelevant(record.priorOtherConditions)
            .any { it.name.lowercase().contains(nameToFind.lowercase()) }

        if (hasHadPriorConditionWithName) {
            return EvaluationFactory.pass("Patient has history of $nameToFind", "History of $nameToFind")
        }
        return EvaluationFactory.fail("Patient has no history of $nameToFind", "No history of $nameToFind")
    }
}