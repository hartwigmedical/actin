package com.hartwig.actin.molecular.orange.interpretation;

import java.util.StringJoiner;

import com.hartwig.actin.molecular.orange.datamodel.linx.LinxDisruption;
import com.hartwig.actin.molecular.orange.datamodel.linx.LinxFusion;
import com.hartwig.actin.molecular.orange.datamodel.linx.LinxHomozygousDisruption;
import com.hartwig.actin.molecular.orange.datamodel.purple.PurpleCodingEffect;
import com.hartwig.actin.molecular.orange.datamodel.purple.PurpleCopyNumber;
import com.hartwig.actin.molecular.orange.datamodel.purple.PurpleTranscriptImpact;
import com.hartwig.actin.molecular.orange.datamodel.purple.PurpleVariant;
import com.hartwig.actin.molecular.orange.datamodel.purple.PurpleVariantEffect;
import com.hartwig.actin.molecular.orange.datamodel.virus.VirusInterpretation;
import com.hartwig.actin.molecular.orange.datamodel.virus.VirusInterpreterEntry;
import com.hartwig.actin.molecular.orange.util.AminoAcid;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

public final class DriverEventFactory {

    private static final Logger LOGGER = LogManager.getLogger(DriverEventFactory.class);

    private DriverEventFactory() {
    }

    @NotNull
    public static String variantEvent(@NotNull PurpleVariant variant) {
        return variant.gene() + " " + impact(variant);
    }

    @NotNull
    private static String impact(@NotNull PurpleVariant variant) {
        PurpleTranscriptImpact canonical = variant.canonicalImpact();
        if (!canonical.hgvsProteinImpact().isEmpty() && !canonical.hgvsProteinImpact().equals("p.?")) {
            return reformatProteinImpact(canonical.hgvsProteinImpact());
        }

        if (!canonical.hgvsCodingImpact().isEmpty()) {
            return canonical.codingEffect() == PurpleCodingEffect.SPLICE
                    ? canonical.hgvsCodingImpact() + " splice"
                    : canonical.hgvsCodingImpact();
        }

        if (canonical.effects().contains(PurpleVariantEffect.UPSTREAM_GENE)) {
            return "upstream";
        }

        LOGGER.warn("Unexpected variant with potentially unexpected formatting on canonical impact: {}", variant);

        StringJoiner joiner = new StringJoiner("&");
        for (PurpleVariantEffect effect : canonical.effects()) {
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
