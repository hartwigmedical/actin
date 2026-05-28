package com.hartwig.actin.algo.evaluation.laboratory

import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.LabMeasurement
import com.hartwig.actin.datamodel.clinical.LabValue
import java.time.LocalDate

internal interface LabValueSelector {
    fun select(interpretation: LabInterpretation, minValidDate: LocalDate): LabValueSelectionResult
}

internal fun normalizeAndValidate(
    measurement: LabMeasurement,
    labValue: LabValue?,
    minValidDate: LocalDate
): LabValue? =
    labValue?.let { LabUnitConverter.normalizeLabValue(measurement, it) }
        ?.takeIf { LabEvaluation.isValid(it, measurement, minValidDate) }

internal sealed class LabValueSelectionResult {
    data class Found(val values: Map<LabMeasurement, LabValue>) : LabValueSelectionResult()
    data class NotFound(val evaluation: Evaluation) : LabValueSelectionResult()
}
