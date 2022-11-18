package com.hartwig.actin.molecular.orange.evidence;

import com.hartwig.serve.datamodel.common.GeneAlteration;
import com.hartwig.serve.datamodel.common.GeneRole;
import com.hartwig.serve.datamodel.common.ProteinEffect;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class TestEvidenceFactory {

    private TestEvidenceFactory() {
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
