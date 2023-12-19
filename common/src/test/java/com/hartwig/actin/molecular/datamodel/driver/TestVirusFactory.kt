package com.hartwig.actin.molecular.datamodel.driver;

import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;

public final class TestVirusFactory {

    private TestVirusFactory() {
    }

    @NotNull
    public static ImmutableVirus.Builder builder() {
        return ImmutableVirus.builder()
                .from(TestDriverFactory.createEmptyDriver())
                .name(Strings.EMPTY)
                .type(VirusType.OTHER)
                .isReliable(false)
                .integrations(0);
    }
}
