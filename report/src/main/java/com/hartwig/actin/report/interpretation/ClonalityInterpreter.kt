package com.hartwig.actin.report.interpretation;

import com.hartwig.actin.molecular.datamodel.driver.Variant;

import org.jetbrains.annotations.NotNull;

public final class ClonalityInterpreter {

    public static final double CLONAL_CUTOFF = 0.5;

    private ClonalityInterpreter() {
    }

    public static boolean isPotentiallySubclonal(@NotNull Variant variant) {
        return variant.clonalLikelihood() < CLONAL_CUTOFF;
    }
}
