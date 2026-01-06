package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.Format
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentType

class HasHadLimitedWeeksOfTreatmentOfCategoryWithTypes(
    private val category: TreatmentCategory,
    private val types: Set<TreatmentType>,
    private val maxWeeks: Int
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val treatmentEvaluations = record.oncologicalHistory.map { treatmentHistoryEntry ->
            val mayMatchAsTrial = TrialFunctions.treatmentMayMatchAsTrial(treatmentHistoryEntry, setOf(category))
            val categoryMatches = treatmentHistoryEntry.categories().contains(category)

            TreatmentHistoryEntryFunctions.portionOfTreatmentHistoryEntryMatchingPredicate(treatmentHistoryEntry) {
                categoryMatches && treatmentHistoryEntry.matchesTypeFromSet(types) == true
            }?.let { matchingPortionOfEntry ->
                val durationWeeks = TreatmentHistoryEntryFunctions.weeksBetweenDates(matchingPortionOfEntry)
                val durationWeeksMax = TreatmentHistoryEntryFunctions.maxWeeksBetweenDates(matchingPortionOfEntry)

                TreatmentEvaluation.create(
                    hadTreatment = true,
                    hadTrial = mayMatchAsTrial,
                    lessThanMaxWeeks = durationWeeksMax != null && durationWeeksMax <= maxWeeks,
                    hadUnclearWeeks = durationWeeks == null && ((durationWeeksMax != null && durationWeeksMax > maxWeeks) || durationWeeksMax == null)
                )
            } ?: TreatmentEvaluation.create(
                hadTreatment = if (categoryMatches && !treatmentHistoryEntry.hasTypeConfigured()) null else false,
                hadTrial = mayMatchAsTrial
            )
        }

        return when {
            TreatmentEvaluation.HAS_HAD_TREATMENT_FOR_AT_MOST_WEEKS in treatmentEvaluations -> {
                EvaluationFactory.pass("Has had ${treatment()} for less than $maxWeeks weeks")
            }

            TreatmentEvaluation.HAS_HAD_TREATMENT_AND_UNCLEAR_WEEKS in treatmentEvaluations -> {
                EvaluationFactory.undetermined("Has received ${treatment()} but unknown nb of weeks")
            }

            TreatmentEvaluation.HAS_HAD_UNCLEAR_TREATMENT_OR_TRIAL in treatmentEvaluations -> {
                EvaluationFactory.undetermined("Unclear if received " + category.display())
            }

            TreatmentEvaluation.HAS_HAD_TREATMENT in treatmentEvaluations -> {
                EvaluationFactory.fail("Has had ${treatment()} treatment but for more than $maxWeeks weeks")
            }

            else -> {
                EvaluationFactory.fail("No ${treatment()} treatment")
            }
        }
    }

    private fun treatment(): String {
        return "${Format.concatItemsWithOr(types)} ${category.display()} treatment"
    }

    private enum class TreatmentEvaluation {
        HAS_HAD_TREATMENT_FOR_AT_MOST_WEEKS,
        HAS_HAD_TREATMENT_AND_UNCLEAR_WEEKS,
        HAS_HAD_UNCLEAR_TREATMENT_OR_TRIAL,
        HAS_HAD_TREATMENT,
        NO_MATCH;

        companion object {
            fun create(
                hadTreatment: Boolean?,
                hadTrial: Boolean,
                lessThanMaxWeeks: Boolean = false,
                hadUnclearWeeks: Boolean = false
            ) = when {
                hadTreatment == true && lessThanMaxWeeks -> HAS_HAD_TREATMENT_FOR_AT_MOST_WEEKS
                hadTreatment == true && hadUnclearWeeks -> HAS_HAD_TREATMENT_AND_UNCLEAR_WEEKS
                hadTreatment == null || hadTrial -> HAS_HAD_UNCLEAR_TREATMENT_OR_TRIAL
                hadTreatment == true -> HAS_HAD_TREATMENT
                else -> NO_MATCH
            }
        }
    }
}