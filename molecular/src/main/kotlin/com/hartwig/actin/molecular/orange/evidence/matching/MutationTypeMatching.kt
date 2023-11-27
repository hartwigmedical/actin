package com.hartwig.actin.molecular.orange.evidence.matching

import com.hartwig.hmftools.datamodel.purple.PurpleCodingEffect
import com.hartwig.hmftools.datamodel.purple.PurpleVariant
import com.hartwig.hmftools.datamodel.purple.PurpleVariantType
import com.hartwig.serve.datamodel.MutationType
import org.apache.logging.log4j.LogManager

object MutationTypeMatching {

    private val LOGGER = LogManager.getLogger(MutationTypeMatching::class.java)

    fun matches(typeToMatch: MutationType, variant: PurpleVariant): Boolean {
        val effect = variant.canonicalImpact().codingEffect()

        return when (typeToMatch) {
            MutationType.NONSENSE_OR_FRAMESHIFT -> effect == PurpleCodingEffect.NONSENSE_OR_FRAMESHIFT
            MutationType.SPLICE -> effect == PurpleCodingEffect.SPLICE
            MutationType.INFRAME -> effect == PurpleCodingEffect.MISSENSE && variant.type() == PurpleVariantType.INDEL
            MutationType.INFRAME_DELETION -> effect == PurpleCodingEffect.MISSENSE && isDelete(variant)
            MutationType.INFRAME_INSERTION -> effect == PurpleCodingEffect.MISSENSE && isInsert(variant)
            MutationType.MISSENSE -> effect == PurpleCodingEffect.MISSENSE
            MutationType.ANY -> effect == PurpleCodingEffect.MISSENSE ||
                    effect == PurpleCodingEffect.NONSENSE_OR_FRAMESHIFT ||
                    effect == PurpleCodingEffect.SPLICE

            else -> {
                LOGGER.warn("Unrecognized mutation type to match: '{}'", typeToMatch)
                false
            }
        }
    }

    private fun isInsert(variant: PurpleVariant): Boolean {
        return variant.type() == PurpleVariantType.INDEL && variant.alt().length > variant.ref().length
    }

    private fun isDelete(variant: PurpleVariant): Boolean {
        return variant.type() == PurpleVariantType.INDEL && variant.alt().length < variant.ref().length
    }
}
