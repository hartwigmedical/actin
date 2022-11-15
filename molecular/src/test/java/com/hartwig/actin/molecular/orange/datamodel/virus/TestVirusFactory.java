package com.hartwig.actin.molecular.orange.datamodel.virus;

import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;

public final class TestVirusFactory {

    private TestVirusFactory() {
    }

    @NotNull
    public static ImmutableVirusInterpreterEntry.Builder builder() {
        return ImmutableVirusInterpreterEntry.builder()
                .name(Strings.EMPTY)
                .integrations(0)
                .driverLikelihood(VirusDriverLikelihood.LOW);
    }
}
