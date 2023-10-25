package com.hartwig.actin.molecular.orange.evidence.actionability

import com.google.common.collect.Lists
import com.google.common.collect.Sets
import com.hartwig.hmftools.datamodel.linx.HomozygousDisruption
import com.hartwig.serve.datamodel.ActionableEvent
import com.hartwig.serve.datamodel.ActionableEvents
import com.hartwig.serve.datamodel.gene.ActionableGene
import com.hartwig.serve.datamodel.gene.GeneEvent

internal class HomozygousDisruptionEvidence private constructor(private val actionableGenes: MutableList<ActionableGene>) : EvidenceMatcher<HomozygousDisruption> {
    override fun findMatches(homozygousDisruption: HomozygousDisruption): MutableList<ActionableEvent> {
        val matches: MutableList<ActionableEvent> = Lists.newArrayList()
        for (actionableGene in actionableGenes) {
            if (actionableGene.gene() == homozygousDisruption.gene()) {
                matches.add(actionableGene)
            }
        }
        return matches
    }

    companion object {
        private val APPLICABLE_GENE_EVENTS: MutableSet<GeneEvent> = Sets.newHashSet(GeneEvent.DELETION, GeneEvent.INACTIVATION, GeneEvent.ANY_MUTATION)
        fun create(actionableEvents: ActionableEvents): HomozygousDisruptionEvidence {
            val actionableGenes: MutableList<ActionableGene> = Lists.newArrayList()
            for (actionableGene in actionableEvents.genes()) {
                if (APPLICABLE_GENE_EVENTS.contains(actionableGene.event())) {
                    actionableGenes.add(actionableGene)
                }
            }
            return HomozygousDisruptionEvidence(actionableGenes)
        }
    }
}
