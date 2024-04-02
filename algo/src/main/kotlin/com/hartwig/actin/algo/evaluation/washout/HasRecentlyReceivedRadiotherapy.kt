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
            .map { entry -> evaluateRadiotherapyEntry(entry)
        }
        val bodyLocationMessage = "to body location $requestedLocation "
        val weekEvaluation = radiotherapyEvaluations.map { it.first }
        val locationEvaluation = radiotherapyEvaluations.map { it.second }

        return if (radiotherapyEvaluations.any { weekEvaluation.any { it == true } && locationEvaluation.any { it == true } }) {
            EvaluationFactory.pass(
                "Patient has recently received radiotherapy $bodyLocationMessage- pay attention to washout period",
                "Has recently received radiotherapy $bodyLocationMessage- pay attention to washout period"
            )
        } else if (weekEvaluation.any { it == null } && locationEvaluation.all { it == true }) {
            EvaluationFactory.undetermined(
                "Has received prior radiotherapy with unknown date - if recent: pay attention to washout period",
                "Has received prior radiotherapy with unknown date - pay attention to washout period")
        } else if (weekEvaluation.any { it == true } && locationEvaluation.any { it == null }) {
            EvaluationFactory.recoverableUndetermined(
                "Patient has received radiotherapy but undetermined if target location was $requestedLocation - assuming not",
                "Undetermined recent $requestedLocation radiation therapy - assuming none")
        } else {
            EvaluationFactory.fail("Patient has not recently received radiotherapy $bodyLocationMessage",
                "No recent radiotherapy $bodyLocationMessage")
        }
    }

    private fun evaluateRadiotherapyEntry(entry: TreatmentHistoryEntry): Pair<Boolean?, Boolean?> {
        val withinWeeks = entry.startYear?.let { year ->
            val month = entry.startMonth
            year >= referenceYear && (month == null || (month >= referenceMonth ||
                    YearMonth.of(year, month).isAfter(YearMonth.of(referenceYear, referenceMonth))))
        }

        val toBodyLocation = if (requestedLocation != null) {
            entry.treatmentHistoryDetails?.bodyLocations?.any { location ->
                location.lowercase().contains(requestedLocation.lowercase())
            }
        } else true

        return Pair(withinWeeks, toBodyLocation)
    }
}