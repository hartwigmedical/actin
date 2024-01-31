package com.hartwig.actin.trial.interpretation

import com.hartwig.actin.trial.datamodel.TestFunctionInputResolveFactory

object TestEligibilityFactoryFactory {

    fun createTestEligibilityFactory(): EligibilityFactory {
        return EligibilityFactory(TestFunctionInputResolveFactory.createTestResolver())
    }
}