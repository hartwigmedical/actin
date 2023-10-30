package com.hartwig.actin.molecular.orange.evidence.known

import com.hartwig.actin.molecular.orange.evidence.matching.HotspotMatching
import com.hartwig.actin.molecular.orange.evidence.matching.RangeMatching
import com.hartwig.hmftools.datamodel.linx.HomozygousDisruption
import com.hartwig.hmftools.datamodel.linx.LinxBreakend
import com.hartwig.hmftools.datamodel.linx.LinxFusion
import com.hartwig.hmftools.datamodel.purple.PurpleGainLoss
import com.hartwig.hmftools.datamodel.purple.PurpleVariant
import com.hartwig.serve.datamodel.KnownEvents
import com.hartwig.serve.datamodel.common.GeneAlteration
import com.hartwig.serve.datamodel.fusion.KnownFusion
import com.hartwig.serve.datamodel.gene.KnownGene
import com.hartwig.serve.datamodel.hotspot.KnownHotspot
import com.hartwig.serve.datamodel.range.KnownCodon
import com.hartwig.serve.datamodel.range.KnownExon

class KnownEventResolver internal constructor(private val knownEvents: KnownEvents, private val aggregatedKnownGenes: Set<KnownGene>) {
    fun resolveForVariant(variant: PurpleVariant): GeneAlteration? {
        val hotspot = findHotspot(knownEvents.hotspots(), variant)
        if (hotspot != null) {
            return hotspot
        }
        val codon = findCodon(knownEvents.codons(), variant)
        if (codon != null) {
            return codon
        }
        val exon = findExon(knownEvents.exons(), variant)
        return exon ?: GeneLookup.find(aggregatedKnownGenes, variant.gene())
    }

    fun resolveForCopyNumber(gainLoss: PurpleGainLoss): GeneAlteration? {
        val knownCopyNumber = CopyNumberLookup.findForCopyNumber(knownEvents.copyNumbers(), gainLoss)
        return knownCopyNumber ?: GeneLookup.find(aggregatedKnownGenes, gainLoss.gene())
    }

    fun resolveForHomozygousDisruption(homozygousDisruption: HomozygousDisruption): GeneAlteration? {
        // Assume a homozygous disruption always has the same annotation as a loss.
        val knownCopyNumber = CopyNumberLookup.findForHomozygousDisruption(knownEvents.copyNumbers(), homozygousDisruption)
        return knownCopyNumber ?: GeneLookup.find(aggregatedKnownGenes, homozygousDisruption.gene())
    }

    fun resolveForBreakend(breakend: LinxBreakend): GeneAlteration? {
        return GeneLookup.find(aggregatedKnownGenes, breakend.gene())
    }

    fun resolveForFusion(fusion: LinxFusion): KnownFusion? {
        return FusionLookup.find(knownEvents.fusions(), fusion)
    }

    companion object {
        private fun findHotspot(knownHotspots: Iterable<KnownHotspot>, variant: PurpleVariant): KnownHotspot? {
            for (knownHotspot in knownHotspots) {
                if (HotspotMatching.isMatch(knownHotspot, variant)) {
                    return knownHotspot
                }
            }
            return null
        }

        private fun findCodon(knownCodons: Iterable<KnownCodon>, variant: PurpleVariant): KnownCodon? {
            for (knownCodon in knownCodons) {
                if (RangeMatching.isMatch(knownCodon, variant)) {
                    return knownCodon
                }
            }
            return null
        }

        private fun findExon(knownExons: Iterable<KnownExon>, variant: PurpleVariant): KnownExon? {
            for (knownExon in knownExons) {
                if (RangeMatching.isMatch(knownExon, variant)) {
                    return knownExon
                }
            }
            return null
        }
    }
}
