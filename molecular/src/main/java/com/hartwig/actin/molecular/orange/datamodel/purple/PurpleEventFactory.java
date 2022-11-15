package com.hartwig.actin.molecular.orange.datamodel.purple;

import java.util.StringJoiner;

import com.hartwig.actin.molecular.datamodel.driver.Amplification;
import com.hartwig.actin.molecular.datamodel.driver.Loss;

import org.jetbrains.annotations.NotNull;

public final class PurpleEventFactory {

    private PurpleEventFactory() {
    }

    @NotNull
    public static String variantEvent(@NotNull PurpleVariant variant) {
        return variant.gene() + " " + impact(variant);
    }

    @NotNull
    private static String impact(@NotNull PurpleVariant variant) {
        if (!variant.canonicalHgvsProteinImpact().isEmpty() && !variant.canonicalHgvsProteinImpact().equals("p.?")) {
            return variant.canonicalHgvsProteinImpact();
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
    public static String amplificationEvent(@NotNull Amplification amplification) {
        return amplification.gene() + " amp";
    }

    @NotNull
    public static String lossEvent(@NotNull Loss loss) {
        return loss.gene() + " del";
    }

}
