package com.hartwig.actin.datamodel.clinical

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
