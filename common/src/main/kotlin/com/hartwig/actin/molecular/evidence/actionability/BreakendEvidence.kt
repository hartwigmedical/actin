package com.hartwig.actin.molecular.evidence.actionability

import com.hartwig.actin.datamodel.molecular.orange.driver.Disruption
import com.hartwig.serve.datamodel.efficacy.EfficacyEvidence
import com.hartwig.serve.datamodel.molecular.gene.GeneEvent
import com.hartwig.serve.datamodel.trial.ActionableTrial

class BreakendEvidence(
    private val applicableGeneEvidences: List<EfficacyEvidence>,
    private val applicableGeneTrials: List<ActionableTrial>
) : ActionabilityMatcher<Disruption> {

    override fun findMatches(event: Disruption): ActionabilityMatch {
        val evidences = applicableGeneEvidences.filter {
            event.isReportable && ActionableEventsExtraction.extractGene(it).gene() == event.gene
        }
        val trials = applicableGeneTrials.filter {
            event.isReportable && ActionableEventsExtraction.extractGenes(it).any { actionableGene -> actionableGene.gene() == event.gene }
        }
        return ActionabilityMatch(evidences, trials)
    }

    companion object {
        private val BREAKEND_EVENTS = setOf(GeneEvent.ANY_MUTATION)

        fun create(evidences: List<EfficacyEvidence>, trials: List<ActionableTrial>): BreakendEvidence {
            val applicableEvidences = ActionableEventsExtraction.extractGeneEvidence(evidences, BREAKEND_EVENTS)
            val applicableTrials = ActionableEventsExtraction.extractGeneTrials(trials, BREAKEND_EVENTS)

            return BreakendEvidence(applicableEvidences, applicableTrials)
        }
    }
}
