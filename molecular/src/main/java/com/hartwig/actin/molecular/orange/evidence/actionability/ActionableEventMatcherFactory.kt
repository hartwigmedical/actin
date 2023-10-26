package com.hartwig.actin.molecular.orange.evidence.actionability

import com.google.common.annotations.VisibleForTesting
import com.hartwig.actin.doid.DoidModel
import com.hartwig.actin.molecular.orange.evidence.actionability.ActionableEventMatcherFactory.ActionableFactory
import com.hartwig.actin.molecular.orange.evidence.curation.ApplicabilityFiltering
import com.hartwig.actin.molecular.orange.evidence.curation.ExternalTrialMapper
import com.hartwig.serve.datamodel.ActionableEvent
import com.hartwig.serve.datamodel.ActionableEvents
import com.hartwig.serve.datamodel.ImmutableActionableEvents
import com.hartwig.serve.datamodel.ImmutableTreatment
import com.hartwig.serve.datamodel.Knowledgebase
import com.hartwig.serve.datamodel.characteristic.ActionableCharacteristic
import com.hartwig.serve.datamodel.characteristic.ImmutableActionableCharacteristic
import com.hartwig.serve.datamodel.fusion.ActionableFusion
import com.hartwig.serve.datamodel.fusion.ImmutableActionableFusion
import com.hartwig.serve.datamodel.gene.ActionableGene
import com.hartwig.serve.datamodel.gene.ImmutableActionableGene
import com.hartwig.serve.datamodel.hotspot.ActionableHotspot
import com.hartwig.serve.datamodel.hotspot.ImmutableActionableHotspot
import com.hartwig.serve.datamodel.immuno.ActionableHLA
import com.hartwig.serve.datamodel.immuno.ImmutableActionableHLA
import com.hartwig.serve.datamodel.range.ActionableRange
import com.hartwig.serve.datamodel.range.ImmutableActionableRange
import java.util.Set
import java.util.function.Predicate
import java.util.stream.Collectors

class ActionableEventMatcherFactory(private val externalTrialMapper: ExternalTrialMapper, private val doidModel: DoidModel,
                                    private val tumorDoids: MutableSet<String>) {
    fun create(actionableEvents: ActionableEvents): ActionableEventMatcher {
        val filtered = filterForApplicability(filterForSources(actionableEvents, ACTIONABLE_EVENT_SOURCES))
        val curated = curateExternalTrials(filtered)
        val personalizedActionabilityFactory: PersonalizedActionabilityFactory = PersonalizedActionabilityFactory.create(doidModel, tumorDoids)
        return fromActionableEvents(personalizedActionabilityFactory, curated)
    }

    @VisibleForTesting
    fun curateExternalTrials(actionableEvents: ActionableEvents): ActionableEvents {
        return ImmutableActionableEvents.builder()
            .hotspots(curateHotspots(actionableEvents.hotspots()))
            .codons(curateRanges(actionableEvents.codons()))
            .exons(curateRanges(actionableEvents.exons()))
            .genes(curateGenes(actionableEvents.genes()))
            .fusions(curateFusions(actionableEvents.fusions()))
            .characteristics(curateCharacteristics(actionableEvents.characteristics()))
            .hla(curateHla(actionableEvents.hla()))
            .build()
    }

    private fun curateHotspots(hotspots: MutableList<ActionableHotspot>): MutableList<ActionableHotspot> {
        return curateTreatments(hotspots,
            { event: ActionableHotspot, curatedTreatmentName: String ->
                ImmutableActionableHotspot.builder()
                    .from(event)
                    .treatment(ImmutableTreatment.builder().from(event.treatment()).name(curatedTreatmentName).build())
                    .build()
            })
    }

    private fun curateRanges(ranges: MutableList<ActionableRange>): MutableList<ActionableRange> {
        return curateTreatments(ranges,
            { event: ActionableRange, curatedTreatmentName: String ->
                ImmutableActionableRange.builder()
                    .from(event)
                    .treatment(ImmutableTreatment.builder().from(event.treatment()).name(curatedTreatmentName).build())
                    .build()
            })
    }

    private fun curateGenes(genes: MutableList<ActionableGene>): MutableList<ActionableGene> {
        return curateTreatments(genes,
            { event: ActionableGene, curatedTreatmentName: String ->
                ImmutableActionableGene.builder()
                    .from(event)
                    .treatment(ImmutableTreatment.builder().from(event.treatment()).name(curatedTreatmentName).build())
                    .build()
            })
    }

    private fun curateFusions(fusions: MutableList<ActionableFusion>): MutableList<ActionableFusion> {
        return curateTreatments(fusions,
            { event: ActionableFusion, curatedTreatmentName: String ->
                ImmutableActionableFusion.builder()
                    .from(event)
                    .treatment(ImmutableTreatment.builder().from(event.treatment()).name(curatedTreatmentName).build())
                    .build()
            })
    }

    private fun curateCharacteristics(characteristics: MutableList<ActionableCharacteristic>): MutableList<ActionableCharacteristic> {
        return curateTreatments<ActionableCharacteristic>(characteristics
        ) { event: ActionableCharacteristic, curatedTreatmentName: String ->
            ImmutableActionableCharacteristic.builder()
                .from(event)
                .treatment(ImmutableTreatment.builder().from(event.treatment()).name(curatedTreatmentName).build())
                .build()
        }
    }

    private fun curateHla(hlas: MutableList<ActionableHLA>): MutableList<ActionableHLA> {
        return curateTreatments<ActionableHLA>(hlas,
            ActionableFactory<ActionableHLA> { event: ActionableHLA, curatedTreatmentName: String ->
                ImmutableActionableHLA.builder()
                    .from(event)
                    .treatment(ImmutableTreatment.builder().from(event.treatment()).name(curatedTreatmentName).build())
                    .build()
            })
    }

    private fun <T : ActionableEvent> curateTreatments(events: MutableList<T>, factory: ActionableFactory<T>): MutableList<T> {
        return events.stream().map { event: T ->
            val curatedTreatmentName = determineCuratedTreatmentName(event)
            if (curatedTreatmentName != event.treatment().name()) factory.create(event, curatedTreatmentName) else event
        }.collect(Collectors.toList())
    }

    private fun determineCuratedTreatmentName(event: ActionableEvent): String {
        return if (event.source() == ActionabilityConstants.EXTERNAL_TRIAL_SOURCE) {
            externalTrialMapper.map(event.treatment().name())
        } else event.treatment().name()
    }

    private fun interface ActionableFactory<T : ActionableEvent?> {
        open fun create(event: T, curatedTreatmentName: String): T
    }

    companion object {
        val ACTIONABLE_EVENT_SOURCES = Set.of(ActionabilityConstants.EVIDENCE_SOURCE, ActionabilityConstants.EXTERNAL_TRIAL_SOURCE)
        private fun fromActionableEvents(personalizedActionabilityFactory: PersonalizedActionabilityFactory,
                                         actionableEvents: ActionableEvents): ActionableEventMatcher {
            val signatureEvidence: SignatureEvidence = SignatureEvidence.create(actionableEvents)
            val variantEvidence: VariantEvidence = VariantEvidence.create(actionableEvents)
            val copyNumberEvidence: CopyNumberEvidence = CopyNumberEvidence.create(actionableEvents)
            val homozygousDisruptionEvidence: HomozygousDisruptionEvidence = HomozygousDisruptionEvidence.create(actionableEvents)
            val breakendEvidence: BreakendEvidence = BreakendEvidence.create(actionableEvents)
            val fusionEvidence: FusionEvidence = FusionEvidence.create(actionableEvents)
            val virusEvidence: VirusEvidence = VirusEvidence.create(actionableEvents)
            return ActionableEventMatcher(personalizedActionabilityFactory,
                signatureEvidence,
                variantEvidence,
                copyNumberEvidence,
                homozygousDisruptionEvidence,
                breakendEvidence,
                fusionEvidence,
                virusEvidence)
        }

        @VisibleForTesting
        fun filterForSources(actionableEvents: ActionableEvents, sourcesToInclude: MutableSet<Knowledgebase?>): ActionableEvents {
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

        private fun <T : ActionableEvent> filterActionableForSources(actionables: MutableList<T>,
                                                                     sourcesToInclude: MutableSet<Knowledgebase?>): MutableSet<T> {
            return actionables.stream().filter { actionable: T -> sourcesToInclude.contains(actionable.source()) }.collect(Collectors.toSet())
        }

        @VisibleForTesting
        fun filterForApplicability(actionableEvents: ActionableEvents): ActionableEvents {
            return ImmutableActionableEvents.builder()
                .from(actionableEvents)
                .hotspots(filterHotspotsForApplicability(actionableEvents.hotspots()))
                .codons(filterRangesForApplicability(actionableEvents.codons()))
                .exons(filterRangesForApplicability(actionableEvents.exons()))
                .genes(filterGenesForApplicability(actionableEvents.genes()))
                .build()
        }

        private fun <T : ActionableEvent> filterEventsForApplicability(list: MutableList<T>,
                                                                       predicate: Predicate<T>): MutableList<T> {
            return list.stream().filter(predicate).collect(Collectors.toList())
        }

        private fun filterHotspotsForApplicability(hotspots: MutableList<ActionableHotspot>): MutableList<ActionableHotspot> {
            return filterEventsForApplicability(hotspots, Predicate { obj: ActionableHotspot -> ApplicabilityFiltering.isApplicable(obj) })
        }

        private fun filterRangesForApplicability(ranges: MutableList<ActionableRange>): MutableList<ActionableRange> {
            return filterEventsForApplicability(ranges, Predicate { obj: ActionableRange -> ApplicabilityFiltering.isApplicable(obj) })
        }

        private fun filterGenesForApplicability(genes: MutableList<ActionableGene>): MutableList<ActionableGene> {
            return filterEventsForApplicability(genes, Predicate { obj: ActionableGene -> ApplicabilityFiltering.isApplicable(obj) })
        }
    }
}
