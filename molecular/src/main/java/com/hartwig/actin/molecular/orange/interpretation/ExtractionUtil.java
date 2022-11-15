package com.hartwig.actin.molecular.orange.interpretation;

import com.hartwig.actin.molecular.datamodel.driver.GeneAlteration;
import com.hartwig.actin.molecular.datamodel.driver.GeneRole;
import com.hartwig.actin.molecular.datamodel.driver.ProteinEffect;
import com.hartwig.actin.molecular.datamodel.evidence.ActionableEvidence;
import com.hartwig.actin.molecular.datamodel.evidence.ImmutableActionableEvidence;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

final class ExtractionUtil {

    private ExtractionUtil() {
    }

    public static double keep3Digits(double input) {
        return Math.round(input * 1000) / 1000D;
    }

    @NotNull
    public static ActionableEvidence createEmptyEvidence() {
        return ImmutableActionableEvidence.builder().build();
    }

    @NotNull
    public static GeneAlteration createBaseGeneAlteration(@NotNull String gene) {
        return new GeneAlteration() {
            @NotNull
            @Override
            public String gene() {
                return gene;
            }

            @NotNull
            @Override
            public GeneRole geneRole() {
                return GeneRole.UNKNOWN;
            }

            @NotNull
            @Override
            public ProteinEffect proteinEffect() {
                return ProteinEffect.NO_EFFECT;
            }

            @Nullable
            @Override
            public Boolean isAssociatedWithDrugResistance() {
                return null;
            }
        };
    }
}
