package com.hartwig.actin.molecular.orange.evidence.matching

import com.hartwig.actin.molecular.datamodel.driver.CodingEffect
import com.hartwig.actin.molecular.datamodel.driver.Variant
import com.hartwig.actin.molecular.datamodel.driver.VariantType
import com.hartwig.serve.datamodel.MutationType
import org.apache.logging.log4j.LogManager

object MutationTypeMatching {

    private val LOGGER = LogManager.getLogger(MutationTypeMatching::class.java)

    fun matches(typeToMatch: MutationType, variant: Variant): Boolean {
        val effect = variant.canonicalImpact.codingEffect

        return when (typeToMatch) {
            MutationType.NONSENSE_OR_FRAMESHIFT -> effect == CodingEffect.NONSENSE_OR_FRAMESHIFT
            MutationType.SPLICE -> effect == CodingEffect.SPLICE
            MutationType.INFRAME -> effect == CodingEffect.MISSENSE && isIndel(variant)
            MutationType.INFRAME_DELETION -> effect == CodingEffect.MISSENSE && variant.type == VariantType.DELETE
            MutationType.INFRAME_INSERTION -> effect == CodingEffect.MISSENSE && variant.type == VariantType.INSERT
            MutationType.MISSENSE -> effect == CodingEffect.MISSENSE
            MutationType.ANY -> effect == CodingEffect.MISSENSE ||
                    effect == CodingEffect.NONSENSE_OR_FRAMESHIFT ||
                    effect == CodingEffect.SPLICE

            else -> {
                LOGGER.warn("Unrecognized mutation type to match: '{}'", typeToMatch)
                false
            }
        }
    }

    private fun isIndel(variant: Variant): Boolean {
        return variant.type == VariantType.INSERT || variant.type == VariantType.DELETE
    }
}
