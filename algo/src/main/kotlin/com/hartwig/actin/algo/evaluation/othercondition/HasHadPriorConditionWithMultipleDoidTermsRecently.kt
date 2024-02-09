package com.hartwig.actin.algo.evaluation.othercondition

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.DateComparison
import com.hartwig.actin.algo.othercondition.OtherConditionSelector
import com.hartwig.actin.clinical.datamodel.PriorOtherCondition
import com.hartwig.actin.doid.DoidModel
import java.time.LocalDate

class HasHadPriorConditionWithMultipleDoidTermsRecently(
    private val doidModel: DoidModel,
    private val doidsToFind: Set<String>,
    private val priorOtherConditionTerm: String,
    private val minDate: LocalDate
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        var matchingConditionAfterMinDate: String? = null
        var matchingConditionUnclearDate: String? = null
        var matchingConditionIsWithinWarnDate = false
        for (condition in OtherConditionSelector.selectClinicallyRelevant(record.clinical.priorOtherConditions)) {
            if (conditionHasDoid(condition, doidsToFind)) {
                val isAfterMinDate = DateComparison.isAfterDate(minDate, condition.year, condition.month)
                if (isAfterMinDate == null) {
                    matchingConditionUnclearDate = condition.name
                } else if (isAfterMinDate) {
                    matchingConditionAfterMinDate = condition.name
                    val isBeforeWarnDate = DateComparison.isBeforeDate(minDate.plusMonths(2), condition.year, condition.month)
                    matchingConditionIsWithinWarnDate = isBeforeWarnDate == true
                }
            }
        }
        if (matchingConditionAfterMinDate != null) {
            return if (matchingConditionIsWithinWarnDate) {
                EvaluationFactory.warn(
                    "Patient has had $matchingConditionAfterMinDate (belonging to $priorOtherConditionTerm) within specified time frame",
                    "Recent $priorOtherConditionTerm"
                )
            } else {
                EvaluationFactory.pass(
                    "Patient has had $matchingConditionAfterMinDate (belonging to $priorOtherConditionTerm) within specified time frame",
                    "Recent $priorOtherConditionTerm"
                )
            }
        }
        return if (matchingConditionUnclearDate != null) {
            EvaluationFactory.undetermined(
                "Patient has had $matchingConditionUnclearDate (belonging to $priorOtherConditionTerm), " +
                        "but undetermined whether that is within specified time frame", "Recent $priorOtherConditionTerm"
            )
        } else
            EvaluationFactory.fail(
                "Patient has had no recent condition belonging to $priorOtherConditionTerm",
                "No recent $priorOtherConditionTerm"
            )
    }

    private fun conditionHasDoid(condition: PriorOtherCondition, doidsToFind: Set<String>): Boolean {
        return condition.doids.flatMap { doidModel.doidWithParents(it) }.any { doidsToFind.contains(it) }
    }
}