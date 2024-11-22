package com.hartwig.actin.molecular.evidence.actionability

import com.hartwig.actin.datamodel.molecular.orange.driver.FusionDriverType
import com.hartwig.actin.molecular.evidence.actionability.ActionableEventsExtraction.filterAndExpandTrials
import com.hartwig.actin.molecular.evidence.actionability.ActionableEventsExtraction.filterEfficacyEvidence
import com.hartwig.actin.molecular.evidence.actionability.ActionableEventsExtraction.fusionFilter
import com.hartwig.actin.molecular.evidence.actionability.ActionableEventsExtraction.geneFilter
import com.hartwig.actin.molecular.evidence.actionability.ActionableEventsExtraction.extractFusion
import com.hartwig.actin.molecular.evidence.actionability.ActionableEventsExtraction.extractGene
import com.hartwig.actin.molecular.evidence.matching.EvidenceMatcher
import com.hartwig.actin.molecular.evidence.matching.FusionMatchCriteria
import com.hartwig.actin.molecular.evidence.matching.FusionMatching
import com.hartwig.serve.datamodel.molecular.fusion.FusionPair
import com.hartwig.serve.datamodel.molecular.gene.ActionableGene
import com.hartwig.serve.datamodel.molecular.gene.GeneEvent

class FusionEvidence(
    private val actionablePromiscuous: ActionableEvents,
    private val actionableFusions: ActionableEvents
) : EvidenceMatcher<FusionMatchCriteria> {

    override fun findMatches(event: FusionMatchCriteria): ActionableEvents {
        val evidences =
            filterPromiscuousAndFusion(actionablePromiscuous.evidences, actionableFusions.evidences, event, ::extractGene, ::extractFusion)
        val trials =
            filterPromiscuousAndFusion(actionablePromiscuous.trials, actionableFusions.trials, event, ::extractGene, ::extractFusion)
        return ActionableEvents(evidences, trials)
    }

    companion object {
        private val APPLICABLE_PROMISCUOUS_EVENTS = setOf(GeneEvent.FUSION, GeneEvent.ACTIVATION, GeneEvent.ANY_MUTATION)

        fun create(actionableEvents: ActionableEvents): FusionEvidence {
            val evidences = filterEfficacyEvidence(actionableEvents.evidences, fusionFilter())
            val trials = filterAndExpandTrials(actionableEvents.trials, fusionFilter())
            val actionablePromiscuousEvidences = filterEfficacyEvidence(actionableEvents.evidences, geneFilter()).filter {
                APPLICABLE_PROMISCUOUS_EVENTS.contains(
                    ActionableEventsExtraction.extractGene(it).event()
                )
            }
            val actionablePromiscuousTrials = filterAndExpandTrials(actionableEvents.trials, geneFilter()).filter {
                APPLICABLE_PROMISCUOUS_EVENTS.contains(
                    ActionableEventsExtraction.extractGene(it).event()
                )
            }
            return FusionEvidence(
                ActionableEvents(actionablePromiscuousEvidences, actionablePromiscuousTrials),
                ActionableEvents(evidences, trials)
            )
        }

        private fun <T> filterPromiscuousAndFusion(
            promiscuousItems: List<T>,
            fusionItems: List<T>,
            event: FusionMatchCriteria,
            getGene: (T) -> ActionableGene,
            getFusion: (T) -> FusionPair
        ): List<T> {
            return promiscuousItems.filter {
                isPromiscuousMatch(getGene(it), event) && event.isReportable
            } + fusionItems.filter {
                FusionMatching.isGeneMatch(getFusion(it), event) && FusionMatching.isExonMatch(getFusion(it), event) && event.isReportable
            }
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
