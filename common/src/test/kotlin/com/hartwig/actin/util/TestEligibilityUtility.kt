package com.hartwig.actin.util

import com.hartwig.actin.EligibilityFactory
import com.hartwig.actin.trial.input.TestFunctionInputResolverFactory

object TestEligibilityUtility {

    val functionInputResolver = TestFunctionInputResolverFactory.createTestResolver()

    fun isValidInclusionCriterion(criterion: String): Boolean {
        return try {
            val eligibilityFunction = EligibilityFactory.generateEligibilityFunction(criterion)
            val hasValidInputs = functionInputResolver.hasValidInputs(eligibilityFunction)
            return !(hasValidInputs == null || !hasValidInputs)
        } catch (_: Exception) {
            false
        }
    }
}