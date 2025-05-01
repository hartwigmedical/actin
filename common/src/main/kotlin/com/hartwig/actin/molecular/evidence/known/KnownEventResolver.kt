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
import com.hartwig.serve.datamodel.molecular.common.GeneAlteration as ServeGeneAlteration
import com.hartwig.serve.datamodel.molecular.fusion.KnownFusion
import com.hartwig.serve.datamodel.molecular.gene.KnownGene
import com.hartwig.serve.datamodel.molecular.hotspot.KnownHotspot
import com.hartwig.serve.datamodel.molecular.range.KnownCodon
import com.hartwig.serve.datamodel.molecular.range.KnownExon

class KnownEventResolver(
    private val knownEvents: KnownEvents,
    private val filteredKnownEvents: KnownEvents,
    private val aggregatedKnownGenes: Set<KnownGene>
) {

    fun resolveForVariant(variantMatchCriteria: VariantMatchCriteria): VariantAlteration {
        val serveAlteration = findHotspot(filteredKnownEvents.hotspots(), variantMatchCriteria)
            ?: findCodon(filteredKnownEvents.codons(), variantMatchCriteria)
            ?: findExon(filteredKnownEvents.exons(), variantMatchCriteria)
            ?: GeneLookup.find(aggregatedKnownGenes, variantMatchCriteria.gene)

        val alteration = GeneAlterationFactory.convertAlteration(variantMatchCriteria.gene, serveAlteration)
        val isHotspot = HotspotFunctions.isHotspot(serveAlteration)
                || findHotspot(knownEvents.hotspots(), variantMatchCriteria) != null

        return object : VariantAlteration, GeneAlteration by alteration {
            override val isHotspot: Boolean = isHotspot
        }
    }

    fun resolveForCopyNumber(copyNumber: CopyNumber): ServeGeneAlteration? {
        return CopyNumberLookup.findForCopyNumber(filteredKnownEvents.copyNumbers(), copyNumber)
            ?: GeneLookup.find(aggregatedKnownGenes, copyNumber.gene)
    }

    fun resolveForHomozygousDisruption(homozygousDisruption: HomozygousDisruption): ServeGeneAlteration? {
        // Assume a homozygous disruption always has the same annotation as a deletion.
        return CopyNumberLookup.findForHomozygousDisruption(filteredKnownEvents.copyNumbers(), homozygousDisruption)
            ?: GeneLookup.find(aggregatedKnownGenes, homozygousDisruption.gene)
    }

    fun resolveForDisruption(disruption: Disruption): ServeGeneAlteration? {
        return GeneLookup.find(aggregatedKnownGenes, disruption.gene)
    }

    fun resolveForFusion(fusion: FusionMatchCriteria): KnownFusion? {
        return FusionLookup.find(filteredKnownEvents.fusions(), fusion)
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
