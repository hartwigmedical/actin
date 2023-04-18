package com.hartwig.actin.soc.evaluation

import com.hartwig.actin.doid.DoidModel
import com.hartwig.actin.molecular.interpretation.MolecularInputChecker
import com.hartwig.actin.soc.calendar.ReferenceDateProvider
import com.hartwig.actin.soc.evaluation.composite.And
import com.hartwig.actin.soc.evaluation.composite.Not
import com.hartwig.actin.soc.evaluation.composite.Or
import com.hartwig.actin.soc.evaluation.composite.WarnIf
import com.hartwig.actin.treatment.datamodel.EligibilityFunction
import com.hartwig.actin.treatment.datamodel.EligibilityRule
import com.hartwig.actin.treatment.input.FunctionInputResolver
import com.hartwig.actin.treatment.input.composite.CompositeRules

class EvaluationFunctionFactory(functionCreatorMap: Map<EligibilityRule, FunctionCreator>,
                                functionInputResolver: FunctionInputResolver) {

    private val functionCreatorMap: Map<EligibilityRule, FunctionCreator>
    private val functionInputResolver: FunctionInputResolver

    init {
        this.functionCreatorMap = functionCreatorMap
        this.functionInputResolver = functionInputResolver
    }

    fun create(function: EligibilityFunction): EvaluationFunction {
        val hasValidInputs: Boolean? = functionInputResolver.hasValidInputs(function)
        check(hasValidInputs ?: false) { "No valid inputs defined for $function" }
        return if (CompositeRules.isComposite(function.rule())) {
            createCompositeFunction(function)
        } else {
            functionCreatorMap[function.rule()]?.create(function) ?: throw NullPointerException("Could not find function creator for rule "
            + function.rule())
        }
    }

    private fun createCompositeFunction(function: EligibilityFunction): EvaluationFunction {
        return when (function.rule()) {
            EligibilityRule.AND -> And(createMultipleCompositeParameters(function))
            EligibilityRule.OR -> Or(createMultipleCompositeParameters(function))
            EligibilityRule.NOT -> Not(createSingleCompositeParameter(function))
            EligibilityRule.WARN_IF -> WarnIf(createSingleCompositeParameter(function))
            else -> {
                throw IllegalStateException("Could not create evaluation function for composite rule '" + function.rule() + "'")
            }
        }
    }

    private fun createSingleCompositeParameter(function: EligibilityFunction): EvaluationFunction {
        return create(FunctionInputResolver.createOneCompositeParameter(function))
    }

    private fun createMultipleCompositeParameters(function: EligibilityFunction): List<EvaluationFunction> {
        val parameters: MutableList<EvaluationFunction> = mutableListOf()
        for (input in FunctionInputResolver.createAtLeastTwoCompositeParameters(function)) {
            parameters.add(create(input))
        }
        return parameters
    }

    companion object {
        fun create(doidModel: DoidModel, referenceDateProvider: ReferenceDateProvider): EvaluationFunctionFactory {
            // We assume we never check validity of a gene inside algo.
            val molecularInputChecker: MolecularInputChecker = MolecularInputChecker.createAnyGeneValid()
            val functionInputResolver = FunctionInputResolver(doidModel, molecularInputChecker)
            return EvaluationFunctionFactory(FunctionCreatorFactory.create(referenceDateProvider, doidModel, functionInputResolver),
                    functionInputResolver)
        }
    }
}