package com.hartwig.actin.molecular.evidence.actionability

import com.hartwig.actin.datamodel.molecular.orange.driver.Disruption
import com.hartwig.serve.datamodel.efficacy.EfficacyEvidence
import com.hartwig.serve.datamodel.molecular.MolecularCriterium
import com.hartwig.serve.datamodel.molecular.gene.GeneEvent
import com.hartwig.serve.datamodel.trial.ActionableTrial
import java.util.function.Predicate

class BreakendEvidence(
    private val applicableGeneEvidences: List<EfficacyEvidence>,
    private val applicableTrialMatcher: ActionableTrialMatcher
) : ActionabilityMatcher<Disruption> {

    override fun findMatches(event: Disruption): ActionabilityMatch {
        val matchPredicate: Predicate<MolecularCriterium> =
            Predicate { event.isReportable && ActionableEventsExtraction.extractGene(it).gene() == event.gene }

        val evidences = applicableGeneEvidences.filter { matchPredicate.test(it.molecularCriterium()) }
        val matchingCriteriaPerTrialMatch = applicableTrialMatcher.matchTrials(matchPredicate)

        return ActionabilityMatch(evidences, matchingCriteriaPerTrialMatch)
    }

    companion object {
        private val BREAKEND_EVENTS = setOf(GeneEvent.ANY_MUTATION)

        fun create(evidences: List<EfficacyEvidence>, trials: List<ActionableTrial>): BreakendEvidence {
            val applicableEvidences = ActionableEventsExtraction.extractGeneEvidence(evidences, BREAKEND_EVENTS)
            val (applicableTrials, molecularPredicate) = ActionableEventsExtraction.extractGeneTrials(trials, BREAKEND_EVENTS)
            val actionableTrialMatcher = ActionableTrialMatcher(applicableTrials, molecularPredicate)

            return BreakendEvidence(applicableEvidences, actionableTrialMatcher)
        }
    }
}
