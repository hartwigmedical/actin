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
            icdCodes = setOf(IcdCode(icdMainCode, icdExtensionCode)),
            year = year,
            month = month
        )
    }
}
