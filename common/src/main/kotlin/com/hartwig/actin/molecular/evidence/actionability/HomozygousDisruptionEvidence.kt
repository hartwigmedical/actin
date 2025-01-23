package com.hartwig.actin.molecular.evidence.actionability

import com.hartwig.actin.datamodel.molecular.driver.HomozygousDisruption
import com.hartwig.serve.datamodel.efficacy.EfficacyEvidence
import com.hartwig.serve.datamodel.molecular.MolecularCriterium
import com.hartwig.serve.datamodel.molecular.gene.GeneEvent
import com.hartwig.serve.datamodel.trial.ActionableTrial
import java.util.function.Predicate

class HomozygousDisruptionEvidence(
    private val applicableEvidences: List<EfficacyEvidence>,
    private val applicableTrialMatcher: ActionableTrialMatcher
) : ActionabilityMatcher<HomozygousDisruption> {

    override fun findMatches(event: HomozygousDisruption): ActionabilityMatch {
        val matchPredicate: Predicate<MolecularCriterium> = Predicate { ActionableEventExtraction.extractGene(it).gene() == event.gene }

        val evidenceMatches = applicableEvidences.filter { matchPredicate.test(it.molecularCriterium()) }
        val matchingCriteriaPerTrialMatch = applicableTrialMatcher.apply(matchPredicate)

        return ActionabilityMatch(evidenceMatches = evidenceMatches, matchingCriteriaPerTrialMatch = matchingCriteriaPerTrialMatch)
    }

    companion object {
        private val HOMOZYGOUS_DISRUPTION_EVENTS = setOf(GeneEvent.DELETION, GeneEvent.INACTIVATION, GeneEvent.ANY_MUTATION)

        fun create(evidences: List<EfficacyEvidence>, trials: List<ActionableTrial>): HomozygousDisruptionEvidence {
            val applicableEvidences = EfficacyEvidenceExtractor.extractGeneEvidence(evidences, HOMOZYGOUS_DISRUPTION_EVENTS)
            val applicableTrialMatcher = ActionableTrialMatcherFactory.createGeneTrialMatcher(trials, HOMOZYGOUS_DISRUPTION_EVENTS)

            return HomozygousDisruptionEvidence(applicableEvidences, applicableTrialMatcher)
        }
    }
}
