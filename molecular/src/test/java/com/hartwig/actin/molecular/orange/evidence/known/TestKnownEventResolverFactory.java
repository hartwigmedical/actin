package com.hartwig.actin.molecular.orange.evidence.known;

import com.google.common.collect.Lists;
import com.hartwig.serve.datamodel.ImmutableKnownEvents;

import org.jetbrains.annotations.NotNull;

public final class TestKnownEventResolverFactory {

    private TestKnownEventResolverFactory() {
    }

    @NotNull
    public static KnownEventResolver createEmpty() {
        return new KnownEventResolver(ImmutableKnownEvents.builder().build(),
                Lists.newArrayList());
    }
}
