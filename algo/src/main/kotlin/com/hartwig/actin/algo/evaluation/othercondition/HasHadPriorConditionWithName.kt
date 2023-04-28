package com.hartwig.actin.algo.evaluation.othercondition

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.othercondition.OtherConditionSelector
import com.hartwig.actin.util.ApplicationConfig

class HasHadPriorConditionWithName internal constructor(private val nameToFind: String) : EvaluationFunction {
    override fun evaluate(record: PatientRecord): Evaluation {
        val hasHadPriorConditionWithName = OtherConditionSelector.selectClinicallyRelevant(record.clinical().priorOtherConditions())
            .any { it.name().lowercase(ApplicationConfig.LOCALE).contains(nameToFind.lowercase(ApplicationConfig.LOCALE)) }

        if (hasHadPriorConditionWithName) {
            return EvaluationFactory.pass("Patient has history of $nameToFind", "History of $nameToFind")
        }
        return EvaluationFactory.fail("Patient has no history of $nameToFind", "No history of $nameToFind")
    }
}