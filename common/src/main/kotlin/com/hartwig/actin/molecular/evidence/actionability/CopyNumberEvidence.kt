package com.hartwig.actin.molecular.evidence.actionability

import com.hartwig.actin.datamodel.molecular.orange.driver.CopyNumber
import com.hartwig.actin.datamodel.molecular.orange.driver.CopyNumberType
import com.hartwig.serve.datamodel.efficacy.EfficacyEvidence
import com.hartwig.serve.datamodel.molecular.MolecularCriterium
import com.hartwig.serve.datamodel.molecular.gene.GeneEvent
import com.hartwig.serve.datamodel.trial.ActionableTrial
import java.util.function.Predicate

class CopyNumberEvidence(
    private val applicableAmplificationEvidences: List<EfficacyEvidence>,
    private val amplificationTrialMatcher: ActionableTrialMatcher,
    private val applicableLossEvidences: List<EfficacyEvidence>,
    private val lossTrialMatcher: ActionableTrialMatcher
) : ActionabilityMatcher<CopyNumber> {

    override fun findMatches(event: CopyNumber): ActionabilityMatch {
        return when (event.type) {
            CopyNumberType.FULL_GAIN, CopyNumberType.PARTIAL_GAIN -> {
                findMatches(event, applicableAmplificationEvidences, amplificationTrialMatcher)
            }

            CopyNumberType.LOSS -> {
                findMatches(event, applicableLossEvidences, lossTrialMatcher)
            }

            else -> {
                ActionabilityMatch(evidenceMatches = emptyList(), matchingCriteriaPerTrialMatch = emptyMap())
            }
        }
    }

    private fun findMatches(
        copyNumber: CopyNumber,
        applicableEvidences: List<EfficacyEvidence>,
        applicableTrialMatcher: ActionableTrialMatcher
    ): ActionabilityMatch {
        val matchPredicate: Predicate<MolecularCriterium> =
            Predicate { ActionableEventsExtraction.extractGene(it).gene() == copyNumber.gene }

        return ActionabilityMatch(
            evidenceMatches = applicableEvidences.filter { matchPredicate.test(it.molecularCriterium()) },
            matchingCriteriaPerTrialMatch = applicableTrialMatcher.matchTrials(matchPredicate)
        )
    }

    companion object {
        private val AMPLIFICATION_EVENTS = setOf(GeneEvent.AMPLIFICATION)
        private val LOSS_EVENTS = setOf(GeneEvent.DELETION)

        fun create(evidences: List<EfficacyEvidence>, trials: List<ActionableTrial>): CopyNumberEvidence {
            val applicableAmplificationEvidences = ActionableEventsExtraction.extractGeneEvidence(evidences, AMPLIFICATION_EVENTS)
            val applicableLossEvidences = ActionableEventsExtraction.extractGeneEvidence(evidences, LOSS_EVENTS)

            val (applicableAmplificationTrials, amplificationMolecularPredicate) = ActionableEventsExtraction.extractGeneTrials(
                trials,
                AMPLIFICATION_EVENTS
            )
            val amplificationTrialMatcher = ActionableTrialMatcher(applicableAmplificationTrials, amplificationMolecularPredicate)

            val (applicableLossTrials, lossMolecularPredicate) = ActionableEventsExtraction.extractGeneTrials(trials, LOSS_EVENTS)
            val lossTrialMatcher = ActionableTrialMatcher(applicableLossTrials, lossMolecularPredicate)


            return CopyNumberEvidence(
                applicableAmplificationEvidences,
                amplificationTrialMatcher,
                applicableLossEvidences,
                lossTrialMatcher
            )
        }
    }
}
