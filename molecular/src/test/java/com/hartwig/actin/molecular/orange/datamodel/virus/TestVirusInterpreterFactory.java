package com.hartwig.actin.molecular.orange.datamodel.virus;

import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;

public final class TestVirusInterpreterFactory {

    private TestVirusInterpreterFactory() {
    }

    @NotNull
    public static ImmutableVirusInterpreterEntry.Builder builder() {
        return ImmutableVirusInterpreterEntry.builder()
                .reported(true)
                .name(Strings.EMPTY)
                .qcStatus(VirusQCStatus.NO_ABNORMALITIES)
                .integrations(0)
                .driverLikelihood(VirusDriverLikelihood.LOW);
    }
}
