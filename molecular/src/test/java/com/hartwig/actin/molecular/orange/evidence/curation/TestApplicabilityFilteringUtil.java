package com.hartwig.actin.molecular.orange.evidence.curation;

import org.jetbrains.annotations.NotNull;

public final class TestApplicabilityFilteringUtil {

    private TestApplicabilityFilteringUtil() {
    }

    @NotNull
    public static String nonApplicableGene() {
        return ApplicabilityFiltering.NON_APPLICABLE_GENES.iterator().next();
    }

    @NotNull
    public static String nonApplicableAmplification() {
        return ApplicabilityFiltering.NON_APPLICABLE_AMPLIFICATIONS.iterator().next();
    }
}
