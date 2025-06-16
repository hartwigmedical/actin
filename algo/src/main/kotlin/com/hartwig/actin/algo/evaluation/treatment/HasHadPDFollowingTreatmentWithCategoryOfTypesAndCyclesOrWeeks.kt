package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationFactory.fail
import com.hartwig.actin.algo.evaluation.EvaluationFactory.pass
import com.hartwig.actin.algo.evaluation.EvaluationFactory.recoverableUndetermined
import com.hartwig.actin.algo.evaluation.EvaluationFactory.undetermined
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.DateComparison.minWeeksBetweenDates
import com.hartwig.actin.algo.evaluation.util.Format
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentType

class HasHadPDFollowingTreatmentWithCategoryOfTypesAndCyclesOrWeeks(
    private val category: TreatmentCategory,
    private val types: Set<TreatmentType>, private val minCycles: Int?, private val minWeeks: Int?
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val treatmentEvaluations = record.oncologicalHistory.map { treatmentHistoryEntry ->
            val mayMatchAsTrial = TrialFunctions.treatmentMayMatchAsTrial(treatmentHistoryEntry, setOf(category))
            val categoryMatches = treatmentHistoryEntry.categories().contains(category)

            TreatmentHistoryEntryFunctions.portionOfTreatmentHistoryEntryMatchingPredicate(treatmentHistoryEntry) {
                categoryMatches && treatmentHistoryEntry.matchesTypeFromSet(types) == true
            }?.let { matchingPortionOfEntry ->
                val cycles = matchingPortionOfEntry.treatmentHistoryDetails?.cycles
                val treatmentResultedInPD = ProgressiveDiseaseFunctions.treatmentResultedInPD(matchingPortionOfEntry)

                val durationWeeks: Long? = minWeeksBetweenDates(
                    matchingPortionOfEntry.startYear,
                    matchingPortionOfEntry.startMonth,
                    matchingPortionOfEntry.treatmentHistoryDetails?.stopYear,
                    matchingPortionOfEntry.treatmentHistoryDetails?.stopMonth
                )
                val meetsMinCycles = minCycles == null || (cycles != null && cycles >= minCycles)
                val meetsMinWeeks = minWeeks == null || (durationWeeks != null && durationWeeks >= minWeeks)

                PDFollowingTreatmentEvaluation.create(
                    hadTreatment = true,
                    hadTrial = mayMatchAsTrial,
                    hadPD = treatmentResultedInPD,
                    hadCyclesOrWeeks = meetsMinCycles && meetsMinWeeks,
                    hadUnclearCycles = minCycles != null && cycles == null,
                    hadUnclearWeeks = minWeeks != null && durationWeeks == null
                )
            } ?: PDFollowingTreatmentEvaluation.create(
                hadTreatment = if (categoryMatches && !treatmentHistoryEntry.hasTypeConfigured()) null else false,
                hadTrial = mayMatchAsTrial
            )
        }
            .toSet()
        
        return when {
            PDFollowingTreatmentEvaluation.HAS_HAD_TREATMENT_WITH_PD_AND_CYCLES_OR_WEEKS in treatmentEvaluations -> {
                if (minCycles == null && minWeeks == null) {
                    pass(hasTreatmentMessage())
                } else if (minCycles != null) {
                    pass(hasTreatmentMessage(" and at least $minCycles cycles"))
                } else {
                    pass(hasTreatmentMessage(" for at least $minWeeks weeks"))
                }
            }

            PDFollowingTreatmentEvaluation.HAS_HAD_TREATMENT_WITH_PD_AND_UNCLEAR_CYCLES in treatmentEvaluations -> {
                undetermined(hasTreatmentMessage(" but unknown nr of cycles"))
            }

            PDFollowingTreatmentEvaluation.HAS_HAD_TREATMENT_WITH_PD_AND_UNCLEAR_WEEKS in treatmentEvaluations -> {
                undetermined(hasTreatmentMessage(" but unknown nr of weeks"))
            }

            PDFollowingTreatmentEvaluation.HAS_HAD_TREATMENT_WITH_UNCLEAR_PD_STATUS in treatmentEvaluations -> {
                recoverableUndetermined("Has received ${treatment()} but uncertain if there has been PD")
            }

            PDFollowingTreatmentEvaluation.HAS_HAD_TREATMENT_WITH_UNCLEAR_PD_STATUS_AND_UNCLEAR_CYCLES in treatmentEvaluations -> {
                recoverableUndetermined("Has received ${treatment()} but uncertain if there has been PD & unknown nr of cycles")
            }

            PDFollowingTreatmentEvaluation.HAS_HAD_TREATMENT_WITH_UNCLEAR_PD_STATUS_AND_UNCLEAR_WEEKS in treatmentEvaluations -> {
                recoverableUndetermined("Has received ${treatment()} but uncertain if there has been PD & unclear nr of weeks")
            }

            PDFollowingTreatmentEvaluation.HAS_HAD_UNCLEAR_TREATMENT_OR_TRIAL in treatmentEvaluations -> {
                undetermined(hasTreatmentMessage("Unclear if received " + category.display()))
            }

            PDFollowingTreatmentEvaluation.HAS_HAD_TREATMENT in treatmentEvaluations -> {
                fail("No PD after " + category.display())
            }

            else -> {
                fail("No ${treatment()} with PD")
            }
        }
    }

    private fun hasTreatmentMessage(suffix: String = ""): String {
        return "Has had ${treatment()} with PD$suffix"
    }

    private fun treatment(): String {
        return "${Format.concatItemsWithOr(types)} ${category.display()} treatment"
    }

    private enum class PDFollowingTreatmentEvaluation {
        HAS_HAD_TREATMENT_WITH_PD_AND_CYCLES_OR_WEEKS,
        HAS_HAD_TREATMENT_WITH_PD_AND_UNCLEAR_CYCLES,
        HAS_HAD_TREATMENT_WITH_PD_AND_UNCLEAR_WEEKS,
        HAS_HAD_TREATMENT_WITH_UNCLEAR_PD_STATUS,
        HAS_HAD_TREATMENT_WITH_UNCLEAR_PD_STATUS_AND_UNCLEAR_CYCLES,
        HAS_HAD_TREATMENT_WITH_UNCLEAR_PD_STATUS_AND_UNCLEAR_WEEKS,
        HAS_HAD_UNCLEAR_TREATMENT_OR_TRIAL,
        HAS_HAD_TREATMENT,
        NO_MATCH;

        companion object {
            fun create(
                hadTreatment: Boolean?,
                hadTrial: Boolean,
                hadPD: Boolean? = false,
                hadCyclesOrWeeks: Boolean = false,
                hadUnclearCycles: Boolean = false,
                hadUnclearWeeks: Boolean = false
            ) = when {
                hadTreatment == true && hadPD == true && hadCyclesOrWeeks -> HAS_HAD_TREATMENT_WITH_PD_AND_CYCLES_OR_WEEKS
                hadTreatment == true && hadPD == true && hadUnclearCycles -> HAS_HAD_TREATMENT_WITH_PD_AND_UNCLEAR_CYCLES
                hadTreatment == true && hadPD == true && hadUnclearWeeks -> HAS_HAD_TREATMENT_WITH_PD_AND_UNCLEAR_WEEKS
                hadTreatment == true && hadPD == null && hadUnclearCycles -> HAS_HAD_TREATMENT_WITH_UNCLEAR_PD_STATUS_AND_UNCLEAR_CYCLES
                hadTreatment == true && hadPD == null && hadUnclearWeeks -> HAS_HAD_TREATMENT_WITH_UNCLEAR_PD_STATUS_AND_UNCLEAR_WEEKS
                hadTreatment == true && hadPD == null -> HAS_HAD_TREATMENT_WITH_UNCLEAR_PD_STATUS
                hadTreatment == null || hadTrial -> HAS_HAD_UNCLEAR_TREATMENT_OR_TRIAL
                hadTreatment == true -> HAS_HAD_TREATMENT
                else -> NO_MATCH
            }
        }
    }
}