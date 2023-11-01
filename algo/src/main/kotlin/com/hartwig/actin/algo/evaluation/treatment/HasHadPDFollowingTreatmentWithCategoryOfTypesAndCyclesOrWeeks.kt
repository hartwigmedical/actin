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

class HasHadPDFollowingTreatmentWithCategoryOfTypesAndCyclesOrWeeks(
    private val category: TreatmentCategory,
    private val types: Set<TreatmentType>, private val minCycles: Int?, private val minWeeks: Int?
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        var hasHadTreatment = false
        var hasPotentiallyHadTreatment = false
        var hasHadTreatmentWithPDAndCyclesOrWeeks = false
        var hasHadTreatmentWithPDAndUnclearCycles = false
        var hasHadTreatmentWithPDAndUnclearWeeks = false
        var hasHadTreatmentWithUnclearPDStatus = false
        var hasHadTreatmentWithUnclearPDStatusAndUnclearCycles = false
        var hasHadTreatmentWithUnclearPDStatusAndUnclearWeeks = false
        var hasHadTrial = false
        for (treatmentHistoryEntry in record.clinical().treatmentHistory()) {
            if (treatmentHistoryEntry.categories().contains(category)) {
                if (treatmentHistoryEntry.matchesTypeFromSet(types) == true) {
                    hasHadTreatment = true
                    val cycles = treatmentHistoryEntry.treatmentHistoryDetails()?.cycles()
                    val treatmentResultedInPDOption = ProgressiveDiseaseFunctions.treatmentResultedInPDOption(treatmentHistoryEntry)
                    val durationWeeks: Long? = minWeeksBetweenDates(
                        treatmentHistoryEntry.startYear(),
                        treatmentHistoryEntry.startMonth(),
                        treatmentHistoryEntry.treatmentHistoryDetails()?.stopYear(),
                        treatmentHistoryEntry.treatmentHistoryDetails()?.stopMonth()
                    )
                    if (treatmentResultedInPDOption != null) {
                        val meetsMinCycles = minCycles == null || cycles != null && cycles >= minCycles
                        val meetsMinWeeks = when (minWeeks) {
                            null -> true
                            else -> durationWeeks?.let { it >= minWeeks } ?: false
                        }
                        if (treatmentResultedInPDOption) {
                            if (meetsMinCycles && meetsMinWeeks) {
                                hasHadTreatmentWithPDAndCyclesOrWeeks = true
                            } else if (minCycles != null && cycles == null) {
                                hasHadTreatmentWithPDAndUnclearCycles = true
                            } else if (minWeeks != null && durationWeeks == null) {
                                hasHadTreatmentWithPDAndUnclearWeeks = true
                            }
                        }
                    } else if (minCycles == null && minWeeks == null) {
                        hasHadTreatmentWithUnclearPDStatus = true
                    } else if (minCycles != null && cycles == null) {
                        hasHadTreatmentWithUnclearPDStatusAndUnclearCycles = true
                    } else if (minWeeks != null && durationWeeks == null) {
                        hasHadTreatmentWithUnclearPDStatusAndUnclearWeeks = true
                    }
                } else if (!treatmentHistoryEntry.hasTypeConfigured()) {
                    hasPotentiallyHadTreatment = true
                }
            }
            if (TreatmentSummaryForCategory.treatmentMayMatchCategoryAsTrial(treatmentHistoryEntry, category)) {
                hasHadTrial = true
            }
        }
        return when {
            hasHadTreatmentWithPDAndCyclesOrWeeks -> {
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

            hasHadTreatmentWithPDAndUnclearCycles -> {
                undetermined(" with PD but unknown nr of cycles")
            }

            hasHadTreatmentWithPDAndUnclearWeeks -> {
                undetermined(" with PD but unknown nr of weeks")
            }

            hasHadTreatmentWithUnclearPDStatus -> {
                undetermined(" with unclear PD status")
            }

            hasHadTreatmentWithUnclearPDStatusAndUnclearCycles -> {
                undetermined(" with unclear PD status & nr of cycles")
            }

            hasHadTreatmentWithUnclearPDStatusAndUnclearWeeks -> {
                undetermined(" with unclear PD status & nr of weeks")
            }

            hasPotentiallyHadTreatment || hasHadTrial -> {
                undetermined(
                    "Unclear whether patient has received " + treatment(),
                    "Unclear if received " + category.display()
                )
            }

            hasHadTreatment -> {
                fail("Patient has received " + treatment() + " but not with PD", "No PD after " + category.display())
            }

            else -> {
                fail("No " + treatment() + " treatment with PD", "No " + category.display())
            }
        }
    }

    private fun hasTreatmentSpecificMessage(suffix: String): String {
        return "Patient has received " + treatment() + suffix
    }

    private fun hasTreatmentGeneralMessage(suffix: String): String {
        return category.display() + suffix
    }

    private fun undetermined(suffix: String): Evaluation {
        return undetermined(hasTreatmentSpecificMessage(suffix), hasTreatmentGeneralMessage(suffix))
    }

    private fun treatment(): String {
        return "${concatItems(types)} ${category.display()} treatment"
    }
}