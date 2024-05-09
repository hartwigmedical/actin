package com.hartwig.actin.molecular.evidence.known

import com.hartwig.actin.molecular.datamodel.driver.CopyNumber
import com.hartwig.actin.molecular.datamodel.driver.Disruption
import com.hartwig.actin.molecular.datamodel.driver.HomozygousDisruption
import com.hartwig.actin.molecular.evidence.matching.FusionMatchCriteria
import com.hartwig.actin.molecular.evidence.matching.HotspotMatching
import com.hartwig.actin.molecular.evidence.matching.RangeMatching
import com.hartwig.actin.molecular.evidence.matching.VariantMatchCriteria
import com.hartwig.serve.datamodel.KnownEvents
import com.hartwig.serve.datamodel.common.GeneAlteration
import com.hartwig.serve.datamodel.fusion.KnownFusion
import com.hartwig.serve.datamodel.gene.KnownGene
import com.hartwig.serve.datamodel.hotspot.KnownHotspot
import com.hartwig.serve.datamodel.range.KnownCodon
import com.hartwig.serve.datamodel.range.KnownExon

class KnownEventResolver(private val knownEvents: KnownEvents, private val aggregatedKnownGenes: Set<KnownGene>) {

    fun resolveForVariant(variant: VariantMatchCriteria): List<GeneAlteration> {
        return listOfNotNull(
            findHotspot(knownEvents.hotspots(), variant),
            findCodon(knownEvents.codons(), variant),
            findExon(knownEvents.exons(), variant),
            GeneLookup.find(aggregatedKnownGenes, variant.gene)
        )
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

    fun resolveForFusion(fusion: FusionMatchCriteria): KnownFusion? {
        return FusionLookup.find(knownEvents.fusions(), fusion)
    }

    companion object {
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
}
