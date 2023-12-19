package com.hartwig.actin.molecular.datamodel.immunology;

import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;

public final class TestHlaAlleleFactory {

    private TestHlaAlleleFactory() {
    }

    @NotNull
    public static ImmutableHlaAllele.Builder builder() {
        return ImmutableHlaAllele.builder().name(Strings.EMPTY).tumorCopyNumber(0D).hasSomaticMutations(false);
    }
}
