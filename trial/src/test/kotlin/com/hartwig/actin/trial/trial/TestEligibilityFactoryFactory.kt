package com.hartwig.actin.trial.trial

import com.hartwig.actin.treatment.datamodel.TestFunctionInputResolveFactory

object TestEligibilityFactoryFactory {

    fun createTestEligibilityFactory(): EligibilityFactory {
        return EligibilityFactory(TestFunctionInputResolveFactory.createTestResolver())
    }
}