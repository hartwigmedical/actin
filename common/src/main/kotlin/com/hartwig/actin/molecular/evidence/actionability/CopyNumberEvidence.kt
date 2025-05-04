package com.hartwig.actin.molecular.evidence.actionability

import com.hartwig.actin.datamodel.molecular.driver.CopyNumber
import com.hartwig.actin.datamodel.molecular.driver.CopyNumberType
import com.hartwig.serve.datamodel.molecular.MolecularCriterium
import com.hartwig.serve.datamodel.molecular.gene.GeneEvent
import com.hartwig.serve.datamodel.trial.ActionableTrial
import java.util.function.Predicate

class CopyNumberEvidence(
    private val actionableToEvidences: ActionableToEvidences,
    private val amplificationTrialMatcher: ActionableTrialMatcher,
    private val lossTrialMatcher: ActionableTrialMatcher
) : ActionabilityMatcher<CopyNumber> {

    override fun findMatches(event: CopyNumber): ActionabilityMatch {
        return when (event.canonicalImpact.type) {
            CopyNumberType.FULL_GAIN, CopyNumberType.PARTIAL_GAIN -> {
                findMatches(event, AMPLIFICATION_EVENTS, amplificationTrialMatcher)
            }

            CopyNumberType.LOSS -> {
                findMatches(event, LOSS_EVENTS, lossTrialMatcher)
            }

            else -> {
                ActionabilityMatch(evidenceMatches = emptyList(), matchingCriteriaPerTrialMatch = emptyMap())
            }
        }
    }

    private fun findMatches(
        copyNumber: CopyNumber,
        validGeneEvents: Set<GeneEvent>,
        applicableTrialMatcher: ActionableTrialMatcher
    ): ActionabilityMatch {
        val matchPredicate: Predicate<MolecularCriterium> =
            Predicate { ActionableEventExtraction.extractGene(it).gene() == copyNumber.gene }

        // TODO check if we are missing having to apply the matchPredicate to the evidence
        val evidences = (actionableToEvidences[copyNumber] ?: emptySet())
            .filter { evidence ->
                ActionableEventExtraction.geneFilter(validGeneEvents).test(evidence.molecularCriterium())
            }

        return ActionabilityMatch(
            evidenceMatches = evidences.toList(),
            matchingCriteriaPerTrialMatch = applicableTrialMatcher.apply(matchPredicate)
        )
    }

    companion object {
        private val AMPLIFICATION_EVENTS = setOf(GeneEvent.AMPLIFICATION)
        private val LOSS_EVENTS = setOf(GeneEvent.DELETION)

        fun create(actionableToEvidences: ActionableToEvidences, trials: List<ActionableTrial>): CopyNumberEvidence {
            val amplificationTrialMatcher = ActionableTrialMatcherFactory.createGeneTrialMatcher(trials, AMPLIFICATION_EVENTS)
            val lossTrialMatcher = ActionableTrialMatcherFactory.createGeneTrialMatcher(trials, LOSS_EVENTS)

            return CopyNumberEvidence(
                actionableToEvidences,
                amplificationTrialMatcher,
                lossTrialMatcher
            )
        }
    }
}
