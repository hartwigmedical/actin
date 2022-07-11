package com.hartwig.actin.treatment.datamodel;

import com.hartwig.actin.doid.DoidModel;
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

    @NotNull
    public static FunctionInputResolver createResolverWithDoid(@NotNull String doid, @NotNull String term) {
        DoidModel doidModel = TestDoidModelFactory.createWithOneDoidAndTerm(doid, term);
        return new FunctionInputResolver(doidModel);
    }
}
