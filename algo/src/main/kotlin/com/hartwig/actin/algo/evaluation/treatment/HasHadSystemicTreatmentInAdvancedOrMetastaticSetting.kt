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
        val palliativeIntentTreatments = priorSystemicTreatments.filter { it.intents?.contains(Intent.PALLIATIVE) ?: false }

        return when {
            curativeTreatments.isNotEmpty() && nonCurativeTreatments.isEmpty() -> {
                val messageEnding = createMessageEnding(
                    "had prior systemic treatment with curative intent - thus presumably not in metastatic or advanced setting",
                    priorSystemicTreatments
                )
                EvaluationFactory.fail("Patient has $messageEnding", "Has $messageEnding")
            }

            palliativeIntentTreatments.isNotEmpty() -> {
                val messageEnding =
                    createMessageEnding("had prior systemic treatment in advanced or metastatic setting", palliativeIntentTreatments)
                EvaluationFactory.pass("Patient has $messageEnding", "Has $messageEnding")
            }

            recentNonCurativeTreatments.isNotEmpty() -> {
                val messageEnding =
                    createMessageEnding("had recent systemic treatment - presumably in metastatic or advanced setting", recentNonCurativeTreatments)
                EvaluationFactory.pass("Patient has $messageEnding", "Has $messageEnding")
            }

            nonCurativeTreatments.size > 2 -> {
                val messageEnding = createMessageEnding(
                    "had more than two systemic lines with unknown or non-curative intent - presumably at least one in metastatic setting",
                    nonCurativeTreatments
                )
                EvaluationFactory.pass("Patient has $messageEnding", "Has $messageEnding")
            }

            nonCurativeTreatmentsWithUnknownStopDate.isNotEmpty() -> {
                val messageEnding = createMessageEnding(
                    "had prior systemic treatment but undetermined if in advanced or metastatic setting",
                    nonCurativeTreatmentsWithUnknownStopDate
                )
                EvaluationFactory.undetermined("Patient has $messageEnding", "Has $messageEnding")
            }

            nonRecentNonCurativeTreatments.isNotEmpty() -> {
                val messageEnding = createMessageEnding(
                    "had prior systemic treatment >6 months ago - undetermined if in advanced or metastatic setting",
                    nonRecentNonCurativeTreatments
                )
                EvaluationFactory.undetermined("Patient has $messageEnding", "Has $messageEnding")
            }

            else -> {
                EvaluationFactory.fail(
                    "Patient has not had prior systemic treatment in advanced or metastatic setting",
                    "No prior systemic treatment in advanced or metastatic setting"
                )
            }
        }
    }

    private fun createMessageEnding(string: String, treatments: List<TreatmentHistoryEntry>): String {
        return "$string (${concatWithCommaAndAnd(treatments.map { it.treatmentDisplay() })})"
    }
}