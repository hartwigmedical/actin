package com.hartwig.actin.molecular.orange.evidence.actionability

import com.google.common.collect.Lists
import com.hartwig.hmftools.datamodel.linx.LinxBreakend
import com.hartwig.serve.datamodel.ActionableEvent
import com.hartwig.serve.datamodel.ActionableEvents
import com.hartwig.serve.datamodel.gene.ActionableGene
import com.hartwig.serve.datamodel.gene.GeneEvent

internal class BreakendEvidence private constructor(private val applicableActionableGenes: MutableList<ActionableGene>) : EvidenceMatcher<LinxBreakend> {
    override fun findMatches(breakend: LinxBreakend): MutableList<ActionableEvent> {
        val matches: MutableList<ActionableEvent> = Lists.newArrayList()
        for (actionableGene in applicableActionableGenes) {
            if (breakend.reportedDisruption() && actionableGene.gene() == breakend.gene()) {
                matches.add(actionableGene)
            }
        }
        return matches
    }

    companion object {
        fun create(actionableEvents: ActionableEvents): BreakendEvidence {
            val applicableActionableGenes: MutableList<ActionableGene> = Lists.newArrayList()
            for (actionableGene in actionableEvents.genes()) {
                if (actionableGene.event() == GeneEvent.ANY_MUTATION) {
                    applicableActionableGenes.add(actionableGene)
                }
            }
            return BreakendEvidence(applicableActionableGenes)
        }
    }
}
