package com.hartwig.actin.molecular.evidence.actionability

import com.hartwig.actin.doid.DoidModel
import com.hartwig.actin.molecular.evidence.curation.ApplicabilityFiltering
import com.hartwig.serve.datamodel.ActionableEvent
import com.hartwig.serve.datamodel.ActionableEvents
import com.hartwig.serve.datamodel.ImmutableActionableEvents
import com.hartwig.serve.datamodel.Knowledgebase
import com.hartwig.serve.datamodel.characteristic.ActionableCharacteristic
import com.hartwig.serve.datamodel.fusion.ActionableFusion
import com.hartwig.serve.datamodel.gene.ActionableGene
import com.hartwig.serve.datamodel.hotspot.ActionableHotspot
import com.hartwig.serve.datamodel.immuno.ActionableHLA
import com.hartwig.serve.datamodel.range.ActionableRange

class ActionableEventMatcherFactory(
    private val doidModel: DoidModel,
    private val tumorDoids: Set<String>
) {

    val actionableEventSources = setOf(ActionabilityConstants.EVIDENCE_SOURCE, ActionabilityConstants.EXTERNAL_TRIAL_SOURCE)

    fun create(actionableEvents: ActionableEvents): ActionableEventMatcher {
        val filtered = filterForApplicability(
            filterForSources(
                actionableEvents,
                actionableEventSources
            )
        )
        val personalizedActionabilityFactory: PersonalizedActionabilityFactory =
            PersonalizedActionabilityFactory.create(doidModel, tumorDoids)
        return fromActionableEvents(
            personalizedActionabilityFactory,
            filtered
        )
    }

    internal fun filterForSources(actionableEvents: ActionableEvents, sourcesToInclude: Set<Knowledgebase?>): ActionableEvents {
        return ImmutableActionableEvents.builder()
            .hotspots(filterActionableForSources<ActionableHotspot>(actionableEvents.hotspots(), sourcesToInclude))
            .codons(filterActionableForSources<ActionableRange>(actionableEvents.codons(), sourcesToInclude))
            .exons(filterActionableForSources<ActionableRange>(actionableEvents.exons(), sourcesToInclude))
            .genes(filterActionableForSources<ActionableGene>(actionableEvents.genes(), sourcesToInclude))
            .fusions(filterActionableForSources<ActionableFusion>(actionableEvents.fusions(), sourcesToInclude))
            .characteristics(filterActionableForSources<ActionableCharacteristic>(actionableEvents.characteristics(), sourcesToInclude))
            .hla(filterActionableForSources<ActionableHLA>(actionableEvents.hla(), sourcesToInclude))
            .build()
    }

    private fun <T : ActionableEvent> filterActionableForSources(
        actionables: List<T>,
        sourcesToInclude: Set<Knowledgebase?>
    ): MutableSet<T> {
        return actionables.filter { actionable: T -> sourcesToInclude.contains(actionable.source()) }.toMutableSet()
    }

    internal fun filterForApplicability(actionableEvents: ActionableEvents): ActionableEvents {
        return ImmutableActionableEvents.builder()
            .from(actionableEvents)
            .hotspots(
                filterHotspotsForApplicability(
                    actionableEvents.hotspots()
                )
            )
            .codons(
                filterRangesForApplicability(
                    actionableEvents.codons()
                )
            )
            .exons(
                filterRangesForApplicability(
                    actionableEvents.exons()
                )
            )
            .genes(
                filterGenesForApplicability(
                    actionableEvents.genes()
                )
            )
            .build()
    }

    private fun <T : ActionableEvent> filterEventsForApplicability(list: List<T>, predicate: (T) -> Boolean): List<T> {
        return list.filter { predicate(it) }.toList()
    }

    private fun filterHotspotsForApplicability(hotspots: List<ActionableHotspot>): List<ActionableHotspot> {
        return filterEventsForApplicability(hotspots) { obj: ActionableHotspot ->
            ApplicabilityFiltering.isApplicable(
                obj
            )
        }
    }

    private fun filterRangesForApplicability(ranges: List<ActionableRange>): List<ActionableRange> {
        return filterEventsForApplicability(ranges) { obj: ActionableRange ->
            ApplicabilityFiltering.isApplicable(
                obj
            )
        }
    }

    private fun filterGenesForApplicability(genes: List<ActionableGene>): List<ActionableGene> {
        return filterEventsForApplicability(genes) { obj: ActionableGene ->
            ApplicabilityFiltering.isApplicable(
                obj
            )
        }
    }

    private fun fromActionableEvents(
        personalizedActionabilityFactory: PersonalizedActionabilityFactory,
        actionableEvents: ActionableEvents
    ): ActionableEventMatcher {
        val signatureEvidence: SignatureEvidence = SignatureEvidence.create(actionableEvents)
        val variantEvidence: VariantEvidence = VariantEvidence.create(actionableEvents)
        val copyNumberEvidence: CopyNumberEvidence = CopyNumberEvidence.create(actionableEvents)
        val homozygousDisruptionEvidence: HomozygousDisruptionEvidence = HomozygousDisruptionEvidence.create(actionableEvents)
        val breakendEvidence: BreakendEvidence = BreakendEvidence.create(actionableEvents)
        val fusionEvidence: FusionEvidence = FusionEvidence.create(actionableEvents)
        val virusEvidence: VirusEvidence = VirusEvidence.create(actionableEvents)
        return ActionableEventMatcher(
            personalizedActionabilityFactory,
            signatureEvidence,
            variantEvidence,
            copyNumberEvidence,
            homozygousDisruptionEvidence,
            breakendEvidence,
            fusionEvidence,
            virusEvidence
        )
    }
}
