package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.DateComparison
import com.hartwig.actin.algo.evaluation.util.Format
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentType

class HasHadLimitedTreatmentsOfCategoryWithTypesAndStopReasonNotPD(
    private val category: TreatmentCategory, private val types: Set<TreatmentType>,
    private val maxWeeks: Int?
) : EvaluationFunction {
    override fun evaluate(record: PatientRecord): Evaluation {

        val treatmentEvaluations = record.oncologicalHistory.map { treatmentHistoryEntry ->
            val mayMatchAsTrial = TrialFunctions.treatmentMayMatchAsTrial(treatmentHistoryEntry, category)
            val categoryMatches = treatmentHistoryEntry.categories().contains(category)

            TreatmentHistoryEntryFunctions.portionOfTreatmentHistoryEntryMatchingPredicate(treatmentHistoryEntry) {
                categoryMatches && treatmentHistoryEntry.matchesTypeFromSet(types) == true
            }?.let { matchingPortionOfEntry ->
                val treatmentResultedInPD = ProgressiveDiseaseFunctions.treatmentResultedInPD(matchingPortionOfEntry)

                val durationWeeks: Long? = DateComparison.minWeeksBetweenDates(
                    matchingPortionOfEntry.startYear,
                    matchingPortionOfEntry.startMonth,
                    matchingPortionOfEntry.treatmentHistoryDetails?.stopYear,
                    matchingPortionOfEntry.treatmentHistoryDetails?.stopMonth
                )
                val meetsMaxWeeks = if (maxWeeks != null) durationWeeks != null && durationWeeks <= maxWeeks else true

                PDFollowingTreatmentEvaluation.create(
                    hadTreatment = true,
                    hadTrial = mayMatchAsTrial,
                    hadPD = treatmentResultedInPD,
                    lessThanMaxWeeks = meetsMaxWeeks,
                    hadUnclearWeeks = if (maxWeeks != null) durationWeeks == null else false
                )
            } ?: PDFollowingTreatmentEvaluation.create(
                hadTreatment = if (categoryMatches && !treatmentHistoryEntry.hasTypeConfigured()) null else false,
                hadTrial = mayMatchAsTrial
            )
        }.toSet()

        return when {
            PDFollowingTreatmentEvaluation.HAS_HAD_TREATMENT_WITHOUT_PD_AND_WEEKS in treatmentEvaluations -> {
                val suffix = if (maxWeeks != null) " for less than $maxWeeks weeks" else ""
                EvaluationFactory.pass(
                    hasTreatmentSpecificMessage(suffix),
                    hasTreatmentGeneralMessage(suffix)
                )
            }

            PDFollowingTreatmentEvaluation.HAS_HAD_TREATMENT_WITHOUT_PD_AND_UNCLEAR_WEEKS in treatmentEvaluations -> {
                undetermined("without stop reason PD but unknown nr of weeks")
            }

            PDFollowingTreatmentEvaluation.HAS_HAD_TREATMENT_WITH_UNCLEAR_PD_STATUS in treatmentEvaluations -> {
                val weekMessage = if (maxWeeks != null) "for less than $maxWeeks weeks " else ""
                val suffix = weekMessage + "but uncertain if there has been PD"
                undetermined(suffix)
            }

            PDFollowingTreatmentEvaluation.HAS_HAD_TREATMENT_WITH_UNCLEAR_PD_STATUS_AND_UNCLEAR_WEEKS in treatmentEvaluations -> {
                val weekMessage = if (maxWeeks != null) " & unclear nr of weeks " else ""
                val suffix = "but uncertain if there has been PD$weekMessage"
                undetermined(suffix)
            }

            PDFollowingTreatmentEvaluation.HAS_HAD_UNCLEAR_TREATMENT_OR_TRIAL in treatmentEvaluations -> {
                EvaluationFactory.undetermined(
                    "Unclear whether patient has received " + treatment(),
                    "Unclear if received " + category.display()
                )
            }

            PDFollowingTreatmentEvaluation.HAS_HAD_TREATMENT in treatmentEvaluations -> {
                EvaluationFactory.fail(
                    "Patient has received ${treatment()} but with stop reason PD",
                    "Has received ${treatment()} with stop reason PD"
                )
            }

            else -> {
                EvaluationFactory.fail("No ${treatment()} treatment with PD", "No " + category.display())
            }
        }
    }

    private fun hasTreatmentSpecificMessage(suffix: String = ""): String {
        return "Patient has received ${treatment()}$suffix without stop reason PD"
    }

    private fun hasTreatmentGeneralMessage(suffix: String = ""): String {
        return "Patient has had ${treatment()}$suffix without stop reason PD"
    }

    private fun undetermined(suffix: String): Evaluation {
        return EvaluationFactory.undetermined(
            "Patient has received ${treatment()} $suffix",
            "Has received ${treatment()} $suffix",
        )
    }

    private fun treatment(): String {
        return "${Format.concatItems(types)} ${category.display()} treatment"
    }

    private enum class PDFollowingTreatmentEvaluation {
        HAS_HAD_TREATMENT_WITHOUT_PD_AND_WEEKS,
        HAS_HAD_TREATMENT_WITHOUT_PD_AND_UNCLEAR_WEEKS,
        HAS_HAD_TREATMENT_WITH_UNCLEAR_PD_STATUS,
        HAS_HAD_TREATMENT_WITH_UNCLEAR_PD_STATUS_AND_UNCLEAR_WEEKS,
        HAS_HAD_UNCLEAR_TREATMENT_OR_TRIAL,
        HAS_HAD_TREATMENT,
        NO_MATCH;

        companion object {
            fun create(
                hadTreatment: Boolean?,
                hadTrial: Boolean,
                hadPD: Boolean? = null,
                lessThanMaxWeeks: Boolean = false,
                hadUnclearWeeks: Boolean = false
            ) = when {
                hadTreatment == true && hadPD == false && lessThanMaxWeeks -> HAS_HAD_TREATMENT_WITHOUT_PD_AND_WEEKS
                hadTreatment == true && hadPD == false && hadUnclearWeeks -> HAS_HAD_TREATMENT_WITHOUT_PD_AND_UNCLEAR_WEEKS
                hadTreatment == true && hadPD == null && hadUnclearWeeks -> HAS_HAD_TREATMENT_WITH_UNCLEAR_PD_STATUS_AND_UNCLEAR_WEEKS
                hadTreatment == true && hadPD == null -> HAS_HAD_TREATMENT_WITH_UNCLEAR_PD_STATUS
                hadTreatment == null || hadTrial -> HAS_HAD_UNCLEAR_TREATMENT_OR_TRIAL
                hadTreatment == true -> HAS_HAD_TREATMENT
                else -> NO_MATCH
            }
        }
    }
}