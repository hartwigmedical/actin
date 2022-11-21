package com.hartwig.actin.molecular.orange.interpretation;

import java.util.StringJoiner;

import com.hartwig.actin.molecular.orange.datamodel.linx.LinxDisruption;
import com.hartwig.actin.molecular.orange.datamodel.linx.LinxFusion;
import com.hartwig.actin.molecular.orange.datamodel.linx.LinxHomozygousDisruption;
import com.hartwig.actin.molecular.orange.datamodel.purple.PurpleCodingEffect;
import com.hartwig.actin.molecular.orange.datamodel.purple.PurpleCopyNumber;
import com.hartwig.actin.molecular.orange.datamodel.purple.PurpleVariant;
import com.hartwig.actin.molecular.orange.datamodel.purple.PurpleVariantEffect;
import com.hartwig.actin.molecular.orange.datamodel.virus.VirusInterpretation;
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
    public static String copyNumberEvent(@NotNull PurpleCopyNumber copyNumber) {
        if (copyNumber.interpretation().isGain()) {
            return copyNumber.gene() + " amp";
        } else if (copyNumber.interpretation().isLoss()) {
            return copyNumber.gene() + " del";
        } else {
            return copyNumber.gene() + " unknown";
        }
    }

    @NotNull
    public static String homozygousDisruptionEvent(@NotNull LinxHomozygousDisruption homozygousDisruption) {
        return homozygousDisruption.gene() + " hom disruption";
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
        VirusInterpretation interpretation = virus.interpretation();
        return interpretation != null ? interpretation + " positive" : virus.name() + " positive";
    }
}
