package com.hartwig.actin.molecular.orange.evidence.known

import com.hartwig.actin.molecular.datamodel.driver.CopyNumber
import com.hartwig.actin.molecular.datamodel.driver.Disruption
import com.hartwig.actin.molecular.datamodel.driver.Fusion
import com.hartwig.actin.molecular.datamodel.driver.HomozygousDisruption
import com.hartwig.actin.molecular.datamodel.driver.Variant
import com.hartwig.actin.molecular.orange.evidence.matching.HotspotMatching
import com.hartwig.actin.molecular.orange.evidence.matching.RangeMatching
import com.hartwig.serve.datamodel.KnownEvents
import com.hartwig.serve.datamodel.common.GeneAlteration
import com.hartwig.serve.datamodel.fusion.KnownFusion
import com.hartwig.serve.datamodel.gene.KnownGene
import com.hartwig.serve.datamodel.hotspot.KnownHotspot
import com.hartwig.serve.datamodel.range.KnownCodon
import com.hartwig.serve.datamodel.range.KnownExon

class KnownEventResolver(private val knownEvents: KnownEvents, private val aggregatedKnownGenes: Set<KnownGene>) {

    fun resolveForVariant(variant: Variant): GeneAlteration? {
        return findHotspot(knownEvents.hotspots(), variant)
            ?: findCodon(knownEvents.codons(), variant)
            ?: findExon(knownEvents.exons(), variant)
            ?: GeneLookup.find(aggregatedKnownGenes, variant.gene)
    }

    fun resolveForCopyNumber(copyNumber: CopyNumber): GeneAlteration? {
        return CopyNumberLookup.findForCopyNumber(knownEvents.copyNumbers(), copyNumber)
            ?: GeneLookup.find(aggregatedKnownGenes, copyNumber.gene)
    }

    fun resolveForHomozygousDisruption(homozygousDisruption: HomozygousDisruption): GeneAlteration? {
        // Assume a homozygous disruption always has the same annotation as a loss.
        return CopyNumberLookup.findForHomozygousDisruption(knownEvents.copyNumbers(), homozygousDisruption)
            ?: GeneLookup.find(aggregatedKnownGenes, homozygousDisruption.gene)
    }

    fun resolveForBreakend(disruptiopn: Disruption): GeneAlteration? {
        return GeneLookup.find(aggregatedKnownGenes, disruptiopn.gene)
    }

    fun resolveForFusion(fusion: Fusion): KnownFusion? {
        return FusionLookup.find(knownEvents.fusions(), fusion)
    }

    companion object {
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
}
