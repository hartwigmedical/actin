package com.hartwig.actin.algo.evaluation.othercondition

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.DateComparison.isAfterDate
import com.hartwig.actin.algo.evaluation.util.DateComparison.isBeforeDate
import com.hartwig.actin.algo.othercondition.OtherConditionSelector
import com.hartwig.actin.clinical.datamodel.PriorOtherCondition
import com.hartwig.actin.doid.DoidModel
import java.time.LocalDate

class HasHadPriorConditionWithDoidRecently internal constructor(
    private val doidModel: DoidModel, private val doidToFind: String, private val minDate: LocalDate
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val doidTerm = doidModel.resolveTermForDoid(doidToFind)
        var matchingConditionAfterMinDate: String? = null
        var matchingConditionUnclearDate: String? = null
        var matchingConditionIsWithinWarnDate = false
        for (condition in OtherConditionSelector.selectClinicallyRelevant(record.priorOtherConditions)) {
            if (conditionHasDoid(condition, doidToFind)) {
                val isAfterMinDate = isAfterDate(minDate, condition.year, condition.month)
                if (isAfterMinDate == null) {
                    matchingConditionUnclearDate = condition.name
                } else if (isAfterMinDate) {
                    matchingConditionAfterMinDate = condition.name
                    val isBeforeWarnDate = isBeforeDate(minDate.plusMonths(2), condition.year, condition.month)
                    matchingConditionIsWithinWarnDate = isBeforeWarnDate != null && isBeforeWarnDate
                }
            }
        }
        if (matchingConditionAfterMinDate != null) {
            return if (matchingConditionIsWithinWarnDate) {
                EvaluationFactory.warn(
                    "Patient has had $matchingConditionAfterMinDate (belonging to $doidTerm) within specified time frame",
                    "Recent $doidTerm"
                )
            } else {
                EvaluationFactory.pass(
                    "Patient has had $matchingConditionAfterMinDate (belonging to $doidTerm) within specified time frame",
                    "Recent $doidTerm"
                )
            }
        }
        return if (matchingConditionUnclearDate != null) {
            EvaluationFactory.undetermined(
                "Patient has had $matchingConditionUnclearDate (belonging to $doidTerm), " +
                        "but undetermined whether that is within specified time frame", "Recent $doidTerm"
            )
        } else
            EvaluationFactory.fail(
                "Patient has had no recent condition belonging to $doidTerm",
                "No relevant non-oncological condition"
            )
    }

    private fun conditionHasDoid(condition: PriorOtherCondition, doidToFind: String): Boolean {
        return condition.doids.flatMap { doidModel.doidWithParents(it) }.contains(doidToFind)
    }
}