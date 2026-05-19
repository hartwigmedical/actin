package com.hartwig.actin.algo.evaluation.laboratory

import com.hartwig.actin.datamodel.clinical.LabMeasurement
import java.time.LocalDate

internal class SingleLabValueSelector(
    private val measurement: LabMeasurement,
    private val highestFirst: Boolean = true
) : LabValueSelector {

    override fun select(interpretation: LabInterpretation, minValidDate: LocalDate): LabValueSelectionResult {
        val mostRecent = interpretation.mostRecentValue(measurement, highestFirst)
        return when (val normalized = normalizeAndValidate(measurement, mostRecent, minValidDate)) {
            null -> LabValueSelectionResult.NotFound(LabEvaluation.evaluateInvalidLabValue(measurement, mostRecent, minValidDate))
            else -> LabValueSelectionResult.Found(
                values = mapOf(measurement to normalized.value),
                conversionNotes = listOfNotNull(normalized.conversionNote)
            )
        }
    }
}
