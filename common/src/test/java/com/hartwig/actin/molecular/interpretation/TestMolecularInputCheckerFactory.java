package com.hartwig.actin.molecular.interpretation;

import com.google.common.collect.Sets;

import org.jetbrains.annotations.NotNull;

public final class TestMolecularInputCheckerFactory {

    private TestMolecularInputCheckerFactory() {
    }

    @NotNull
    public static MolecularInputChecker createEmptyChecker() {
        return new MolecularInputChecker(Sets.newHashSet());
    }
}
