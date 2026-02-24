package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.treatment.Treatment
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import com.hartwig.actin.datamodel.clinical.treatment.history.TreatmentHistoryEntry

enum class TreatmentDurationType {
    LIMITED,
    SUFFICIENT
}

class TreatmentDurationEvaluator(
    private val matchesTreatment: (Treatment) -> Boolean?,
    private val specificMatchCannotBeDetermined: (TreatmentHistoryEntry) -> Boolean,
    private val potentialTrialCategories: Set<TreatmentCategory>,
    private val treatmentMessage: String,
    private val treatmentDurationType: TreatmentDurationType,
    private val weeks: Int?
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
                        correctNumberOfWeeks = correctNumberOfWeeks(durationWeeksMatchingPortion, durationWeeksMaxMatchingPortion),
                        hadUnclearWeeks = hadUnclearWeeks(durationWeeksMatchingPortion, durationWeeksMaxMatchingPortion)
                    )
                } ?: TreatmentEvaluation.create(
                    hadTreatment = if (specificMatchCannotBeDetermined(treatmentHistoryEntry)) null else false,
                    hadTrial = mayMatchAsTrial,
                correctNumberOfWeeks = correctNumberOfWeeks(durationWeeks, durationWeeksMax),
                    hadUnclearWeeks = hadUnclearWeeks(durationWeeks, durationWeeksMax)
                )
        }

        return when {
            treatmentDurationType == TreatmentDurationType.LIMITED && TreatmentEvaluation.HAS_HAD_TREATMENT_WITH_INCORRECT_WEEKS in treatmentEvaluations -> {
                EvaluationFactory.fail("Has had $treatmentMessage treatment but for ${durationMessageParts.first} $weeks weeks")
            }

            weeks != null && treatmentEvaluations.size > 1 -> {
                EvaluationFactory.undetermined("Undetermined if multiple received $treatmentMessage is counted as received for ${durationMessageParts.first} $weeks weeks")
            }

            TreatmentEvaluation.HAS_HAD_TREATMENT_FOR_CORRECT_WEEKS in treatmentEvaluations -> {
                val weeksString = if (weeks != null) " for ${durationMessageParts.second} $weeks weeks" else ""
                EvaluationFactory.pass("Has received $treatmentMessage$weeksString")

            }

            TreatmentEvaluation.HAS_HAD_TREATMENT_AND_UNCLEAR_WEEKS in treatmentEvaluations -> {
                EvaluationFactory.undetermined("Has received $treatmentMessage but unknown nb of weeks")
            }

            TreatmentEvaluation.HAS_HAD_UNCLEAR_TREATMENT_OR_TRIAL in treatmentEvaluations -> {
                EvaluationFactory.undetermined("Undetermined if treatment received in previous trial contained $treatmentMessage for ${durationMessageParts.second} $weeks weeks")
            }

            else -> {
                EvaluationFactory.fail("Has not received $treatmentMessage")
            }
        }
    }

    private enum class TreatmentEvaluation {
        HAS_HAD_TREATMENT_FOR_CORRECT_WEEKS,
        HAS_HAD_TREATMENT_AND_UNCLEAR_WEEKS,
        HAS_HAD_UNCLEAR_TREATMENT_OR_TRIAL,
        HAS_HAD_TREATMENT_WITH_INCORRECT_WEEKS,
        NO_MATCH;

        companion object {
            fun create(
                hadTreatment: Boolean?,
                hadTrial: Boolean,
                correctNumberOfWeeks: Boolean = false,
                hadUnclearWeeks: Boolean = false
            ) = when {
                hadTreatment == true && correctNumberOfWeeks -> HAS_HAD_TREATMENT_FOR_CORRECT_WEEKS
                hadTreatment == true && hadUnclearWeeks -> HAS_HAD_TREATMENT_AND_UNCLEAR_WEEKS
                (hadTreatment == null || hadTrial) && (correctNumberOfWeeks || hadUnclearWeeks) -> HAS_HAD_UNCLEAR_TREATMENT_OR_TRIAL
                hadTreatment == true -> HAS_HAD_TREATMENT_WITH_INCORRECT_WEEKS
                else -> NO_MATCH
            }
        }
    }

    val durationMessageParts = when (treatmentDurationType) {
        TreatmentDurationType.LIMITED -> Pair("more than", "less than")
        TreatmentDurationType.SUFFICIENT -> Pair("less than", "at least")
    }

    private fun correctNumberOfWeeks(durationWeeks: Long?, durationWeeksMax: Long?): Boolean =
        when (treatmentDurationType) {
            TreatmentDurationType.LIMITED -> weeks == null || (durationWeeksMax != null && durationWeeksMax <= weeks)
            TreatmentDurationType.SUFFICIENT -> weeks == null || (durationWeeks != null && durationWeeks >= weeks)
        }

    private fun hadUnclearWeeks(durationWeeks: Long?, durationWeeksMax: Long?): Boolean =
        when (treatmentDurationType) {
            TreatmentDurationType.LIMITED -> weeks == null || (durationWeeks == null && ((durationWeeksMax != null && durationWeeksMax > weeks) || durationWeeksMax == null))
            TreatmentDurationType.SUFFICIENT -> weeks != null && durationWeeks == null
        }
}