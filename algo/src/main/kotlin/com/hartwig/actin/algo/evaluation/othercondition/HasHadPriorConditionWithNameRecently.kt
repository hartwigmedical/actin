package com.hartwig.actin.algo.evaluation.othercondition

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.DateComparison
import com.hartwig.actin.algo.othercondition.OtherConditionSelector
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.PriorOtherCondition
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
                    "History of ${
                        matchingConditionSummary[EvaluationResult.PASS]?.joinToString(
                            ", ",
                            transform = PriorOtherCondition::name
                        )
                    } " +
                            "(matched to condition name: $conditionNameToFind) within specified time frame"
                )
            }

            matchingConditionSummary.containsKey(EvaluationResult.WARN) -> {
                EvaluationFactory.warn(
                    "History of ${
                        matchingConditionSummary[EvaluationResult.WARN]?.joinToString(
                            ", ",
                            transform = PriorOtherCondition::name
                        )
                    } " +
                            "(matched to condition name: $conditionNameToFind) near start of specified time frame"
                )
            }

            matchingConditionSummary.containsKey(EvaluationResult.UNDETERMINED) -> {
                EvaluationFactory.undetermined(
                    "History of ${
                        matchingConditionSummary[EvaluationResult.UNDETERMINED]?.joinToString(
                            ", ",
                            transform = PriorOtherCondition::name
                        )
                    } " +
                            "(matched to condition name: $conditionNameToFind), but undetermined whether that is within specified time frame"
                )
            }

            else -> {
                EvaluationFactory.fail("No recent history of $conditionNameToFind")
            }
        }
    }
}