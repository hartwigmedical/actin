package com.hartwig.actin.molecular.evidence.actionability

import com.hartwig.actin.molecular.evidence.actionability.ActionableEventsExtraction.codonFilter
import com.hartwig.actin.molecular.evidence.actionability.ActionableEventsExtraction.exonFilter
import com.hartwig.actin.molecular.evidence.actionability.ActionableEventsExtraction.filterEfficacyEvidence
import com.hartwig.actin.molecular.evidence.actionability.ActionableEventsExtraction.filterTrials
import com.hartwig.actin.molecular.evidence.actionability.ActionableEventsExtraction.geneFilter
import com.hartwig.actin.molecular.evidence.actionability.ActionableEventsExtraction.hotspotFilter
import com.hartwig.actin.molecular.evidence.matching.EvidenceMatcher
import com.hartwig.actin.molecular.evidence.matching.GeneMatching
import com.hartwig.actin.molecular.evidence.matching.HotspotMatching
import com.hartwig.actin.molecular.evidence.matching.RangeMatching
import com.hartwig.actin.molecular.evidence.matching.VariantMatchCriteria
import com.hartwig.serve.datamodel.efficacy.EfficacyEvidence
import com.hartwig.serve.datamodel.molecular.gene.GeneEvent
import com.hartwig.serve.datamodel.trial.ActionableTrial

class VariantEvidence(
    private val actionableHotspots: ActionableEvents,
    private val actionableRanges: ActionableEvents,
    private val applicableActionableGenes: ActionableEvents
) : EvidenceMatcher<VariantMatchCriteria> {

    override fun findMatches(event: VariantMatchCriteria): ActionableEvents {
        return hotspotMatches(event).let { (hotspotEvidences, hotspotTrials) ->
            rangeMatches(event).let { (rangeEvidences, rangeTrials) ->
                geneMatches(event).let { (geneEvidences, geneTrials) ->
                    ActionableEvents((hotspotEvidences + rangeEvidences + geneEvidences), (hotspotTrials + rangeTrials + geneTrials))
                }
            }
        }
    }

    private fun hotspotMatches(variant: VariantMatchCriteria): ActionableEvents {
        return filterMatchingEvents(
            actionableHotspots,
            variant,
            HotspotMatching::isMatch,
            ActionableEventsExtraction::extractHotspot,
            ActionableEventsExtraction::extractHotspot
        )
    }

    private fun rangeMatches(variant: VariantMatchCriteria): ActionableEvents {
        return filterMatchingEvents(
            actionableRanges,
            variant,
            RangeMatching::isMatch,
            ActionableEventsExtraction::extractRange,
            ActionableEventsExtraction::extractRange
        )
    }

    private fun geneMatches(variant: VariantMatchCriteria): ActionableEvents {
        return filterMatchingEvents(
            applicableActionableGenes,
            variant,
            GeneMatching::isMatch,
            ActionableEventsExtraction::extractGene,
            ActionableEventsExtraction::extractGene
        )
    }

    private fun <T> filterMatchingEvents(
        events: ActionableEvents,
        variant: VariantMatchCriteria,
        isMatch: (T, VariantMatchCriteria) -> Boolean,
        getEventFromEvidence: (EfficacyEvidence) -> T,
        getEventFromTrial: (ActionableTrial) -> T
    ): ActionableEvents {
        return if (!variant.isReportable) ActionableEvents() else ActionableEvents(events.evidences.filter {
            isMatch.invoke(
                getEventFromEvidence.invoke(it),
                variant
            )
        }, events.trials.filter { isMatch.invoke(getEventFromTrial.invoke(it), variant) })
    }

    companion object {
        private val APPLICABLE_GENE_EVENTS = setOf(GeneEvent.ACTIVATION, GeneEvent.INACTIVATION, GeneEvent.ANY_MUTATION)

        fun create(actionableEvents: ActionableEvents): VariantEvidence {
            with(actionableEvents) {
                val hotspotEvidences = filterEfficacyEvidence(evidences, hotspotFilter())
                val hotspotTrials = filterTrials(trials, hotspotFilter())

                val codonEvidences = filterEfficacyEvidence(evidences, codonFilter())
                val codonTrials = filterTrials(trials, codonFilter())

                val exonEvidences = filterEfficacyEvidence(evidences, exonFilter())
                val exonTrials = filterTrials(trials, exonFilter())

                val rangeEvidences = listOf(codonEvidences, exonEvidences).flatten()
                val rangeTrials = listOf(codonTrials, exonTrials).flatten()

                val applicableActionableGeneEvidences = filterEfficacyEvidence(evidences, geneFilter()).filter {
                    APPLICABLE_GENE_EVENTS.contains(ActionableEventsExtraction.extractGene(it).event())
                }
                val applicableActionableGeneTrials = filterTrials(trials, geneFilter()).filter {
                    APPLICABLE_GENE_EVENTS.contains(ActionableEventsExtraction.extractGene(it).event())
                }

                return VariantEvidence(
                    ActionableEvents(hotspotEvidences, hotspotTrials),
                    ActionableEvents(rangeEvidences, rangeTrials),
                    ActionableEvents(applicableActionableGeneEvidences, applicableActionableGeneTrials)
                )
            }
        }
    }
}