package com.hartwig.actin.molecular.driverlikelihood

import com.hartwig.actin.molecular.datamodel.CodingEffect
import com.hartwig.actin.molecular.datamodel.GeneRole
import com.hartwig.actin.molecular.datamodel.ProteinEffect
import com.hartwig.actin.molecular.datamodel.Variant
import com.hartwig.actin.molecular.datamodel.VariantType
import kotlin.math.max

class GeneDriverLikelihoodModel(private val dndsDatabase: DndsDatabase) {

    fun evaluate(gene: String, geneRole: GeneRole, variants: List<Variant>): GeneDriverLikelihood {
        val hasGainOrLossOfFunction = variants.any {
            it.proteinEffect in setOf(
                ProteinEffect.GAIN_OF_FUNCTION,
                ProteinEffect.GAIN_OF_FUNCTION_PREDICTED,
                ProteinEffect.LOSS_OF_FUNCTION,
                ProteinEffect.LOSS_OF_FUNCTION_PREDICTED
            )
        }
        return if (variants.isEmpty()) {
            return GeneDriverLikelihood()
        } else if (hasGainOrLossOfFunction) {
            GeneDriverLikelihood(1.0, true)
        } else {
            handleVariantsOfUnknownSignificance(gene, geneRole, variants)
        }
    }

    private fun handleVariantsOfUnknownSignificance(gene: String, geneRole: GeneRole, variants: List<Variant>): GeneDriverLikelihood {
        return when (geneRole) {
            GeneRole.ONCO -> GeneDriverLikelihood(oncoLikelihood(lookupDndsPerVariant(variants, gene, geneRole)))

            GeneRole.TSG -> GeneDriverLikelihood(tsgLikelihood(lookupDndsPerVariant(variants, gene, geneRole)))

            GeneRole.BOTH -> GeneDriverLikelihood(
                max(
                    oncoLikelihood(lookupDndsPerVariant(variants, gene, GeneRole.ONCO)),
                    tsgLikelihood(lookupDndsPerVariant(variants, gene, GeneRole.TSG))
                )
            )

            GeneRole.UNKNOWN -> GeneDriverLikelihood()
        }
    }

    private fun lookupDndsPerVariant(
        variants: List<Variant>,
        gene: String,
        geneRole: GeneRole
    ) = variants.mapNotNull {
        when {
            (it.type == VariantType.INSERT || it.type == VariantType.DELETE) && it.canonicalImpact.codingEffect == CodingEffect.NONSENSE_OR_FRAMESHIFT -> DndsDriverType.INDEL
            (it.type == VariantType.SNV || it.type == VariantType.MNV) && it.canonicalImpact.codingEffect == CodingEffect.NONSENSE_OR_FRAMESHIFT -> DndsDriverType.NONSENSE
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
        dndsDatabase.find(gene, geneRole, it)
    }

    private fun oncoLikelihood(dndEntries: List<DndsDatabaseEntry>) =
        dndEntries.maxOf { getLikelihood(it.driversPerSample, it.probabilityVariantNonDriver) }

    private fun tsgLikelihood(dndEntries: List<DndsDatabaseEntry>) =
        if (dndEntries.size == 1) getLikelihood(
            dndEntries.first().driversPerSample,
            dndEntries.first().probabilityVariantNonDriver
        ) else jointProbability(dndEntries.take(2))


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