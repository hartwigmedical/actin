package com.hartwig.actin.clinical.datamodel

import org.apache.logging.log4j.util.Strings

object TestPriorSecondPrimaryFactory {
    @JvmStatic
    fun builder(): ImmutablePriorSecondPrimary.Builder {
        return ImmutablePriorSecondPrimary.builder()
            .tumorLocation(Strings.EMPTY)
            .tumorSubLocation(Strings.EMPTY)
            .tumorType(Strings.EMPTY)
            .tumorSubType(Strings.EMPTY)
            .treatmentHistory(Strings.EMPTY)
            .status(TumorStatus.INACTIVE)
    }
}
