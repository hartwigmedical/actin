package com.hartwig.actin.molecular.evidence.actionability

import com.hartwig.actin.molecular.evidence.matching.GeneMatching
import com.hartwig.actin.molecular.evidence.matching.HotspotMatching
import com.hartwig.actin.molecular.evidence.matching.RangeMatching
import com.hartwig.actin.molecular.evidence.matching.VariantMatchCriteria
import com.hartwig.serve.datamodel.efficacy.EfficacyEvidence
import com.hartwig.serve.datamodel.molecular.gene.GeneEvent
import com.hartwig.serve.datamodel.trial.ActionableTrial

class VariantEvidence(
    private val hotspotEvidences: List<EfficacyEvidence>,
    private val hotspotTrials: List<ActionableTrial>,
    private val rangeEvidences: List<EfficacyEvidence>,
    private val rangeTrials: List<ActionableTrial>,
    private val applicableGeneEvidences: List<EfficacyEvidence>,
    private val applicableGeneTrials: List<ActionableTrial>,
) : ActionabilityMatcher<VariantMatchCriteria> {

    override fun findMatches(event: VariantMatchCriteria): ActionabilityMatch {
        val evidenceMatches = hotspotEvidenceMatches(event) + rangeEvidenceMatches(event) + geneEvidenceMatches(event)
        val trialMatches = hotspotTrialMatches(event) + rangeTrialMatches(event) + geneTrialMatches(event)

        return ActionabilityMatch(evidenceMatches, trialMatches)
    }

    private fun hotspotEvidenceMatches(variant: VariantMatchCriteria): List<EfficacyEvidence> {
        return selectMatchingEvidences(
            hotspotEvidences,
            variant,
            HotspotMatching::isMatch,
            ActionableEventsExtraction::extractHotspot
        )
    }

    private fun hotspotTrialMatches(variant: VariantMatchCriteria): List<ActionableTrial> {
        return selectMatchingTrials(
            hotspotTrials,
            variant,
            HotspotMatching::isMatch,
            ActionableEventsExtraction::extractHotspots
        )
    }

    private fun rangeEvidenceMatches(variant: VariantMatchCriteria): List<EfficacyEvidence> {
        return selectMatchingEvidences(
            rangeEvidences,
            variant,
            RangeMatching::isMatch,
            ActionableEventsExtraction::extractRange
        )
    }

    private fun rangeTrialMatches(variant: VariantMatchCriteria): List<ActionableTrial> {
        return selectMatchingTrials(
            rangeTrials,
            variant,
            RangeMatching::isMatch,
            ActionableEventsExtraction::extractRanges
        )
    }

    private fun geneEvidenceMatches(variant: VariantMatchCriteria): List<EfficacyEvidence> {
        return selectMatchingEvidences(
            applicableGeneEvidences,
            variant,
            GeneMatching::isMatch,
            ActionableEventsExtraction::extractGene
        )
    }

    private fun geneTrialMatches(variant: VariantMatchCriteria): List<ActionableTrial> {
        return selectMatchingTrials(
            applicableGeneTrials,
            variant,
            GeneMatching::isMatch,
            ActionableEventsExtraction::extractGenes
        )
    }

    private fun <T> selectMatchingEvidences(
        evidences: List<EfficacyEvidence>,
        variant: VariantMatchCriteria,
        isMatch: (T, VariantMatchCriteria) -> Boolean,
        extractActionableEventFromEvidence: (EfficacyEvidence) -> T,
    ): List<EfficacyEvidence> {
        return if (!variant.isReportable) {
            emptyList()
        } else {
            evidences.filter {
                isMatch.invoke(
                    extractActionableEventFromEvidence.invoke(it),
                    variant
                )
            }
        }
    }

    private fun <T> selectMatchingTrials(
        trials: List<ActionableTrial>,
        variant: VariantMatchCriteria,
        isMatch: (T, VariantMatchCriteria) -> Boolean,
        extractActionableEventsFromTrial: (ActionableTrial) -> Set<T>,
    ): List<ActionableTrial> {
        return if (!variant.isReportable) {
            emptyList()
        } else {
            trials.filter {
                extractActionableEventsFromTrial(it).any { actionableEvent -> isMatch(actionableEvent, variant) }
            }
        }
    }

    companion object {
        private val VARIANT_GENE_EVENTS = setOf(GeneEvent.ACTIVATION, GeneEvent.INACTIVATION, GeneEvent.ANY_MUTATION)

        fun create(evidences: List<EfficacyEvidence>, trials: List<ActionableTrial>): VariantEvidence {
            val hotspotEvidences = ActionableEventsExtraction.extractHotspotEvidence(evidences)
            val hotspotTrials = ActionableEventsExtraction.extractHotspotTrials(trials)

            val codonEvidences = ActionableEventsExtraction.extractCodonEvidence(evidences)
            val codonTrials = ActionableEventsExtraction.extractCodonTrials(trials)

            val exonEvidences = ActionableEventsExtraction.extractExonEvidence(evidences)
            val exonTrials = ActionableEventsExtraction.extractExonTrials(trials)

            val rangeEvidences = listOf(codonEvidences, exonEvidences).flatten()
            val rangeTrials = listOf(codonTrials, exonTrials).flatten()

            val applicableGeneEvidences = ActionableEventsExtraction.extractGeneEvidence(evidences, VARIANT_GENE_EVENTS)
            val applicableGeneTrials = ActionableEventsExtraction.extractGeneTrials(trials, VARIANT_GENE_EVENTS)

            return VariantEvidence(
                hotspotEvidences = hotspotEvidences,
                hotspotTrials = hotspotTrials,
                rangeEvidences = rangeEvidences,
                rangeTrials = rangeTrials,
                applicableGeneEvidences = applicableGeneEvidences,
                applicableGeneTrials = applicableGeneTrials
            )
        }
    }
}