package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.treatment.Treatment
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import com.hartwig.actin.datamodel.clinical.treatment.history.TreatmentHistoryEntry

class TreatmentWithLimitedWeeksEvaluator(
    private val matchesTreatment: (Treatment) -> Boolean?,
    private val specificMatchCannotBeDetermined: (TreatmentHistoryEntry) -> Boolean,
    private val potentialTrialCategories: Set<TreatmentCategory>,
    private val treatmentMessage: String,
    private val maxWeeks: Int?
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val treatmentEvaluations = record.oncologicalHistory.map { treatmentHistoryEntry ->
            val mayMatchAsTrial = TrialFunctions.treatmentMayMatchAsTrial(treatmentHistoryEntry, potentialTrialCategories)

            val durationWeeks = TreatmentHistoryEntryFunctions.weeksBetweenDates(treatmentHistoryEntry)
            val durationWeeksMax = TreatmentHistoryEntryFunctions.maxWeeksBetweenDates(treatmentHistoryEntry)

            TreatmentHistoryEntryFunctions.portionOfTreatmentHistoryEntryMatchingPredicate(treatmentHistoryEntry) { matchesTreatment(it) == true }
                ?.let { matchingPortionOfEntry ->
                    val durationWeeksMatchingPortion = TreatmentHistoryEntryFunctions.weeksBetweenDates(matchingPortionOfEntry)
                    val durationWeeksMaxMatchingPortion = TreatmentHistoryEntryFunctions.maxWeeksBetweenDates(matchingPortionOfEntry)

                    TreatmentEvaluation.create(
                        hadTreatment = true,
                        hadTrial = mayMatchAsTrial,
                        lessThanMaxWeeks = lessThanMaxWeeks(durationWeeksMaxMatchingPortion),
                        hadUnclearWeeks = hadUnclearWeeks(durationWeeksMatchingPortion, durationWeeksMaxMatchingPortion)
                    )
                } ?: TreatmentEvaluation.create(
                    hadTreatment = if (specificMatchCannotBeDetermined(treatmentHistoryEntry)) null else false,
                    hadTrial = mayMatchAsTrial,
                    lessThanMaxWeeks = lessThanMaxWeeks(durationWeeksMax),
                    hadUnclearWeeks = hadUnclearWeeks(durationWeeks, durationWeeksMax)
                )
        }

        return when {
            TreatmentEvaluation.HAS_HAD_TREATMENT_WITH_EXCESSIVE_WEEKS in treatmentEvaluations -> {
                EvaluationFactory.fail("Has had $treatmentMessage treatment but for more than $maxWeeks weeks")
            }

            maxWeeks != null && treatmentEvaluations.size > 1 -> {
                EvaluationFactory.undetermined("Undetermined if multiple received $treatmentMessage is counted as received for more than $maxWeeks weeks")
            }

            TreatmentEvaluation.HAS_HAD_TREATMENT_FOR_AT_MOST_WEEKS in treatmentEvaluations -> {
                val maxWeeksString = if (maxWeeks != null) " for less than $maxWeeks weeks" else ""
                EvaluationFactory.pass("Has received $treatmentMessage$maxWeeksString")

            }

            TreatmentEvaluation.HAS_HAD_TREATMENT_AND_UNCLEAR_WEEKS in treatmentEvaluations -> {
                EvaluationFactory.undetermined("Has received $treatmentMessage but unknown nb of weeks")
            }

            TreatmentEvaluation.HAS_HAD_UNCLEAR_TREATMENT_OR_TRIAL in treatmentEvaluations -> {
                EvaluationFactory.undetermined("Undetermined if treatment received in previous trial contained $treatmentMessage for at most $maxWeeks weeks")
            }

            else -> {
                EvaluationFactory.fail("Has not received $treatmentMessage")
            }
        }
    }

    private enum class TreatmentEvaluation {
        HAS_HAD_TREATMENT_FOR_AT_MOST_WEEKS,
        HAS_HAD_TREATMENT_AND_UNCLEAR_WEEKS,
        HAS_HAD_UNCLEAR_TREATMENT_OR_TRIAL,
        HAS_HAD_TREATMENT_WITH_EXCESSIVE_WEEKS,
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
                (hadTreatment == null || hadTrial) && (lessThanMaxWeeks || hadUnclearWeeks) -> HAS_HAD_UNCLEAR_TREATMENT_OR_TRIAL
                hadTreatment == true -> HAS_HAD_TREATMENT_WITH_EXCESSIVE_WEEKS
                else -> NO_MATCH
            }
        }
    }

    private fun lessThanMaxWeeks(durationWeeksMax: Long?) = maxWeeks == null || (durationWeeksMax != null && durationWeeksMax <= maxWeeks)
    private fun hadUnclearWeeks(durationWeeks: Long?, durationWeeksMax: Long?) =
        maxWeeks == null || (durationWeeks == null && ((durationWeeksMax != null && durationWeeksMax > maxWeeks) || durationWeeksMax == null))
}