package com.hartwig.actin.algo.evaluation.othercondition

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.DateComparison
import com.hartwig.actin.algo.othercondition.OtherConditionSelector
import com.hartwig.actin.clinical.datamodel.PriorOtherCondition
import java.time.LocalDate

class HasHadPriorConditionWithNameRecently (
    private val conditionNameToFind: String,
    private val minDate: LocalDate
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val matchingConditionSummary = OtherConditionSelector.selectClinicallyRelevant(record.priorOtherConditions)
            .filter { it.name.lowercase().contains(conditionNameToFind.lowercase()) }
            .groupBy {
                val isAfterMinDate = DateComparison.isAfterDate(minDate, it.year, it.month)
                when {
                    isAfterMinDate == true && DateComparison.isBeforeDate(minDate.plusMonths(2), it.year, it.month) == true -> {
                        EvaluationResult.WARN
                    }

                    isAfterMinDate == true -> EvaluationResult.PASS
                    isAfterMinDate == null -> EvaluationResult.UNDETERMINED
                    else -> EvaluationResult.FAIL
                }
            }

        return when {
            matchingConditionSummary.containsKey(EvaluationResult.PASS) -> {
                EvaluationFactory.pass(
                    "Patient has history of ${matchingConditionSummary[EvaluationResult.PASS]?.joinToString(", ", transform = PriorOtherCondition::name)} " +
                            "(matched to condition name: $conditionNameToFind) within specified time frame",
                    "Recent history of $conditionNameToFind"
                )
            }

            matchingConditionSummary.containsKey(EvaluationResult.WARN) -> {
                EvaluationFactory.warn(
                    "Patient has history of ${matchingConditionSummary[EvaluationResult.WARN]?.joinToString(", ", transform = PriorOtherCondition::name)} " +
                            "(matched to condition name: $conditionNameToFind) near start of specified time frame",
                    "Recent history of $conditionNameToFind"
                )
            }

            matchingConditionSummary.containsKey(EvaluationResult.UNDETERMINED) -> {
                EvaluationFactory.undetermined(
                    "Patient has history of ${matchingConditionSummary[EvaluationResult.UNDETERMINED]?.joinToString(", ", transform = PriorOtherCondition::name)} " +
                            "(matched to condition name: $conditionNameToFind), but undetermined whether that is within specified time frame",
                    "History of $conditionNameToFind"
                )
            }

            else -> {
                EvaluationFactory.fail(
                    "Patient has no recent history of $conditionNameToFind",
                    "No recent history of $conditionNameToFind"
                )
            }
        }
    }
}