package com.hartwig.actin.algo.matchcomparison

import com.hartwig.actin.datamodel.trial.EligibilityFunction
import com.hartwig.actin.trial.input.EligibilityRule
import com.hartwig.actin.trial.input.composite.CompositeRules
import com.hartwig.actin.trial.input.ruleAsEnum

object EligibilityFunctionComparison {

    fun determineEligibilityFunctionDifferences(oldFunction: EligibilityFunction, newFunction: EligibilityFunction): FunctionDifferences {
        return extractFunctionDifferences(listOf(oldFunction), listOf(newFunction))
    }

    private tailrec fun extractFunctionDifferences(
        oldFunctions: List<EligibilityFunction>,
        newFunctions: List<EligibilityFunction>,
        differences: FunctionDifferences = FunctionDifferences()
    ): FunctionDifferences {
        return if (oldFunctions.isEmpty() || newFunctions.isEmpty()) {
            differences
        } else {
            val (oldFunctionsToAdd, newFunctionsToAdd, newDifferences) = functionDifferences(oldFunctions.first(), newFunctions.first())
            extractFunctionDifferences(
                oldFunctions.drop(1) + oldFunctionsToAdd,
                newFunctions.drop(1) + newFunctionsToAdd,
                differences + newDifferences
            )
        }
    }

    private fun functionDifferences(oldFunction: EligibilityFunction, newFunction: EligibilityFunction):
            Triple<List<EligibilityFunction>, List<EligibilityFunction>, FunctionDifferences> {
        return when {
            oldFunction == newFunction ->
                Triple(emptyList(), emptyList(), FunctionDifferences())

            newFunction.ruleAsEnum() == EligibilityRule.HAS_HAD_TREATMENT_WITH_ANY_DRUG_X ->
                Triple(
                    emptyList(), emptyList(), FunctionDifferences(
                        nameToDrugDifferences = listOf(
                            "${oldFunction.ruleAsEnum()}[${paramString(oldFunction)}] -> HAS_HAD_TREATMENT_WITH_ANY_DRUG_X[${newFunction.parameters[0]}]"
                        )
                    )
                )

            oldFunction.ruleAsEnum() == EligibilityRule.OR && newFunction.ruleAsEnum() == EligibilityRule.OR && newFunction.parameters
                .mapNotNull { (it as? EligibilityFunction)?.ruleAsEnum() }
                .all { it == EligibilityRule.HAS_HAD_TREATMENT_NAME_X || it == EligibilityRule.HAS_HAD_TREATMENT_WITH_ANY_DRUG_X } -> {
                Triple(
                    emptyList(), emptyList(), FunctionDifferences(
                        nameToDrugDifferences = listOf(
                            "${oldFunction.rule}[${paramString(oldFunction)}] -> \"${newFunction.rule}[${paramString(newFunction)}]}]"
                        )
                    )
                )
            }

            CompositeRules.isComposite(oldFunction.ruleAsEnum()) && CompositeRules.isComposite(newFunction.ruleAsEnum()) -> {
                val ruleDifferences = if (oldFunction.rule != newFunction.rule) {
                    listOf("${oldFunction.rule} != ${newFunction.rule}")
                } else {
                    emptyList()
                }
                Triple(
                    oldFunction.parameters.mapNotNull { it as? EligibilityFunction },
                    newFunction.parameters.mapNotNull { it as? EligibilityFunction },
                    FunctionDifferences(otherDifferences = ruleDifferences)
                )
            }

            oldFunction.rule == newFunction.rule ->
                Triple(
                    emptyList(),
                    emptyList(),
                    FunctionDifferences(parameterDifferences = listOf("${paramString(oldFunction)} != ${paramString(newFunction)}"))
                )

            else -> Triple(emptyList(), emptyList(), FunctionDifferences(otherDifferences = listOf("$oldFunction != $newFunction")))
        }
    }

    private fun paramString(eligibilityFunction: EligibilityFunction) = eligibilityFunction.parameters.joinToString(";")
}