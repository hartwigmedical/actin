package com.hartwig.actin.clinical.interpretation

import com.hartwig.actin.clinical.datamodel.LabUnit
import com.hartwig.actin.clinical.datamodel.LabValue
import java.time.LocalDate

internal object LabInterpretationTestFactory {
    
    fun createMinimal(): LabValue {
        return LabValue(
            date = LocalDate.of(2017, 10, 20),
            code = "",
            name = "",
            comparator = "",
            value = 0.0,
            unit = LabUnit.NONE
        )
    }
}
