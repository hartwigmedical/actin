package com.hartwig.actin.datamodel.clinical

object TestOtherConditionFactory {

    fun createMinimal(): OtherCondition {
        return OtherCondition(
            name = "",
            icdCodes = setOf(IcdCode("", null))
        )
    }

    fun create(name: String, year: Int?, month: Int?, icdMainCode: String = "", icdExtensionCode: String? = null): OtherCondition {
        return OtherCondition(
            name = name,
            year = year,
            month = month,
            icdCodes = setOf(IcdCode(icdMainCode, icdExtensionCode))
        )
    }
}
