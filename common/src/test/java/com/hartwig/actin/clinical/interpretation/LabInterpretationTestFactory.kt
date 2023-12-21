package com.hartwig.actin.clinical.interpretation

import com.hartwig.actin.clinical.datamodel.ImmutableLabValue
import com.hartwig.actin.clinical.datamodel.LabUnit
import org.apache.logging.log4j.util.Strings
import java.time.LocalDate

internal object LabInterpretationTestFactory {
    fun builder(): ImmutableLabValue.Builder {
        return ImmutableLabValue.builder()
            .date(LocalDate.of(2017, 10, 20))
            .code(Strings.EMPTY)
            .name(Strings.EMPTY)
            .comparator(Strings.EMPTY)
            .value(0.0)
            .unit(LabUnit.NONE)
    }
}
