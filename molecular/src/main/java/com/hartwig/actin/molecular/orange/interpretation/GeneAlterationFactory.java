package com.hartwig.actin.molecular.orange.interpretation;

import com.hartwig.actin.molecular.datamodel.driver.GeneAlteration;
import com.hartwig.actin.molecular.datamodel.driver.GeneRole;
import com.hartwig.actin.molecular.datamodel.driver.ProteinEffect;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class GeneAlterationFactory {

    private GeneAlterationFactory() {
    }

    @NotNull
    public static GeneAlteration convertAlteration(@NotNull String gene,
            @Nullable com.hartwig.serve.datamodel.common.GeneAlteration input) {
        return new GeneAlteration() {
            @NotNull
            @Override
            public String gene() {
                return gene;
            }

            @NotNull
            @Override
            public GeneRole geneRole() {
                return input != null ? convertGeneRole(input.geneRole()) : GeneRole.UNKNOWN;
            }

            @NotNull
            @Override
            public ProteinEffect proteinEffect() {
                return input != null ? convertProteinEffect(input.proteinEffect()) : ProteinEffect.UNKNOWN;
            }

            @Nullable
            @Override
            public Boolean isAssociatedWithDrugResistance() {
                return input != null ? input.associatedWithDrugResistance() : null;
            }
        };
    }

    @NotNull
    private static GeneRole convertGeneRole(@NotNull com.hartwig.serve.datamodel.common.GeneRole input) {
        switch (input) {
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
                throw new IllegalStateException("Could not convert gene role input: " + input);
            }
        }
    }

    @NotNull
    public static ProteinEffect convertProteinEffect(@NotNull com.hartwig.serve.datamodel.common.ProteinEffect input) {
        switch (input) {
            case UNKNOWN: {
                return ProteinEffect.UNKNOWN;
            }
            case AMBIGUOUS: {
                return ProteinEffect.AMBIGUOUS;
            }
            case NO_EFFECT: {
                return ProteinEffect.NO_EFFECT;
            }
            case NO_EFFECT_PREDICTED: {
                return ProteinEffect.NO_EFFECT_PREDICTED;
            }
            case LOSS_OF_FUNCTION: {
                return ProteinEffect.LOSS_OF_FUNCTION;
            }
            case LOSS_OF_FUNCTION_PREDICTED: {
                return ProteinEffect.LOSS_OF_FUNCTION_PREDICTED;
            }
            case GAIN_OF_FUNCTION: {
                return ProteinEffect.GAIN_OF_FUNCTION;
            }
            case GAIN_OF_FUNCTION_PREDICTED: {
                return ProteinEffect.GAIN_OF_FUNCTION_PREDICTED;
            }
            default: {
                throw new IllegalStateException("Could not convert protein effect: " + input);
            }
        }
    }
}
