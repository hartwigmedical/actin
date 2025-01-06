package com.hartwig.actin.algo.evaluation.othercondition

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.tumor.DoidEvaluationFunctions
import com.hartwig.actin.algo.evaluation.util.DateComparison
import com.hartwig.actin.algo.othercondition.OtherConditionSelector
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.PriorOtherCondition
import com.hartwig.actin.doid.DoidModel
import java.time.LocalDate

class HasHadPriorConditionWithDoidsFromSetRecently(
    private val doidModel: DoidModel,
    private val doidsToFind: Set<String>,
    private val priorOtherConditionTerm: String,
    private val minDate: LocalDate
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val matchingConditionSummary = OtherConditionSelector.selectClinicallyRelevant(record.priorOtherConditions)
            .filter { DoidEvaluationFunctions.isOfAtLeastOneDoidType(doidModel, it.doids, doidsToFind) }
            .groupBy {
                val isAfter = DateComparison.isAfterDate(minDate, it.year, it.month)
                when {
                    isAfter == true && DateComparison.isBeforeDate(minDate.plusMonths(2), it.year, it.month) == true -> {
                        EvaluationResult.WARN
                    }

                    isAfter == true -> EvaluationResult.PASS
                    isAfter == null -> EvaluationResult.UNDETERMINED
                    else -> EvaluationResult.FAIL
                }
            }

        return when {
            matchingConditionSummary.containsKey(EvaluationResult.PASS) -> {
                EvaluationFactory.pass(
                    "Has had DOIDs ${
                        matchingConditionSummary[EvaluationResult.PASS]?.joinToString(", ")
                        { extractDoids(it) }
                    } (belonging to $priorOtherConditionTerm) within specified time frame"
                )
            }

            matchingConditionSummary.containsKey(EvaluationResult.WARN) -> {
                EvaluationFactory.warn(
                    "Has had DOIDs ${
                        matchingConditionSummary[EvaluationResult.WARN]?.joinToString(", ")
                        { extractDoids(it) }
                    } (belonging to $priorOtherConditionTerm) near start of specified time frame"
                )
            }

            matchingConditionSummary.containsKey(EvaluationResult.UNDETERMINED) -> {
                EvaluationFactory.undetermined(
                    "Has had DOIDs ${
                        matchingConditionSummary[EvaluationResult.UNDETERMINED]?.joinToString(", ")
                        { extractDoids(it) }
                    } (belonging to $priorOtherConditionTerm) but undetermined whether that is within specified time frame"
                )
            }

            else -> {
                EvaluationFactory.fail("No recent condition/DOIDs belonging to $priorOtherConditionTerm")
            }
        }
    }

    private fun extractDoids(priorOtherCondition: PriorOtherCondition): String {
        return priorOtherCondition.doids.joinToString(" + ")
    }
}