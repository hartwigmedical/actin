package com.hartwig.actin.algo.matchcomparison

data class EvaluationDifferences(
    val resultDifferences: List<String>,
    val recoverableDifferences: List<String>,
    val messageDifferences: List<String>,
    val eligibilityDifferences: List<String>,
    val nameToDrugDifferences: List<String>,
    val parameterDifferences: List<String>,
    val otherDifferences: List<String>,
    val mapKeyDifferences: List<String>
) {

    operator fun plus(other: EvaluationDifferences): EvaluationDifferences {
        return EvaluationDifferences(
            resultDifferences = resultDifferences + other.resultDifferences,
            recoverableDifferences = recoverableDifferences + other.recoverableDifferences,
            messageDifferences = messageDifferences + other.messageDifferences,
            eligibilityDifferences = eligibilityDifferences + other.eligibilityDifferences,
            nameToDrugDifferences = nameToDrugDifferences + other.nameToDrugDifferences,
            parameterDifferences = parameterDifferences + other.parameterDifferences,
            otherDifferences = otherDifferences + other.otherDifferences,
            mapKeyDifferences = mapKeyDifferences + other.mapKeyDifferences
        )
    }

    fun summary(): String {
        return mapOf(
            "result" to resultDifferences,
            "recoverable" to recoverableDifferences,
            "message" to messageDifferences,
            "eligibility" to eligibilityDifferences,
            "name-to-drug" to nameToDrugDifferences,
            "parameter" to parameterDifferences,
            "other" to otherDifferences,
            "map key" to mapKeyDifferences
        ).map { (name, differences) -> "${differences.distinct().size} unique $name differences (${differences.size} total)" }
            .joinToString("\n")
    }

    companion object {

        fun create(
            resultDifferences: List<String> = emptyList(),
            recoverableDifferences: List<String> = emptyList(),
            messageDifferences: List<String> = emptyList(),
            eligibilityDifferences: List<String> = emptyList(),
            functionDifferences: FunctionDifferences = FunctionDifferences(),
            mapKeyDifferences: List<String> = emptyList()
        ): EvaluationDifferences {
            return EvaluationDifferences(
                resultDifferences = resultDifferences,
                recoverableDifferences = recoverableDifferences,
                messageDifferences = messageDifferences,
                eligibilityDifferences = eligibilityDifferences,
                nameToDrugDifferences = functionDifferences.nameToDrugDifferences,
                parameterDifferences = functionDifferences.parameterDifferences,
                otherDifferences = functionDifferences.otherDifferences,
                mapKeyDifferences = mapKeyDifferences
            )
        }
    }
}
