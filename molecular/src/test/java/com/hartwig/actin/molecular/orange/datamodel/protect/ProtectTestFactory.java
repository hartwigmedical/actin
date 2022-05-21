package com.hartwig.actin.molecular.orange.datamodel.protect;

import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;

public final class ProtectTestFactory {

    private ProtectTestFactory() {
    }

    @NotNull
    public static ImmutableProtectEvidence.Builder builder() {
        return ImmutableProtectEvidence.builder()
                .reported(false)
                .event(Strings.EMPTY)
                .treatment(Strings.EMPTY)
                .onLabel(false)
                .level(EvidenceLevel.A)
                .direction(EvidenceDirection.RESPONSIVE);
    }

    @NotNull
    public static ProtectEvidence create() {
        return builder().build();
    }

    @NotNull
    public static ImmutableProtectSource.Builder sourceBuilder() {
        return ImmutableProtectSource.builder().name(Strings.EMPTY).event(Strings.EMPTY).type(ProtectEvidenceType.HOTSPOT_MUTATION);
    }

    @NotNull
    public static ProtectSource createSource() {
        return sourceBuilder().build();
    }
}
