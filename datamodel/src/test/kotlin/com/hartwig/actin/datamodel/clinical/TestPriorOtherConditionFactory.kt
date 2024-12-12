package com.hartwig.actin.datamodel.clinical

object TestPriorOtherConditionFactory {

    fun createMinimal(): PriorOtherCondition {
        return PriorOtherCondition(
            name = "",
            category = "",
            icdCode = IcdCode("", null),
            isContraindicationForTherapy = false
        )
    }

    fun create(name: String, year: Int?, month: Int?, icdCode: String = "", icdExtensionCode: String? = null): PriorOtherCondition {
        return PriorOtherCondition(
            name = name,
            category = "",
            icdCode = IcdCode(icdCode, icdExtensionCode),
            isContraindicationForTherapy = false,
            year = year,
            month = month
        )
    }
}
