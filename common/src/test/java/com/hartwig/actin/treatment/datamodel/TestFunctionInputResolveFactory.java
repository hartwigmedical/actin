package com.hartwig.actin.treatment.datamodel;

import com.hartwig.actin.doid.DoidModel;
import com.hartwig.actin.doid.TestDoidModelFactory;
import com.hartwig.actin.molecular.interpretation.TestMolecularInputCheckerFactory;
import com.hartwig.actin.treatment.input.FunctionInputResolver;

import org.jetbrains.annotations.NotNull;

public final class TestFunctionInputResolveFactory {

    private TestFunctionInputResolveFactory() {
    }

    @NotNull
    public static FunctionInputResolver createTestResolver() {
        return new FunctionInputResolver(TestDoidModelFactory.createMinimalTestDoidModel(),
                TestMolecularInputCheckerFactory.createEmptyChecker());
    }

    @NotNull
    public static FunctionInputResolver createResolverWithDoidAndTerm(@NotNull String doid, @NotNull String term) {
        return createResolverWithDoidModel(TestDoidModelFactory.createWithOneDoidAndTerm(doid, term));
    }

    @NotNull
    public static FunctionInputResolver createResolverWithDoidModel(@NotNull DoidModel doidModel) {
        return new FunctionInputResolver(doidModel, TestMolecularInputCheckerFactory.createEmptyChecker());
    }
}
