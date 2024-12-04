package com.hartwig.actin.molecular.evidence.actionability

import com.hartwig.actin.datamodel.molecular.orange.driver.Disruption
import com.hartwig.actin.molecular.evidence.actionability.ActionableEventsExtraction.filterEfficacyEvidence
import com.hartwig.actin.molecular.evidence.actionability.ActionableEventsExtraction.filterTrials
import com.hartwig.actin.molecular.evidence.actionability.ActionableEventsExtraction.geneFilter
import com.hartwig.serve.datamodel.efficacy.EfficacyEvidence
import com.hartwig.serve.datamodel.molecular.gene.GeneEvent
import com.hartwig.serve.datamodel.trial.ActionableTrial

class BreakendEvidence(
    private val applicableGeneEvidences: List<EfficacyEvidence>,
    private val applicableGeneTrials: List<ActionableTrial>
) : ActionabilityMatcher<Disruption> {

    override fun findMatches(event: Disruption): ActionabilityMatch {
        val evidences = applicableGeneEvidences.filter {
            event.isReportable && ActionableEventsExtraction.extractGene(it).gene() == event.gene
        }
        val trials = applicableGeneTrials.filter {
            event.isReportable && ActionableEventsExtraction.extractGenes(it).gene() == event.gene
        }
        return ActionabilityMatch(evidences, trials)
    }

    companion object {
        fun create(evidences: List<EfficacyEvidence>, trials: List<ActionableTrial>): BreakendEvidence {
            val applicableEvidences = filterEfficacyEvidence(evidences, geneFilter()).filter {
                ActionableEventsExtraction.extractGene(it).event() == GeneEvent.ANY_MUTATION
            }
            val applicableTrials = filterTrials(trials, geneFilter()).filter {
                ActionableEventsExtraction.extractGenes(it).event() == GeneEvent.ANY_MUTATION
            }
            return BreakendEvidence(applicableEvidences, applicableTrials)
        }
    }
}
