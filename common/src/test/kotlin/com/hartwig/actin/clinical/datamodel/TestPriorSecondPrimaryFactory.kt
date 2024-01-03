package com.hartwig.actin.clinical.datamodel

object TestPriorSecondPrimaryFactory {

    fun createMinimal(): PriorSecondPrimary {
        return PriorSecondPrimary(
            tumorLocation = "",
            tumorSubLocation = "",
            tumorType = "",
            tumorSubType = "",
            treatmentHistory = "",
            status = TumorStatus.INACTIVE
        )
    }
}
