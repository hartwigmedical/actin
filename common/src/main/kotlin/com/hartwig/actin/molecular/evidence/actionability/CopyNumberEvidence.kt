package com.hartwig.actin.molecular.evidence.actionability

import com.hartwig.actin.datamodel.molecular.orange.driver.CopyNumber
import com.hartwig.actin.datamodel.molecular.orange.driver.CopyNumberType
import com.hartwig.actin.molecular.evidence.actionability.ActionableEventsExtraction.extractGene
import com.hartwig.actin.molecular.evidence.actionability.ActionableEventsExtraction.extractGenes
import com.hartwig.actin.molecular.evidence.actionability.ActionableEventsExtraction.filterEfficacyEvidence
import com.hartwig.actin.molecular.evidence.actionability.ActionableEventsExtraction.filterTrials
import com.hartwig.actin.molecular.evidence.actionability.ActionableEventsExtraction.geneFilter
import com.hartwig.serve.datamodel.efficacy.EfficacyEvidence
import com.hartwig.serve.datamodel.molecular.gene.ActionableGene
import com.hartwig.serve.datamodel.molecular.gene.GeneEvent
import com.hartwig.serve.datamodel.trial.ActionableTrial

class CopyNumberEvidence(
    private val applicableAmplificationEvidences: List<EfficacyEvidence>,
    private val applicableAmplificationTrials: List<ActionableTrial>,
    private val applicableLossEvidences: List<EfficacyEvidence>,
    private val applicableLossTrials: List<ActionableTrial>
) : ActionabilityMatcher<CopyNumber> {

    override fun findMatches(event: CopyNumber): ActionabilityMatch {
        return when (event.type) {
            CopyNumberType.FULL_GAIN, CopyNumberType.PARTIAL_GAIN -> {
                findMatches(event, applicableAmplificationEvidences, applicableAmplificationTrials)
            }

            CopyNumberType.LOSS -> {
                findMatches(event, applicableLossEvidences, applicableLossTrials)
            }

            else -> {
                ActionabilityMatch(evidenceMatches = emptyList(), trialMatches = emptyList())
            }
        }
    }

    private fun findMatches(
        copyNumber: CopyNumber,
        applicableEvidences: List<EfficacyEvidence>,
        applicableTrials: List<ActionableTrial>
    ): ActionabilityMatch {
        return ActionabilityMatch(
            evidenceMatches = applicableEvidences.filter { extractGene(it).gene() == copyNumber.gene },
            trialMatches = applicableTrials.filter { extractGenes(it).gene() == copyNumber.gene }
        )
    }

    companion object {
        fun create(evidences: List<EfficacyEvidence>, trials: List<ActionableTrial>): CopyNumberEvidence {
            val applicableEvidences = filterEfficacyEvidence(evidences, geneFilter())
            val applicableTrials = filterTrials(trials, geneFilter())

            val (applicableAmplificationEvidences, applicableLossEvidences) = extractActionableAmplificationsAndLosses(
                applicableEvidences, ::extractGene
            )
            val (applicableAmplificationTrials, applicableLossTrials) = extractActionableAmplificationsAndLosses(
                applicableTrials, ::extractGenes
            )

            return CopyNumberEvidence(
                applicableAmplificationEvidences,
                applicableAmplificationTrials,
                applicableLossEvidences,
                applicableLossTrials
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
    }
}
