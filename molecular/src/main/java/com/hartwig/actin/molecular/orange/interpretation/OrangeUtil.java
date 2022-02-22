package com.hartwig.actin.molecular.orange.interpretation;

import com.hartwig.actin.molecular.orange.datamodel.EvidenceType;
import com.hartwig.actin.molecular.orange.datamodel.TreatmentEvidence;
import com.hartwig.actin.molecular.orange.util.EventFormatter;

import org.jetbrains.annotations.NotNull;

public final class OrangeUtil {

    private OrangeUtil() {
    }

    @NotNull
    public static String toEvent(@NotNull TreatmentEvidence evidence) {
        String gene = evidence.gene();
        String event = EventFormatter.format(evidence.event());

        // Promiscuous fusions have the gene embedded in the event.
        return gene != null && evidence.type() != EvidenceType.PROMISCUOUS_FUSION ? gene + " " + event : event;
    }
}
