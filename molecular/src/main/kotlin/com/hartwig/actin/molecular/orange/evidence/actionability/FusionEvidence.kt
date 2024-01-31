package com.hartwig.actin.molecular.orange.evidence.actionability

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

    override fun findMatches(event: LinxFusion): List<ActionableEvent> {
        return actionablePromiscuous.filter {
            isPromiscuousMatch(it, event) && event.reported()
        } + actionableFusions.filter {
            FusionMatching.isGeneMatch(it, event) && FusionMatching.isExonMatch(it, event) && event.reported()
        }
    }

    companion object {
        private val APPLICABLE_PROMISCUOUS_EVENTS = setOf(GeneEvent.FUSION, GeneEvent.ACTIVATION, GeneEvent.ANY_MUTATION)

        fun create(actionableEvents: ActionableEvents): FusionEvidence {
            val actionablePromiscuous = actionableEvents.genes().filter { APPLICABLE_PROMISCUOUS_EVENTS.contains(it.event()) }
            return FusionEvidence(actionablePromiscuous, actionableEvents.fusions())
        }

        private fun isPromiscuousMatch(actionable: ActionableGene, fusion: LinxFusion): Boolean {
            return when (fusion.reportedType()) {
                LinxFusionType.PROMISCUOUS_3 -> {
                    actionable.gene() == fusion.geneEnd()
                }

                LinxFusionType.PROMISCUOUS_5 -> {
                    actionable.gene() == fusion.geneStart()
                }

                else -> {
                    actionable.gene() == fusion.geneStart() || actionable.gene() == fusion.geneEnd()
                }
            }
        }
    }
}
