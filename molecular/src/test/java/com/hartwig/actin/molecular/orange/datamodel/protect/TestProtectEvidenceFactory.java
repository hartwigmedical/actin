package com.hartwig.actin.molecular.orange.datamodel.protect;

import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;

public final class TestProtectEvidenceFactory {

    private TestProtectEvidenceFactory() {
    }

    @NotNull
    public static ProtectEvidence create() {
        return ImmutableProtectEvidence.builder()
                .reported(false)
                .event(Strings.EMPTY)
                .treatment(Strings.EMPTY)
                .onLabel(false)
                .type(EvidenceType.HOTSPOT_MUTATION)
                .level(EvidenceLevel.A)
                .direction(EvidenceDirection.RESPONSIVE)
                .build();
    }
}
