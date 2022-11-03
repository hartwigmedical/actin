package com.hartwig.actin.molecular.datamodel.driver;

import com.hartwig.actin.molecular.datamodel.evidence.ImmutableActionableEvidence;

import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;

public final class TestVirusFactory {

    private TestVirusFactory() {
    }

    @NotNull
    public static ImmutableVirus.Builder builder() {
        return ImmutableVirus.builder()
                .driverLikelihood(DriverLikelihood.LOW)
                .evidence(ImmutableActionableEvidence.builder().build())
                .name(Strings.EMPTY)
                .integrations(0);
    }
}
