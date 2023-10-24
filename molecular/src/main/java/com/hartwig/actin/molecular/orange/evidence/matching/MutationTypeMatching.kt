package com.hartwig.actin.molecular.orange.evidence.matching;

import com.hartwig.hmftools.datamodel.purple.PurpleCodingEffect;
import com.hartwig.hmftools.datamodel.purple.PurpleVariant;
import com.hartwig.hmftools.datamodel.purple.PurpleVariantType;
import com.hartwig.serve.datamodel.MutationType;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

public final class MutationTypeMatching {

    private static final Logger LOGGER = LogManager.getLogger(MutationTypeMatching.class);

    private MutationTypeMatching() {
    }

    public static boolean matches(@NotNull MutationType typeToMatch, @NotNull PurpleVariant variant) {
        PurpleCodingEffect effect = variant.canonicalImpact().codingEffect();

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
