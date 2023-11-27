package com.hartwig.actin.molecular.orange.evidence.actionability

import com.google.common.collect.Lists
import com.google.common.collect.Sets
import com.hartwig.actin.molecular.orange.evidence.matching.FusionMatching
import com.hartwig.hmftools.datamodel.linx.LinxFusion
import com.hartwig.hmftools.datamodel.linx.LinxFusionType
import com.hartwig.serve.datamodel.ActionableEvent
import com.hartwig.serve.datamodel.ActionableEvents
import com.hartwig.serve.datamodel.fusion.ActionableFusion
import com.hartwig.serve.datamodel.gene.ActionableGene
import com.hartwig.serve.datamodel.gene.GeneEvent

internal class FusionEvidence private constructor(
    private val actionablePromiscuous: List<ActionableGene>,
    private val actionableFusions: List<ActionableFusion>
) : EvidenceMatcher<LinxFusion> {

    override fun findMatches(fusion: LinxFusion): List<ActionableEvent> {
        val matches: MutableList<ActionableEvent> = Lists.newArrayList()
        for (actionable in actionablePromiscuous) {
            if (isPromiscuousMatch(actionable, fusion) && fusion.reported()) {
                matches.add(actionable)
            }
        }
        for (actionable in actionableFusions) {
            if (FusionMatching.isGeneMatch(actionable, fusion) && FusionMatching.isExonMatch(actionable, fusion) && fusion.reported()) {
                matches.add(actionable)
            }
        }
        return matches
    }

    companion object {
        private val APPLICABLE_PROMISCUOUS_EVENTS: MutableSet<GeneEvent> =
            Sets.newHashSet(GeneEvent.FUSION, GeneEvent.ACTIVATION, GeneEvent.ANY_MUTATION)

        fun create(actionableEvents: ActionableEvents): FusionEvidence {
            val actionablePromiscuous: MutableList<ActionableGene> = Lists.newArrayList()
            for (actionableGene in actionableEvents.genes()) {
                if (APPLICABLE_PROMISCUOUS_EVENTS.contains(actionableGene.event())) {
                    actionablePromiscuous.add(actionableGene)
                }
            }
            return FusionEvidence(actionablePromiscuous, actionableEvents.fusions())
        }

        private fun isPromiscuousMatch(actionable: ActionableGene, fusion: LinxFusion): Boolean {
            return if (fusion.reportedType() == LinxFusionType.PROMISCUOUS_3) {
                actionable.gene() == fusion.geneEnd()
            } else if (fusion.reportedType() == LinxFusionType.PROMISCUOUS_5) {
                actionable.gene() == fusion.geneStart()
            } else {
                actionable.gene() == fusion.geneStart() || actionable.gene() == fusion.geneEnd()
            }
        }
    }
}
