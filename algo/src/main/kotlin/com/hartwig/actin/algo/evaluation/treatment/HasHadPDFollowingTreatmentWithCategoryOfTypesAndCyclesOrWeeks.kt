package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationFactory.fail
import com.hartwig.actin.algo.evaluation.EvaluationFactory.pass
import com.hartwig.actin.algo.evaluation.EvaluationFactory.recoverableUndetermined
import com.hartwig.actin.algo.evaluation.EvaluationFactory.undetermined
import com.hartwig.actin.algo.evaluation.EvaluationFactory.warn
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.EvaluationLabels
import com.hartwig.actin.algo.evaluation.util.Format
import com.hartwig.actin.clinical.interpretation.ProgressiveDiseaseFunctions
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentType

class HasHadPDFollowingTreatmentWithCategoryOfTypesAndCyclesOrWeeks(
    private val category: TreatmentCategory,
    private val types: Set<TreatmentType>, private val minCycles: Int?, private val minWeeks: Int?,
    private val labels: EvaluationLabels.Treatment
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val history = record.oncologicalHistory
        val treatmentEvaluations = history.map { treatmentHistoryEntry ->
            val mayMatchAsTrial = TrialFunctions.treatmentMayMatchAsTrial(treatmentHistoryEntry, setOf(category))
            val categoryMatches = treatmentHistoryEntry.categories().contains(category)

            TreatmentHistoryEntryFunctions.portionOfTreatmentHistoryEntryMatchingPredicate(treatmentHistoryEntry) {
                categoryMatches && treatmentHistoryEntry.matchesTypeFromSet(types) == true
            }?.let { matchingPortionOfEntry ->
                val cycles = matchingPortionOfEntry.treatmentHistoryDetails?.cycles
                val treatmentResultedInPD = ProgressiveDiseaseFunctions.treatmentResultedInPD(matchingPortionOfEntry, history)

                val durationWeeks = TreatmentHistoryEntryFunctions.weeksBetweenDates(matchingPortionOfEntry)
                val meetsMinCycles = minCycles == null || (cycles != null && cycles >= minCycles)
                val meetsMinWeeks = minWeeks == null || (durationWeeks != null && durationWeeks >= minWeeks)

                PDFollowingTreatmentEvaluation.create(
                    hadTreatment = true,
                    hadTrial = mayMatchAsTrial,
                    hadPD = treatmentResultedInPD,
                    hadCycles = meetsMinCycles,
                    hadWeeks = meetsMinWeeks,
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
                pass(hasTreatmentMessage(suffix()))
            }

            PDFollowingTreatmentEvaluation.HAS_HAD_TREATMENT_WITH_PD_AND_UNCLEAR_CYCLES in treatmentEvaluations -> {
                undetermined(hasTreatmentMessage(labels.hasHadPDFollowingTreatmentWithCategoryOfTypesAndCyclesOrWeeksSuffixUnknownCycles()))
            }

            PDFollowingTreatmentEvaluation.HAS_HAD_TREATMENT_WITH_PD_AND_UNCLEAR_WEEKS in treatmentEvaluations -> {
                undetermined(hasTreatmentMessage(labels.hasHadPDFollowingTreatmentWithCategoryOfTypesAndCyclesOrWeeksSuffixUnknownWeeks()))
            }

            PDFollowingTreatmentEvaluation.HAS_HAD_TREATMENT_WITH_UNCLEAR_PD_STATUS in treatmentEvaluations -> {
                recoverableUndetermined(
                    labels.hasHadPDFollowingTreatmentWithCategoryOfTypesAndCyclesOrWeeksRecoverableUndeterminedUncertainPd(treatment())
                )
            }

            PDFollowingTreatmentEvaluation.HAS_HAD_TREATMENT_WITH_UNCLEAR_PD_STATUS_AND_UNCLEAR_CYCLES in treatmentEvaluations -> {
                recoverableUndetermined(
                    labels.hasHadPDFollowingTreatmentWithCategoryOfTypesAndCyclesOrWeeksRecoverableUndeterminedUncertainPdUnknownCycles(treatment())
                )
            }

            PDFollowingTreatmentEvaluation.HAS_HAD_TREATMENT_WITH_UNCLEAR_PD_STATUS_AND_UNCLEAR_WEEKS in treatmentEvaluations -> {
                recoverableUndetermined(
                    labels.hasHadPDFollowingTreatmentWithCategoryOfTypesAndCyclesOrWeeksRecoverableUndeterminedUncertainPdUnclearWeeks(treatment())
                )
            }

            PDFollowingTreatmentEvaluation.HAS_HAD_UNCLEAR_TREATMENT_OR_TRIAL in treatmentEvaluations -> {
                undetermined(labels.hasHadPDFollowingTreatmentWithCategoryOfTypesAndCyclesOrWeeksUndeterminedIfReceived(treatment()))
            }

            PDFollowingTreatmentEvaluation.HAS_HAD_TREATMENT_WITH_PD_AND_INSUFFICIENT_CYCLES in treatmentEvaluations -> {
                warn(hasTreatmentMessage(labels.hasHadPDFollowingTreatmentWithCategoryOfTypesAndCyclesOrWeeksSuffixLessThanCycles(minCycles)))
            }

            PDFollowingTreatmentEvaluation.HAS_HAD_TREATMENT_WITH_PD_AND_INSUFFICIENT_WEEKS in treatmentEvaluations -> {
                fail(hasTreatmentMessage(labels.hasHadPDFollowingTreatmentWithCategoryOfTypesAndCyclesOrWeeksSuffixLessThanWeeks(minWeeks)))
            }

            PDFollowingTreatmentEvaluation.HAS_HAD_TREATMENT in treatmentEvaluations -> fail(hasNoPDAfterMessage(suffix()))

            else -> fail(hasNoTreatmentMessage(suffix()))
        }
    }

    private fun hasTreatmentMessage(suffix: String = ""): String {
        return labels.hasHadPDFollowingTreatmentWithCategoryOfTypesAndCyclesOrWeeksMessage(treatment(), suffix)
    }

    private fun hasNoPDAfterMessage(suffix: String = ""): String {
        return labels.hasHadPDFollowingTreatmentWithCategoryOfTypesAndCyclesOrWeeksFailNoPdAfter(category.display(), suffix)
    }

    private fun hasNoTreatmentMessage(suffix: String = ""): String {
        return labels.hasHadPDFollowingTreatmentWithCategoryOfTypesAndCyclesOrWeeksFailNoTreatment(treatment(), suffix)
    }

    private fun suffix(): String = when {
        minCycles == null && minWeeks == null -> ""
        minCycles != null -> labels.hasHadPDFollowingTreatmentWithCategoryOfTypesAndCyclesOrWeeksSuffixAndAtLeastCycles(minCycles)
        else -> labels.hasHadPDFollowingTreatmentWithCategoryOfTypesAndCyclesOrWeeksSuffixForAtLeastWeeks(minWeeks!!)
    }

    private fun treatment(): String {
        return labels.hasHadPDFollowingTreatmentWithCategoryOfTypesAndCyclesOrWeeksTreatmentDescription(
            Format.concatItemsWithOr(types),
            category.display()
        )
    }

    private enum class PDFollowingTreatmentEvaluation {
        HAS_HAD_TREATMENT_WITH_PD_AND_CYCLES_OR_WEEKS,
        HAS_HAD_TREATMENT_WITH_PD_AND_UNCLEAR_CYCLES,
        HAS_HAD_TREATMENT_WITH_PD_AND_UNCLEAR_WEEKS,
        HAS_HAD_TREATMENT_WITH_UNCLEAR_PD_STATUS,
        HAS_HAD_TREATMENT_WITH_UNCLEAR_PD_STATUS_AND_UNCLEAR_CYCLES,
        HAS_HAD_TREATMENT_WITH_UNCLEAR_PD_STATUS_AND_UNCLEAR_WEEKS,
        HAS_HAD_UNCLEAR_TREATMENT_OR_TRIAL,
        HAS_HAD_TREATMENT_WITH_PD_AND_INSUFFICIENT_CYCLES,
        HAS_HAD_TREATMENT_WITH_PD_AND_INSUFFICIENT_WEEKS,
        HAS_HAD_TREATMENT,
        NO_MATCH;

        companion object {
            fun create(
                hadTreatment: Boolean?,
                hadTrial: Boolean,
                hadPD: Boolean? = false,
                hadCycles: Boolean = false,
                hadWeeks: Boolean = false,
                hadUnclearCycles: Boolean = false,
                hadUnclearWeeks: Boolean = false
            ) = when {
                hadTreatment == true && hadPD == true && hadCycles && hadWeeks -> HAS_HAD_TREATMENT_WITH_PD_AND_CYCLES_OR_WEEKS
                hadTreatment == true && hadPD == true && hadUnclearCycles -> HAS_HAD_TREATMENT_WITH_PD_AND_UNCLEAR_CYCLES
                hadTreatment == true && hadPD == true && hadUnclearWeeks -> HAS_HAD_TREATMENT_WITH_PD_AND_UNCLEAR_WEEKS
                hadTreatment == true && hadPD == null && hadUnclearCycles -> HAS_HAD_TREATMENT_WITH_UNCLEAR_PD_STATUS_AND_UNCLEAR_CYCLES
                hadTreatment == true && hadPD == null && hadUnclearWeeks -> HAS_HAD_TREATMENT_WITH_UNCLEAR_PD_STATUS_AND_UNCLEAR_WEEKS
                hadTreatment == true && hadPD == null -> HAS_HAD_TREATMENT_WITH_UNCLEAR_PD_STATUS
                hadTreatment == null || hadTrial -> HAS_HAD_UNCLEAR_TREATMENT_OR_TRIAL
                hadTreatment && hadPD == true && !hadCycles -> HAS_HAD_TREATMENT_WITH_PD_AND_INSUFFICIENT_CYCLES
                hadTreatment && hadPD == true && !hadWeeks -> HAS_HAD_TREATMENT_WITH_PD_AND_INSUFFICIENT_WEEKS
                hadTreatment -> HAS_HAD_TREATMENT
                else -> NO_MATCH
            }
        }
    }
}
