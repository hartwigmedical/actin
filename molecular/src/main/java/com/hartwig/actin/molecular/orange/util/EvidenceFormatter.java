package com.hartwig.actin.molecular.orange.util;

import com.hartwig.actin.molecular.orange.datamodel.TreatmentEvidence;

import org.jetbrains.annotations.NotNull;

public final class EvidenceFormatter {

    private EvidenceFormatter() {
    }

    @NotNull
    public static String format(@NotNull TreatmentEvidence evidence) {
        String gene = evidence.gene();
        String event = gene != null ? gene + " " + evidence.event() : evidence.event();

        return event + ": " + evidence.treatment() + " (" + evidence.type() + ")";
    }
}
