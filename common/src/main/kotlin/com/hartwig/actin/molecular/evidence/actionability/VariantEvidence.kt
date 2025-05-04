package com.hartwig.actin.molecular.evidence.actionability

import com.hartwig.actin.datamodel.molecular.driver.DriverLikelihood
import com.hartwig.actin.datamodel.molecular.driver.Variant
import com.hartwig.actin.molecular.evidence.matching.GeneMatching
import com.hartwig.actin.molecular.evidence.matching.HotspotMatching
import com.hartwig.actin.molecular.evidence.matching.MatchingCriteriaFunctions
import com.hartwig.actin.molecular.evidence.matching.RangeMatching
import com.hartwig.actin.molecular.evidence.matching.VariantMatchCriteria
import com.hartwig.serve.datamodel.efficacy.EfficacyEvidence
import com.hartwig.serve.datamodel.molecular.MolecularCriterium
import com.hartwig.serve.datamodel.molecular.gene.GeneEvent
import com.hartwig.serve.datamodel.trial.ActionableTrial
import java.util.function.Predicate

// TODO so it seems like we don't need per-event (gene, codon, etc) specific methods anymore?
//  or have I done something wrong?
class VariantEvidence(
//    private val hotspotEvidences: List<EfficacyEvidence>,
    private val hotspotTrialMatcher: ActionableTrialMatcher,
//    private val codonEvidences: List<EfficacyEvidence>,
    private val codonTrialMatcher: ActionableTrialMatcher,
//    private val exonEvidences: List<EfficacyEvidence>,
    private val exonTrialMatcher: ActionableTrialMatcher,
//    private val applicableGeneEvidences: List<EfficacyEvidence>,
    private val applicableGeneTrialMatcher: ActionableTrialMatcher,
    private val actionableToEvidences: ActionableToEvidences

) : ActionabilityMatcher<Variant> {

    override fun findMatches(event: Variant): ActionabilityMatch {
        val hotspotEvidenceMatches = hotspotEvidenceMatches(event)
        val codonEvidenceMatches = codonEvidenceMatches(event)
        val exonEvidenceMatches = exonEvidenceMatches(event)
        val geneEvidenceMatches = geneEvidenceMatches(event)

        val matchCriteria = MatchingCriteriaFunctions.createVariantCriteria(event)
        val hotspotMatchingCriteriaPerTrialMatch = hotspotTrialMatches(matchCriteria)
        val codonMatchingCriteriaPerTrialMatch = codonTrialMatches(matchCriteria)
        val exonMatchingCriteriaPerTrialMatch = exonTrialMatches(matchCriteria)
        val geneMatchingCriteriaPerTrialMatch = geneTrialMatches(matchCriteria)

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
        return (actionableToEvidences[variant] ?: emptySet()).toList()
    }

//    private fun hotspotEvidenceMatches(variant: VariantMatchCriteria): List<EfficacyEvidence> {
//        return selectMatchingEvidences(hotspotEvidences, variant, HotspotMatching::isMatch, ActionableEventExtraction::extractHotspot)
//    }

    private fun hotspotTrialMatches(variant: VariantMatchCriteria): Map<ActionableTrial, Set<MolecularCriterium>> {
        return selectMatchingTrials(hotspotTrialMatcher, variant, HotspotMatching::isMatch, ActionableEventExtraction::extractHotspot)
    }

    private fun codonEvidenceMatches(variant: Variant): List<EfficacyEvidence> {
        return selectMatchingEvidences(variant)
    }

//    private fun codonEvidenceMatches(variant: VariantMatchCriteria): List<EfficacyEvidence> {
//        return selectMatchingEvidences(codonEvidences, variant, RangeMatching::isMatch, ActionableEventExtraction::extractCodon)
//    }

    private fun codonTrialMatches(variant: VariantMatchCriteria): Map<ActionableTrial, Set<MolecularCriterium>> {
        return selectMatchingTrials(codonTrialMatcher, variant, RangeMatching::isMatch, ActionableEventExtraction::extractCodon)
    }

    private fun exonEvidenceMatches(variant: Variant): List<EfficacyEvidence> {
        return selectMatchingEvidences(variant)
    }

//    private fun exonEvidenceMatches(variant: VariantMatchCriteria): List<EfficacyEvidence> {
//        return selectMatchingEvidences(exonEvidences, variant, RangeMatching::isMatch, ActionableEventExtraction::extractExon)
//    }

    private fun exonTrialMatches(variant: VariantMatchCriteria): Map<ActionableTrial, Set<MolecularCriterium>> {
        return selectMatchingTrials(exonTrialMatcher, variant, RangeMatching::isMatch, ActionableEventExtraction::extractExon)
    }

    private fun geneEvidenceMatches(variant: Variant): List<EfficacyEvidence> {
        return selectMatchingEvidences(variant)
    }

//    private fun geneEvidenceMatches(variant: VariantMatchCriteria): List<EfficacyEvidence> {
//        return selectMatchingEvidences(applicableGeneEvidences, variant, GeneMatching::isMatch, ActionableEventExtraction::extractGene)
//    }

    private fun geneTrialMatches(variant: VariantMatchCriteria): Map<ActionableTrial, Set<MolecularCriterium>> {
        return selectMatchingTrials(applicableGeneTrialMatcher, variant, GeneMatching::isMatch, ActionableEventExtraction::extractGene)
    }

    private fun selectMatchingEvidences(variant: Variant): List<EfficacyEvidence> {
        return if (variant.isReportable && variant.driverLikelihood == DriverLikelihood.HIGH) {
            actionableToEvidences[variant]?.toList() ?: emptyList()
        } else {
            emptyList()
        }
    }

//    private fun <T> selectMatchingEvidences(
//        evidences: List<EfficacyEvidence>,
//        variant: VariantMatchCriteria,
//        isMatch: (T, VariantMatchCriteria) -> Boolean,
//        extractActionableEvent: (MolecularCriterium) -> T,
//    ): List<EfficacyEvidence> {
//        return evidences.filter {
//            variant.isReportable && variant.driverLikelihood == DriverLikelihood.HIGH && isMatch.invoke(
//                extractActionableEvent.invoke(it.molecularCriterium()),
//                variant
//            )
//        }
//    }

    private fun <T> selectMatchingTrials(
        trialMatcher: ActionableTrialMatcher,
        variant: VariantMatchCriteria,
        isMatch: (T, VariantMatchCriteria) -> Boolean,
        extractActionableEvent: (MolecularCriterium) -> T,
    ): Map<ActionableTrial, Set<MolecularCriterium>> {
        val matchPredicate: Predicate<MolecularCriterium> =
            Predicate {
                variant.isReportable && variant.driverLikelihood == DriverLikelihood.HIGH && isMatch(
                    extractActionableEvent.invoke(it),
                    variant
                )
            }

        return trialMatcher.apply(matchPredicate)
    }

    companion object {
        private val VARIANT_GENE_EVENTS = setOf(GeneEvent.ACTIVATION, GeneEvent.INACTIVATION, GeneEvent.ANY_MUTATION)

        fun create(actionableToEvidences: ActionableToEvidences, trials: List<ActionableTrial>): VariantEvidence {
//            val hotspotEvidences = EfficacyEvidenceExtractor.extractHotspotEvidence(evidences)
            val hotspotTrialMatcher = ActionableTrialMatcherFactory.createHotspotTrialMatcher(trials)

//            val codonEvidences = EfficacyEvidenceExtractor.extractCodonEvidence(evidences)
            val codonTrialMatcher = ActionableTrialMatcherFactory.createCodonTrialMatcher(trials)

//            val exonEvidences = EfficacyEvidenceExtractor.extractExonEvidence(evidences)
            val exonTrialMatcher = ActionableTrialMatcherFactory.createExonTrialMatcher(trials)

//            val applicableGeneEvidences = EfficacyEvidenceExtractor.extractGeneEvidence(evidences, VARIANT_GENE_EVENTS)
            val applicableGeneTrialMatcher = ActionableTrialMatcherFactory.createGeneTrialMatcher(trials, VARIANT_GENE_EVENTS)

            return VariantEvidence(
//                hotspotEvidences = hotspotEvidences,
                hotspotTrialMatcher = hotspotTrialMatcher,
//                codonEvidences = codonEvidences,
                codonTrialMatcher = codonTrialMatcher,
//                exonEvidences = exonEvidences,
                exonTrialMatcher = exonTrialMatcher,
//                applicableGeneEvidences = applicableGeneEvidences,
                applicableGeneTrialMatcher = applicableGeneTrialMatcher,
                actionableToEvidences = actionableToEvidences
            )
        }
    }
}