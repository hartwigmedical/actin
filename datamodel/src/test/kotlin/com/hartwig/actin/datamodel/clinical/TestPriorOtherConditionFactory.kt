package com.hartwig.actin.datamodel.clinical

object TestPriorOtherConditionFactory {

    fun createMinimal(): PriorOtherCondition {
        return PriorOtherCondition(
            name = "",
            category = "",
            icdCode = "",
            isContraindicationForTherapy = false
        )
    }

    fun create(name: String, year: Int?, month: Int?, icdCode: String = ""): PriorOtherCondition {
        return PriorOtherCondition(
            name = name,
            category = "",
            icdCode = icdCode,
            isContraindicationForTherapy = false,
            year = year,
            month = month
        )
    }
}
