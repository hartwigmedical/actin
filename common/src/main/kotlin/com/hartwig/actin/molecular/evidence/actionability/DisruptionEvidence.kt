package com.hartwig.actin.molecular.evidence.actionability

import com.hartwig.actin.datamodel.molecular.driver.Disruption
import com.hartwig.actin.datamodel.molecular.driver.GeneRole
import com.hartwig.serve.datamodel.molecular.MolecularCriterium
import com.hartwig.serve.datamodel.molecular.gene.GeneEvent
import com.hartwig.serve.datamodel.trial.ActionableTrial
import java.util.function.Predicate

class DisruptionEvidence(
//    private val applicableGeneEvidences: List<EfficacyEvidence>,
    private val actionableToEvidences: ActionableToEvidences,
    private val applicableTrialMatcher: ActionableTrialMatcher
) : ActionabilityMatcher<Disruption> {

    override fun findMatches(event: Disruption): ActionabilityMatch {
        val matchPredicate: Predicate<MolecularCriterium> =
            Predicate {
                event.isReportable && ActionableEventExtraction.extractGene(it).gene() == event.gene && event.geneRole != GeneRole.TSG
            }
//        val evidenceMatches = applicableGeneEvidences.filter { matchPredicate.test(it.molecularCriterium()) }
        val evidenceMatches = (actionableToEvidences[event] ?: emptySet())
            .filter { matchPredicate.test(it.molecularCriterium()) }
            .filter { evidence ->
                ActionableEventExtraction.geneFilter(DISRUPTION_EVENTS).test(evidence.molecularCriterium())
            }
            .toList()
        val matchingCriteriaPerTrialMatch = applicableTrialMatcher.apply(matchPredicate)

        return ActionabilityMatch(evidenceMatches = evidenceMatches, matchingCriteriaPerTrialMatch = matchingCriteriaPerTrialMatch)
    }

    companion object {
        private val DISRUPTION_EVENTS = setOf(GeneEvent.ANY_MUTATION)

        fun create(actionableToEvidences: ActionableToEvidences, trials: List<ActionableTrial>): DisruptionEvidence {
//            val applicableEvidences = EfficacyEvidenceExtractor.extractGeneEvidence(evidences, DISRUPTION_EVENTS)
            val actionableTrialMatcher = ActionableTrialMatcherFactory.createGeneTrialMatcher(trials, DISRUPTION_EVENTS)

            return DisruptionEvidence(actionableToEvidences, actionableTrialMatcher)
        }
    }
}
