package com.hartwig.actin.molecular.orange.evidence;

import com.google.common.collect.Lists;
import com.hartwig.serve.datamodel.ImmutableActionableEvents;
import com.hartwig.serve.datamodel.ImmutableKnownEvents;

import org.jetbrains.annotations.NotNull;

public final class TestEvidenceDatabaseFactory {

    private TestEvidenceDatabaseFactory() {
    }

    @NotNull
    public static EvidenceDatabase createEmptyDatabase() {
        return new EvidenceDatabase(ImmutableKnownEvents.builder().build(),
                ImmutableActionableEvents.builder().build(),
                Lists.newArrayList());
    }
}
