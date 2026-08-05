package com.hartwig.actin.algo.evaluation.comorbidity

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.EvaluationLabels
import com.hartwig.actin.algo.evaluation.util.Format
import com.hartwig.actin.calendar.DateComparison
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.IcdCode
import com.hartwig.actin.datamodel.clinical.OtherCondition
import com.hartwig.actin.icd.IcdModel
import java.time.LocalDate

class HasHadOtherConditionWithIcdCodeFromSetRecently(
    private val icdModel: IcdModel,
    private val targetIcdCodes: Set<IcdCode>,
    private val diseaseDescription: String,
    private val minDate: LocalDate,
    private val maxMonthsAgo: Int,
    private val labels: EvaluationLabels.Comorbidity
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val icdMatches = icdModel.findInstancesMatchingAnyIcdCode(record.otherConditions, targetIcdCodes)
        val fullMatchSummary = evaluateConditionsByDate(icdMatches.fullMatches)
        val mainMatchesWithUnknownExtension = evaluateConditionsByDate(icdMatches.mainCodeMatchesWithUnknownExtension)
            .filterNot { it.key == EvaluationResult.FAIL }
            .values.flatten()

        return when {
            fullMatchSummary.containsKey(EvaluationResult.PASS) -> {
                EvaluationFactory.pass(
                    labels.hasHadOtherConditionWithIcdCodeFromSetRecentlyPass(
                        diseaseDescription, displayConditions(fullMatchSummary, EvaluationResult.PASS)
                    )
                )
            }

            fullMatchSummary.containsKey(EvaluationResult.WARN) -> {
                EvaluationFactory.warn(
                    labels.hasHadOtherConditionWithIcdCodeFromSetRecentlyWarn(
                        diseaseDescription, maxMonthsAgo, displayConditions(fullMatchSummary, EvaluationResult.WARN, true)
                    )
                )
            }

            fullMatchSummary.containsKey(EvaluationResult.UNDETERMINED) -> {
                EvaluationFactory.undetermined(
                    labels.hasHadOtherConditionWithIcdCodeFromSetRecentlyUndeterminedWithinMonths(
                        diseaseDescription, displayConditions(fullMatchSummary, EvaluationResult.UNDETERMINED, true), maxMonthsAgo
                    )
                )
            }

            mainMatchesWithUnknownExtension.isNotEmpty() -> {
                EvaluationFactory.undetermined(
                    labels.hasHadOtherConditionWithIcdCodeFromSetRecentlyUndeterminedUnknownExtension(
                        Format.concatItemsWithAnd(mainMatchesWithUnknownExtension, true), diseaseDescription
                    )
                )
            }

            else -> {
                EvaluationFactory.fail(labels.hasHadOtherConditionWithIcdCodeFromSetRecentlyFail(diseaseDescription))
            }
        }
    }

    private fun evaluateConditionsByDate(conditions: List<OtherCondition>): Map<EvaluationResult, List<OtherCondition>> {
        return conditions
            .groupBy {
                val isAfter = DateComparison.isAfterDate(minDate, it.year, it.month)
                when {
                    DateComparison.isExactYearAndMonth(minDate, it.year, it.month) -> EvaluationResult.WARN
                    isAfter == true && DateComparison.isBeforeDate(minDate.plusMonths(2), it.year, it.month) == true -> {
                        EvaluationResult.WARN
                    }

                    isAfter == true -> EvaluationResult.PASS
                    isAfter == null -> EvaluationResult.UNDETERMINED
                    else -> EvaluationResult.FAIL
                }
            }
    }

    private fun displayConditions(
        fullMatchSummary: Map<EvaluationResult, List<OtherCondition>>,
        evaluation: EvaluationResult,
        withDate: Boolean = false
    ): String {
        return fullMatchSummary[evaluation]
            ?.joinToString(", ", prefix = " (", postfix = ")") { conditionWithOptionalDate(it, withDate) } ?: ""
    }

    private fun conditionWithOptionalDate(condition: OtherCondition, withDate: Boolean): String {
        val dateString = if (withDate) " (${condition.year}-${condition.month})" else ""
        return condition.display() + dateString
    }
}