package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.treatment.Treatment

class HasHadLimitedWeeksOfSpecificTreatment(
    private val treatmentToFind: Treatment,
    private val maxWeeks: Int?
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val treatmentToFindString = treatmentToFind.name.lowercase()
        val treatmentEvaluations = record.oncologicalHistory.map { treatmentHistoryEntry ->
            val potentialTrialCategory =
                treatmentToFind.categories().isEmpty() || treatmentToFind.categories().any(TrialFunctions::categoryAllowsTrialMatches)
            val mayMatchAsTrial = record.oncologicalHistory.any { it.isTrial && it.allTreatments().isEmpty() } && potentialTrialCategory
            val matchTreatment =
                record.oncologicalHistory.any { it.allTreatments().any { treatment -> treatment.name == treatmentToFind.name } }

            val durationWeeks = TreatmentHistoryEntryFunctions.durationWeeks(treatmentHistoryEntry)
            val durationWeeksMax = TreatmentHistoryEntryFunctions.durationWeeksMax(treatmentHistoryEntry)

            TreatmentHistoryEntryFunctions.portionOfTreatmentHistoryEntryMatchingPredicate(treatmentHistoryEntry) { matchTreatment }
                ?.let { matchingPortionOfEntry ->
                    val durationWeeksMatchingPortion = TreatmentHistoryEntryFunctions.durationWeeks(matchingPortionOfEntry)
                    val durationWeeksMaxMatchingPortion = TreatmentHistoryEntryFunctions.durationWeeksMax(matchingPortionOfEntry)

                    TreatmentEvaluation.create(
                        hadTreatment = true,
                        hadTrial = mayMatchAsTrial,
                        lessThanMaxWeeks = lessThanMaxWeeks(durationWeeksMaxMatchingPortion),
                        hadUnclearWeeks = hadUnclearWeeks(durationWeeksMatchingPortion, durationWeeksMaxMatchingPortion)
                    )
                } ?: TreatmentEvaluation.create(
                hadTreatment = if (matchTreatment) null else false,
                hadTrial = mayMatchAsTrial,
                lessThanMaxWeeks = lessThanMaxWeeks(durationWeeksMax),
                hadUnclearWeeks = hadUnclearWeeks(durationWeeks, durationWeeksMax)
            )
        }

        return when {
            TreatmentEvaluation.HAS_HAD_TREATMENT_FOR_AT_MOST_WEEKS in treatmentEvaluations -> {
                val maxWeeksString = if (maxWeeks != null) " for less than $maxWeeks weeks" else ""
                EvaluationFactory.pass("Has received $treatmentToFindString$maxWeeksString")
            }

            TreatmentEvaluation.HAS_HAD_TREATMENT_AND_UNCLEAR_WEEKS in treatmentEvaluations -> {
                EvaluationFactory.undetermined("Has received $treatmentToFindString but unknown nb of weeks")
            }

            TreatmentEvaluation.HAS_HAD_UNCLEAR_TREATMENT_OR_TRIAL in treatmentEvaluations -> {
                EvaluationFactory.undetermined("Undetermined if treatment received in previous trial contained $treatmentToFindString for at most $maxWeeks weeks")
            }

            TreatmentEvaluation.HAS_HAD_TREATMENT in treatmentEvaluations -> {
                EvaluationFactory.fail("Has had $treatmentToFindString treatment but for more than $maxWeeks weeks")
            }

            else -> {
                EvaluationFactory.fail("Has not received $treatmentToFindString")
            }
        }
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
                (hadTreatment == null || hadTrial) && (lessThanMaxWeeks || hadUnclearWeeks) -> HAS_HAD_UNCLEAR_TREATMENT_OR_TRIAL
                hadTreatment == true -> HAS_HAD_TREATMENT
                else -> NO_MATCH
            }
        }
    }

    private fun lessThanMaxWeeks(durationWeeksMax: Long?) = maxWeeks == null || (durationWeeksMax != null && durationWeeksMax <= maxWeeks)
    private fun hadUnclearWeeks(durationWeeks: Long?, durationWeeksMax: Long?) =
        maxWeeks == null || (durationWeeks == null && ((durationWeeksMax != null && durationWeeksMax > maxWeeks) || durationWeeksMax == null))
}