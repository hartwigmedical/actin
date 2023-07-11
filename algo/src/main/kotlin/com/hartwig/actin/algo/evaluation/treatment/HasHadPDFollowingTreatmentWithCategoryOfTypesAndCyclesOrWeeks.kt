package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory.fail
import com.hartwig.actin.algo.evaluation.EvaluationFactory.pass
import com.hartwig.actin.algo.evaluation.EvaluationFactory.undetermined
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.DateComparison.minWeeksBetweenDates
import com.hartwig.actin.algo.evaluation.util.Format.concat
import com.hartwig.actin.clinical.datamodel.treatment.PriorTumorTreatment
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory

class HasHadPDFollowingTreatmentWithCategoryOfTypesAndCyclesOrWeeks(
    private val category: TreatmentCategory,
    private val types: List<String>, private val minCycles: Int?, private val minWeeks: Int?
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
        for (treatment in record.clinical().priorTumorTreatments()) {
            if (treatment.categories().contains(category)) {
                if (hasValidType(treatment)) {
                    hasHadTreatment = true
                    val cycles = treatment.cycles()
                    val treatmentResultedInPDOption = ProgressiveDiseaseFunctions.treatmentResultedInPDOption(treatment)
                    val durationWeeks: Long? = minWeeksBetweenDates(
                        treatment.startYear(),
                        treatment.startMonth(),
                        treatment.stopYear(),
                        treatment.stopMonth()
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
                } else if (!TreatmentTypeResolver.hasTypeConfigured(treatment, category)) {
                    hasPotentiallyHadTreatment = true
                }
            }
            if (TreatmentSummaryForCategory.treatmentMayMatchCategoryAsTrial(treatment, category)) {
                hasHadTrial = true
            }
        }
        return if (hasHadTreatmentWithPDAndCyclesOrWeeks) {
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
        } else if (hasHadTreatmentWithPDAndUnclearCycles) {
            undetermined(" with PD but unknown nr of cycles")
        } else if (hasHadTreatmentWithPDAndUnclearWeeks) {
            undetermined(" with PD but unknown nr of weeks")
        } else if (hasHadTreatmentWithUnclearPDStatus) {
            undetermined(" with unclear PD status")
        } else if (hasHadTreatmentWithUnclearPDStatusAndUnclearCycles) {
            undetermined(" with unclear PD status & nr of cycles")
        } else if (hasHadTreatmentWithUnclearPDStatusAndUnclearWeeks) {
            undetermined(" with unclear PD status & nr of weeks")
        } else if (hasPotentiallyHadTreatment || hasHadTrial) {
            undetermined(
                "Unclear whether patient has received " + treatment(),
                "Unclear if received " + category.display()
            )
        } else if (hasHadTreatment) {
            fail("Patient has received " + treatment() + " but not with PD", "No PD after " + category.display())
        } else {
            fail("No " + treatment() + " treatment with PD", "No " + category.display())
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

    private fun hasValidType(treatment: PriorTumorTreatment): Boolean {
        return types.any { TreatmentTypeResolver.isOfType(treatment, category, it) }
    }

    private fun treatment(): String {
        return concat(types) + " " + category.display() + " treatment"
    }
}