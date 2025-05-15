package com.hartwig.actin.molecular.evidence.actionability

import com.hartwig.actin.datamodel.molecular.driver.CopyNumber
import com.hartwig.actin.datamodel.molecular.driver.CopyNumberType
import com.hartwig.serve.datamodel.efficacy.EfficacyEvidence
import com.hartwig.serve.datamodel.molecular.MolecularCriterium
import com.hartwig.serve.datamodel.molecular.gene.ActionableGene
import com.hartwig.serve.datamodel.molecular.gene.GeneEvent
import com.hartwig.serve.datamodel.trial.ActionableTrial
import java.util.function.Predicate

class CopyNumberEvidence(
    private val amplificationEvidences: List<EfficacyEvidence>,
    private val amplificationTrialMatcher: ActionableTrialMatcher,
    private val deletionEvidences: List<EfficacyEvidence>,
    private val deletionTrialMatcher: ActionableTrialMatcher
) : ActionabilityMatcher<CopyNumber> {

    override fun findMatches(event: CopyNumber): ActionabilityMatch {
        return when (event.canonicalImpact.type) {
            CopyNumberType.FULL_GAIN, CopyNumberType.PARTIAL_GAIN -> {
                findMatches(event, amplificationEvidences, amplificationTrialMatcher)
            }

            CopyNumberType.DEL -> {
                findMatches(event, deletionEvidences, deletionTrialMatcher)
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
        private val DELETION_EVENTS = setOf(GeneEvent.DELETION)

        fun create(evidences: List<EfficacyEvidence>, trials: List<ActionableTrial>): CopyNumberEvidence {
            val amplificationEvidences = EfficacyEvidenceExtractor.extractGeneEvidence(evidences, AMPLIFICATION_EVENTS)
            val amplificationTrialMatcher = ActionableTrialMatcherFactory.createGeneTrialMatcher(trials, AMPLIFICATION_EVENTS)

            val deletionEvidences = EfficacyEvidenceExtractor.extractGeneEvidence(evidences, DELETION_EVENTS)
            val deletionTrialMatcher = ActionableTrialMatcherFactory.createGeneTrialMatcher(trials, DELETION_EVENTS)

            return CopyNumberEvidence(
                amplificationEvidences,
                amplificationTrialMatcher,
                deletionEvidences,
                deletionTrialMatcher
            )
        }

        fun isAmplificationEvent(geneEvent: GeneEvent): Boolean {
            return AMPLIFICATION_EVENTS.contains(geneEvent)
        }

        fun isDeletionEvent(geneEvent: GeneEvent): Boolean {
            return DELETION_EVENTS.contains(geneEvent)
        }

        fun isAmplificationMatch(actionableGene: ActionableGene, copyNumber: CopyNumber): Boolean {
            return (copyNumber.canonicalImpact.type == CopyNumberType.FULL_GAIN
                    || copyNumber.canonicalImpact.type == CopyNumberType.PARTIAL_GAIN)
                    && copyNumber.gene == actionableGene.gene()
        }

        fun isDeletionMatch(actionableGene: ActionableGene, copyNumber: CopyNumber): Boolean {
            return copyNumber.canonicalImpact.type == CopyNumberType.DEL
                    && copyNumber.gene == actionableGene.gene()
        }
    }
}
