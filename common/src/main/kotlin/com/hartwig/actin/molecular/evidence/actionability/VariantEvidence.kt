package com.hartwig.actin.molecular.evidence.actionability

import com.hartwig.actin.molecular.evidence.actionability.ActionableEventsFiltering.codonFilter
import com.hartwig.actin.molecular.evidence.actionability.ActionableEventsFiltering.exonFilter
import com.hartwig.actin.molecular.evidence.actionability.ActionableEventsFiltering.filterAndExpandTrials
import com.hartwig.actin.molecular.evidence.actionability.ActionableEventsFiltering.filterEfficacyEvidence
import com.hartwig.actin.molecular.evidence.actionability.ActionableEventsFiltering.geneFilter
import com.hartwig.actin.molecular.evidence.actionability.ActionableEventsFiltering.hotspotFilter
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
    private val actionableRanges: ActionableEvents, private val applicableActionableGenes: ActionableEvents
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
            ActionableEventsFiltering::getHotspot,
            ActionableEventsFiltering::getHotspot
        )
    }

    private fun rangeMatches(variant: VariantMatchCriteria): ActionableEvents {
        return filterMatchingEvents(
            actionableRanges,
            variant,
            RangeMatching::isMatch,
            ActionableEventsFiltering::getRange,
            ActionableEventsFiltering::getRange
        )
    }

    private fun geneMatches(variant: VariantMatchCriteria): ActionableEvents {
        return filterMatchingEvents(
            applicableActionableGenes,
            variant,
            GeneMatching::isMatch,
            ActionableEventsFiltering::getGene,
            ActionableEventsFiltering::getGene
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
                val applicableActionableGenesEvidences = filterEfficacyEvidence(evidences, geneFilter()).filter {
                    APPLICABLE_GENE_EVENTS.contains(
                        ActionableEventsFiltering.getGene(it).event()
                    )
                }
                val applicableActionableGenesTrials = filterAndExpandTrials(trials, geneFilter()).filter {
                    APPLICABLE_GENE_EVENTS.contains(
                        ActionableEventsFiltering.getGene(it).event()
                    )
                }
                val codonsEvidence = filterEfficacyEvidence(evidences, codonFilter())
                val codonsTrials = filterAndExpandTrials(trials, codonFilter())

                val exonsEvidence = filterEfficacyEvidence(evidences, exonFilter())
                val exonsTrials = filterAndExpandTrials(trials, exonFilter())

                val hotspotsEvidence = filterEfficacyEvidence(evidences, hotspotFilter())
                val hotspotsTrials = filterAndExpandTrials(trials, hotspotFilter())

                val rangesEvidence = listOf(codonsEvidence, exonsEvidence).flatten()
                val rangesTrials = listOf(codonsTrials, exonsTrials).flatten()

                return VariantEvidence(
                    ActionableEvents(hotspotsEvidence, hotspotsTrials),
                    ActionableEvents(rangesEvidence, rangesTrials),
                    ActionableEvents(applicableActionableGenesEvidences, applicableActionableGenesTrials)
                )
            }
        }
    }
}