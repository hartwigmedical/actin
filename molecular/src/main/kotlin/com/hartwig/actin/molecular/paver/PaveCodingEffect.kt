package com.hartwig.actin.molecular.paver

enum class PaveCodingEffect(private val rank: Int) {
    NONSENSE_OR_FRAMESHIFT(4),
    SPLICE(3),
    MISSENSE(2),
    SYNONYMOUS(1),
    NONE(0);

    companion object {
        fun worstCodingEffect(effects: List<PaveCodingEffect>): PaveCodingEffect {
            return effects.maxByOrNull { it.rank } ?: NONE
        }

        fun fromPaveVariantEffect(variantEffect: PaveVariantEffect): PaveCodingEffect {
            when (variantEffect) {
                PaveVariantEffect.FRAMESHIFT,
                PaveVariantEffect.STOP_GAINED,
                PaveVariantEffect.STOP_LOST,
                PaveVariantEffect.START_LOST -> return NONSENSE_OR_FRAMESHIFT

                PaveVariantEffect.SPLICE_ACCEPTOR,
                PaveVariantEffect.SPLICE_DONOR -> return SPLICE

                PaveVariantEffect.MISSENSE,
                PaveVariantEffect.INFRAME_DELETION,
                PaveVariantEffect.INFRAME_INSERTION,
                PaveVariantEffect.PHASED_INFRAME_DELETION,
                PaveVariantEffect.PHASED_INFRAME_INSERTION,
                PaveVariantEffect.PHASED_MISSENSE -> return MISSENSE

                PaveVariantEffect.SYNONYMOUS,
                PaveVariantEffect.PHASED_SYNONYMOUS -> return SYNONYMOUS

                else -> return NONE
            }
        }
    }
}