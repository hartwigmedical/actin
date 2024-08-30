package com.hartwig.actin.algo.evaluation.infection

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.ValueComparison.stringCaseInsensitivelyMatchesQueryCollection
import com.hartwig.actin.algo.othercondition.OtherConditionSelector
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation

class HasKnownEBVInfection: EvaluationFunction {
    override fun evaluate(record: PatientRecord): Evaluation {
        val matchingCondition = OtherConditionSelector.selectClinicallyRelevant(record.priorOtherConditions)
            .find { stringCaseInsensitivelyMatchesQueryCollection(it.name, EBV_TERMS) }

        return if (matchingCondition != null) {
            EvaluationFactory.pass("Patient has known EBV infection: " + matchingCondition.name, "Present EBV infection")
        } else {
            EvaluationFactory.fail("Patient has no known EBV infection", "EBV infection(s) not present")
        }
    }

    companion object {
        val EBV_TERMS = setOf("EBV", "Epstein Barr")
    }
}