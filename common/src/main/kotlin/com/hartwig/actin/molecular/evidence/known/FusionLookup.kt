package com.hartwig.actin.molecular.evidence.known

import com.hartwig.actin.molecular.evidence.matching.FusionMatchCriteria
import com.hartwig.actin.molecular.evidence.matching.FusionMatching
import com.hartwig.serve.datamodel.molecular.common.ProteinEffect
import com.hartwig.serve.datamodel.molecular.fusion.ImmutableKnownFusion
import com.hartwig.serve.datamodel.molecular.fusion.KnownFusion

object FusionLookup {

    fun find(knownFusions: Iterable<KnownFusion>, fusion: FusionMatchCriteria): KnownFusion {
        var best: KnownFusion? = null
        for (knownFusion in knownFusions) {
            if (FusionMatching.isGeneMatch(knownFusion, fusion)) {
                val matchesExonUp = FusionMatching.explicitlyMatchesExonUp(knownFusion, fusion)
                val matchesExonDown = FusionMatching.explicitlyMatchesExonDown(knownFusion, fusion)
                if (matchesExonUp && matchesExonDown) {
                    best = knownFusion
                } else {
                    val meetsExonUp = matchesExonUp && !hasDownRequirements(knownFusion)
                    val meetsExonDown = matchesExonDown && !hasUpRequirements(knownFusion)
                    if (meetsExonUp || meetsExonDown) {
                        if (best == null || !hasUpRequirements(best) || !hasDownRequirements(best)) {
                            best = knownFusion
                        }
                    } else if (!hasUpRequirements(knownFusion) && !hasDownRequirements(knownFusion)) {
                        best = knownFusion
                    }
                }
            }
        }
        return best ?: ImmutableKnownFusion.builder().geneUp(fusion.geneStart).geneDown(fusion.geneEnd).proteinEffect(ProteinEffect.UNKNOWN)
            .build()
    }

    private fun hasUpRequirements(knownFusion: KnownFusion): Boolean {
        return knownFusion.minExonUp() != null && knownFusion.maxExonUp() != null
    }

    private fun hasDownRequirements(knownFusion: KnownFusion): Boolean {
        return knownFusion.minExonDown() != null && knownFusion.maxExonDown() != null
    }
}
