package com.hartwig.actin.molecular.orange.evidence.actionability;

import java.util.Set;

import com.google.common.collect.Sets;
import com.hartwig.actin.molecular.orange.evidence.TestEvidenceFactory;
import com.hartwig.serve.datamodel.ActionableEvent;
import com.hartwig.serve.datamodel.CancerType;
import com.hartwig.serve.datamodel.EvidenceDirection;
import com.hartwig.serve.datamodel.EvidenceLevel;
import com.hartwig.serve.datamodel.ImmutableCancerType;
import com.hartwig.serve.datamodel.ImmutableTreatment;
import com.hartwig.serve.datamodel.Knowledgebase;
import com.hartwig.serve.datamodel.Treatment;
import com.hartwig.serve.datamodel.characteristic.ImmutableActionableCharacteristic;
import com.hartwig.serve.datamodel.characteristic.TumorCharacteristicType;
import com.hartwig.serve.datamodel.fusion.ImmutableActionableFusion;
import com.hartwig.serve.datamodel.gene.ImmutableActionableGene;
import com.hartwig.serve.datamodel.hotspot.ImmutableActionableHotspot;
import com.hartwig.serve.datamodel.range.ImmutableActionableRange;

import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;

public final class TestServeActionabilityFactory {

    private TestServeActionabilityFactory() {
    }

    @NotNull
    public static ImmutableActionableHotspot.Builder hotspotBuilder() {
        return ImmutableActionableHotspot.builder().from(createEmptyActionableEvent()).from(TestEvidenceFactory.createEmptyHotspot());
    }

    @NotNull
    public static ImmutableActionableRange.Builder rangeBuilder() {
        return ImmutableActionableRange.builder().from(createEmptyActionableEvent()).from(TestEvidenceFactory.createEmptyRangeAnnotation());
    }

    @NotNull
    public static ImmutableActionableGene.Builder geneBuilder() {
        return ImmutableActionableGene.builder().from(createEmptyActionableEvent()).from(TestEvidenceFactory.createEmptyGeneAnnotation());
    }

    @NotNull
    public static ImmutableActionableFusion.Builder fusionBuilder() {
        return ImmutableActionableFusion.builder().from(createEmptyActionableEvent()).from(TestEvidenceFactory.createEmptyFusionPair());
    }

    @NotNull
    public static ImmutableActionableCharacteristic.Builder characteristicBuilder() {
        return ImmutableActionableCharacteristic.builder()
                .from(createEmptyActionableEvent())
                .type(TumorCharacteristicType.MICROSATELLITE_STABLE);
    }

    @NotNull
    public static ImmutableTreatment.Builder treatmentBuilder() {
        return ImmutableTreatment.builder().name(Strings.EMPTY);
    }

    @NotNull
    public static ImmutableCancerType.Builder cancerTypeBuilder() {
        return ImmutableCancerType.builder().name(Strings.EMPTY).doid(Strings.EMPTY);
    }

    @NotNull
    private static ActionableEvent createEmptyActionableEvent() {
        return new ActionableEvent() {
            @NotNull
            @Override
            public Knowledgebase source() {
                return Knowledgebase.UNKNOWN;
            }

            @NotNull
            @Override
            public String sourceEvent() {
                return Strings.EMPTY;
            }

            @NotNull
            @Override
            public Set<String> sourceUrls() {
                return Sets.newHashSet();
            }

            @NotNull
            @Override
            public Treatment treatment() {
                return treatmentBuilder().build();
            }

            @NotNull
            @Override
            public CancerType applicableCancerType() {
                return cancerTypeBuilder().build();
            }

            @NotNull
            @Override
            public Set<CancerType> blacklistCancerTypes() {
                return Sets.newHashSet();
            }

            @NotNull
            @Override
            public EvidenceLevel level() {
                return EvidenceLevel.D;
            }

            @NotNull
            @Override
            public EvidenceDirection direction() {
                return EvidenceDirection.NO_BENEFIT;
            }

            @NotNull
            @Override
            public Set<String> evidenceUrls() {
                return Sets.newHashSet();
            }
        };
    }
}
