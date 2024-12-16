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
            Predicate { event.isReportable && ActionableEventExtraction.extractGene(it).gene() == event.gene }

        val evidenceMatches = applicableGeneEvidences.filter { matchPredicate.test(it.molecularCriterium()) }
        val matchingCriteriaPerTrialMatch = applicableTrialMatcher.apply(matchPredicate)

        return ActionabilityMatch(evidenceMatches = evidenceMatches, matchingCriteriaPerTrialMatch = matchingCriteriaPerTrialMatch)
    }

    companion object {
        private val BREAKEND_EVENTS = setOf(GeneEvent.ANY_MUTATION)

        fun create(evidences: List<EfficacyEvidence>, trials: List<ActionableTrial>): BreakendEvidence {
            val applicableEvidences = EfficacyEvidenceExtractor.extractGeneEvidence(evidences, BREAKEND_EVENTS)
            val actionableTrialMatcher = ActionableTrialMatcherFactory.createGeneTrialMatcher(trials, BREAKEND_EVENTS)

            return BreakendEvidence(applicableEvidences, actionableTrialMatcher)
        }
    }
}
