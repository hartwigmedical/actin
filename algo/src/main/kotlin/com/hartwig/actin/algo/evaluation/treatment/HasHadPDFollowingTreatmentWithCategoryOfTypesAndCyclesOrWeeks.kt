package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory.fail
import com.hartwig.actin.algo.evaluation.EvaluationFactory.pass
import com.hartwig.actin.algo.evaluation.EvaluationFactory.undetermined
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.DateComparison.minWeeksBetweenDates
import com.hartwig.actin.algo.evaluation.util.Format.concatItems
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentType
import com.hartwig.actin.clinical.datamodel.treatment.history.TreatmentStage

class HasHadPDFollowingTreatmentWithCategoryOfTypesAndCyclesOrWeeks(
    private val category: TreatmentCategory,
    private val types: Set<TreatmentType>, private val minCycles: Int?, private val minWeeks: Int?
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val treatmentEvaluations = record.clinical().treatmentHistory().map { treatmentHistoryEntry ->
            val isTrial = TreatmentSummaryForCategory.treatmentMayMatchCategoryAsTrial(treatmentHistoryEntry, category)
            if (treatmentHistoryEntry.categories().contains(category)) {
                if (treatmentHistoryEntry.matchesTypeFromSet(types) == true) {
                    val cycles = treatmentHistoryEntry.treatmentHistoryDetails()?.cycles()
                    val treatmentResultedInPD = ProgressiveDiseaseFunctions.treatmentResultedInPD(treatmentHistoryEntry)
                    val (stopYear, stopMonth) = treatmentHistoryEntry.treatmentHistoryDetails()?.let { details ->
                        val switchToTreatmentsWithDates = details.switchToTreatments()?.filter(::treatmentStageWithDateDoesNotMatch)
                        when {
                            !switchToTreatmentsWithDates.isNullOrEmpty() -> {
                                switchToTreatmentsWithDates.map(::datesForTreatmentStage)
                                    .sortedWith(compareBy<Pair<Int?, Int?>> { it.first!! }.thenBy { it.second ?: Int.MAX_VALUE })
                                    .first()
                            }

                            treatmentStageWithDateDoesNotMatch(details.maintenanceTreatment()) -> {
                                datesForTreatmentStage(details.maintenanceTreatment())
                            }

                            else -> Pair(details.stopYear(), details.stopMonth())
                        }
                    } ?: Pair(null, null)
                    
                    val durationWeeks: Long? = minWeeksBetweenDates(
                        treatmentHistoryEntry.startYear(),
                        treatmentHistoryEntry.startMonth(),
                        stopYear,
                        stopMonth
                    )
                    val meetsMinCycles = minCycles == null || cycles != null && cycles >= minCycles
                    val meetsMinWeeks = when (minWeeks) {
                        null -> true
                        else -> durationWeeks?.let { it >= minWeeks } ?: false
                    }
                    PDFollowingTreatmentEvaluation.create(
                        hadTreatment = true,
                        hadPD = treatmentResultedInPD,
                        hadCyclesOrWeeks = meetsMinCycles && meetsMinWeeks,
                        hadUnclearCycles = minCycles != null && cycles == null,
                        hadUnclearWeeks = minWeeks != null && durationWeeks == null,
                        hadTrial = isTrial
                    )
                } else if (!treatmentHistoryEntry.hasTypeConfigured()) {
                    PDFollowingTreatmentEvaluation.create(null, hadTrial = isTrial)
                } else {
                    PDFollowingTreatmentEvaluation.create(false, hadTrial = isTrial)
                }
            } else {
                PDFollowingTreatmentEvaluation.create(false, hadTrial = isTrial)
            }
        }
            .toSet()
        
        return when {
            PDFollowingTreatmentEvaluation.HAS_HAD_TREATMENT_WITH_PD_AND_CYCLES_OR_WEEKS in treatmentEvaluations -> {
                if (minCycles == null && minWeeks == null) {
                    pass(hasTreatmentSpecificMessage(" with PD"), hasTreatmentGeneralMessage(" with PD"))
                } else if (minCycles != null) {
                    pass(
                        hasTreatmentSpecificMessage(" with PD and at least $minCycles cycles"),
                        hasTreatmentGeneralMessage(" with PD and sufficient cycles")
                    )
                } else {
                    pass(
                        hasTreatmentSpecificMessage(" with PD for at least $minWeeks weeks"),
                        hasTreatmentGeneralMessage(" with PD for sufficient weeks")
                    )
                }
            }

            PDFollowingTreatmentEvaluation.HAS_HAD_TREATMENT_WITH_PD_AND_UNCLEAR_CYCLES in treatmentEvaluations -> {
                undetermined(" with PD but unknown nr of cycles")
            }

            PDFollowingTreatmentEvaluation.HAS_HAD_TREATMENT_WITH_PD_AND_UNCLEAR_WEEKS in treatmentEvaluations -> {
                undetermined(" with PD but unknown nr of weeks")
            }

            PDFollowingTreatmentEvaluation.HAS_HAD_TREATMENT_WITH_UNCLEAR_PD_STATUS in treatmentEvaluations -> {
                undetermined(" but uncertain if there has been PD")
            }

            PDFollowingTreatmentEvaluation.HAS_HAD_TREATMENT_WITH_UNCLEAR_PD_STATUS_AND_UNCLEAR_CYCLES in treatmentEvaluations -> {
                undetermined(" but uncertain if there has been PD & unknown nr of cycles")
            }

            PDFollowingTreatmentEvaluation.HAS_HAD_TREATMENT_WITH_UNCLEAR_PD_STATUS_AND_UNCLEAR_WEEKS in treatmentEvaluations -> {
                undetermined(" but uncertain if there has been PD & unclear nr of weeks")
            }

            PDFollowingTreatmentEvaluation.HAS_HAD_UNCLEAR_TREATMENT_OR_TRIAL in treatmentEvaluations -> {
                undetermined(
                    "Unclear whether patient has received " + treatment(),
                    "Unclear if received " + category.display()
                )
            }

            PDFollowingTreatmentEvaluation.HAS_HAD_TREATMENT in treatmentEvaluations -> {
                fail("Patient has received " + treatment() + " but not with PD", "No PD after " + category.display())
            }

            else -> {
                fail("No " + treatment() + " treatment with PD", "No " + category.display())
            }
        }
    }

    private fun datesForTreatmentStage(treatmentStage: TreatmentStage?) =
        Pair(treatmentStage?.startYear(), treatmentStage?.startMonth())

    private fun treatmentStageWithDateDoesNotMatch(treatmentStage: TreatmentStage?) =
        ((treatmentStage?.treatment()?.types()?.intersect(types)?.isEmpty() ?: false) && treatmentStage?.startYear() != null)

    private fun hasTreatmentSpecificMessage(suffix: String): String {
        return "Patient has received " + treatment() + suffix
    }

    private fun hasTreatmentGeneralMessage(suffix: String): String {
        return "Patient has had " + treatment() + suffix
    }

    private fun undetermined(suffix: String): Evaluation {
        return undetermined(hasTreatmentSpecificMessage(suffix), hasTreatmentGeneralMessage(suffix))
    }

    private fun treatment(): String {
        return "${concatItems(types)} ${category.display()} treatment"
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
                hadTreatment: Boolean? = false,
                hadPD: Boolean? = false,
                hadCyclesOrWeeks: Boolean = false,
                hadUnclearCycles: Boolean = false,
                hadUnclearWeeks: Boolean = false,
                hadTrial: Boolean = false
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