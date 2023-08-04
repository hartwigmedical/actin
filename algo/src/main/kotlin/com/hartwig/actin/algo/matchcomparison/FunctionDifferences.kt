package com.hartwig.actin.algo.matchcomparison

data class FunctionDifferences(
    val nameToDrugDifferences: List<String> = emptyList(),
    val parameterDifferences: List<String> = emptyList(),
    val otherDifferences: List<String> = emptyList()
) {

    operator fun plus(other: FunctionDifferences): FunctionDifferences {
        return FunctionDifferences(
            nameToDrugDifferences = nameToDrugDifferences + other.nameToDrugDifferences,
            parameterDifferences = parameterDifferences + other.parameterDifferences,
            otherDifferences = otherDifferences + other.otherDifferences
        )
    }

    fun isNotEmpty() = nameToDrugDifferences.isNotEmpty() || parameterDifferences.isNotEmpty() || otherDifferences.isNotEmpty()

    fun asList(): List<String> {
        return listOf(
            "name to drug" to nameToDrugDifferences,
            "parameter" to parameterDifferences,
            "other" to otherDifferences
        ).flatMap { (name, differences) -> differences.map { "* $name: $it" } }
    }
}