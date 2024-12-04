package com.hartwig.actin.molecular.evidence.actionability

import com.hartwig.actin.molecular.evidence.actionability.ActionableEventsExtraction.codonFilter
import com.hartwig.actin.molecular.evidence.actionability.ActionableEventsExtraction.exonFilter
import com.hartwig.actin.molecular.evidence.actionability.ActionableEventsExtraction.filterEfficacyEvidence
import com.hartwig.actin.molecular.evidence.actionability.ActionableEventsExtraction.filterTrials
import com.hartwig.actin.molecular.evidence.actionability.ActionableEventsExtraction.geneFilter
import com.hartwig.actin.molecular.evidence.actionability.ActionableEventsExtraction.hotspotFilter
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
            ActionableEventsExtraction::extractHotspot
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
            ActionableEventsExtraction::extractRange
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
        getEventFromEvidence: (EfficacyEvidence) -> T,
    ): List<EfficacyEvidence> {
        return if (!variant.isReportable) {
            emptyList()
        } else {
            evidences.filter {
                isMatch.invoke(
                    getEventFromEvidence.invoke(it),
                    variant
                )
            }
        }
    }

    private fun <T> selectMatchingTrials(
        trials: List<ActionableTrial>,
        variant: VariantMatchCriteria,
        isMatch: (T, VariantMatchCriteria) -> Boolean,
        getEventFromTrial: (ActionableTrial) -> T,
    ): List<ActionableTrial> {
        return if (!variant.isReportable) {
            emptyList()
        } else {
            trials.filter {
                isMatch.invoke(
                    getEventFromTrial.invoke(it),
                    variant
                )
            }
        }
    }

    companion object {
        private val APPLICABLE_GENE_EVENTS = setOf(GeneEvent.ACTIVATION, GeneEvent.INACTIVATION, GeneEvent.ANY_MUTATION)

        fun create(evidences: List<EfficacyEvidence>, trials: List<ActionableTrial>): VariantEvidence {
            val hotspotEvidences = filterEfficacyEvidence(evidences, hotspotFilter())
            val hotspotTrials = filterTrials(trials, hotspotFilter())

            val codonEvidences = filterEfficacyEvidence(evidences, codonFilter())
            val codonTrials = filterTrials(trials, codonFilter())

            val exonEvidences = filterEfficacyEvidence(evidences, exonFilter())
            val exonTrials = filterTrials(trials, exonFilter())

            val rangeEvidences = listOf(codonEvidences, exonEvidences).flatten()
            val rangeTrials = listOf(codonTrials, exonTrials).flatten()

            val applicableGeneEvidences = filterEfficacyEvidence(evidences, geneFilter()).filter {
                APPLICABLE_GENE_EVENTS.contains(ActionableEventsExtraction.extractGene(it).event())
            }
            val applicableGeneTrials = filterTrials(trials, geneFilter()).filter {
                APPLICABLE_GENE_EVENTS.contains(ActionableEventsExtraction.extractGenes(it).event())
            }

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