package com.hartwig.actin.molecular.evidence.known

import com.hartwig.actin.datamodel.molecular.driver.CodingEffect
import com.hartwig.actin.datamodel.molecular.driver.CopyNumber
import com.hartwig.actin.datamodel.molecular.driver.Disruption
import com.hartwig.actin.datamodel.molecular.driver.Fusion
import com.hartwig.actin.datamodel.molecular.driver.GeneAlteration
import com.hartwig.actin.datamodel.molecular.driver.GeneRole
import com.hartwig.actin.datamodel.molecular.driver.HomozygousDisruption
import com.hartwig.actin.datamodel.molecular.driver.ProteinEffect
import com.hartwig.actin.datamodel.molecular.driver.Variant
import com.hartwig.actin.datamodel.molecular.driver.VariantAlteration
import com.hartwig.actin.molecular.evidence.matching.HotspotMatching
import com.hartwig.actin.molecular.evidence.matching.RangeMatching
import com.hartwig.actin.molecular.interpretation.GeneAlterationFactory
import com.hartwig.serve.datamodel.molecular.KnownEvents
import com.hartwig.serve.datamodel.molecular.fusion.KnownFusion
import com.hartwig.serve.datamodel.molecular.gene.KnownGene
import com.hartwig.serve.datamodel.molecular.hotspot.KnownHotspot
import com.hartwig.serve.datamodel.molecular.range.KnownCodon
import com.hartwig.serve.datamodel.molecular.range.KnownExon

class KnownEventResolver(
    private val primaryKnownEvents: KnownEvents,
    private val secondaryKnownEvents: KnownEvents,
    private val aggregatedKnownGenes: Set<KnownGene>
) {

    fun resolveForVariant(variant: Variant): VariantAlteration {
        val primaryAlteration = findHotspot(primaryKnownEvents.hotspots(), variant)
            ?: findCodon(primaryKnownEvents.codons(), variant)
            ?: findExon(primaryKnownEvents.exons(), variant)
            ?: GeneLookup.find(aggregatedKnownGenes, variant.gene)

        val secondaryAlteration = findHotspot(secondaryKnownEvents.hotspots(), variant)

        val geneAlteration = reannotateProteinEffect(variant, GeneAlterationFactory.convertAlteration(variant.gene, primaryAlteration))
        val isCancerAssociatedVariant = CancerAssociatedVariantFunctions.isCancerAssociatedVariant(primaryAlteration) ||
                CancerAssociatedVariantFunctions.isCancerAssociatedVariant(secondaryAlteration)

        return VariantAlteration(
            gene = geneAlteration.gene,
            geneRole = geneAlteration.geneRole,
            proteinEffect = geneAlteration.proteinEffect,
            isAssociatedWithDrugResistance = geneAlteration.isAssociatedWithDrugResistance,
            isCancerAssociatedVariant = isCancerAssociatedVariant
        )
    }

    fun reannotateProteinEffect(variant: Variant, alteration: GeneAlteration): GeneAlteration {
        val overwrite = variant.canonicalImpact.codingEffect == CodingEffect.NONSENSE_OR_FRAMESHIFT && alteration.geneRole == GeneRole.TSG
        return object : GeneAlteration {
            override val gene: String = alteration.gene
            override val geneRole: GeneRole = alteration.geneRole
            override val proteinEffect: ProteinEffect = if (overwrite) ProteinEffect.LOSS_OF_FUNCTION else alteration.proteinEffect
            override val isAssociatedWithDrugResistance: Boolean? = alteration.isAssociatedWithDrugResistance
        }
    }

    fun resolveForCopyNumber(copyNumber: CopyNumber): GeneAlteration {
        return GeneAlterationFactory.convertAlteration(
            copyNumber.gene,
            CopyNumberLookup.findForCopyNumber(primaryKnownEvents.copyNumbers(), copyNumber) ?: GeneLookup.find(
                aggregatedKnownGenes,
                copyNumber.gene
            )
        )
    }

    fun resolveForHomozygousDisruption(homozygousDisruption: HomozygousDisruption): GeneAlteration {
        // Assume a homozygous disruption always has the same annotation as a deletion.
        return GeneAlterationFactory.convertAlteration(
            homozygousDisruption.gene,
            CopyNumberLookup.findForHomozygousDisruption(primaryKnownEvents.copyNumbers(), homozygousDisruption) ?: GeneLookup.find(
                aggregatedKnownGenes,
                homozygousDisruption.gene
            )
        )
    }

    fun resolveForDisruption(disruption: Disruption): GeneAlteration {
        return GeneAlterationFactory.convertAlteration(disruption.gene, GeneLookup.find(aggregatedKnownGenes, disruption.gene))
    }

    fun resolveForFusion(fusion: Fusion): KnownFusion {
        return FusionLookup.find(primaryKnownEvents.fusions(), fusion)
    }

    private fun findHotspot(knownHotspots: Iterable<KnownHotspot>, variant: Variant): KnownHotspot? {
        return knownHotspots.find { HotspotMatching.isMatch(it, variant) }
    }

    private fun findCodon(knownCodons: Iterable<KnownCodon>, variant: Variant): KnownCodon? {
        return knownCodons.find { RangeMatching.isMatch(it, variant) }
    }

    private fun findExon(knownExons: Iterable<KnownExon>, variant: Variant): KnownExon? {
        return knownExons.find { RangeMatching.isMatch(it, variant) }
    }
}
