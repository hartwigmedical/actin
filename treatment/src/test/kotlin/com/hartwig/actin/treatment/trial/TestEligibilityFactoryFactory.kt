package com.hartwig.actin.treatment.trial

import com.hartwig.actin.treatment.datamodel.TestFunctionInputResolveFactory

object TestEligibilityFactoryFactory {
    fun createTestEligibilityFactory(): EligibilityFactory {
        return EligibilityFactory(TestFunctionInputResolveFactory.createTestResolver())
    }
}