package com.hartwig.actin.datamodel.clinical

object TestPriorPrimaryFactory {

    fun createMinimal(): PriorPrimary {
        return PriorPrimary(
            tumorLocation = "",
            tumorSubLocation = "",
            tumorType = "",
            tumorSubType = "",
            treatmentHistory = "",
            status = TumorStatus.INACTIVE
        )
    }
}
