package com.hartwig.actin.molecular.orange.evidence.known;

import java.util.Set;

import com.hartwig.serve.datamodel.common.GeneAlteration;
import com.hartwig.serve.datamodel.common.GeneRole;
import com.hartwig.serve.datamodel.common.ProteinEffect;
import com.hartwig.serve.datamodel.gene.KnownGene;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

final class GeneLookup {

    private GeneLookup() {
    }

    @Nullable
    public static GeneAlteration find(@NotNull Set<KnownGene> knownGenes, @NotNull String gene) {
        for (KnownGene knownGene : GeneAggregator.aggregate(knownGenes)) {
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
                return knownGene.geneRole();
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
}
