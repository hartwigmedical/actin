package com.hartwig.actin.algo.evaluation.laboratory

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.datamodel.clinical.LabMeasurement
import java.time.LocalDate

internal class SameDateLabValueSelector(
    private val measurements: Set<LabMeasurement>
) : LabValueSelector {

    override fun select(interpretation: LabInterpretation, minValidDate: LocalDate): LabValueSelectionResult {
        val mostRecentSharedDate = measurements
            .map { measurement -> interpretation.allValues(measurement)?.map { it.date }?.toSet() ?: emptySet() }
            .reduceOrNull { acc, dates -> acc intersect dates }
            ?.filter { !it.isBefore(minValidDate) }
            ?.maxOrNull()
            ?: return LabValueSelectionResult.NotFound(
                EvaluationFactory.recoverableUndetermined(
                    "No shared date found for all required lab values: ${measurements.joinToString { it.display() }}"
                )
            )

        val conversionNotes = mutableListOf<String>()
        val selected = measurements.associateWith { measurement ->
            val value = interpretation.valuesOnDate(measurement, mostRecentSharedDate).firstOrNull()
            val normalized = normalizeAndValidate(measurement, value, minValidDate)
                ?: return LabValueSelectionResult.NotFound(LabEvaluation.evaluateInvalidLabValue(measurement, value, minValidDate))
            normalized.conversionNote?.let { conversionNotes.add(it) }
            normalized.value
        }
        return LabValueSelectionResult.Found(values = selected, conversionNotes = conversionNotes)
    }

}
