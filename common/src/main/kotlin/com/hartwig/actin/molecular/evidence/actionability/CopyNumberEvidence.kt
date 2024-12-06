package com.hartwig.actin.molecular.evidence.actionability

import com.hartwig.actin.datamodel.molecular.orange.driver.CopyNumber
import com.hartwig.actin.datamodel.molecular.orange.driver.CopyNumberType
import com.hartwig.serve.datamodel.efficacy.EfficacyEvidence
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
            evidenceMatches = applicableEvidences.filter { ActionableEventsExtraction.extractGene(it).gene() == copyNumber.gene },
            trialMatches = applicableTrials.filter {
                ActionableEventsExtraction.extractGenes(it).any { actionableGene -> actionableGene.gene() == copyNumber.gene }
            }
        )
    }

    companion object {
        private val AMPLIFICATION_EVENTS = setOf(GeneEvent.AMPLIFICATION)
        private val LOSS_EVENTS = setOf(GeneEvent.DELETION)

        fun create(evidences: List<EfficacyEvidence>, trials: List<ActionableTrial>): CopyNumberEvidence {
            val applicableAmplificationEvidences = ActionableEventsExtraction.extractGeneEvidence(evidences, AMPLIFICATION_EVENTS)
            val applicableLossEvidences = ActionableEventsExtraction.extractGeneEvidence(evidences, LOSS_EVENTS)

            val applicableAmplificationTrials = ActionableEventsExtraction.extractGeneTrials(trials, AMPLIFICATION_EVENTS)
            val applicableLossTrials = ActionableEventsExtraction.extractGeneTrials(trials, LOSS_EVENTS)

            return CopyNumberEvidence(
                applicableAmplificationEvidences,
                applicableAmplificationTrials,
                applicableLossEvidences,
                applicableLossTrials
            )
        }
    }
}
