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
        val matchPredicate: Predicate<MolecularCriterium> =
            Predicate { event.isReportable && ActionableEventsExtraction.extractGene(it).gene() == event.gene }

        val evidences = applicableEvidences.filter { matchPredicate.test(it.molecularCriterium()) }
        val trials = applicableTrialMatcher.matchTrials(matchPredicate)

        return ActionabilityMatch(evidences, trials)
    }

    companion object {
        private val HOMOZYGOUS_DISRUPTION_EVENTS = setOf(GeneEvent.DELETION, GeneEvent.INACTIVATION, GeneEvent.ANY_MUTATION)

        fun create(evidences: List<EfficacyEvidence>, trials: List<ActionableTrial>): HomozygousDisruptionEvidence {
            val applicableEvidences = ActionableEventsExtraction.extractGeneEvidence(evidences, HOMOZYGOUS_DISRUPTION_EVENTS)
            val (applicableTrials, molecularPredicate) = ActionableEventsExtraction.extractGeneTrials(trials, HOMOZYGOUS_DISRUPTION_EVENTS)
            val applicableTrialMatcher = ActionableTrialMatcher(applicableTrials, molecularPredicate)

            return HomozygousDisruptionEvidence(applicableEvidences, applicableTrialMatcher)
        }
    }
}
