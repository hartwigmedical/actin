package com.hartwig.actin.trial

import com.hartwig.actin.trial.input.TestFunctionInputResolverFactory

object TestEligibilityFactoryFactory {

    fun createTestEligibilityFactory(): EligibilityFactory {
        return EligibilityFactory(TestFunctionInputResolverFactory.createTestResolver())
    }
}