package com.hartwig.actin.molecular.orange.evidence.algo;

import com.hartwig.actin.molecular.orange.datamodel.purple.PurpleCodingEffect;
import com.hartwig.actin.molecular.orange.datamodel.purple.PurpleVariant;
import com.hartwig.actin.molecular.orange.datamodel.purple.PurpleVariantType;
import com.hartwig.serve.datamodel.MutationType;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

public final class MutationTypeMatcher {

    private static final Logger LOGGER = LogManager.getLogger(MutationTypeMatcher.class);

    private MutationTypeMatcher() {
    }

    public static boolean matches(@NotNull PurpleVariant variant, @NotNull MutationType typeToMatch) {
        PurpleCodingEffect effect = variant.canonicalCodingEffect();

        switch (typeToMatch) {
            case NONSENSE_OR_FRAMESHIFT:
                return effect == PurpleCodingEffect.NONSENSE_OR_FRAMESHIFT;
            case SPLICE:
                return effect == PurpleCodingEffect.SPLICE;
            case INFRAME:
                return effect == PurpleCodingEffect.MISSENSE && variant.type() == PurpleVariantType.INDEL;
            case INFRAME_DELETION:
                return effect == PurpleCodingEffect.MISSENSE && isDelete(variant);
            case INFRAME_INSERTION:
                return effect == PurpleCodingEffect.MISSENSE && isInsert(variant);
            case MISSENSE:
                return effect == PurpleCodingEffect.MISSENSE;
            case ANY:
                return effect == PurpleCodingEffect.MISSENSE || effect == PurpleCodingEffect.NONSENSE_OR_FRAMESHIFT
                        || effect == PurpleCodingEffect.SPLICE;
            default: {
                LOGGER.warn("Unrecognized mutation type to match: '{}'", typeToMatch);
                return false;
            }
        }
    }

    private static boolean isInsert(@NotNull PurpleVariant variant) {
        return variant.type() == PurpleVariantType.INDEL && variant.alt().length() > variant.ref().length();
    }

    private static boolean isDelete(@NotNull PurpleVariant variant) {
        return variant.type() == PurpleVariantType.INDEL && variant.alt().length() < variant.ref().length();
    }
}
