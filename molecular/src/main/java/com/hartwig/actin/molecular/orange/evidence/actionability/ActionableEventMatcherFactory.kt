package com.hartwig.actin.molecular.orange.evidence.actionability;

import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.google.common.annotations.VisibleForTesting;
import com.hartwig.actin.doid.DoidModel;
import com.hartwig.actin.molecular.orange.evidence.curation.ApplicabilityFiltering;
import com.hartwig.actin.molecular.orange.evidence.curation.ExternalTrialMapper;
import com.hartwig.serve.datamodel.ActionableEvent;
import com.hartwig.serve.datamodel.ActionableEvents;
import com.hartwig.serve.datamodel.ImmutableActionableEvents;
import com.hartwig.serve.datamodel.ImmutableTreatment;
import com.hartwig.serve.datamodel.Knowledgebase;
import com.hartwig.serve.datamodel.characteristic.ActionableCharacteristic;
import com.hartwig.serve.datamodel.characteristic.ImmutableActionableCharacteristic;
import com.hartwig.serve.datamodel.fusion.ActionableFusion;
import com.hartwig.serve.datamodel.fusion.ImmutableActionableFusion;
import com.hartwig.serve.datamodel.gene.ActionableGene;
import com.hartwig.serve.datamodel.gene.ImmutableActionableGene;
import com.hartwig.serve.datamodel.hotspot.ActionableHotspot;
import com.hartwig.serve.datamodel.hotspot.ImmutableActionableHotspot;
import com.hartwig.serve.datamodel.immuno.ActionableHLA;
import com.hartwig.serve.datamodel.immuno.ImmutableActionableHLA;
import com.hartwig.serve.datamodel.range.ActionableRange;
import com.hartwig.serve.datamodel.range.ImmutableActionableRange;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ActionableEventMatcherFactory {

    static final Set<Knowledgebase> ACTIONABLE_EVENT_SOURCES =
            Set.of(ActionabilityConstants.EVIDENCE_SOURCE, ActionabilityConstants.EXTERNAL_TRIAL_SOURCE);

    @NotNull
    private final ExternalTrialMapper externalTrialMapper;
    @NotNull
    private final DoidModel doidModel;
    @Nullable
    private final Set<String> tumorDoids;

    public ActionableEventMatcherFactory(@NotNull final ExternalTrialMapper externalTrialMapper, @NotNull final DoidModel doidModel,
            @Nullable final Set<String> tumorDoids) {
        this.externalTrialMapper = externalTrialMapper;
        this.doidModel = doidModel;
        this.tumorDoids = tumorDoids;
    }

    @NotNull
    public ActionableEventMatcher create(@NotNull ActionableEvents actionableEvents) {
        ActionableEvents filtered = filterForApplicability(filterForSources(actionableEvents, ACTIONABLE_EVENT_SOURCES));

        ActionableEvents curated = curateExternalTrials(filtered);

        PersonalizedActionabilityFactory personalizedActionabilityFactory = PersonalizedActionabilityFactory.create(doidModel, tumorDoids);

        return fromActionableEvents(personalizedActionabilityFactory, curated);
    }

    @NotNull
    private static ActionableEventMatcher fromActionableEvents(@NotNull PersonalizedActionabilityFactory personalizedActionabilityFactory,
            @NotNull ActionableEvents actionableEvents) {
        SignatureEvidence signatureEvidence = SignatureEvidence.create(actionableEvents);
        VariantEvidence variantEvidence = VariantEvidence.create(actionableEvents);
        CopyNumberEvidence copyNumberEvidence = CopyNumberEvidence.create(actionableEvents);
        HomozygousDisruptionEvidence homozygousDisruptionEvidence = HomozygousDisruptionEvidence.create(actionableEvents);
        BreakendEvidence breakendEvidence = BreakendEvidence.create(actionableEvents);
        FusionEvidence fusionEvidence = FusionEvidence.create(actionableEvents);
        VirusEvidence virusEvidence = VirusEvidence.create(actionableEvents);

        return new ActionableEventMatcher(personalizedActionabilityFactory,
                signatureEvidence,
                variantEvidence,
                copyNumberEvidence,
                homozygousDisruptionEvidence,
                breakendEvidence,
                fusionEvidence,
                virusEvidence);
    }

    @NotNull
    @VisibleForTesting
    ActionableEvents curateExternalTrials(@NotNull ActionableEvents actionableEvents) {
        return ImmutableActionableEvents.builder()
                .hotspots(curateHotspots(actionableEvents.hotspots()))
                .codons(curateRanges(actionableEvents.codons()))
                .exons(curateRanges(actionableEvents.exons()))
                .genes(curateGenes(actionableEvents.genes()))
                .fusions(curateFusions(actionableEvents.fusions()))
                .characteristics(curateCharacteristics(actionableEvents.characteristics()))
                .hla(curateHla(actionableEvents.hla()))
                .build();
    }

    @NotNull
    private List<ActionableHotspot> curateHotspots(@NotNull List<ActionableHotspot> hotspots) {
        return curateTreatments(hotspots,
                (event, curatedTreatmentName) -> ImmutableActionableHotspot.builder()
                        .from(event)
                        .treatment(ImmutableTreatment.builder().from(event.treatment()).name(curatedTreatmentName).build())
                        .build());
    }

    @NotNull
    private List<ActionableRange> curateRanges(@NotNull List<ActionableRange> ranges) {
        return curateTreatments(ranges,
                (event, curatedTreatmentName) -> ImmutableActionableRange.builder()
                        .from(event)
                        .treatment(ImmutableTreatment.builder().from(event.treatment()).name(curatedTreatmentName).build())
                        .build());
    }

    @NotNull
    private List<ActionableGene> curateGenes(@NotNull List<ActionableGene> genes) {
        return curateTreatments(genes,
                (event, curatedTreatmentName) -> ImmutableActionableGene.builder()
                        .from(event)
                        .treatment(ImmutableTreatment.builder().from(event.treatment()).name(curatedTreatmentName).build())
                        .build());
    }

    @NotNull
    private List<ActionableFusion> curateFusions(@NotNull List<ActionableFusion> fusions) {
        return curateTreatments(fusions,
                (event, curatedTreatmentName) -> ImmutableActionableFusion.builder()
                        .from(event)
                        .treatment(ImmutableTreatment.builder().from(event.treatment()).name(curatedTreatmentName).build())
                        .build());
    }

    @NotNull
    private List<ActionableCharacteristic> curateCharacteristics(@NotNull List<ActionableCharacteristic> characteristics) {
        return curateTreatments(characteristics,
                (event, curatedTreatmentName) -> ImmutableActionableCharacteristic.builder()
                        .from(event)
                        .treatment(ImmutableTreatment.builder().from(event.treatment()).name(curatedTreatmentName).build())
                        .build());
    }

    @NotNull
    private List<ActionableHLA> curateHla(@NotNull List<ActionableHLA> hlas) {
        return curateTreatments(hlas,
                (event, curatedTreatmentName) -> ImmutableActionableHLA.builder()
                        .from(event)
                        .treatment(ImmutableTreatment.builder().from(event.treatment()).name(curatedTreatmentName).build())
                        .build());
    }

    @NotNull
    private <T extends ActionableEvent> List<T> curateTreatments(@NotNull List<T> events, @NotNull ActionableFactory<T> factory) {
        return events.stream().map(event -> {
            String curatedTreatmentName = determineCuratedTreatmentName(event);
            return !curatedTreatmentName.equals(event.treatment().name()) ? factory.create(event, curatedTreatmentName) : event;
        }).collect(Collectors.toList());
    }

    @NotNull
    private String determineCuratedTreatmentName(@NotNull ActionableEvent event) {
        if (event.source() == ActionabilityConstants.EXTERNAL_TRIAL_SOURCE) {
            return externalTrialMapper.map(event.treatment().name());
        }

        return event.treatment().name();
    }

    @NotNull
    @VisibleForTesting
    static ActionableEvents filterForSources(@NotNull ActionableEvents actionableEvents, @NotNull Set<Knowledgebase> sourcesToInclude) {
        return ImmutableActionableEvents.builder()
                .hotspots(filterActionableForSources(actionableEvents.hotspots(), sourcesToInclude))
                .codons(filterActionableForSources(actionableEvents.codons(), sourcesToInclude))
                .exons(filterActionableForSources(actionableEvents.exons(), sourcesToInclude))
                .genes(filterActionableForSources(actionableEvents.genes(), sourcesToInclude))
                .fusions(filterActionableForSources(actionableEvents.fusions(), sourcesToInclude))
                .characteristics(filterActionableForSources(actionableEvents.characteristics(), sourcesToInclude))
                .hla(filterActionableForSources(actionableEvents.hla(), sourcesToInclude))
                .build();
    }

    @NotNull
    private static <T extends ActionableEvent> Set<T> filterActionableForSources(@NotNull List<T> actionables,
            @NotNull Set<Knowledgebase> sourcesToInclude) {
        return actionables.stream().filter(actionable -> sourcesToInclude.contains(actionable.source())).collect(Collectors.toSet());
    }

    @NotNull
    @VisibleForTesting
    static ActionableEvents filterForApplicability(@NotNull ActionableEvents actionableEvents) {
        return ImmutableActionableEvents.builder()
                .from(actionableEvents)
                .hotspots(filterHotspotsForApplicability(actionableEvents.hotspots()))
                .codons(filterRangesForApplicability(actionableEvents.codons()))
                .exons(filterRangesForApplicability(actionableEvents.exons()))
                .genes(filterGenesForApplicability(actionableEvents.genes()))
                .build();
    }

    @NotNull
    private static <T extends ActionableEvent> List<T> filterEventsForApplicability(@NotNull List<T> list,
            @NotNull Predicate<T> predicate) {
        return list.stream().filter(predicate).collect(Collectors.toList());
    }

    @NotNull
    private static List<ActionableHotspot> filterHotspotsForApplicability(@NotNull List<ActionableHotspot> hotspots) {
        return filterEventsForApplicability(hotspots, ApplicabilityFiltering::isApplicable);
    }

    @NotNull
    private static List<ActionableRange> filterRangesForApplicability(@NotNull List<ActionableRange> ranges) {
        return filterEventsForApplicability(ranges, ApplicabilityFiltering::isApplicable);
    }

    @NotNull
    private static List<ActionableGene> filterGenesForApplicability(@NotNull List<ActionableGene> genes) {
        return filterEventsForApplicability(genes, ApplicabilityFiltering::isApplicable);
    }

    private interface ActionableFactory<T extends ActionableEvent> {
        @NotNull
        T create(@NotNull T event, @NotNull String curatedTreatmentName);
    }
}
