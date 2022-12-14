package com.hartwig.actin.molecular.orange.datamodel.lilac;

import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;

public final class TestLilacFactory {

    private TestLilacFactory() {
    }

    @NotNull
    public static ImmutableLilacHlaAllele.Builder builder() {
        return ImmutableLilacHlaAllele.builder()
                .allele(Strings.EMPTY)
                .tumorCopyNumber(0D)
                .somaticMissense(0)
                .somaticNonsenseOrFrameshift(0)
                .somaticSplice(0)
                .somaticInframeIndel(0);
    }
}
