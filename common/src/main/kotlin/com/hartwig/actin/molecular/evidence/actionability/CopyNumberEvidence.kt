package com.hartwig.actin.molecular.evidence.actionability

import com.hartwig.actin.datamodel.molecular.orange.driver.CopyNumber
import com.hartwig.actin.datamodel.molecular.orange.driver.CopyNumberType
import com.hartwig.serve.datamodel.efficacy.EfficacyEvidence
import com.hartwig.serve.datamodel.molecular.MolecularCriterium
import com.hartwig.serve.datamodel.molecular.gene.GeneEvent
import com.hartwig.serve.datamodel.trial.ActionableTrial
import java.util.function.Predicate

class CopyNumberEvidence(
    private val amplificationEvidences: List<EfficacyEvidence>,
    private val amplificationTrialMatcher: ActionableTrialMatcher,
    private val lossEvidences: List<EfficacyEvidence>,
    private val lossTrialMatcher: ActionableTrialMatcher
) : ActionabilityMatcher<CopyNumber> {

    override fun findMatches(event: CopyNumber): ActionabilityMatch {
        return when (event.canonicalImpact.type) {
            CopyNumberType.FULL_GAIN, CopyNumberType.PARTIAL_GAIN -> {
                findMatches(event, amplificationEvidences, amplificationTrialMatcher)
            }

            CopyNumberType.LOSS -> {
                findMatches(event, lossEvidences, lossTrialMatcher)
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
            Predicate { ActionableEventExtraction.extractGene(it).gene() == copyNumber.gene }

        return ActionabilityMatch(
            evidenceMatches = applicableEvidences.filter { matchPredicate.test(it.molecularCriterium()) },
            matchingCriteriaPerTrialMatch = applicableTrialMatcher.apply(matchPredicate)
        )
    }

    companion object {
        private val AMPLIFICATION_EVENTS = setOf(GeneEvent.AMPLIFICATION)
        private val LOSS_EVENTS = setOf(GeneEvent.DELETION)

        fun create(evidences: List<EfficacyEvidence>, trials: List<ActionableTrial>): CopyNumberEvidence {
            val amplificationEvidences = EfficacyEvidenceExtractor.extractGeneEvidence(evidences, AMPLIFICATION_EVENTS)
            val amplificationTrialMatcher = ActionableTrialMatcherFactory.createGeneTrialMatcher(trials, AMPLIFICATION_EVENTS)

            val lossEvidences = EfficacyEvidenceExtractor.extractGeneEvidence(evidences, LOSS_EVENTS)
            val lossTrialMatcher = ActionableTrialMatcherFactory.createGeneTrialMatcher(trials, LOSS_EVENTS)

            return CopyNumberEvidence(
                amplificationEvidences,
                amplificationTrialMatcher,
                lossEvidences,
                lossTrialMatcher
            )
        }
    }
}
