package com.hartwig.actin.datamodel.clinical

object TestPriorPrimaryFactory {

    fun createMinimal(): PriorPrimary {
        return PriorPrimary(
            name = "",
            treatmentHistory = "",
            status = TumorStatus.INACTIVE
        )
    }
}
