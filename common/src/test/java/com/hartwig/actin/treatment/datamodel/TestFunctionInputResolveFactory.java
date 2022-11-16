package com.hartwig.actin.treatment.datamodel;

import com.google.common.collect.Sets;
import com.hartwig.actin.doid.DoidModel;
import com.hartwig.actin.doid.TestDoidModelFactory;
import com.hartwig.actin.molecular.interpretation.MolecularInputChecker;
import com.hartwig.actin.treatment.input.FunctionInputResolver;

import org.jetbrains.annotations.NotNull;

public final class TestFunctionInputResolveFactory {

    private TestFunctionInputResolveFactory() {
    }

    @NotNull
    public static FunctionInputResolver createTestResolver() {
        return createResolverWithDoidModel(TestDoidModelFactory.createMinimalTestDoidModel());
    }

    @NotNull
    public static FunctionInputResolver createResolverWithDoidAndTerm(@NotNull String doid, @NotNull String term) {
        return createResolverWithDoidModel(TestDoidModelFactory.createWithOneDoidAndTerm(doid, term));
    }

    @NotNull
    public static FunctionInputResolver createResolverWithOneValidGene(@NotNull String gene) {
        return new FunctionInputResolver(TestDoidModelFactory.createMinimalTestDoidModel(),
                MolecularInputChecker.createSpecificGenesValid(Sets.newHashSet(gene)));
    }

    @NotNull
    public static FunctionInputResolver createResolverWithDoidModel(@NotNull DoidModel doidModel) {
        return new FunctionInputResolver(doidModel, MolecularInputChecker.createAnyGeneValid());
    }
}
