package com.hartwig.actin.molecular.evidence.actionability

import com.hartwig.actin.datamodel.molecular.driver.DriverLikelihood
import com.hartwig.actin.datamodel.molecular.driver.Variant
import com.hartwig.actin.molecular.evidence.matching.GeneMatching
import com.hartwig.actin.molecular.evidence.matching.HotspotMatching
import com.hartwig.actin.molecular.evidence.matching.RangeMatching
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
) : ActionabilityMatcher<Variant> {

    override fun findMatches(event: Variant): ActionabilityMatch {
        val hotspotEvidenceMatches = hotspotEvidenceMatches(event)
        val codonEvidenceMatches = codonEvidenceMatches(event)
        val exonEvidenceMatches = exonEvidenceMatches(event)
        val geneEvidenceMatches = geneEvidenceMatches(event)

        val hotspotMatchingCriteriaPerTrialMatch = hotspotTrialMatches(event)
        val codonMatchingCriteriaPerTrialMatch = codonTrialMatches(event)
        val exonMatchingCriteriaPerTrialMatch = exonTrialMatches(event)
        val geneMatchingCriteriaPerTrialMatch = geneTrialMatches(event)

        return ActionabilityMatchFactory.create(
            evidenceMatchLists = listOf(
                hotspotEvidenceMatches,
                codonEvidenceMatches,
                exonEvidenceMatches,
                geneEvidenceMatches
            ),
            matchingCriteriaPerTrialMatchLists = listOf(
                hotspotMatchingCriteriaPerTrialMatch,
                codonMatchingCriteriaPerTrialMatch,
                exonMatchingCriteriaPerTrialMatch,
                geneMatchingCriteriaPerTrialMatch
            )
        )
    }

    private fun hotspotEvidenceMatches(variant: Variant): List<EfficacyEvidence> {
        return selectMatchingEvidences(hotspotEvidences, variant, HotspotMatching::isMatch, ActionableEventExtraction::extractHotspot)
    }

    private fun hotspotTrialMatches(variant: Variant): Map<ActionableTrial, Set<MolecularCriterium>> {
        return selectMatchingTrials(hotspotTrialMatcher, variant, HotspotMatching::isMatch, ActionableEventExtraction::extractHotspot)
    }

    private fun codonEvidenceMatches(variant: Variant): List<EfficacyEvidence> {
        return selectMatchingEvidences(codonEvidences, variant, RangeMatching::isMatch, ActionableEventExtraction::extractCodon)
    }

    private fun codonTrialMatches(variant: Variant): Map<ActionableTrial, Set<MolecularCriterium>> {
        return selectMatchingTrials(codonTrialMatcher, variant, RangeMatching::isMatch, ActionableEventExtraction::extractCodon)
    }

    private fun exonEvidenceMatches(variant: Variant): List<EfficacyEvidence> {
        return selectMatchingEvidences(exonEvidences, variant, RangeMatching::isMatch, ActionableEventExtraction::extractExon)
    }

    private fun exonTrialMatches(variant: Variant): Map<ActionableTrial, Set<MolecularCriterium>> {
        return selectMatchingTrials(exonTrialMatcher, variant, RangeMatching::isMatch, ActionableEventExtraction::extractExon)
    }

    private fun geneEvidenceMatches(variant: Variant): List<EfficacyEvidence> {
        return selectMatchingEvidences(applicableGeneEvidences, variant, GeneMatching::isMatch, ActionableEventExtraction::extractGene)
    }

    private fun geneTrialMatches(variant: Variant): Map<ActionableTrial, Set<MolecularCriterium>> {
        return selectMatchingTrials(applicableGeneTrialMatcher, variant, GeneMatching::isMatch, ActionableEventExtraction::extractGene)
    }

    private fun <T> selectMatchingEvidences(
        evidences: List<EfficacyEvidence>,
        variant: Variant,
        isMatch: (T, Variant) -> Boolean,
        extractActionableEvent: (MolecularCriterium) -> T,
    ): List<EfficacyEvidence> {
        return evidences.filter {
            variant.isReportable && variant.driverLikelihood == DriverLikelihood.HIGH && isMatch.invoke(
                extractActionableEvent.invoke(it.molecularCriterium()),
                variant
            )
        }
    }

    private fun <T> selectMatchingTrials(
        trialMatcher: ActionableTrialMatcher,
        variant: Variant,
        isMatch: (T, Variant) -> Boolean,
        extractActionableEvent: (MolecularCriterium) -> T,
    ): Map<ActionableTrial, Set<MolecularCriterium>> {
        val matchPredicate: Predicate<MolecularCriterium> =
            Predicate {
                isVariantEligible(variant) && isMatch(extractActionableEvent.invoke(it), variant)
            }

        return trialMatcher.apply(matchPredicate)
    }

    companion object {
        private val VARIANT_GENE_EVENTS = setOf(GeneEvent.ACTIVATION, GeneEvent.INACTIVATION, GeneEvent.ANY_MUTATION)

        fun create(evidences: List<EfficacyEvidence>, trials: List<ActionableTrial>): VariantEvidence {
            val hotspotEvidences = EfficacyEvidenceExtractor.extractHotspotEvidence(evidences)
            val hotspotTrialMatcher = ActionableTrialMatcherFactory.createHotspotTrialMatcher(trials)

            val codonEvidences = EfficacyEvidenceExtractor.extractCodonEvidence(evidences)
            val codonTrialMatcher = ActionableTrialMatcherFactory.createCodonTrialMatcher(trials)

            val exonEvidences = EfficacyEvidenceExtractor.extractExonEvidence(evidences)
            val exonTrialMatcher = ActionableTrialMatcherFactory.createExonTrialMatcher(trials)

            val applicableGeneEvidences = EfficacyEvidenceExtractor.extractGeneEvidence(evidences, VARIANT_GENE_EVENTS)
            val applicableGeneTrialMatcher = ActionableTrialMatcherFactory.createGeneTrialMatcher(trials, VARIANT_GENE_EVENTS)

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

        fun isVariantEligible(variant: Variant): Boolean =
            variant.isReportable && variant.driverLikelihood == DriverLikelihood.HIGH
    }
}