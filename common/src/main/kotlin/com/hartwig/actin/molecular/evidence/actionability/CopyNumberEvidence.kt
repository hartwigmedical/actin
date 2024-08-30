package com.hartwig.actin.molecular.evidence.actionability

import com.hartwig.actin.datamodel.molecular.orange.driver.CopyNumber
import com.hartwig.actin.datamodel.molecular.orange.driver.CopyNumberType
import com.hartwig.actin.molecular.evidence.matching.EvidenceMatcher
import com.hartwig.serve.datamodel.ActionableEvent
import com.hartwig.serve.datamodel.ActionableEvents
import com.hartwig.serve.datamodel.gene.ActionableGene
import com.hartwig.serve.datamodel.gene.GeneEvent

class CopyNumberEvidence(
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
                emptyList()
            }
        }
    }

    companion object {
        fun create(actionableEvents: ActionableEvents): CopyNumberEvidence {
            val (actionableAmplifications, actionableLosses) = actionableEvents.genes()
                .fold(Pair(emptyList<ActionableGene>(), emptyList<ActionableGene>())) { acc, actionableGene ->
                    when (actionableGene.event()) {
                        GeneEvent.AMPLIFICATION -> Pair(acc.first + actionableGene, acc.second)
                        GeneEvent.DELETION -> Pair(acc.first, acc.second + actionableGene)
                        else -> acc
                    }
                }
            return CopyNumberEvidence(actionableAmplifications, actionableLosses)
        }

        private fun findMatches(copyNumber: CopyNumber, actionableEvents: List<ActionableGene>): List<ActionableEvent> {
            return actionableEvents.filter { it.gene() == copyNumber.gene }
        }
    }
}
