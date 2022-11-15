package com.hartwig.actin.molecular.orange.datamodel.purple;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;

public enum PurpleVariantEffect {
    STOP_GAINED("stop_gained"),
    STOP_LOST("stop_lost"),
    START_LOST("start_lost"),
    FRAMESHIFT("frameshift_variant"),
    SPLICE_ACCEPTOR("splice_acceptor_variant"),
    SPLICE_DONOR("splice_donor_variant"),
    INFRAME_INSERTION("inframe_insertion"),
    INFRAME_DELETION("inframe_deletion"),
    MISSENSE("missense_variant"),
    PHASED_INFRAME_INSERTION("phased_inframe_insertion"),
    PHASED_INFRAME_DELETION("phased_inframe_deletion"),
    SYNONYMOUS("synonymous_variant"),
    INTRONIC("intron_variant"),
    FIVE_PRIME_UTR("5_prime_UTR_variant"),
    THREE_PRIME_UTR("3_prime_UTR_variant"),
    UPSTREAM_GENE("upstream_gene_variant"),
    NON_CODING_TRANSCRIPT("non_coding_transcript_exon_variant"),
    OTHER("other");

    private static final String VARIANT_EFFECTS_DELIMITER = "&";

    @NotNull
    private final String display;

    PurpleVariantEffect(@NotNull final String display) {
        this.display = display;
    }

    // TODO Move resolving to variant effect from display into ORANGE
    @NotNull
    public static List<PurpleVariantEffect> fromEffectString(@NotNull String effectString) {
        String[] variantEffectStrings = effectString.split(VARIANT_EFFECTS_DELIMITER);
        return Arrays.stream(variantEffectStrings).map(PurpleVariantEffect::fromEffect).collect(Collectors.toList());
    }

    @NotNull
    private static PurpleVariantEffect fromEffect(@NotNull String display) {
        for (PurpleVariantEffect variantEffect : PurpleVariantEffect.values()) {
            if (variantEffect.display.equals(display)) {
                return variantEffect;
            }
        }

        throw new IllegalStateException("Could not interpret purple variant effect: " + display);
    }
}
