package com.hartwig.actin.molecular.evidence.actionability

import com.hartwig.actin.datamodel.molecular.orange.driver.CopyNumber
import com.hartwig.actin.datamodel.molecular.orange.driver.CopyNumberType
import com.hartwig.actin.molecular.evidence.actionability.ActionableEventsExtraction.filterAndExpandTrials
import com.hartwig.actin.molecular.evidence.actionability.ActionableEventsExtraction.filterEfficacyEvidence
import com.hartwig.actin.molecular.evidence.actionability.ActionableEventsExtraction.geneFilter
import com.hartwig.actin.molecular.evidence.actionability.ActionableEventsExtraction.extractGene
import com.hartwig.actin.molecular.evidence.matching.EvidenceMatcher
import com.hartwig.serve.datamodel.molecular.gene.ActionableGene
import com.hartwig.serve.datamodel.molecular.gene.GeneEvent

class CopyNumberEvidence(
    private val actionableAmplifications: ActionableEvents,
    private val actionableLosses: ActionableEvents
) : EvidenceMatcher<CopyNumber> {

    override fun findMatches(event: CopyNumber): ActionableEvents {
        return when (event.type) {
            CopyNumberType.FULL_GAIN, CopyNumberType.PARTIAL_GAIN -> {
                findMatches(event, actionableAmplifications)
            }

            CopyNumberType.LOSS -> {
                findMatches(event, actionableLosses)
            }

            else -> {
                ActionableEvents()
            }
        }
    }

    companion object {
        fun create(actionableEvents: ActionableEvents): CopyNumberEvidence {
            val evidences = filterEfficacyEvidence(actionableEvents.evidences, geneFilter())
            val trials = filterAndExpandTrials(actionableEvents.trials, geneFilter())
            val (actionableAmplificationsEvidence, actionableLossesEvidence) = extractActionableAmplificationsAndLosses(
                evidences,
                ::extractGene
            )
            val (actionableAmplificationsTrials, actionableLossesTrials) = extractActionableAmplificationsAndLosses(trials, ::extractGene)
            return CopyNumberEvidence(
                ActionableEvents(actionableAmplificationsEvidence, actionableAmplificationsTrials),
                ActionableEvents(actionableLossesEvidence, actionableLossesTrials)
            )
        }

        private fun <T> extractActionableAmplificationsAndLosses(items: List<T>, getGene: (T) -> ActionableGene): Pair<List<T>, List<T>> {
            return items.fold(Pair(emptyList(), emptyList())) { acc, actionableGene ->
                when (getGene(actionableGene).event()) {
                    GeneEvent.AMPLIFICATION -> Pair(acc.first + actionableGene, acc.second)
                    GeneEvent.DELETION -> Pair(acc.first, acc.second + actionableGene)
                    else -> acc
                }
            }
        }

        private fun findMatches(copyNumber: CopyNumber, actionableEvents: ActionableEvents): ActionableEvents {
            return ActionableEvents(actionableEvents.evidences.filter {
                extractGene(it)
                    .gene() == copyNumber.gene
            }, actionableEvents.trials.filter {
                extractGene(it)
                    .gene() == copyNumber.gene
            })
        }
    }
}
