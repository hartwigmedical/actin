package com.hartwig.actin.molecular.orange.evidence.actionable;

import com.google.common.collect.Lists;
import com.hartwig.serve.datamodel.ImmutableActionableEvents;

import org.jetbrains.annotations.NotNull;

public final class TestActionableEventResolverFactory {

    private TestActionableEventResolverFactory() {
    }

    @NotNull
    public static ActionableEventResolver createEmpty() {
        return new ActionableEventResolver(ImmutableActionableEvents.builder().build(),
                Lists.newArrayList());
    }
}
