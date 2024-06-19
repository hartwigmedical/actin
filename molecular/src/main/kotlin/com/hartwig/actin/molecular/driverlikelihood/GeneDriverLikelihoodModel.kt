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
        return if (hasGainOrLossOfFunction) {
            GeneDriverLikelihood(1.0, true)
        } else if (variants.any { it.geneRole == GeneRole.UNKNOWN }) {
            GeneDriverLikelihood()
        } else {
            handleVariantsOfUnknownSignificance(gene, geneRole, variants)
        }
    }

    private fun handleVariantsOfUnknownSignificance(gene: String, geneRole: GeneRole, variants: List<Variant>): GeneDriverLikelihood {
        val oncoLikelihood = variants.mapNotNull {
            when {
                (it.type == VariantType.INSERT || it.type == VariantType.DELETE) && it.canonicalImpact.codingEffect == CodingEffect.NONSENSE_OR_FRAMESHIFT -> DndsDriverType.INDEL
                (it.type == VariantType.SNV || it.type == VariantType.MNV) && it.canonicalImpact.codingEffect == CodingEffect.NONSENSE_OR_FRAMESHIFT -> DndsDriverType.NONESENSE
                it.canonicalImpact.codingEffect == CodingEffect.MISSENSE -> DndsDriverType.MISSENSE
                it.canonicalImpact.codingEffect == CodingEffect.SPLICE -> DndsDriverType.SPLICE
                else -> null
            }
        }.mapNotNull {
            val dnds = dndsDatabase.find(gene, geneRole, it)
            if (dnds != null) dnds to it else null
        }.map {
            val dnds = it.first
            val likelihood =
                getLikelihood(dnds.driversPerSample, dnds.probabilityVariantNonDriver)
            Triple(likelihood, it.second, dnds)
        }

        return if (variants.size == 1) {
            return GeneDriverLikelihood(oncoLikelihood.first().first)
        } else if (geneRole == GeneRole.ONCO) {
            return GeneDriverLikelihood(oncoLikelihood(oncoLikelihood))
        } else if (geneRole == GeneRole.TSG) {
            return GeneDriverLikelihood(tsgLikelihood(oncoLikelihood))
        } else {
            GeneDriverLikelihood(max(oncoLikelihood(oncoLikelihood), tsgLikelihood(oncoLikelihood)))
        }
    }

    private fun oncoLikelihood(driversAndLikelihoods: List<Triple<Double, DndsDriverType, DndsDatabaseEntry>>) =
        driversAndLikelihoods.maxOf { it.first }

    private fun tsgLikelihood(driversAndLikelihoods: List<Triple<Double, DndsDriverType, DndsDatabaseEntry>>) =
        jointProbability(driversAndLikelihoods.sortedBy {
            when (it.second) {
                DndsDriverType.NONESENSE -> 1
                DndsDriverType.INDEL -> 2
                DndsDriverType.SPLICE -> 3
                DndsDriverType.MISSENSE -> 4
            }
        }.take(2).map { it.third })

    private fun getLikelihood(driversPerSample: Double, probabilityOfNonDriver: Double) =
        driversPerSample / (driversPerSample + probabilityOfNonDriver * (1 - driversPerSample))

    private fun jointProbability(topTwo: List<DndsDatabaseEntry>): Double {
        val firstVariant = topTwo[0]
        val secondVariant = topTwo[1]
        val driversPerSample = max(firstVariant.driversPerSample, secondVariant.driversPerSample)
        val probabilityVariantNonDriver = firstVariant.probabilityVariantNonDriver + secondVariant.probabilityVariantNonDriver
        return getLikelihood(driversPerSample, probabilityVariantNonDriver)
    }
}