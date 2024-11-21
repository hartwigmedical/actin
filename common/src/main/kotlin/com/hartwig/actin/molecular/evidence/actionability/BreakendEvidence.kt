package com.hartwig.actin.molecular.evidence.actionability

import com.hartwig.actin.datamodel.molecular.orange.driver.Disruption
import com.hartwig.actin.molecular.evidence.actionability.ActionableEventsFiltering.filterAndExpandTrials
import com.hartwig.actin.molecular.evidence.actionability.ActionableEventsFiltering.filterEfficacyEvidence
import com.hartwig.actin.molecular.evidence.actionability.ActionableEventsFiltering.geneFilter
import com.hartwig.actin.molecular.evidence.matching.EvidenceMatcher
import com.hartwig.serve.datamodel.molecular.gene.GeneEvent

class BreakendEvidence(private val applicableActionableGenes: ActionableEvents) :
    EvidenceMatcher<Disruption> {

    override fun findMatches(event: Disruption): ActionableEvents {
        val evidences = applicableActionableGenes.evidences.filter {
            event.isReportable && ActionableEventsFiltering.getGene(it)
                .gene() == event.gene
        }
        val trials = applicableActionableGenes.trials.filter {
            event.isReportable && ActionableEventsFiltering.getGene(it)
                .gene() == event.gene
        }
        return ActionableEvents(evidences, trials)
    }

    companion object {
        fun create(actionableEvents: ActionableEvents): BreakendEvidence {
            val evidences = filterEfficacyEvidence(actionableEvents.evidences, geneFilter()).filter {
                ActionableEventsFiltering.getGene(it)
                    .event() == GeneEvent.ANY_MUTATION
            }
            val trials = filterAndExpandTrials(actionableEvents.trials, geneFilter()).filter {
                ActionableEventsFiltering.getGene(it)
                    .event() == GeneEvent.ANY_MUTATION
            }
            return BreakendEvidence(ActionableEvents(evidences, trials))
        }
    }
}
