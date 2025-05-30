package com.hartwig.actin.molecular.evidence.matching

import com.hartwig.actin.datamodel.molecular.driver.CodingEffect
import com.hartwig.actin.datamodel.molecular.driver.Variant
import com.hartwig.actin.datamodel.molecular.driver.VariantType
import com.hartwig.serve.datamodel.molecular.MutationType
import org.apache.logging.log4j.LogManager

object MutationTypeMatching {

    private val LOGGER = LogManager.getLogger(MutationTypeMatching::class.java)

    fun matches(typeToMatch: MutationType, variant: Variant): Boolean {
        val effect = variant.canonicalImpact.codingEffect

        return when (typeToMatch) {
            MutationType.NONSENSE_OR_FRAMESHIFT -> effect == CodingEffect.NONSENSE_OR_FRAMESHIFT
            MutationType.SPLICE -> effect == CodingEffect.SPLICE
            MutationType.INFRAME -> effect == CodingEffect.MISSENSE &&
                    (variant.type == VariantType.MNV || variant.type == VariantType.INSERT || variant.type == VariantType.DELETE)

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
}
