package com.hartwig.actin.molecular.evidence.actionability

import com.hartwig.actin.datamodel.molecular.orange.driver.FusionDriverType
import com.hartwig.actin.molecular.evidence.matching.FusionMatchCriteria
import com.hartwig.actin.molecular.evidence.matching.FusionMatching
import com.hartwig.serve.datamodel.efficacy.EfficacyEvidence
import com.hartwig.serve.datamodel.molecular.fusion.ActionableFusion
import com.hartwig.serve.datamodel.molecular.gene.ActionableGene
import com.hartwig.serve.datamodel.molecular.gene.GeneEvent
import com.hartwig.serve.datamodel.trial.ActionableTrial

class FusionEvidence(
    private val applicableFusionEvidences: List<EfficacyEvidence>,
    private val applicableFusionTrials: List<ActionableTrial>,
    private val applicablePromiscuousEvidences: List<EfficacyEvidence>,
    private val applicablePromiscuousTrials: List<ActionableTrial>,
) : ActionabilityMatcher<FusionMatchCriteria> {

    override fun findMatches(event: FusionMatchCriteria): ActionabilityMatch {
        val matchedFusionEvidence = applicableFusionEvidences.filter { isFusionMatch(ActionableEventsExtraction.extractFusion(it), event) }
        val matchedPromiscuousEvidence =
            applicablePromiscuousEvidences.filter { isPromiscuousMatch(ActionableEventsExtraction.extractGene(it), event) }

        val matchedFusionTrials =
            applicableFusionTrials.filter {
                ActionableEventsExtraction.extractFusions(it).any { actionableFusion -> isFusionMatch(actionableFusion, event) }
            }
        val matchedPromiscuousTrials =
            applicablePromiscuousTrials.filter {
                ActionableEventsExtraction.extractGenes(it).any { actionableGene -> isPromiscuousMatch(actionableGene, event) }
            }

        return ActionabilityMatch(matchedFusionEvidence + matchedPromiscuousEvidence, matchedFusionTrials + matchedPromiscuousTrials)
    }

    private fun isFusionMatch(actionable: ActionableFusion, fusion: FusionMatchCriteria): Boolean {
        return fusion.isReportable && FusionMatching.isGeneMatch(actionable, fusion) && FusionMatching.isExonMatch(actionable, fusion)
    }

    private fun isPromiscuousMatch(actionable: ActionableGene, fusion: FusionMatchCriteria): Boolean {
        if (!fusion.isReportable) {
            return false
        }

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

    companion object {
        private val APPLICABLE_PROMISCUOUS_EVENTS = setOf(GeneEvent.FUSION, GeneEvent.ACTIVATION, GeneEvent.ANY_MUTATION)

        fun create(evidences: List<EfficacyEvidence>, trials: List<ActionableTrial>): FusionEvidence {
            val applicableFusionEvidences =
                ActionableEventsExtraction.filterEfficacyEvidence(evidences, ActionableEventsExtraction.fusionFilter())
            val applicableFusionTrials = ActionableEventsExtraction.filterTrials(trials, ActionableEventsExtraction.fusionFilter())

            val applicablePromiscuousEvidences =
                ActionableEventsExtraction.filterEfficacyEvidence(
                    evidences,
                    ActionableEventsExtraction.geneFilter(APPLICABLE_PROMISCUOUS_EVENTS)
                )
            val applicablePromiscuousTrials =
                ActionableEventsExtraction.filterTrials(
                    trials,
                    ActionableEventsExtraction.geneFilter(APPLICABLE_PROMISCUOUS_EVENTS)
                )

            return FusionEvidence(
                applicableFusionEvidences,
                applicableFusionTrials,
                applicablePromiscuousEvidences,
                applicablePromiscuousTrials,
            )
        }
    }
}
