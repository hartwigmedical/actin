package com.hartwig.actin.molecular.driverlikelihood

import com.hartwig.actin.datamodel.molecular.driver.CodingEffect
import com.hartwig.actin.datamodel.molecular.driver.GeneRole
import com.hartwig.actin.datamodel.molecular.driver.Variant
import com.hartwig.actin.datamodel.molecular.driver.VariantType
import kotlin.math.max

class GeneDriverLikelihoodModel(private val dndsModel: DndsModel) {

    fun evaluate(gene: String, geneRole: GeneRole, variants: List<Variant>): Double? {
        val hasCancerAssociatedVariant = variants.any { it.isCancerAssociatedVariant }
        return when {
            variants.isEmpty() -> null
            hasCancerAssociatedVariant -> 1.0
            variants.any { it.isBiallelic == true && it.canonicalImpact.codingEffect != CodingEffect.MISSENSE } -> 1.0
            else -> handleVariantsOfUnknownSignificance(gene, geneRole, variants)
        }
    }

    private fun handleVariantsOfUnknownSignificance(
        gene: String,
        geneRole: GeneRole,
        variants: List<Variant>
    ): Double? {
        return when (geneRole) {
            GeneRole.ONCO -> oncoLikelihood(lookupDndsPerVariant(variants, gene, geneRole))

            GeneRole.TSG -> tsgLikelihood(lookupDndsPerVariant(variants, gene, geneRole))

            GeneRole.BOTH -> {
                val oncoLikelihood = oncoLikelihood(lookupDndsPerVariant(variants, gene, GeneRole.ONCO))
                val tsgLikelihood = tsgLikelihood(lookupDndsPerVariant(variants, gene, GeneRole.TSG))
                if (oncoLikelihood == null || tsgLikelihood == null) {
                    null
                } else {
                    max(oncoLikelihood, tsgLikelihood)
                }
            }

            GeneRole.UNKNOWN -> null
        }
    }

    private fun lookupDndsPerVariant(
        variants: List<Variant>,
        gene: String,
        geneRole: GeneRole
    ) = variants.mapNotNull {
        when {
            (it.type == VariantType.INSERT || it.type == VariantType.DELETE) &&
                    it.canonicalImpact.codingEffect == CodingEffect.NONSENSE_OR_FRAMESHIFT -> DndsDriverType.INDEL

            (it.type == VariantType.SNV || it.type == VariantType.MNV) &&
                    it.canonicalImpact.codingEffect == CodingEffect.NONSENSE_OR_FRAMESHIFT -> DndsDriverType.NONSENSE

            it.canonicalImpact.codingEffect == CodingEffect.MISSENSE -> DndsDriverType.MISSENSE
            it.canonicalImpact.codingEffect == CodingEffect.SPLICE -> DndsDriverType.SPLICE
            else -> null
        }
    }.sortedBy {
        when (it) {
            DndsDriverType.NONSENSE -> 1
            DndsDriverType.INDEL -> 2
            DndsDriverType.SPLICE -> 3
            DndsDriverType.MISSENSE -> 4
        }
    }.mapNotNull {
        dndsModel.find(gene, geneRole, it)
    }

    private fun oncoLikelihood(dndsEntries: List<DndsDatabaseEntry>): Double? {
        return if (dndsEntries.isEmpty()) {
            null
        } else {
            dndsEntries.maxOf { getLikelihood(it.driversPerSample, it.probabilityVariantNonDriver) }
        }
    }

    private fun tsgLikelihood(dndsEntries: List<DndsDatabaseEntry>): Double? {
        return when (dndsEntries.size) {
            0 -> null
            1 -> getLikelihood(
                dndsEntries.first().driversPerSample,
                dndsEntries.first().probabilityVariantNonDriver
            )

            else -> jointProbability(dndsEntries.take(2))
        }
    }

    private fun jointProbability(topTwo: List<DndsDatabaseEntry>): Double {
        val firstVariant = topTwo[0]
        val secondVariant = topTwo[1]
        val driversPerSample = max(firstVariant.driversPerSample, secondVariant.driversPerSample)
        val probabilityVariantNonDriver = firstVariant.probabilityVariantNonDriver * secondVariant.probabilityVariantNonDriver
        return getLikelihood(driversPerSample, probabilityVariantNonDriver)
    }

    private fun getLikelihood(driversPerSample: Double, probabilityOfNonDriver: Double) =
        driversPerSample / (driversPerSample + probabilityOfNonDriver * (1 - driversPerSample))
}