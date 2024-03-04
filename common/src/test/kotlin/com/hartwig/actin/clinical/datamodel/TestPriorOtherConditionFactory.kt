package com.hartwig.actin.clinical.datamodel

object TestPriorOtherConditionFactory {

    fun createMinimal(): PriorOtherCondition {
        return PriorOtherCondition(
            name = "",
            category = "",
            isContraindicationForTherapy = false
        )
    }
}
