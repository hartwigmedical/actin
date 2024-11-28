package com.hartwig.actin.trial.interpretation

import com.hartwig.actin.datamodel.trial.EligibilityFunction
import com.hartwig.actin.datamodel.trial.EligibilityRule
import com.hartwig.actin.trial.input.FunctionInputResolver
import com.hartwig.actin.trial.input.composite.CompositeRules
import org.apache.logging.log4j.LogManager

class EligibilityFactory(private val functionInputResolver: FunctionInputResolver) {

    fun isValidInclusionCriterion(criterion: String): Boolean {
        return try {
            generateEligibilityFunction(criterion)
            true
        } catch (exc: Exception) {
            LOGGER.warn(exc.message)
            false
        }
    }

    fun generateEligibilityFunction(criterion: String): EligibilityFunction {
        val trimmed = criterion.trim { it <= ' ' }
        val (rule: EligibilityRule, parameters) = when {
            isCompositeCriterion(trimmed) -> {
                Pair(extractCompositeRule(trimmed), extractCompositeInputs(trimmed).map { generateEligibilityFunction(it) })
            }

            isParameterizedCriterion(trimmed) -> {
                Pair(extractParameterizedRule(trimmed), extractParameterizedInputs(trimmed))
            }

            else -> {
                Pair(EligibilityRule.valueOf(trimmed), emptyList())
            }
        }
        val function = EligibilityFunction(rule = rule, parameters = parameters)
        val hasValidInputs = functionInputResolver.hasValidInputs(function)
        check(!(hasValidInputs == null || !hasValidInputs)) { "Function ${function.rule} has invalid inputs: '${function.parameters}' (source criterion: '$criterion')" }
        return function
    }

    companion object {
        private val LOGGER = LogManager.getLogger(EligibilityFactory::class.java)
        private const val COMPOSITE_START = '('
        private const val COMPOSITE_END = ')'
        private const val PARAM_START = '['
        private const val PARAM_END = ']'

        private fun extractCompositeRule(criterion: String): EligibilityRule {
            val rule = EligibilityRule.valueOf(criterion.substring(0, criterion.indexOf(COMPOSITE_START)))
            check(CompositeRules.isComposite(rule)) { "Not a valid composite rule: '$rule'" }
            return rule
        }

        private fun extractParameterizedRule(criterion: String): EligibilityRule {
            return EligibilityRule.valueOf(criterion.substring(0, criterion.indexOf(PARAM_START)).trim { it <= ' ' })
        }

        private fun extractCompositeInputs(criterion: String): List<String> {
            val params = criterion.substring(criterion.indexOf(COMPOSITE_START) + 1, criterion.lastIndexOf(COMPOSITE_END))
            val relevantCommaPositions = findSeparatingCommaPositions(params)
            if (relevantCommaPositions.isEmpty()) {
                return listOf(params.trim { it <= ' ' })
            }
            val result: MutableList<String> = mutableListOf()
            var index = 0
            while (index < relevantCommaPositions.size) {
                val start = if (index == 0) -1 else relevantCommaPositions[index - 1]
                result.add(params.substring(start + 1, relevantCommaPositions[index]).trim { it <= ' ' })
                index++
            }
            result.add(params.substring(relevantCommaPositions[relevantCommaPositions.size - 1] + 1).trim { it <= ' ' })
            return result
        }

        private fun findSeparatingCommaPositions(params: String): List<Int> {
            var nestedCompositeLevel = 0
            var nestedParameterSection = 0
            val commaPositions: MutableList<Int> = mutableListOf()
            for (i in params.indices) {
                val character = params[i]
                if (character == COMPOSITE_START) {
                    nestedCompositeLevel++
                } else if (character == COMPOSITE_END) {
                    nestedCompositeLevel--
                } else if (character == PARAM_START) {
                    nestedParameterSection++
                } else if (character == PARAM_END) {
                    nestedParameterSection--
                } else if (character == ',' && nestedCompositeLevel == 0 && nestedParameterSection == 0) {
                    commaPositions.add(i)
                }
            }
            return commaPositions
        }

        private fun extractParameterizedInputs(criterion: String): List<String> {
            val parameterString = criterion.substring(criterion.indexOf(PARAM_START) + 1, criterion.lastIndexOf(PARAM_END))
            return parameterString.split(",").map { it.trim() }.dropLastWhile { it.isEmpty() }
        }

        private fun isCompositeCriterion(criterion: String): Boolean {
            return criterion.contains(COMPOSITE_START.toString()) && criterion.endsWith(COMPOSITE_END.toString())
        }

        private fun isParameterizedCriterion(criterion: String): Boolean {
            return criterion.contains(PARAM_START.toString()) && criterion.endsWith(PARAM_END.toString())
        }
    }
}