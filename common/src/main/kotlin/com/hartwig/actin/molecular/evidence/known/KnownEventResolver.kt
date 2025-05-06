package com.hartwig.actin.molecular.evidence.known

import com.hartwig.actin.datamodel.molecular.driver.CopyNumber
import com.hartwig.actin.datamodel.molecular.driver.Disruption
import com.hartwig.actin.datamodel.molecular.driver.GeneAlteration
import com.hartwig.actin.datamodel.molecular.driver.HomozygousDisruption
import com.hartwig.actin.datamodel.molecular.driver.VariantAlteration
import com.hartwig.actin.molecular.evidence.matching.FusionMatchCriteria
import com.hartwig.actin.molecular.evidence.matching.HotspotMatching
import com.hartwig.actin.molecular.evidence.matching.RangeMatching
import com.hartwig.actin.molecular.evidence.matching.VariantMatchCriteria
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

    fun resolveForVariant(variantMatchCriteria: VariantMatchCriteria): VariantAlteration {
        val ckbAlteration = findHotspot(primaryKnownEvents.hotspots(), variantMatchCriteria)
            ?: findCodon(primaryKnownEvents.codons(), variantMatchCriteria)
            ?: findExon(primaryKnownEvents.exons(), variantMatchCriteria)
            ?: GeneLookup.find(aggregatedKnownGenes, variantMatchCriteria.gene)

        val geneAlteration = GeneAlterationFactory.convertAlteration(variantMatchCriteria.gene, ckbAlteration)
        val isHotspot =
            HotspotFunctions.isHotspot(ckbAlteration) || findHotspot(secondaryKnownEvents.hotspots(), variantMatchCriteria) != null

        return VariantAlteration(
            gene = geneAlteration.gene,
            geneRole = geneAlteration.geneRole,
            proteinEffect = geneAlteration.proteinEffect,
            isAssociatedWithDrugResistance = geneAlteration.isAssociatedWithDrugResistance,
            isHotspot = isHotspot
        )
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

    fun resolveForFusion(fusion: FusionMatchCriteria): KnownFusion? {
        return FusionLookup.find(primaryKnownEvents.fusions(), fusion)
    }

    private fun findHotspot(knownHotspots: Iterable<KnownHotspot>, variant: VariantMatchCriteria): KnownHotspot? {
        return knownHotspots.find { HotspotMatching.isMatch(it, variant) }
    }

    private fun findCodon(knownCodons: Iterable<KnownCodon>, variant: VariantMatchCriteria): KnownCodon? {
        return knownCodons.find { RangeMatching.isMatch(it, variant) }
    }

    private fun findExon(knownExons: Iterable<KnownExon>, variant: VariantMatchCriteria): KnownExon? {
        return knownExons.find { RangeMatching.isMatch(it, variant) }
    }
}
