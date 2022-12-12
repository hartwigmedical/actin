package com.hartwig.actin.molecular.orange.evidence.known;

import com.hartwig.actin.molecular.orange.evidence.TestServeFactory;
import com.hartwig.serve.datamodel.common.GeneAlteration;
import com.hartwig.serve.datamodel.common.GeneRole;
import com.hartwig.serve.datamodel.common.ProteinEffect;
import com.hartwig.serve.datamodel.fusion.ImmutableKnownFusion;
import com.hartwig.serve.datamodel.gene.ImmutableKnownCopyNumber;
import com.hartwig.serve.datamodel.hotspot.ImmutableKnownHotspot;
import com.hartwig.serve.datamodel.range.ImmutableKnownCodon;
import com.hartwig.serve.datamodel.range.ImmutableKnownExon;

import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class TestServeKnownFactory {

    private TestServeKnownFactory() {
    }

    @NotNull
    public static ImmutableKnownHotspot.Builder hotspotBuilder() {
        return ImmutableKnownHotspot.builder()
                .from(createEmptyGeneAlteration())
                .from(TestServeFactory.createEmptyHotspot())
                .inputProteinAnnotation(Strings.EMPTY);
    }

    @NotNull
    public static ImmutableKnownCodon.Builder codonBuilder() {
        return ImmutableKnownCodon.builder()
                .from(createEmptyGeneAlteration())
                .from(TestServeFactory.createEmptyRangeAnnotation())
                .inputTranscript(Strings.EMPTY)
                .inputCodonRank(0);
    }

    @NotNull
    public static ImmutableKnownExon.Builder exonBuilder() {
        return ImmutableKnownExon.builder()
                .from(createEmptyGeneAlteration())
                .from(TestServeFactory.createEmptyRangeAnnotation())
                .inputTranscript(Strings.EMPTY)
                .inputExonRank(0);
    }

    @NotNull
    public static ImmutableKnownCopyNumber.Builder copyNumberBuilder() {
        return ImmutableKnownCopyNumber.builder().from(createEmptyGeneAlteration()).from(TestServeFactory.createEmptyGeneAnnotation());
    }

    @NotNull
    public static ImmutableKnownFusion.Builder fusionBuilder() {
        return ImmutableKnownFusion.builder().from(TestServeFactory.createEmptyFusionPair()).proteinEffect(ProteinEffect.UNKNOWN);
    }

    @NotNull
    public static GeneAlteration createEmptyGeneAlteration() {
        return createGeneAlteration(GeneRole.UNKNOWN, ProteinEffect.UNKNOWN);
    }

    @NotNull
    public static GeneAlteration createGeneAlteration(@NotNull GeneRole geneRole, @NotNull ProteinEffect proteinEffect) {
        return createGeneAlteration(geneRole, proteinEffect, null);
    }

    @NotNull
    public static GeneAlteration createGeneAlteration(@NotNull GeneRole geneRole, @NotNull ProteinEffect proteinEffect,
            @Nullable Boolean associatedWithDrugResistance) {
        return new GeneAlteration() {
            @NotNull
            @Override
            public GeneRole geneRole() {
                return geneRole;
            }

            @NotNull
            @Override
            public ProteinEffect proteinEffect() {
                return proteinEffect;
            }

            @Nullable
            @Override
            public Boolean associatedWithDrugResistance() {
                return associatedWithDrugResistance;
            }
        };
    }
}
