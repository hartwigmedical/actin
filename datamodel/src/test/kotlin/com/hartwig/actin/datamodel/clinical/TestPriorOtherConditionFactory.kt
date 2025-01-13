package com.hartwig.actin.datamodel.clinical

object TestPriorOtherConditionFactory {

    fun createMinimal(): PriorOtherCondition {
        return PriorOtherCondition(
            name = "",
            icdCodes = setOf(IcdCode("", null))
        )
    }

    fun create(name: String, year: Int?, month: Int?, icdMainCode: String = "", icdExtensionCode: String? = null): PriorOtherCondition {
        return PriorOtherCondition(
            name = name,
            year = year,
            month = month,
            icdCodes = setOf(IcdCode(icdMainCode, icdExtensionCode))
        )
    }
}
