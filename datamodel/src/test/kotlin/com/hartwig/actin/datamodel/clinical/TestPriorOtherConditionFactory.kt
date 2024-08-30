package com.hartwig.actin.datamodel.clinical

object TestPriorOtherConditionFactory {

    fun createMinimal(): PriorOtherCondition {
        return PriorOtherCondition(
            name = "",
            category = "",
            isContraindicationForTherapy = false
        )
    }
}
