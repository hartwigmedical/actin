package com.hartwig.actin.molecular.orange.evidence.actionability

import com.hartwig.hmftools.datamodel.linx.LinxHomozygousDisruption
import com.hartwig.serve.datamodel.ActionableEvent
import com.hartwig.serve.datamodel.ActionableEvents
import com.hartwig.serve.datamodel.gene.ActionableGene
import com.hartwig.serve.datamodel.gene.GeneEvent

internal class HomozygousDisruptionEvidence private constructor(private val actionableGenes: List<ActionableGene>) :
    EvidenceMatcher<LinxHomozygousDisruption> {

    override fun findMatches(event: LinxHomozygousDisruption): List<ActionableEvent> {
        return actionableGenes.filter { it.gene() == event.gene() }
    }

    companion object {
        private val APPLICABLE_GENE_EVENTS = setOf(GeneEvent.DELETION, GeneEvent.INACTIVATION, GeneEvent.ANY_MUTATION)

        fun create(actionableEvents: ActionableEvents): HomozygousDisruptionEvidence {
            return HomozygousDisruptionEvidence(actionableEvents.genes().filter { APPLICABLE_GENE_EVENTS.contains(it.event()) })
        }
    }
}
