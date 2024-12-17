package com.hartwig.actin.algo.evaluation.othercondition

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.DateComparison
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.IcdCode
import com.hartwig.actin.datamodel.clinical.PriorOtherCondition
import com.hartwig.actin.icd.IcdModel
import java.time.LocalDate

class HasHadPriorConditionWithIcdCodeFromSetRecently(
    private val icdModel: IcdModel,
    private val targetIcdCodes: Set<IcdCode>,
    private val diseaseDescription: String,
    private val minDate: LocalDate
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val matchingConditionSummary =
            PriorOtherConditionFunctions.findRelevantPriorConditionsMatchingAnyIcdCode(icdModel, record, targetIcdCodes).fullMatches
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
                    "Patient has had disease of ICD category ${
                        matchingConditionSummary[EvaluationResult.PASS]?.joinToString(", ")
                        { resolveIcdTitle(it) }
                    } (belonging to $diseaseDescription) within specified time frame",
                    "Recent $diseaseDescription"
                )
            }

            matchingConditionSummary.containsKey(EvaluationResult.WARN) -> {
                EvaluationFactory.warn(
                    "Patient has had disease of ICD category ${
                        matchingConditionSummary[EvaluationResult.WARN]?.joinToString(", ")
                        { resolveIcdTitle(it) }
                    } (belonging to $diseaseDescription) near start of specified time frame",
                    "Borderline recent $diseaseDescription"
                )
            }

            matchingConditionSummary.containsKey(EvaluationResult.UNDETERMINED) -> {
                EvaluationFactory.undetermined(
                    "Patient has had disease of ICD category ${
                        matchingConditionSummary[EvaluationResult.UNDETERMINED]?.joinToString(", ")
                        { resolveIcdTitle(it) }
                    } (belonging to $diseaseDescription), but undetermined whether that is within specified time frame",
                    "Recent $diseaseDescription"
                )
            }

            else -> {
                EvaluationFactory.fail(
                    "Patient has had no recent condition belonging to $diseaseDescription",
                    "No recent $diseaseDescription"
                )
            }
        }
    }

    private fun resolveIcdTitle(condition: PriorOtherCondition): String {
        return icdModel.resolveTitleForCode(condition.icdCode)
    }
}