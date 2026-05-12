package com.hartwig.actin.algo.evaluation.laboratory

import com.hartwig.actin.datamodel.clinical.LabMeasurement
import java.time.LocalDate

internal class SingleLabValueSelector(
    private val measurement: LabMeasurement,
    private val highestFirst: Boolean = true
) : LabValueSelector {

    override fun select(interpretation: LabInterpretation, minValidDate: LocalDate): LabValueSelectionResult {
        val mostRecent = interpretation.mostRecentValue(measurement, highestFirst)
        return when {
            mostRecent != null && LabEvaluation.isValid(mostRecent, measurement, minValidDate) ->
                LabValueSelectionResult.Found(mapOf(measurement to mostRecent))
            else ->
                LabValueSelectionResult.NotFound(LabEvaluation.evaluateInvalidLabValue(measurement, mostRecent, minValidDate))
        }
    }
}
