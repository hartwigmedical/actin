package com.hartwig.actin.treatment.datamodel;

import com.hartwig.actin.doid.TestDoidModelFactory;
import com.hartwig.actin.treatment.input.FunctionInputResolver;

import org.jetbrains.annotations.NotNull;

public final class TestFunctionInputResolveFactory {

    private TestFunctionInputResolveFactory() {
    }

    @NotNull
    public static FunctionInputResolver createTestResolver() {
        return new FunctionInputResolver(TestDoidModelFactory.createMinimalTestDoidModel());
    }
}
