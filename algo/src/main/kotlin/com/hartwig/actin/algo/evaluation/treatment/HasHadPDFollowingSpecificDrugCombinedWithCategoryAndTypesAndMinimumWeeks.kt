package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.clinical.interpretation.ProgressiveDiseaseFunctions
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.treatment.Drug
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentType

private enum class PDFollowingSpecificCombinationEvaluation {
    HAS_SPECIFIC_COMBINATION_WITH_PD_AND_SUFFICIENT_WEEKS,
    HAS_SPECIFIC_COMBINATION_WITH_PD_AND_UNCLEAR_WEEKS,
    HAS_SPECIFIC_COMBINATION_WITH_PD_AND_INSUFFICIENT_WEEKS,
    HAS_SPECIFIC_COMBINATION_WITH_UNCLEAR_PD_STATUS,
    HAS_HAD_UNCLEAR_TREATMENT_OR_TRIAL,
    HAS_SPECIFIC_COMBINATION_WITH_NO_PD,
    NO_MATCH;

    companion object {
        fun create(
            hadTreatment: Boolean?,
            hadTrial: Boolean,
            hadPD: Boolean? = false,
            hadSufficientWeeks: Boolean = false,
            hadUnclearWeeks: Boolean = false
        ) = when {
            hadTreatment == true && hadPD == true && hadSufficientWeeks -> HAS_SPECIFIC_COMBINATION_WITH_PD_AND_SUFFICIENT_WEEKS
            hadTreatment == true && hadPD == true && hadUnclearWeeks -> HAS_SPECIFIC_COMBINATION_WITH_PD_AND_UNCLEAR_WEEKS
            hadTreatment == true && hadPD == true && !hadSufficientWeeks -> HAS_SPECIFIC_COMBINATION_WITH_PD_AND_INSUFFICIENT_WEEKS
            hadTreatment == true && hadPD == null -> HAS_SPECIFIC_COMBINATION_WITH_UNCLEAR_PD_STATUS
            (hadTreatment == null || hadTrial) && (hadSufficientWeeks || hadUnclearWeeks) -> HAS_HAD_UNCLEAR_TREATMENT_OR_TRIAL
            hadTreatment == true -> HAS_SPECIFIC_COMBINATION_WITH_NO_PD
            else -> NO_MATCH
        }
    }
}

class HasHadPDFollowingSpecificDrugCombinedWithCategoryAndTypesAndMinimumWeeks(
    private val drugToFind: Drug,
    private val category: TreatmentCategory,
    private val types: Set<TreatmentType>?,
    private val minWeeks: Int
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val specificDrugCombinedWithCategoryAndTypesEvaluator =
            SpecificDrugCombinedWithCategoryAndTypesEvaluator(drugToFind, category, types)

        val treatmentEvaluations = specificDrugCombinedWithCategoryAndTypesEvaluator.relevantHistory(record).map { treatmentHistoryEntry ->
            val mayMatchAsTrial = TrialFunctions.treatmentMayMatchAsTrial(treatmentHistoryEntry, setOf(category))

            val durationWeeks = TreatmentHistoryEntryFunctions.weeksBetweenDates(treatmentHistoryEntry)

            TreatmentHistoryEntryFunctions.portionOfTreatmentHistoryEntryMatchingPredicate(treatmentHistoryEntry) { treatment ->
                specificDrugCombinedWithCategoryAndTypesEvaluator.treatmentWithoutDrugMatchesCategoryAndType(treatment)
            }?.let { matchingPortionOfEntry ->
                val treatmentResultedInPD = ProgressiveDiseaseFunctions.treatmentResultedInPD(matchingPortionOfEntry)
                val durationWeeksMatchingPortion = TreatmentHistoryEntryFunctions.weeksBetweenDates(matchingPortionOfEntry)

                PDFollowingSpecificCombinationEvaluation.create(
                    hadTreatment = true,
                    hadTrial = mayMatchAsTrial,
                    hadPD = treatmentResultedInPD,
                    hadSufficientWeeks = correctNumberOfWeeks(
                        durationWeeksMatchingPortion,
                        null,
                        TreatmentDurationType.SUFFICIENT,
                        minWeeks
                    ),
                    hadUnclearWeeks = hadUnclearWeeks(durationWeeksMatchingPortion, null, TreatmentDurationType.SUFFICIENT, minWeeks)
                )
            } ?: PDFollowingSpecificCombinationEvaluation.create(
                hadTreatment = if (treatmentHistoryEntry.treatments.isEmpty()) null else false,
                hadTrial = mayMatchAsTrial,
                hadSufficientWeeks = correctNumberOfWeeks(durationWeeks, null, TreatmentDurationType.SUFFICIENT, minWeeks),
                hadUnclearWeeks = hadUnclearWeeks(durationWeeks, null, TreatmentDurationType.SUFFICIENT, minWeeks)
            )
        }

        val treatmentDesc = specificDrugCombinedWithCategoryAndTypesEvaluator.treatmentString()

        return when {
            treatmentEvaluations.size > 1 -> {
                EvaluationFactory.undetermined("Undetermined if multiple received $treatmentDesc is counted as received for at least $minWeeks weeks")
            }

            PDFollowingSpecificCombinationEvaluation.HAS_SPECIFIC_COMBINATION_WITH_PD_AND_SUFFICIENT_WEEKS in treatmentEvaluations -> {
                EvaluationFactory.pass("Has received $treatmentDesc with PD for at least $minWeeks weeks")
            }

            PDFollowingSpecificCombinationEvaluation.HAS_SPECIFIC_COMBINATION_WITH_PD_AND_UNCLEAR_WEEKS in treatmentEvaluations -> {
                EvaluationFactory.undetermined("Has received $treatmentDesc with PD but unknown nr of weeks")
            }

            PDFollowingSpecificCombinationEvaluation.HAS_SPECIFIC_COMBINATION_WITH_UNCLEAR_PD_STATUS in treatmentEvaluations -> {
                EvaluationFactory.undetermined("Has received $treatmentDesc but uncertain if there has been PD")
            }

            PDFollowingSpecificCombinationEvaluation.HAS_HAD_UNCLEAR_TREATMENT_OR_TRIAL in treatmentEvaluations -> {
                EvaluationFactory.undetermined("Undetermined if received $treatmentDesc")
            }

            PDFollowingSpecificCombinationEvaluation.HAS_SPECIFIC_COMBINATION_WITH_PD_AND_INSUFFICIENT_WEEKS in treatmentEvaluations -> EvaluationFactory.fail(
                "Has received $treatmentDesc with PD but for less than $minWeeks weeks"
            )

            PDFollowingSpecificCombinationEvaluation.HAS_SPECIFIC_COMBINATION_WITH_NO_PD in treatmentEvaluations -> EvaluationFactory.fail("No PD after $treatmentDesc")

            else -> EvaluationFactory.fail("Has not received $treatmentDesc")
        }
    }
}