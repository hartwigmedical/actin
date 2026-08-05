package com.hartwig.actin.algo.evaluation.laboratory

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationLabels
import com.hartwig.actin.datamodel.clinical.LabMeasurement
import java.time.LocalDate

internal class SameDateLabValueSelector(
    private val measurements: Set<LabMeasurement>,
    private val labels: EvaluationLabels.Laboratory
) : LabValueSelector {

    override fun select(interpretation: LabInterpretation, minValidDate: LocalDate): LabValueSelectionResult {
        val mostRecentSharedDate = measurements
            .map { measurement -> interpretation.allValues(measurement)?.map { it.date }?.toSet() ?: emptySet() }
            .reduceOrNull { acc, dates -> acc intersect dates }
            ?.filter { !it.isBefore(minValidDate) }
            ?.maxOrNull()
            ?: return LabValueSelectionResult.NotFound(
                EvaluationFactory.recoverableUndetermined(
                    labels.sameDateLabValueSelectorNoSharedDate(measurements.joinToString { it.display() })
                )
            )

        val selected = measurements.associateWith { measurement ->
            val value = interpretation.valuesOnDate(measurement, mostRecentSharedDate).firstOrNull()
            normalizeAndValidate(measurement, value, minValidDate)
                ?: return LabValueSelectionResult.NotFound(
                    LabEvaluation.evaluateInvalidLabValue(measurement, value, minValidDate, labels)
                )
        }
        return LabValueSelectionResult.Found(selected)
    }

}
