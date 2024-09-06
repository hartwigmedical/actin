package com.hartwig.actin.datamodel.clinical

object TestPriorOtherConditionFactory {

    fun createMinimal(): PriorOtherCondition {
        return PriorOtherCondition(
            name = "",
            category = "",
            isContraindicationForTherapy = false
        )
    }

    fun create(name: String, year: Int?, month: Int?): PriorOtherCondition {
        return PriorOtherCondition(
            name = name,
            category = "",
            isContraindicationForTherapy = false,
            year = year,
            month = month
        )
    }
}
