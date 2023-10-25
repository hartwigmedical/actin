package com.hartwig.actin.molecular.orange.evidence.actionability

import com.google.common.collect.Lists
import com.hartwig.hmftools.datamodel.purple.CopyNumberInterpretation
import com.hartwig.hmftools.datamodel.purple.PurpleGainLoss
import com.hartwig.serve.datamodel.ActionableEvent
import com.hartwig.serve.datamodel.ActionableEvents
import com.hartwig.serve.datamodel.gene.ActionableGene
import com.hartwig.serve.datamodel.gene.GeneEvent

internal class CopyNumberEvidence private constructor(private val actionableAmplifications: MutableList<ActionableGene>,
                                                      private val actionableLosses: MutableList<ActionableGene>) : EvidenceMatcher<PurpleGainLoss> {
    override fun findMatches(gainLoss: PurpleGainLoss): MutableList<ActionableEvent> {
        return when (gainLoss.interpretation()) {
            CopyNumberInterpretation.FULL_GAIN, CopyNumberInterpretation.PARTIAL_GAIN -> {
                findMatches(gainLoss, actionableAmplifications)
            }

            CopyNumberInterpretation.FULL_LOSS, CopyNumberInterpretation.PARTIAL_LOSS -> {
                findMatches(gainLoss, actionableLosses)
            }

            else -> {
                Lists.newArrayList()
            }
        }
    }

    companion object {
        fun create(actionableEvents: ActionableEvents): CopyNumberEvidence {
            val actionableAmplifications: MutableList<ActionableGene> = Lists.newArrayList()
            val actionableLosses: MutableList<ActionableGene> = Lists.newArrayList()
            for (actionableGene in actionableEvents.genes()) {
                if (actionableGene.event() == GeneEvent.AMPLIFICATION) {
                    actionableAmplifications.add(actionableGene)
                } else if (actionableGene.event() == GeneEvent.DELETION) {
                    actionableLosses.add(actionableGene)
                }
            }
            return CopyNumberEvidence(actionableAmplifications, actionableLosses)
        }

        private fun findMatches(gainLoss: PurpleGainLoss, actionableEvents: MutableList<ActionableGene>): MutableList<ActionableEvent> {
            val matches: MutableList<ActionableEvent> = Lists.newArrayList()
            for (actionableEvent in actionableEvents) {
                if (actionableEvent.gene() == gainLoss.gene()) {
                    matches.add(actionableEvent)
                }
            }
            return matches
        }
    }
}
