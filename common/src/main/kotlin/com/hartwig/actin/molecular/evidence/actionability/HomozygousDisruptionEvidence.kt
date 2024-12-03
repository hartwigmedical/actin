package com.hartwig.actin.molecular.evidence.actionability

import com.hartwig.actin.datamodel.molecular.orange.driver.HomozygousDisruption
import com.hartwig.actin.molecular.evidence.actionability.ActionableEventsExtraction.filterEfficacyEvidence
import com.hartwig.actin.molecular.evidence.actionability.ActionableEventsExtraction.filterTrials
import com.hartwig.actin.molecular.evidence.actionability.ActionableEventsExtraction.geneFilter
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
            ActionableEventsExtraction.extractGene(it).gene() == event.gene
        }
        return ActionabilityMatch(evidences, trials)
    }

    companion object {
        private val APPLICABLE_GENE_EVENTS = setOf(GeneEvent.DELETION, GeneEvent.INACTIVATION, GeneEvent.ANY_MUTATION)

        fun create(evidences: List<EfficacyEvidence>, trials: List<ActionableTrial>): HomozygousDisruptionEvidence {
            val applicableEvidences = filterEfficacyEvidence(evidences, geneFilter()).filter {
                APPLICABLE_GENE_EVENTS.contains(ActionableEventsExtraction.extractGene(it).event())
            }
            val applicableTrials = filterTrials(trials, geneFilter()).filter {
                APPLICABLE_GENE_EVENTS.contains(ActionableEventsExtraction.extractGene(it).event())
            }
            return HomozygousDisruptionEvidence(applicableEvidences, applicableTrials)
        }
    }
}
