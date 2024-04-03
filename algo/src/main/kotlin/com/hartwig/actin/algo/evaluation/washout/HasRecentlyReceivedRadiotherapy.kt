package com.hartwig.actin.algo.evaluation.washout

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory
import com.hartwig.actin.clinical.datamodel.treatment.history.TreatmentHistoryEntry
import java.time.YearMonth

class HasRecentlyReceivedRadiotherapy(
    private val referenceYear: Int, private val referenceMonth: Int, private val requestedLocation: String? = null) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val radiotherapyEvaluations = record.oncologicalHistory.filter { it.categories().contains(TreatmentCategory.RADIOTHERAPY) }
            .map(::evaluateRadiotherapyEntry).toSet()
        val bodyLocationMessage = "to body location $requestedLocation "

        return when {
            radiotherapyEvaluations.any { (rightTime, rightPlace) -> rightTime == true && rightPlace == true } -> {
                EvaluationFactory.pass(
                    "Patient has recently received radiotherapy $bodyLocationMessage- pay attention to washout period",
                    "Has recently received radiotherapy $bodyLocationMessage- pay attention to washout period"
                )
            }
            radiotherapyEvaluations.any { (rightTime, rightPlace) -> rightTime == null && rightPlace == true } -> {
                EvaluationFactory.undetermined(
                    "Has received prior radiotherapy $bodyLocationMessage" + "with unknown date - if recent: pay attention to washout period",
                    "Has received prior radiotherapy $bodyLocationMessage" + "with unknown date - pay attention to washout period")
            }
            radiotherapyEvaluations.any { (rightTime, rightPlace) -> rightTime == true && rightPlace == null } -> {
                EvaluationFactory.recoverableUndetermined(
                    "Patient has received radiotherapy but undetermined if target location was $requestedLocation",
                    "Undetermined recent $requestedLocation radiation therapy"
                )
            }
            radiotherapyEvaluations.any { (rightTime, rightPlace) -> rightTime == null && rightPlace == null } -> {
                EvaluationFactory.recoverableUndetermined(
                    "Patient has received prior radiotherapy but undetermined if recent (date unknown) and if $bodyLocationMessage",
                    "Has received prior radiotherapy but undetermined if recent (date unknown) and if $bodyLocationMessage"
                )
            }
            else -> {
                EvaluationFactory.fail("Patient has not recently received radiotherapy $bodyLocationMessage",
                    "No recent radiotherapy $bodyLocationMessage")
            }
        }
    }

    private fun evaluateRadiotherapyEntry(entry: TreatmentHistoryEntry): Pair<Boolean?, Boolean?> {
        val rightTime = entry.startYear?.let { year ->
            val month = entry.startMonth
            year >= referenceYear && (month == null || (month >= referenceMonth ||
                    YearMonth.of(year, month).isAfter(YearMonth.of(referenceYear, referenceMonth))))
        }

        val rightPlace = if (requestedLocation != null) {
            entry.treatmentHistoryDetails?.bodyLocations?.any { location ->
                location.lowercase().contains(requestedLocation.lowercase())
            }
        } else true

        return Pair(rightTime, rightPlace)
    }
}