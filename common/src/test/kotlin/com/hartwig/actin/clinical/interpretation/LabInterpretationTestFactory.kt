package com.hartwig.actin.clinical.interpretation

import com.hartwig.actin.datamodel.clinical.LabMeasurement
import com.hartwig.actin.datamodel.clinical.LabUnit
import com.hartwig.actin.datamodel.clinical.LabValue
import java.time.LocalDate

internal object LabInterpretationTestFactory {
    
    fun createMinimal(): LabValue {
        return LabValue(
            date = LocalDate.of(2017, 10, 20),
            measurement = LabMeasurement.UNKNOWN,
            comparator = "",
            value = 0.0,
            unit = LabUnit.NONE
        )
    }
}
