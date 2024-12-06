package com.hartwig.actin.molecular.evidence.actionability

import com.hartwig.actin.datamodel.molecular.orange.driver.HomozygousDisruption
import com.hartwig.serve.datamodel.efficacy.EfficacyEvidence
import com.hartwig.serve.datamodel.molecular.gene.GeneEvent
import com.hartwig.serve.datamodel.trial.ActionableTrial

class HomozygousDisruptionEvidence(
    private val applicableEvidences: List<EfficacyEvidence>,
    private val applicableTrials: List<ActionableTrial>
) : ActionabilityMatcher<HomozygousDisruption> {

    override fun findMatches(event: HomozygousDisruption): ActionabilityMatch {
        val evidences = applicableEvidences.filter {
            ActionableEventsExtraction.extractGene(it).gene() == event.gene
        }
        val trials = applicableTrials.filter {
            ActionableEventsExtraction.extractGenes(it).any { actionableGene -> actionableGene.gene() == event.gene }
        }
        return ActionabilityMatch(evidences, trials)
    }

    companion object {
        private val HOMOZYGOUS_DISRUPTION_EVENTS = setOf(GeneEvent.DELETION, GeneEvent.INACTIVATION, GeneEvent.ANY_MUTATION)

        fun create(evidences: List<EfficacyEvidence>, trials: List<ActionableTrial>): HomozygousDisruptionEvidence {
            val applicableEvidences = ActionableEventsExtraction.extractGeneEvidence(evidences, HOMOZYGOUS_DISRUPTION_EVENTS)
            val applicableTrials = ActionableEventsExtraction.extractGeneTrials(trials, HOMOZYGOUS_DISRUPTION_EVENTS)

            return HomozygousDisruptionEvidence(applicableEvidences, applicableTrials)
        }
    }
}
