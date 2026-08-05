package com.hartwig.actin.algo.evaluation.washout

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.EvaluationLabels
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import com.hartwig.actin.datamodel.clinical.treatment.history.TreatmentHistoryEntry
import java.time.YearMonth

class HasRecentlyReceivedRadiotherapy(
    private val referenceYear: Int,
    private val referenceMonth: Int,
    private val requestedLocation: String? = null,
    private val labels: EvaluationLabels.Washout
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val radiotherapyEvaluations = record.oncologicalHistory.filter { it.categories().contains(TreatmentCategory.RADIOTHERAPY) }
            .map(::evaluateRadiotherapyEntry).toSet()
        val bodyLocationMessage = if (requestedLocation != null) labels.hasRecentlyReceivedRadiotherapySuffixBodyLocation(requestedLocation) else ""

        return when {
            radiotherapyEvaluations.any { (rightTime, rightPlace) -> rightTime == true && rightPlace == true } -> {
                EvaluationFactory.pass(labels.hasRecentlyReceivedRadiotherapyPass(bodyLocationMessage))
            }

            radiotherapyEvaluations.any { (rightTime, rightPlace) -> rightTime == null && rightPlace == true } -> {
                EvaluationFactory.undetermined(labels.hasRecentlyReceivedRadiotherapyUndeterminedUnknownDate(bodyLocationMessage))
            }

            radiotherapyEvaluations.any { (rightTime, rightPlace) -> rightTime == true && rightPlace == null } -> {
                EvaluationFactory.recoverableUndetermined(
                    labels.hasRecentlyReceivedRadiotherapyRecoverableUndeterminedLocation(requestedLocation.toString())
                )
            }

            radiotherapyEvaluations.any { (rightTime, rightPlace) -> rightTime == null && rightPlace == null } -> {
                EvaluationFactory.recoverableUndetermined(
                    labels.hasRecentlyReceivedRadiotherapyRecoverableUndeterminedBoth(bodyLocationMessage)
                )
            }

            else -> {
                EvaluationFactory.fail(labels.hasRecentlyReceivedRadiotherapyFail(bodyLocationMessage))
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