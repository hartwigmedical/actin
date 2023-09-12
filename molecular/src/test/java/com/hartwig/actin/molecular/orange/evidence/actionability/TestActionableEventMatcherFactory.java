package com.hartwig.actin.molecular.orange.evidence.actionability;

import java.util.Set;

import com.google.common.collect.Sets;
import com.hartwig.actin.doid.DoidModel;
import com.hartwig.actin.doid.TestDoidModelFactory;
import com.hartwig.serve.datamodel.ActionableEvents;
import com.hartwig.serve.datamodel.ImmutableActionableEvents;
import com.hartwig.serve.datamodel.characteristic.ActionableCharacteristic;
import com.hartwig.serve.datamodel.characteristic.TumorCharacteristicType;
import com.hartwig.serve.datamodel.gene.GeneEvent;

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
                .addCodons(TestServeActionabilityFactory.rangeBuilder().build())
                .addExons(TestServeActionabilityFactory.rangeBuilder().build())
                .addGenes(TestServeActionabilityFactory.geneBuilder().event(GeneEvent.DELETION).build())
                .addGenes(TestServeActionabilityFactory.geneBuilder().event(GeneEvent.AMPLIFICATION).build())
                .addGenes(TestServeActionabilityFactory.geneBuilder().event(GeneEvent.ANY_MUTATION).build())
                .addFusions(TestServeActionabilityFactory.fusionBuilder().build())
                .addCharacteristics(create(TumorCharacteristicType.MICROSATELLITE_UNSTABLE))
                .addCharacteristics(create(TumorCharacteristicType.HOMOLOGOUS_RECOMBINATION_DEFICIENT))
                .addCharacteristics(create(TumorCharacteristicType.HIGH_TUMOR_MUTATIONAL_BURDEN))
                .addCharacteristics(create(TumorCharacteristicType.HIGH_TUMOR_MUTATIONAL_LOAD))
                .addCharacteristics(create(TumorCharacteristicType.HPV_POSITIVE))
                .addCharacteristics(create(TumorCharacteristicType.EBV_POSITIVE))
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

    @NotNull
    private static ActionableCharacteristic create(@NotNull TumorCharacteristicType type) {
        return TestServeActionabilityFactory.characteristicBuilder().type(type).build();
    }
}
