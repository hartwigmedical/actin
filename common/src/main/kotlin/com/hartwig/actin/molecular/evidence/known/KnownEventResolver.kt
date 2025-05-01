package com.hartwig.actin.molecular.evidence.known

import com.hartwig.actin.datamodel.molecular.driver.CopyNumber
import com.hartwig.actin.datamodel.molecular.driver.Disruption
import com.hartwig.actin.datamodel.molecular.driver.HomozygousDisruption
import com.hartwig.actin.molecular.evidence.matching.FusionMatchCriteria
import com.hartwig.actin.molecular.evidence.matching.HotspotMatching
import com.hartwig.actin.molecular.evidence.matching.RangeMatching
import com.hartwig.actin.molecular.evidence.matching.VariantMatchCriteria
import com.hartwig.serve.datamodel.molecular.KnownEvents
import com.hartwig.serve.datamodel.molecular.common.GeneAlteration
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

    fun resolveForVariant(variantMatchCriteria: VariantMatchCriteria, fromFilteredKnownEvents: Boolean = true): List<GeneAlteration> {
        val knownEvents = if (fromFilteredKnownEvents) filteredKnownEvents else knownEvents
        return findHotspot(knownEvents.hotspots(), variantMatchCriteria)
            ?: findCodon(knownEvents.codons(), variantMatchCriteria)
            ?: findExon(knownEvents.exons(), variantMatchCriteria)
            ?: GeneLookup.find(aggregatedKnownGenes, variantMatchCriteria.gene)?.let { listOf(it) } ?: emptyList()
    }

    /*
    fun resolveForVariant(variant: VariantMatchCriteria): VariantAlteration {
        // VariantAlteration is a data class with all fields from GeneAlteration + isHotspot (boolean)
        // 1. Look into gene alterations from CKB-only. If exists -> use (isHotspot = true when protein effect is gain/loss)
        // 2. If not exists:
        //    - Lookup hotspot across all sources and pick first match (this won't include a CKB entry by design). 
        //    - Lookup gene alteration for gene (GeneLookup).
        //    - If hotspot exists across other sources -> create VariantAlteration with isHotspot = true, and geneRole from CKB (if found).
        // TODO (CB): Consider what to do with codons/exons, proposal:
        //  1. For CKB, always use them -> if a variant matches with a knownExon with GAIN_OF_FUNCTION it should become a hotspot. 
        //  2. For other sources, only use variant and codon since we don't know protein effect. 
    }
     */

    fun resolveForCopyNumber(copyNumber: CopyNumber): GeneAlteration? {
        return CopyNumberLookup.findForCopyNumber(filteredKnownEvents.copyNumbers(), copyNumber)
            ?: GeneLookup.find(aggregatedKnownGenes, copyNumber.gene)
    }

    fun resolveForHomozygousDisruption(homozygousDisruption: HomozygousDisruption): GeneAlteration? {
        // Assume a homozygous disruption always has the same annotation as a deletion.
        return CopyNumberLookup.findForHomozygousDisruption(filteredKnownEvents.copyNumbers(), homozygousDisruption)
            ?: GeneLookup.find(aggregatedKnownGenes, homozygousDisruption.gene)
    }

    fun resolveForDisruption(disruption: Disruption): GeneAlteration? {
        return GeneLookup.find(aggregatedKnownGenes, disruption.gene)
    }

    fun resolveForFusion(fusion: FusionMatchCriteria): KnownFusion? {
        return FusionLookup.find(filteredKnownEvents.fusions(), fusion)
    }

    private fun findHotspot(knownHotspots: Iterable<KnownHotspot>, variant: VariantMatchCriteria): List<KnownHotspot>? {
        return knownHotspots.filter { HotspotMatching.isMatch(it, variant) }.ifEmpty { null }
    }

    private fun findCodon(knownCodons: Iterable<KnownCodon>, variant: VariantMatchCriteria): List<KnownCodon>? {
        return knownCodons.filter { RangeMatching.isMatch(it, variant) }.ifEmpty { null }
    }

    private fun findExon(knownExons: Iterable<KnownExon>, variant: VariantMatchCriteria): List<KnownExon>? {
        return knownExons.filter { RangeMatching.isMatch(it, variant) }.ifEmpty { null }
    }
}
