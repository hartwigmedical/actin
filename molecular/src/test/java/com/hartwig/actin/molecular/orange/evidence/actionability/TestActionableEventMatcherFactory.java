package com.hartwig.actin.molecular.orange.evidence.actionability;

import com.google.common.collect.Sets;
import com.hartwig.actin.doid.TestDoidModelFactory;
import com.hartwig.serve.datamodel.ActionableEvents;
import com.hartwig.serve.datamodel.ImmutableActionableEvents;

import org.jetbrains.annotations.NotNull;

public final class TestActionableEventMatcherFactory {

    private TestActionableEventMatcherFactory() {
    }

    @NotNull
    public static ActionableEventMatcher createEmpty() {
        PersonalizedActionabilityFactory personalizedActionabilityFactory =
                new PersonalizedActionabilityFactory(TestDoidModelFactory.createMinimalTestDoidModel(), Sets.newHashSet());
        ActionableEvents empty = ImmutableActionableEvents.builder().build();

        return new ActionableEventMatcher(personalizedActionabilityFactory,
                SignatureEvidence.create(empty),
                VariantEvidence.create(empty),
                CopyNumberEvidence.create(empty),
                HomozygousDisruptionEvidence.create(empty),
                BreakendEvidence.create(empty),
                FusionEvidence.create(empty),
                VirusEvidence.create(empty));
    }
}
