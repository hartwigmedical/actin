package com.hartwig.actin.molecular.orange.evidence.known

import com.hartwig.actin.molecular.orange.evidence.matching.HotspotMatching
import com.hartwig.actin.molecular.orange.evidence.matching.RangeMatching
import com.hartwig.hmftools.datamodel.linx.LinxBreakend
import com.hartwig.hmftools.datamodel.linx.LinxFusion
import com.hartwig.hmftools.datamodel.linx.LinxHomozygousDisruption
import com.hartwig.hmftools.datamodel.purple.PurpleGainLoss
import com.hartwig.hmftools.datamodel.purple.PurpleGeneCopyNumber
import com.hartwig.hmftools.datamodel.purple.PurpleVariant
import com.hartwig.serve.datamodel.KnownEvents
import com.hartwig.serve.datamodel.common.GeneAlteration
import com.hartwig.serve.datamodel.fusion.KnownFusion
import com.hartwig.serve.datamodel.gene.KnownGene
import com.hartwig.serve.datamodel.hotspot.KnownHotspot
import com.hartwig.serve.datamodel.range.KnownCodon
import com.hartwig.serve.datamodel.range.KnownExon

class KnownEventResolver(private val knownEvents: KnownEvents, private val aggregatedKnownGenes: Set<KnownGene>) {

    fun resolveForVariant(variant: PurpleVariant): GeneAlteration? {
        return findHotspot(knownEvents.hotspots(), variant)
            ?: findCodon(knownEvents.codons(), variant)
            ?: findExon(knownEvents.exons(), variant)
            ?: GeneLookup.find(aggregatedKnownGenes, variant.gene())
    }

    fun resolveForCopyNumber(gainLoss: PurpleGainLoss): GeneAlteration? {
        return CopyNumberLookup.findForCopyNumber(knownEvents.copyNumbers(), gainLoss)
            ?: GeneLookup.find(aggregatedKnownGenes, gainLoss.gene())
    }

    fun resolveForHomozygousDisruption(linxHomozygousDisruption: LinxHomozygousDisruption): GeneAlteration? {
        // Assume a homozygous disruption always has the same annotation as a loss.
        return CopyNumberLookup.findForHomozygousDisruption(knownEvents.copyNumbers(), linxHomozygousDisruption)
            ?: GeneLookup.find(aggregatedKnownGenes, linxHomozygousDisruption.gene())
    }

    fun resolveForBreakend(breakend: LinxBreakend): GeneAlteration? {
        return GeneLookup.find(aggregatedKnownGenes, breakend.gene())
    }

    fun resolveForFusion(fusion: LinxFusion): KnownFusion? {
        return FusionLookup.find(knownEvents.fusions(), fusion)
    }

    companion object {
        private fun findHotspot(knownHotspots: Iterable<KnownHotspot>, variant: PurpleVariant): KnownHotspot? {
            return knownHotspots.find { HotspotMatching.isMatch(it, variant) }
        }

        private fun findCodon(knownCodons: Iterable<KnownCodon>, variant: PurpleVariant): KnownCodon? {
            return knownCodons.find { RangeMatching.isMatch(it, variant) }
        }

        private fun findExon(knownExons: Iterable<KnownExon>, variant: PurpleVariant): KnownExon? {
            return knownExons.find { RangeMatching.isMatch(it, variant) }
        }
    }
}
