package com.hartwig.actin.molecular.evidence.actionability

import com.hartwig.actin.datamodel.molecular.orange.driver.HomozygousDisruption
import com.hartwig.actin.molecular.evidence.matching.EvidenceMatcher
import com.hartwig.serve.datamodel.ActionableEvent
import com.hartwig.serve.datamodel.ActionableEvents
import com.hartwig.serve.datamodel.gene.ActionableGene
import com.hartwig.serve.datamodel.gene.GeneEvent

class HomozygousDisruptionEvidence(private val actionableGenes: List<ActionableGene>) :
    EvidenceMatcher<HomozygousDisruption> {

    override fun findMatches(event: HomozygousDisruption): List<ActionableEvent> {
        return actionableGenes.filter { it.gene() == event.gene }
    }

    companion object {
        private val APPLICABLE_GENE_EVENTS = setOf(GeneEvent.DELETION, GeneEvent.INACTIVATION, GeneEvent.ANY_MUTATION)

        fun create(actionableEvents: ActionableEvents): HomozygousDisruptionEvidence {
            return HomozygousDisruptionEvidence(actionableEvents.genes().filter { APPLICABLE_GENE_EVENTS.contains(it.event()) })
        }
    }
}
