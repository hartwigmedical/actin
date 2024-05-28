package com.hartwig.actin.molecular.evidence.actionability

import com.hartwig.actin.molecular.datamodel.hmf.driver.FusionDriverType
import com.hartwig.actin.molecular.evidence.matching.FusionMatchCriteria
import com.hartwig.actin.molecular.evidence.matching.FusionMatching
import com.hartwig.serve.datamodel.ActionableEvent
import com.hartwig.serve.datamodel.ActionableEvents
import com.hartwig.serve.datamodel.fusion.ActionableFusion
import com.hartwig.serve.datamodel.gene.ActionableGene
import com.hartwig.serve.datamodel.gene.GeneEvent

internal class FusionEvidence private constructor(
    private val actionablePromiscuous: List<ActionableGene>,
    private val actionableFusions: List<ActionableFusion>
) : EvidenceMatcher<FusionMatchCriteria> {

    override fun findMatches(event: FusionMatchCriteria): List<ActionableEvent> {
        return actionablePromiscuous.filter {
            isPromiscuousMatch(it, event) && event.isReportable
        } + actionableFusions.filter {
            FusionMatching.isGeneMatch(it, event) && FusionMatching.isExonMatch(it, event) && event.isReportable
        }
    }

    companion object {
        private val APPLICABLE_PROMISCUOUS_EVENTS = setOf(GeneEvent.FUSION, GeneEvent.ACTIVATION, GeneEvent.ANY_MUTATION)

        fun create(actionableEvents: ActionableEvents): FusionEvidence {
            val actionablePromiscuous = actionableEvents.genes().filter { APPLICABLE_PROMISCUOUS_EVENTS.contains(it.event()) }
            return FusionEvidence(actionablePromiscuous, actionableEvents.fusions())
        }

        private fun isPromiscuousMatch(actionable: ActionableGene, fusion: FusionMatchCriteria): Boolean {
            return when (fusion.driverType) {
                FusionDriverType.PROMISCUOUS_3 -> {
                    actionable.gene() == fusion.geneEnd
                }

                FusionDriverType.PROMISCUOUS_5 -> {
                    actionable.gene() == fusion.geneStart
                }

                else -> {
                    actionable.gene() == fusion.geneStart || actionable.gene() == fusion.geneEnd
                }
            }
        }
    }
}
