package com.hartwig.actin.molecular.evidence.actionability

import com.hartwig.actin.datamodel.molecular.orange.driver.FusionDriverType
import com.hartwig.actin.molecular.evidence.actionability.ActionableEventsExtraction.extractFusion
import com.hartwig.actin.molecular.evidence.actionability.ActionableEventsExtraction.extractGene
import com.hartwig.actin.molecular.evidence.actionability.ActionableEventsExtraction.filterEfficacyEvidence
import com.hartwig.actin.molecular.evidence.actionability.ActionableEventsExtraction.filterTrials
import com.hartwig.actin.molecular.evidence.actionability.ActionableEventsExtraction.fusionFilter
import com.hartwig.actin.molecular.evidence.actionability.ActionableEventsExtraction.geneFilter
import com.hartwig.actin.molecular.evidence.matching.FusionMatchCriteria
import com.hartwig.actin.molecular.evidence.matching.FusionMatching
import com.hartwig.serve.datamodel.efficacy.EfficacyEvidence
import com.hartwig.serve.datamodel.molecular.fusion.FusionPair
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
        val evidences =
            filterFusionAndPromiscuous(
                fusionItems = applicableFusionEvidences,
                promiscuousItems = applicablePromiscuousEvidences,
                event = event,
                getFusion = ::extractFusion,
                getGene = ::extractGene
            )
        val trials =
            filterFusionAndPromiscuous(
                fusionItems = applicableFusionTrials,
                promiscuousItems = applicablePromiscuousTrials,
                event = event,
                getFusion = ::extractFusion,
                getGene = ::extractGene
            )
        return ActionabilityMatch(evidences, trials)
    }

    private fun <T> filterFusionAndPromiscuous(
        fusionItems: List<T>,
        promiscuousItems: List<T>,
        event: FusionMatchCriteria,
        getFusion: (T) -> FusionPair,
        getGene: (T) -> ActionableGene
    ): List<T> {
        val fusionMatches = fusionItems.filter {
            FusionMatching.isGeneMatch(getFusion(it), event) && FusionMatching.isExonMatch(getFusion(it), event) && event.isReportable
        }
        val promiscuousMatches = promiscuousItems.filter { isPromiscuousMatch(getGene(it), event) && event.isReportable }

        return fusionMatches + promiscuousMatches
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

    companion object {
        private val APPLICABLE_PROMISCUOUS_EVENTS = setOf(GeneEvent.FUSION, GeneEvent.ACTIVATION, GeneEvent.ANY_MUTATION)

        fun create(evidences: List<EfficacyEvidence>, trials: List<ActionableTrial>): FusionEvidence {
            val applicableFusionEvidences = filterEfficacyEvidence(evidences, fusionFilter())
            val applicableFusionTrials = filterTrials(trials, fusionFilter())

            val applicablePromiscuousEvidences = filterEfficacyEvidence(evidences, geneFilter()).filter {
                APPLICABLE_PROMISCUOUS_EVENTS.contains(extractGene(it).event())
            }
            val applicablePromiscuousTrials = filterTrials(applicableFusionTrials, geneFilter()).filter {
                APPLICABLE_PROMISCUOUS_EVENTS.contains(extractGene(it).event())
            }

            return FusionEvidence(
                applicableFusionEvidences,
                applicableFusionTrials,
                applicablePromiscuousEvidences,
                applicablePromiscuousTrials,
            )
        }
    }
}
