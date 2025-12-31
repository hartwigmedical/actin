package com.hartwig.actin.algo.evaluation

import com.hartwig.actin.algo.evaluation.composite.And
import com.hartwig.actin.algo.evaluation.composite.Not
import com.hartwig.actin.algo.evaluation.composite.Or
import com.hartwig.actin.algo.evaluation.composite.WarnIf
import com.hartwig.actin.datamodel.trial.EligibilityFunction
import com.hartwig.actin.datamodel.trial.FunctionParameter
import com.hartwig.actin.trial.input.EligibilityRule
import com.hartwig.actin.trial.input.composite.CompositeRules
import com.hartwig.actin.trial.input.ruleAsEnum

class EvaluationFunctionFactory(
    private val functionCreatorMap: Map<EligibilityRule, FunctionCreator>,
) {

    fun create(function: EligibilityFunction): EvaluationFunction {
        return if (CompositeRules.isComposite(function.ruleAsEnum())) {
            createCompositeFunction(function)
        } else {
            functionCreatorMap[function.ruleAsEnum()]?.invoke(function) ?: throw NullPointerException(
                "Could not find function creator for rule ${function.rule}"
            )
        }
    }

    private fun createCompositeFunction(function: EligibilityFunction): EvaluationFunction {
        return when (function.ruleAsEnum()) {
            EligibilityRule.AND -> And(createMultipleCompositeParameters(function))
            EligibilityRule.OR -> Or(createMultipleCompositeParameters(function))
            EligibilityRule.NOT -> Not(createSingleCompositeParameter(function))
            EligibilityRule.WARN_IF -> WarnIf(createSingleCompositeParameter(function))
            else -> {
                throw IllegalStateException("Could not create evaluation function for composite rule '${function.rule}'")
            }
        }
    }

    private fun createSingleCompositeParameter(function: EligibilityFunction): EvaluationFunction {
        return create(function.param<FunctionParameter>(0).value)
    }

    private fun createMultipleCompositeParameters(function: EligibilityFunction): List<EvaluationFunction> {
        return (0 until function.parameters.size).map { function.param<FunctionParameter>(it).value }.map(::create)
    }

    companion object {
        fun create(resources: RuleMappingResources): EvaluationFunctionFactory {
            return EvaluationFunctionFactory(FunctionCreatorFactory.create(resources))
        }
    }
}
