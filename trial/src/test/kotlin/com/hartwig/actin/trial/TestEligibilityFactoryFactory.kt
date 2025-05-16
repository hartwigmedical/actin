package com.hartwig.actin.trial

import com.hartwig.actin.datamodel.trial.EligibilityFactory.generateEligibilityFunction
import com.hartwig.actin.trial.input.FunctionInputResolver
import com.hartwig.actin.trial.input.TestFunctionInputResolverFactory

class TestEligibilityFactoryFactory(
    private val functionInputResolver: FunctionInputResolver
) {

    fun isValidInclusionCriterion(criterion: String): Boolean {
        return try {
            generateEligibilityFunction(criterion).run {
                val hasValidInputs = functionInputResolver.hasValidInputs(this)
                !(hasValidInputs == null || !hasValidInputs)
            }
        } catch (exc: Exception) {
            false
        }
    }

    companion object {
        fun createTestEligibilityFactory(): TestEligibilityFactoryFactory {
            return TestEligibilityFactoryFactory(TestFunctionInputResolverFactory.createTestResolver())
        }
    }
}