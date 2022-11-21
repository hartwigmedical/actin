package com.hartwig.actin.molecular.orange.evidence.actionable;

import com.google.common.collect.Sets;
import com.hartwig.actin.doid.TestDoidModelFactory;
import com.hartwig.serve.datamodel.ImmutableActionableEvents;

import org.jetbrains.annotations.NotNull;

public final class TestActionableEventMatcherFactory {

    private TestActionableEventMatcherFactory() {
    }

    @NotNull
    public static ActionableEventMatcher createEmpty() {
        PersonalizedActionabilityFactory personalizedActionabilityFactory =
                new PersonalizedActionabilityFactory(TestDoidModelFactory.createMinimalTestDoidModel(), Sets.newHashSet());
        return new ActionableEventMatcher(ImmutableActionableEvents.builder().build(), personalizedActionabilityFactory);
    }
}
