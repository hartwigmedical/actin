package com.hartwig.actin.treatment.trial;

import com.hartwig.actin.treatment.datamodel.TestFunctionInputResolveFactory;

import org.jetbrains.annotations.NotNull;

public final class TestEligibilityFactoryFactory {

    private TestEligibilityFactoryFactory() {
    }

    @NotNull
    public static EligibilityFactory createTestEligibilityFactory() {
        return new EligibilityFactory(TestFunctionInputResolveFactory.createTestResolver());
    }
}
