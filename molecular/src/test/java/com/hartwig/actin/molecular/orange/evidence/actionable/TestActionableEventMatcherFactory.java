package com.hartwig.actin.molecular.orange.evidence.actionable;

import com.hartwig.serve.datamodel.ImmutableActionableEvents;

import org.jetbrains.annotations.NotNull;

public final class TestActionableEventMatcherFactory {

    private TestActionableEventMatcherFactory() {
    }

    @NotNull
    public static ActionableEventMatcher createEmpty() {
        return new ActionableEventMatcher(ImmutableActionableEvents.builder().build());
    }
}
