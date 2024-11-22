package com.hartwig.actin.molecular.evidence.actionability

import com.hartwig.actin.datamodel.molecular.orange.driver.HomozygousDisruption
import com.hartwig.actin.molecular.evidence.actionability.ActionableEventsExtraction.filterAndExpandTrials
import com.hartwig.actin.molecular.evidence.actionability.ActionableEventsExtraction.filterEfficacyEvidence
import com.hartwig.actin.molecular.evidence.actionability.ActionableEventsExtraction.geneFilter
import com.hartwig.actin.molecular.evidence.matching.EvidenceMatcher
import com.hartwig.serve.datamodel.molecular.gene.GeneEvent

class HomozygousDisruptionEvidence(private val actionableGenes: ActionableEvents) :
    EvidenceMatcher<HomozygousDisruption> {

    override fun findMatches(event: HomozygousDisruption): ActionableEvents {
        val evidences = actionableGenes.evidences.filter {
            ActionableEventsExtraction.extractGene(it)
                .gene() == event.gene
        }
        val trials = actionableGenes.trials.filter {
            ActionableEventsExtraction.extractGene(it)
                .gene() == event.gene
        }
        return ActionableEvents(evidences, trials)
    }

    companion object {
        private val APPLICABLE_GENE_EVENTS = setOf(GeneEvent.DELETION, GeneEvent.INACTIVATION, GeneEvent.ANY_MUTATION)

        fun create(actionableEvents: ActionableEvents): HomozygousDisruptionEvidence {
            val evidences = filterEfficacyEvidence(actionableEvents.evidences, geneFilter()).filter {
                APPLICABLE_GENE_EVENTS.contains(
                    ActionableEventsExtraction.extractGene(it).event()
                )
            }
            val trials = filterAndExpandTrials(actionableEvents.trials, geneFilter()).filter {
                APPLICABLE_GENE_EVENTS.contains(
                    ActionableEventsExtraction.extractGene(it).event()
                )
            }
            return HomozygousDisruptionEvidence(ActionableEvents(evidences, trials))
        }
    }
}
