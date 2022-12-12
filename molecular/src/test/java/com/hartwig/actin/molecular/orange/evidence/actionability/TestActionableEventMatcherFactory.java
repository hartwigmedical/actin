package com.hartwig.actin.molecular.orange.evidence.actionability;

import java.util.Set;

import com.google.common.collect.Sets;
import com.hartwig.actin.doid.DoidModel;
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

    @NotNull
    public static ActionableEventMatcher createProper() {
        DoidModel doidModel = TestDoidModelFactory.createWithOneParentChild("parent", "child");
        Set<String> applicableDoids = Sets.newHashSet("parent");

        PersonalizedActionabilityFactory personalizedActionabilityFactory =
                new PersonalizedActionabilityFactory(doidModel, applicableDoids);

        ActionableEvents actionableEvents = ImmutableActionableEvents.builder()
                .addHotspots(TestServeActionabilityFactory.hotspotBuilder().build())
                .addRanges(TestServeActionabilityFactory.rangeBuilder().build())
                .addGenes(TestServeActionabilityFactory.geneBuilder().build())
                .addFusions(TestServeActionabilityFactory.fusionBuilder().build())
                .addCharacteristics(TestServeActionabilityFactory.characteristicBuilder().build())
                .build();

        return new ActionableEventMatcher(personalizedActionabilityFactory,
                SignatureEvidence.create(actionableEvents),
                VariantEvidence.create(actionableEvents),
                CopyNumberEvidence.create(actionableEvents),
                HomozygousDisruptionEvidence.create(actionableEvents),
                BreakendEvidence.create(actionableEvents),
                FusionEvidence.create(actionableEvents),
                VirusEvidence.create(actionableEvents));
    }
}
