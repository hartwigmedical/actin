package com.hartwig.actin.molecular.orange.datamodel.protect;

import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;

public final class TestProtectDataFactory {

    private TestProtectDataFactory() {
    }

    @NotNull
    public static ProtectEvidence create() {
        return ImmutableProtectEvidence.builder()
                .reported(false)
                .event(Strings.EMPTY)
                .treatment(Strings.EMPTY)
                .onLabel(false)
                .level(EvidenceLevel.A)
                .direction(EvidenceDirection.RESPONSIVE)
                .build();
    }

    @NotNull
    public static ProtectSource createSource() {
        return ImmutableProtectSource.builder().name(Strings.EMPTY).event(Strings.EMPTY).type(ProtectEvidenceType.HOTSPOT_MUTATION).build();
    }
}
