package com.hartwig.actin.molecular.orange

import com.hartwig.hmftools.datamodel.purple.PurpleTranscriptImpact
import com.hartwig.hmftools.datamodel.purple.PurpleVariant
import com.hartwig.hmftools.datamodel.purple.PurpleVariantEffect
import org.apache.logging.log4j.LogManager
import kotlin.math.abs

object VariantDedup {

    private const val EPSILON = 1e-10

    private val LOGGER = LogManager.getLogger(VariantDedup::class.java)
    private val PHASED_EFFECTS = setOf(PurpleVariantEffect.PHASED_INFRAME_DELETION, PurpleVariantEffect.PHASED_INFRAME_INSERTION)

    fun apply(variants: Set<PurpleVariant>): Set<PurpleVariant> {
        return variants.filter { variant: PurpleVariant -> include(variant, variants) }.toSet()
    }

    private fun include(variant: PurpleVariant, variants: Set<PurpleVariant>): Boolean {
        return if (hasCanonicalPhasedEffect(variant) && hasSameEffectWithHigherVCN(variants, variant)) {
            LOGGER.debug("Dedup'ing variant '{}'", variant)
            false
        } else {
            true
        }
    }

    private fun hasCanonicalPhasedEffect(variant: PurpleVariant): Boolean {
        return variant.canonicalImpact().effects().any { PHASED_EFFECTS.contains(it) }
    }

    private fun hasSameEffectWithHigherVCN(variants: Set<PurpleVariant>, variantToMatch: PurpleVariant): Boolean {
        // We assume that variants with same effect have unique hgvs coding impact.
        var minVariantCopyNumber: Double? = null
        var uniqueHgvsCodingImpact: String? = null
        val variantImpactToMatch = variantToMatch.canonicalImpact()
        for (variant in variants) {
            val variantImpact: PurpleTranscriptImpact = variant.canonicalImpact()
            if (variantImpact.effects() == variantImpactToMatch.effects() && variant.gene() == variantToMatch.gene() &&
                variantImpact.hgvsProteinImpact() == variantImpactToMatch.hgvsProteinImpact()
            ) {
                if (minVariantCopyNumber == null || lessThan(variant.variantCopyNumber(), minVariantCopyNumber)) {
                    minVariantCopyNumber = variant.variantCopyNumber()
                    uniqueHgvsCodingImpact = variantImpact.hgvsCodingImpact()
                } else if (equal(variant.variantCopyNumber(), minVariantCopyNumber)) {
                    uniqueHgvsCodingImpact = if (uniqueHgvsCodingImpact != null &&
                        variantImpact.hgvsCodingImpact() > uniqueHgvsCodingImpact
                    ) variantImpact.hgvsCodingImpact() else uniqueHgvsCodingImpact
                }
            }
        }
        val matchesMinVariantCopyNumber = minVariantCopyNumber != null && equal(variantToMatch.variantCopyNumber(), minVariantCopyNumber)
        val matchesBestHgvsCodingImpact = variantImpactToMatch.hgvsCodingImpact() == uniqueHgvsCodingImpact
        return !(matchesMinVariantCopyNumber && matchesBestHgvsCodingImpact)
    }

    private fun equal(first: Double, second: Double): Boolean {
        return abs(first - second) < EPSILON
    }

    private fun lessThan(value: Double, reference: Double): Boolean {
        return value - reference < -EPSILON
    }
}