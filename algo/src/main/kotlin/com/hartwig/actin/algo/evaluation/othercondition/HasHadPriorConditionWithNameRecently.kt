package com.hartwig.actin.algo.evaluation.othercondition

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.DateComparison
import com.hartwig.actin.algo.othercondition.OtherConditionSelector
import java.time.LocalDate

class HasHadPriorConditionWithNameRecently internal constructor(private val nameToFind: String, private val minDate: LocalDate) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        var matchingConditionAfterMinDate: String? = null
        var matchingConditionUnclearDate: String? = null
        var matchingConditionIsWithinWarnDate = false

        for (condition in OtherConditionSelector.selectClinicallyRelevant(record.clinical.priorOtherConditions)) {
            if (condition.name.lowercase().contains(nameToFind.lowercase())) {
                val isAfterMinDate = DateComparison.isAfterDate(minDate, condition.year, condition.month)
                if (isAfterMinDate == null) {
                    matchingConditionUnclearDate = condition.name
                } else if (isAfterMinDate) {
                    matchingConditionAfterMinDate = condition.name
                    val isBeforeWarnDate = DateComparison.isBeforeDate(minDate.plusMonths(2), condition.year, condition.month)
                    matchingConditionIsWithinWarnDate = isBeforeWarnDate != null && isBeforeWarnDate
                }
            }
        }
        if (matchingConditionAfterMinDate != null) {
            return if (matchingConditionIsWithinWarnDate) {
                EvaluationFactory.warn(
                    "Patient has history of $matchingConditionAfterMinDate (matched to condition name: $nameToFind) within specified time frame",
                    "History of $nameToFind"
                )
            } else {
                EvaluationFactory.pass(
                    "Patient has history of $matchingConditionAfterMinDate (matched to condition name: $nameToFind) within specified time frame",
                    "History of $nameToFind"
                )
            }
        }
        return if (matchingConditionUnclearDate != null) {
            EvaluationFactory.undetermined(
                "Patient has history of $matchingConditionUnclearDate (matched to condition name: $nameToFind), " +
                        "but undetermined whether that is within specified time frame", "History of $nameToFind"
            )
        } else
            EvaluationFactory.fail(
                "Patient has no recent history of $nameToFind",
                "No history of $nameToFind"
            )
    }
}