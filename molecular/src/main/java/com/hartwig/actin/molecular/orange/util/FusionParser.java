package com.hartwig.actin.molecular.orange.util;

import com.hartwig.actin.molecular.interpretation.FusionGene;
import com.hartwig.actin.molecular.interpretation.ImmutableFusionGene;

import org.jetbrains.annotations.NotNull;

public final class FusionParser {

    private FusionParser() {
    }

    @NotNull
    public static FusionGene fromEvidenceEvent(@NotNull String event) {
        String formattedEvent = event.substring(0, event.indexOf(" fusion"));
        String[] genes = formattedEvent.split(" - ");

        return ImmutableFusionGene.builder().fiveGene(genes[0]).threeGene(genes[1]).build();
    }
}
