package com.hartwig.actin.molecular.orange.evidence.known;

import java.util.List;

import com.hartwig.actin.molecular.serve.KnownGene;
import com.hartwig.serve.datamodel.common.GeneAlteration;
import com.hartwig.serve.datamodel.common.GeneRole;
import com.hartwig.serve.datamodel.common.ProteinEffect;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

final class GeneLookup {

    private GeneLookup() {
    }

    @Nullable
    public static GeneAlteration find(@NotNull List<KnownGene> knownGenes, @NotNull String gene) {
        for (KnownGene knownGene : knownGenes) {
            if (knownGene.gene().equals(gene)) {
                return fromKnownGene(knownGene);
            }
        }
        return null;
    }

    @NotNull
    private static GeneAlteration fromKnownGene(@NotNull KnownGene knownGene) {
        return new GeneAlteration() {
            @NotNull
            @Override
            public GeneRole geneRole() {
                return toServeGeneRole(knownGene.geneRole());
            }

            @NotNull
            @Override
            public ProteinEffect proteinEffect() {
                return ProteinEffect.UNKNOWN;
            }

            @Nullable
            @Override
            public Boolean associatedWithDrugResistance() {
                return null;
            }
        };
    }

    @NotNull
    private static GeneRole toServeGeneRole(@NotNull com.hartwig.actin.molecular.datamodel.driver.GeneRole geneRole) {
        switch (geneRole) {
            case BOTH: {
                return GeneRole.BOTH;
            }
            case ONCO: {
                return GeneRole.ONCO;
            }
            case TSG: {
                return GeneRole.TSG;
            }
            case UNKNOWN: {
                return GeneRole.UNKNOWN;
            }
            default: {
                throw new IllegalStateException("Could not convert gene role: " + geneRole);
            }
        }
    }
}
