package com.hartwig.actin.molecular.evidence.actionability

import com.google.common.collect.Lists
import com.hartwig.actin.molecular.datamodel.hmf.driver.CopyNumber
import com.hartwig.actin.molecular.datamodel.hmf.driver.CopyNumberType
import com.hartwig.serve.datamodel.ActionableEvent
import com.hartwig.serve.datamodel.ActionableEvents
import com.hartwig.serve.datamodel.gene.ActionableGene
import com.hartwig.serve.datamodel.gene.GeneEvent

internal class CopyNumberEvidence private constructor(
    private val actionableAmplifications: List<ActionableGene>,
    private val actionableLosses: List<ActionableGene>
) : EvidenceMatcher<CopyNumber> {

    override fun findMatches(event: CopyNumber): List<ActionableEvent> {
        return when (event.type) {
            CopyNumberType.FULL_GAIN, CopyNumberType.PARTIAL_GAIN -> {
                findMatches(event, actionableAmplifications)
            }

            CopyNumberType.LOSS -> {
                findMatches(event, actionableLosses)
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

        private fun findMatches(copyNumber: CopyNumber, actionableEvents: List<ActionableGene>): List<ActionableEvent> {
            return actionableEvents.filter { it.gene() == copyNumber.gene }
        }
    }
}
