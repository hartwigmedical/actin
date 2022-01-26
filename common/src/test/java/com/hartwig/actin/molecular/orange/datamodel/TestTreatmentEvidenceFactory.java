package com.hartwig.actin.molecular.orange.datamodel;

import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;

public final class TestTreatmentEvidenceFactory {

    private TestTreatmentEvidenceFactory() {
    }

    @NotNull
    public static TreatmentEvidence create() {
        return ImmutableTreatmentEvidence.builder()
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
