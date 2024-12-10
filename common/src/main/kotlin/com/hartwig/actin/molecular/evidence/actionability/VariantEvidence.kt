package com.hartwig.actin.molecular.evidence.actionability

import com.hartwig.actin.molecular.evidence.matching.GeneMatching
import com.hartwig.actin.molecular.evidence.matching.HotspotMatching
import com.hartwig.actin.molecular.evidence.matching.RangeMatching
import com.hartwig.actin.molecular.evidence.matching.VariantMatchCriteria
import com.hartwig.serve.datamodel.efficacy.EfficacyEvidence
import com.hartwig.serve.datamodel.molecular.MolecularCriterium
import com.hartwig.serve.datamodel.molecular.gene.GeneEvent
import com.hartwig.serve.datamodel.trial.ActionableTrial
import java.util.function.Predicate

class VariantEvidence(
    private val hotspotEvidences: List<EfficacyEvidence>,
    private val hotspotTrialMatcher: ActionableTrialMatcher,
    private val codonEvidences: List<EfficacyEvidence>,
    private val codonTrialMatcher: ActionableTrialMatcher,
    private val exonEvidences: List<EfficacyEvidence>,
    private val exonTrialMatcher: ActionableTrialMatcher,
    private val applicableGeneEvidences: List<EfficacyEvidence>,
    private val applicableGeneTrialMatcher: ActionableTrialMatcher
) : ActionabilityMatcher<VariantMatchCriteria> {

    override fun findMatches(event: VariantMatchCriteria): ActionabilityMatch {
        val evidenceMatches =
            hotspotEvidenceMatches(event) + codonEvidenceMatches(event) + exonEvidenceMatches(event) + geneEvidenceMatches(event)
        val trialMatches = hotspotTrialMatches(event) + codonTrialMatches(event) + exonTrialMatches(event) + geneTrialMatches(event)

        return ActionabilityMatch(evidenceMatches, trialMatches)
    }

    private fun hotspotEvidenceMatches(variant: VariantMatchCriteria): List<EfficacyEvidence> {
        return selectMatchingEvidences(hotspotEvidences, variant, HotspotMatching::isMatch, ActionableEventsExtraction::extractHotspot)
    }

    private fun hotspotTrialMatches(variant: VariantMatchCriteria): Map<ActionableTrial, Set<MolecularCriterium>> {
        return selectMatchingTrials(hotspotTrialMatcher, variant, HotspotMatching::isMatch, ActionableEventsExtraction::extractHotspot)
    }

    private fun codonEvidenceMatches(variant: VariantMatchCriteria): List<EfficacyEvidence> {
        return selectMatchingEvidences(codonEvidences, variant, RangeMatching::isMatch, ActionableEventsExtraction::extractCodon)
    }

    private fun codonTrialMatches(variant: VariantMatchCriteria): Map<ActionableTrial, Set<MolecularCriterium>> {
        return selectMatchingTrials(codonTrialMatcher, variant, RangeMatching::isMatch, ActionableEventsExtraction::extractCodon)
    }

    private fun exonEvidenceMatches(variant: VariantMatchCriteria): List<EfficacyEvidence> {
        return selectMatchingEvidences(exonEvidences, variant, RangeMatching::isMatch, ActionableEventsExtraction::extractExon)
    }

    private fun exonTrialMatches(variant: VariantMatchCriteria): Map<ActionableTrial, Set<MolecularCriterium>> {
        return selectMatchingTrials(exonTrialMatcher, variant, RangeMatching::isMatch, ActionableEventsExtraction::extractExon)
    }

    private fun geneEvidenceMatches(variant: VariantMatchCriteria): List<EfficacyEvidence> {
        return selectMatchingEvidences(applicableGeneEvidences, variant, GeneMatching::isMatch, ActionableEventsExtraction::extractGene)
    }

    private fun geneTrialMatches(variant: VariantMatchCriteria): Map<ActionableTrial, Set<MolecularCriterium>> {
        return selectMatchingTrials(applicableGeneTrialMatcher, variant, GeneMatching::isMatch, ActionableEventsExtraction::extractGene)
    }

    private fun <T> selectMatchingEvidences(
        evidences: List<EfficacyEvidence>,
        variant: VariantMatchCriteria,
        isMatch: (T, VariantMatchCriteria) -> Boolean,
        extractActionableEvent: (MolecularCriterium) -> T,
    ): List<EfficacyEvidence> {
        return evidences.filter { variant.isReportable && isMatch.invoke(extractActionableEvent.invoke(it.molecularCriterium()), variant) }
    }

    private fun <T> selectMatchingTrials(
        trialMatcher: ActionableTrialMatcher,
        variant: VariantMatchCriteria,
        isMatch: (T, VariantMatchCriteria) -> Boolean,
        extractActionableEvent: (MolecularCriterium) -> T,
    ): Map<ActionableTrial, Set<MolecularCriterium>> {
        val matchPredicate: Predicate<MolecularCriterium> =
            Predicate { variant.isReportable && isMatch(extractActionableEvent.invoke(it), variant) }

        return trialMatcher.matchTrials(matchPredicate)
    }

    companion object {
        private val VARIANT_GENE_EVENTS = setOf(GeneEvent.ACTIVATION, GeneEvent.INACTIVATION, GeneEvent.ANY_MUTATION)

        fun create(evidences: List<EfficacyEvidence>, trials: List<ActionableTrial>): VariantEvidence {
            val hotspotEvidences = ActionableEventsExtraction.extractHotspotEvidence(evidences)
            val (hotspotTrials, hotspotPredicate) = ActionableEventsExtraction.extractHotspotTrials(trials)
            val hotspotTrialMatcher = ActionableTrialMatcher(hotspotTrials, hotspotPredicate)

            val codonEvidences = ActionableEventsExtraction.extractCodonEvidence(evidences)
            val (codonTrials, codonPredicate) = ActionableEventsExtraction.extractCodonTrials(trials)
            val codonTrialMatcher = ActionableTrialMatcher(codonTrials, codonPredicate)

            val exonEvidences = ActionableEventsExtraction.extractExonEvidence(evidences)
            val (exonTrials, exonPredicate) = ActionableEventsExtraction.extractExonTrials(trials)
            val exonTrialMatcher = ActionableTrialMatcher(exonTrials, exonPredicate)

            val applicableGeneEvidences = ActionableEventsExtraction.extractGeneEvidence(evidences, VARIANT_GENE_EVENTS)
            val (applicableGeneTrials, genePredicate) = ActionableEventsExtraction.extractGeneTrials(trials, VARIANT_GENE_EVENTS)
            val applicableGeneTrialMatcher = ActionableTrialMatcher(applicableGeneTrials, genePredicate)

            return VariantEvidence(
                hotspotEvidences = hotspotEvidences,
                hotspotTrialMatcher = hotspotTrialMatcher,
                codonEvidences = codonEvidences,
                codonTrialMatcher = codonTrialMatcher,
                exonEvidences = exonEvidences,
                exonTrialMatcher = exonTrialMatcher,
                applicableGeneEvidences = applicableGeneEvidences,
                applicableGeneTrialMatcher = applicableGeneTrialMatcher
            )
        }
    }
}