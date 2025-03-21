package com.hartwig.actin.algo.evaluation.washout

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import com.hartwig.actin.datamodel.clinical.treatment.history.TreatmentHistoryEntry
import java.time.YearMonth

class HasRecentlyReceivedRadiotherapy(
    private val referenceYear: Int, private val referenceMonth: Int, private val requestedLocation: String? = null
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val radiotherapyEvaluations = record.oncologicalHistory.filter { it.categories().contains(TreatmentCategory.RADIOTHERAPY) }
            .map(::evaluateRadiotherapyEntry).toSet()
        val bodyLocationMessage = if (requestedLocation != null) " to body location $requestedLocation" else ""

        return when {
            radiotherapyEvaluations.any { (rightTime, rightPlace) -> rightTime == true && rightPlace == true } -> {
                EvaluationFactory.pass("Has recently received radiotherapy$bodyLocationMessage - pay attention to washout period")
            }

            radiotherapyEvaluations.any { (rightTime, rightPlace) -> rightTime == null && rightPlace == true } -> {
                EvaluationFactory.undetermined(
                    "Has received prior radiotherapy$bodyLocationMessage with unknown date - pay attention to washout period"
                )
            }

            radiotherapyEvaluations.any { (rightTime, rightPlace) -> rightTime == true && rightPlace == null } -> {
                EvaluationFactory.recoverableUndetermined("Undetermined if received radiotherapy had target location $requestedLocation")
            }

            radiotherapyEvaluations.any { (rightTime, rightPlace) -> rightTime == null && rightPlace == null } -> {
                EvaluationFactory.recoverableUndetermined(
                    "Has received prior radiotherapy but undetermined if recent (date unknown) and if$bodyLocationMessage"
                )
            }

            else -> {
                EvaluationFactory.fail("No recent radiotherapy$bodyLocationMessage")
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