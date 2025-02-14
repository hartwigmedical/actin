package com.hartwig.actin.clinical.interpretation

import com.hartwig.actin.datamodel.clinical.LabMeasurement
import com.hartwig.actin.datamodel.clinical.LabValue

object LabInterpreter {

    // is this function still needed?
    fun interpret(labValues: List<LabValue>): LabInterpretation {
        val labValuesByCode = labValues.groupBy(LabValue::measurement)
        val baseMeasurements = LabMeasurement.entries.associateWith { labValuesByCode[it] ?: emptyList() }
        return LabInterpretation.fromMeasurements(baseMeasurements)
    }
}
