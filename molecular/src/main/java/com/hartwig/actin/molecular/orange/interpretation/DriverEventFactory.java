package com.hartwig.actin.molecular.orange.interpretation;

import java.util.StringJoiner;

import com.hartwig.actin.molecular.orange.datamodel.linx.LinxDisruption;
import com.hartwig.actin.molecular.orange.datamodel.linx.LinxFusion;
import com.hartwig.actin.molecular.orange.datamodel.purple.GainLossInterpretation;
import com.hartwig.actin.molecular.orange.datamodel.purple.PurpleCodingEffect;
import com.hartwig.actin.molecular.orange.datamodel.purple.PurpleGainLoss;
import com.hartwig.actin.molecular.orange.datamodel.purple.PurpleVariant;
import com.hartwig.actin.molecular.orange.datamodel.purple.PurpleVariantEffect;
import com.hartwig.actin.molecular.orange.datamodel.virus.VirusInterpreterEntry;
import com.hartwig.actin.molecular.orange.util.AminoAcid;

import org.jetbrains.annotations.NotNull;

public final class DriverEventFactory {

    private DriverEventFactory() {
    }

    @NotNull
    public static String variantEvent(@NotNull PurpleVariant variant) {
        return variant.gene() + " " + impact(variant);
    }

    @NotNull
    private static String impact(@NotNull PurpleVariant variant) {
        if (!variant.canonicalHgvsProteinImpact().isEmpty() && !variant.canonicalHgvsProteinImpact().equals("p.?")) {
            return reformatProteinImpact(variant.canonicalHgvsProteinImpact());
        }

        if (!variant.canonicalHgvsCodingImpact().isEmpty()) {
            return variant.canonicalCodingEffect() == PurpleCodingEffect.SPLICE
                    ? variant.canonicalHgvsCodingImpact() + " splice"
                    : variant.canonicalHgvsCodingImpact();
        }

        if (variant.canonicalEffects().contains(PurpleVariantEffect.UPSTREAM_GENE)) {
            return "upstream";
        }

        StringJoiner joiner = new StringJoiner("&");
        for (PurpleVariantEffect effect : variant.canonicalEffects()) {
            joiner.add(effect.toString());
        }
        return joiner.toString();
    }

    @NotNull
    private static String reformatProteinImpact(@NotNull String proteinImpact) {
        String reformatted = proteinImpact.startsWith("p.") ? proteinImpact.substring(2) : proteinImpact;
        return AminoAcid.forceSingleLetterAminoAcids(reformatted);
    }

    @NotNull
    public static String gainLossEvent(@NotNull PurpleGainLoss gainLoss) {
        boolean isAmp = gainLoss.interpretation() == GainLossInterpretation.FULL_GAIN
                || gainLoss.interpretation() == GainLossInterpretation.PARTIAL_GAIN;
        return isAmp ? gainLoss.gene() + " amp" : gainLoss.gene() + " del";
    }

    @NotNull
    public static String disruptionEvent(@NotNull LinxDisruption disruption) {
        return disruption.gene() + " disruption";
    }

    @NotNull
    public static String fusionEvent(@NotNull LinxFusion fusion) {
        return fusion.geneStart() + " - " + fusion.geneEnd() + " fusion";
    }

    @NotNull
    public static String virusEvent(@NotNull VirusInterpreterEntry virus) {
        String interpretation = virus.interpretation();
        return interpretation != null ? interpretation + " positive" : virus.name() + " positive";
    }
}
