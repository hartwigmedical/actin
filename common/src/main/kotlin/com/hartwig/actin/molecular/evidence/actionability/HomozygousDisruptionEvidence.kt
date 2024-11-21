package com.hartwig.actin.molecular.evidence.actionability

import com.hartwig.actin.datamodel.molecular.orange.driver.HomozygousDisruption
import com.hartwig.actin.molecular.evidence.actionability.ActionableEventsFiltering.filterAndExpandTrials
import com.hartwig.actin.molecular.evidence.actionability.ActionableEventsFiltering.filterEfficacyEvidence
import com.hartwig.actin.molecular.evidence.actionability.ActionableEventsFiltering.geneFilter
import com.hartwig.actin.molecular.evidence.matching.EvidenceMatcher
import com.hartwig.serve.datamodel.molecular.gene.GeneEvent

class HomozygousDisruptionEvidence(private val actionableGenes: ActionableEvents) :
    EvidenceMatcher<HomozygousDisruption> {

    override fun findMatches(event: HomozygousDisruption): ActionableEvents {
        val evidences = actionableGenes.evidences.filter {
            ActionableEventsFiltering.getGene(it)
                .gene() == event.gene
        }
        val trials = actionableGenes.trials.filter {
            ActionableEventsFiltering.getGene(it)
                .gene() == event.gene
        }
        return ActionableEvents(evidences, trials)
    }

    companion object {
        private val APPLICABLE_GENE_EVENTS = setOf(GeneEvent.DELETION, GeneEvent.INACTIVATION, GeneEvent.ANY_MUTATION)

        fun create(actionableEvents: ActionableEvents): HomozygousDisruptionEvidence {
            val evidences = filterEfficacyEvidence(actionableEvents.evidences, geneFilter()).filter {
                APPLICABLE_GENE_EVENTS.contains(
                    ActionableEventsFiltering.getGene(it).event()
                )
            }
            val trials = filterAndExpandTrials(actionableEvents.trials, geneFilter()).filter {
                APPLICABLE_GENE_EVENTS.contains(
                    ActionableEventsFiltering.getGene(it).event()
                )
            }
            return HomozygousDisruptionEvidence(ActionableEvents(evidences, trials))
        }
    }
}
