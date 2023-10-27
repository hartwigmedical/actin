package com.hartwig.actin.molecular.orange.evidence.actionability

import com.hartwig.hmftools.datamodel.linx.LinxBreakend
import com.hartwig.serve.datamodel.ActionableEvent
import com.hartwig.serve.datamodel.ActionableEvents
import com.hartwig.serve.datamodel.gene.ActionableGene
import com.hartwig.serve.datamodel.gene.GeneEvent

internal class BreakendEvidence private constructor(private val applicableActionableGenes: List<ActionableGene>) : EvidenceMatcher<LinxBreakend> {
    override fun findMatches(breakend: LinxBreakend): MutableList<ActionableEvent> {
        return applicableActionableGenes.filter { breakend.reportedDisruption() && it.gene() == breakend.gene() }.toMutableList()

    }

    companion object {
        fun create(actionableEvents: ActionableEvents): BreakendEvidence {
            return BreakendEvidence(actionableEvents.genes().filter { it.event() == GeneEvent.ANY_MUTATION })
        }
    }
}
