package com.hartwig.actin.algo.evaluation.othercondition

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.DateComparison
import com.hartwig.actin.algo.evaluation.util.Format
import com.hartwig.actin.algo.othercondition.OtherConditionSelector
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
        val icdMatches = icdModel.findInstancesMatchingAnyIcdCode(
            OtherConditionSelector.selectClinicallyRelevant(
                record.priorOtherConditions
            ), targetIcdCodes
        )
        val fullMatchSummary = evaluateConditionsByDate(icdMatches.fullMatches)
        val mainMatchesWithUnknownExtension =
            evaluateConditionsByDate(icdMatches.mainCodeMatchesWithUnknownExtension).filterNot { it.key == EvaluationResult.FAIL }.values.flatten()

        return when {
            fullMatchSummary.containsKey(EvaluationResult.PASS) -> {
                EvaluationFactory.pass(
                    "Patient has had disease of ICD category ${
                        fullMatchSummary[EvaluationResult.PASS]?.joinToString(", ")
                        { resolveIcdTitle(it) }
                    } (belonging to $diseaseDescription) within specified time frame"
                )
            }

            fullMatchSummary.containsKey(EvaluationResult.WARN) -> {
                EvaluationFactory.warn(
                    "Has had disease of ICD category ${
                        fullMatchSummary[EvaluationResult.WARN]?.joinToString(", ")
                        { resolveIcdTitle(it) }
                    } (belonging to $diseaseDescription) near start of specified time frame"
                )
            }

            fullMatchSummary.containsKey(EvaluationResult.UNDETERMINED) -> {
                EvaluationFactory.undetermined(
                    "Has had disease of ICD category ${
                        fullMatchSummary[EvaluationResult.UNDETERMINED]?.joinToString(", ")
                        { resolveIcdTitle(it) }
                    } (belonging to $diseaseDescription), but undetermined whether that is within specified time frame"
                )
            }

            mainMatchesWithUnknownExtension.isNotEmpty() -> {
                EvaluationFactory.undetermined(
                    "Recent ${Format.concatItemsWithAnd(mainMatchesWithUnknownExtension)} but undetermined if history of $diseaseDescription"
                )
            }

            else -> {
                EvaluationFactory.fail("No recent $diseaseDescription")
            }
        }
    }

    private fun evaluateConditionsByDate(conditions: List<PriorOtherCondition>): Map<EvaluationResult, List<PriorOtherCondition>> {
        return conditions
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
    }

    private fun resolveIcdTitle(condition: PriorOtherCondition): String {
        return Format.concat(condition.icdCodes.map { icdModel.resolveTitleForCode(it) })
    }
}