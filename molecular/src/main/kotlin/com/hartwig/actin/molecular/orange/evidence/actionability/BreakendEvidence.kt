package com.hartwig.actin.molecular.orange.evidence.actionability

import com.hartwig.actin.molecular.datamodel.driver.Disruption
import com.hartwig.serve.datamodel.ActionableEvent
import com.hartwig.serve.datamodel.ActionableEvents
import com.hartwig.serve.datamodel.gene.ActionableGene
import com.hartwig.serve.datamodel.gene.GeneEvent

internal class BreakendEvidence private constructor(private val applicableActionableGenes: List<ActionableGene>) :
    EvidenceMatcher<Disruption> {

    override fun findMatches(event: Disruption): List<ActionableEvent> {
        return applicableActionableGenes.filter { event.isReportable && it.gene() == event.gene }

    }

    companion object {
        fun create(actionableEvents: ActionableEvents): BreakendEvidence {
            return BreakendEvidence(actionableEvents.genes().filter { it.event() == GeneEvent.ANY_MUTATION })
        }
    }
}
