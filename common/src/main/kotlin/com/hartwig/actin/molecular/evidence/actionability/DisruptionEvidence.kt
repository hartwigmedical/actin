package com.hartwig.actin.molecular.evidence.actionability

import com.hartwig.actin.datamodel.molecular.driver.Disruption
import com.hartwig.actin.datamodel.molecular.driver.GeneRole
import com.hartwig.serve.datamodel.efficacy.EfficacyEvidence
import com.hartwig.serve.datamodel.molecular.MolecularCriterium
import com.hartwig.serve.datamodel.molecular.gene.ActionableGene
import com.hartwig.serve.datamodel.molecular.gene.GeneEvent
import com.hartwig.serve.datamodel.trial.ActionableTrial
import java.util.function.Predicate

class DisruptionEvidence(
    private val applicableGeneEvidences: List<EfficacyEvidence>,
    private val applicableTrialMatcher: ActionableTrialMatcher
) : ActionabilityMatcher<Disruption> {

    override fun findMatches(event: Disruption): ActionabilityMatch {

        val matchPredicate: Predicate<MolecularCriterium> =
            Predicate { isDisruptionMatch(ActionableEventExtraction.extractGene(it), event) }
        val evidenceMatches = applicableGeneEvidences.filter { matchPredicate.test(it.molecularCriterium()) }
        val matchingCriteriaPerTrialMatch = applicableTrialMatcher.apply(matchPredicate)

        return ActionabilityMatch(evidenceMatches = evidenceMatches, matchingCriteriaPerTrialMatch = matchingCriteriaPerTrialMatch)
    }

    companion object {
        private val DISRUPTION_EVENTS = setOf(GeneEvent.ANY_MUTATION)

        fun create(evidences: List<EfficacyEvidence>, trials: List<ActionableTrial>): DisruptionEvidence {
            val applicableEvidences = EfficacyEvidenceExtractor.extractGeneEvidence(evidences, DISRUPTION_EVENTS)
            val actionableTrialMatcher = ActionableTrialMatcherFactory.createGeneTrialMatcher(trials, DISRUPTION_EVENTS)

            return DisruptionEvidence(applicableEvidences, actionableTrialMatcher)
        }

        fun isDisruptionEvent(geneEvent: GeneEvent): Boolean {
            return DISRUPTION_EVENTS.contains(geneEvent)
        }

        fun isDisruptionMatch(actionableGene: ActionableGene, disruption: Disruption): Boolean {
            return disruption.isReportable && disruption.gene == actionableGene.gene() && disruption.geneRole != GeneRole.TSG
        }
    }
}
