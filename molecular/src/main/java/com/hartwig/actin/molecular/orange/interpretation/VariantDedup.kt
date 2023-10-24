package com.hartwig.actin.molecular.orange.interpretation;

import java.util.Set;
import java.util.stream.Collectors;

import com.hartwig.hmftools.datamodel.purple.PurpleTranscriptImpact;
import com.hartwig.hmftools.datamodel.purple.PurpleVariant;
import com.hartwig.hmftools.datamodel.purple.PurpleVariantEffect;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class VariantDedup {

    private static final double EPSILON = 1e-10;

    private static final Logger LOGGER = LogManager.getLogger(VariantDedup.class);

    private static final Set<PurpleVariantEffect> PHASED_EFFECTS =
            Set.of(PurpleVariantEffect.PHASED_INFRAME_DELETION, PurpleVariantEffect.PHASED_INFRAME_INSERTION);

    public static Set<PurpleVariant> apply(Set<PurpleVariant> variants) {
        return variants.stream().filter(variant -> include(variant, variants)).collect(Collectors.toSet());
    }

    private static boolean include(PurpleVariant variant, Set<PurpleVariant> variants) {
        if (hasCanonicalPhasedEffect(variant) && hasSameEffectWithHigherVCN(variants, variant)) {
            LOGGER.debug("Dedup'ing variant '{}'", variant);
            return false;
        } else {
            return true;
        }
    }

    private static boolean hasCanonicalPhasedEffect(PurpleVariant variant) {
        return variant.canonicalImpact().effects().stream().anyMatch(PHASED_EFFECTS::contains);
    }

    private static boolean hasSameEffectWithHigherVCN(Set<PurpleVariant> variants, PurpleVariant variantToMatch) {
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
