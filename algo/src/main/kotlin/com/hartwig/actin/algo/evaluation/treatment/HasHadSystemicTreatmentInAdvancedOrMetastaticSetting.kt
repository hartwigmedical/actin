package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.Format.concatWithCommaAndAnd
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.treatment.history.Intent
import com.hartwig.actin.datamodel.clinical.treatment.history.TreatmentHistoryEntry
import java.time.LocalDate

class HasHadSystemicTreatmentInAdvancedOrMetastaticSetting(private val referenceDate: LocalDate) : EvaluationFunction {
    override fun evaluate(record: PatientRecord): Evaluation {
        val priorSystemicTreatments = record.oncologicalHistory.filter { entry -> entry.treatments.any { it.isSystemic } }
        val (curativeTreatments, nonCurativeTreatments) = priorSystemicTreatments.partition { it.intents?.contains(Intent.CURATIVE) == true }
        val (recentNonCurativeTreatments, nonRecentNonCurativeTreatments) = nonCurativeTreatments
            .partition { TreatmentSinceDateFunctions.treatmentSinceMinDate(it, referenceDate.minusMonths(6), false) }
        val nonCurativeTreatmentsWithUnknownStopDate = nonCurativeTreatments.filter { it.treatmentHistoryDetails?.stopYear == null }
        val palliativeIntentTreatments = priorSystemicTreatments.filter { it.intents?.contains(Intent.PALLIATIVE) == true }

        return when {
            curativeTreatments.isNotEmpty() && nonCurativeTreatments.isEmpty() -> {
                EvaluationFactory.fail(
                    createMessage(
                        "Has only had prior systemic treatment with curative intent (thus presumably not in metastatic or advanced setting)",
                        priorSystemicTreatments
                    )
                )
            }

            palliativeIntentTreatments.isNotEmpty() -> {
                EvaluationFactory.pass(
                    createMessage(
                        "Has had prior systemic treatment in advanced or metastatic setting",
                        palliativeIntentTreatments
                    )
                )
            }

            recentNonCurativeTreatments.isNotEmpty() -> {
                EvaluationFactory.pass(
                    createMessage(
                        "Has had recent systemic treatment - presumably in metastatic or advanced setting",
                        recentNonCurativeTreatments
                    )
                )
            }

            nonCurativeTreatments.size > 2 -> {
                EvaluationFactory.pass(
                    createMessage(
                        "Has had more than two systemic lines with unknown or non-curative intent (presumably at least one in metastatic setting)",
                        nonCurativeTreatments
                    )
                )
            }

            nonCurativeTreatmentsWithUnknownStopDate.isNotEmpty() -> {
                EvaluationFactory.undetermined(
                    createMessage(
                        "Has had prior systemic treatment but undetermined if in advanced or metastatic setting",
                        nonCurativeTreatmentsWithUnknownStopDate
                    )
                )
            }

            nonRecentNonCurativeTreatments.isNotEmpty() -> {
                EvaluationFactory.undetermined(
                    createMessage(
                        "Has had prior systemic treatment >6 months ago but undetermined if in advanced or metastatic setting",
                        nonRecentNonCurativeTreatments
                    )
                )
            }

            else -> EvaluationFactory.fail("No prior systemic treatment in advanced or metastatic setting")
        }
    }

    private fun createMessage(string: String, treatments: List<TreatmentHistoryEntry>): String {
        return "$string (${concatWithCommaAndAnd(treatments.map { it.treatmentDisplay() })})"
    }
}