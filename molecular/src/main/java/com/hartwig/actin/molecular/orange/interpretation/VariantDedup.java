package com.hartwig.actin.molecular.orange.interpretation;

import java.util.Set;

import com.google.common.collect.Sets;
import com.hartwig.actin.molecular.orange.datamodel.purple.PurpleTranscriptImpact;
import com.hartwig.actin.molecular.orange.datamodel.purple.PurpleVariant;
import com.hartwig.actin.molecular.orange.datamodel.purple.PurpleVariantEffect;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

public final class VariantDedup {

    private static final double EPSILON = 1e-10;

    private static final Logger LOGGER = LogManager.getLogger(VariantDedup.class);

    private static final Set<PurpleVariantEffect> PHASED_EFFECTS =
            Set.of(PurpleVariantEffect.PHASED_INFRAME_DELETION, PurpleVariantEffect.PHASED_INFRAME_INSERTION);

    @NotNull
    public static Set<PurpleVariant> apply(@NotNull Set<PurpleVariant> variants) {
        Set<PurpleVariant> filtered = Sets.newHashSet();
        for (PurpleVariant variant : variants) {
            if (hasCanonicalPhasedEffect(variant) && hasSameEffectWithHigherVCN(variants, variant)) {
                LOGGER.debug("Dedup'ing variant '{}'", variant);
            } else {
                filtered.add(variant);
            }
        }
        return filtered;
    }

    private static boolean hasCanonicalPhasedEffect(@NotNull PurpleVariant variant) {
        for (PurpleVariantEffect effect : variant.canonicalImpact().effects()) {
            if (PHASED_EFFECTS.contains(effect)) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasSameEffectWithHigherVCN(@NotNull Set<PurpleVariant> variants, @NotNull PurpleVariant variantToMatch) {
        // We assume that variants with same effect have unique hgvs coding impact.
        Double minVariantCopyNumber = null;
        String uniqueHgvsCodingImpact = null;
        PurpleTranscriptImpact variantImpactToMatch = variantToMatch.canonicalImpact();

        for (PurpleVariant variant : variants) {
            PurpleTranscriptImpact variantImpact = variant.canonicalImpact();
            if (variantImpact.effects().equals(variantImpactToMatch.effects()) && variant.gene().equals(variantToMatch.gene())
                    && variantImpact.hgvsProteinImpact().equals(variantImpactToMatch.hgvsProteinImpact())) {
                if (minVariantCopyNumber == null || lessThan(variant.variantCopyNumber(), minVariantCopyNumber)) {
                    minVariantCopyNumber = variant.variantCopyNumber();
                    uniqueHgvsCodingImpact = variantImpact.hgvsCodingImpact();
                } else if (equal(variant.variantCopyNumber(), minVariantCopyNumber)) {
                    uniqueHgvsCodingImpact = variantImpact.hgvsCodingImpact().compareTo(uniqueHgvsCodingImpact) > 0
                            ? variantImpact.hgvsCodingImpact()
                            : uniqueHgvsCodingImpact;
                }
            }
        }

        boolean matchesMinVariantCopyNumber = equal(variantToMatch.variantCopyNumber(), minVariantCopyNumber);
        boolean matchesBestHgvsCodingImpact = variantImpactToMatch.hgvsCodingImpact().equals(uniqueHgvsCodingImpact);
        return !(matchesMinVariantCopyNumber && matchesBestHgvsCodingImpact);
    }

    private static boolean equal(double first, double second) {
        return Math.abs(first - second) < EPSILON;
    }

    public static boolean lessThan(double value, double reference) {
        return value - reference < -EPSILON;
    }
}
