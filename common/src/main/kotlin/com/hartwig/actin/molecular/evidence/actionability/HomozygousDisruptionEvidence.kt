package com.hartwig.actin.molecular.evidence.actionability

import com.hartwig.actin.datamodel.molecular.orange.driver.HomozygousDisruption
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

        val evidences = applicableEvidences.filter { matchPredicate.test(it.molecularCriterium()) }
        val trials = applicableTrialMatcher.apply(matchPredicate)

        return ActionabilityMatch(evidences, trials)
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
