package com.hartwig.actin.molecular.evidence.actionability

import com.hartwig.actin.datamodel.molecular.orange.driver.Disruption
import com.hartwig.actin.molecular.evidence.actionability.ActionableEventsExtraction.filterEfficacyEvidence
import com.hartwig.actin.molecular.evidence.actionability.ActionableEventsExtraction.filterTrials
import com.hartwig.actin.molecular.evidence.actionability.ActionableEventsExtraction.geneFilter
import com.hartwig.actin.molecular.evidence.matching.EvidenceMatcher
import com.hartwig.serve.datamodel.molecular.gene.GeneEvent

class BreakendEvidence(private val applicableActionableGenes: ActionableEvents) : EvidenceMatcher<Disruption> {

    override fun findMatches(event: Disruption): ActionableEvents {
        val evidences = applicableActionableGenes.evidences.filter {
            event.isReportable && ActionableEventsExtraction.extractGene(it).gene() == event.gene
        }
        val trials = applicableActionableGenes.trials.filter {
            event.isReportable && ActionableEventsExtraction.extractGene(it).gene() == event.gene
        }
        return ActionableEvents(evidences, trials)
    }

    companion object {
        fun create(actionableEvents: ActionableEvents): BreakendEvidence {
            val evidences = filterEfficacyEvidence(actionableEvents.evidences, geneFilter()).filter {
                ActionableEventsExtraction.extractGene(it).event() == GeneEvent.ANY_MUTATION
            }
            val trials = filterTrials(actionableEvents.trials, geneFilter()).filter {
                ActionableEventsExtraction.extractGene(it).event() == GeneEvent.ANY_MUTATION
            }
            return BreakendEvidence(ActionableEvents(evidences, trials))
        }
    }
}
