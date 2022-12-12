package com.hartwig.actin.molecular.orange.evidence.actionability;

import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
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

    private static final Set<Knowledgebase> ACTIONABLE_EVENT_SOURCES =
            Sets.newHashSet(ActionabilityConstants.EVIDENCE_SOURCE, ActionabilityConstants.EXTERNAL_TRIAL_SOURCE);

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
        AmplificationEvidence amplificationEvidence = AmplificationEvidence.create(actionableEvents);
        LossEvidence lossEvidence = LossEvidence.create(actionableEvents);
        HomozygousDisruptionEvidence homozygousDisruptionEvidence = HomozygousDisruptionEvidence.create(actionableEvents);
        BreakendEvidence breakendEvidence = BreakendEvidence.create(actionableEvents);
        FusionEvidence fusionEvidence = FusionEvidence.create(actionableEvents);
        VirusEvidence virusEvidence = VirusEvidence.create(actionableEvents);

        return new ActionableEventMatcher(personalizedActionabilityFactory,
                signatureEvidence,
                variantEvidence,
                amplificationEvidence,
                lossEvidence,
                homozygousDisruptionEvidence,
                breakendEvidence,
                fusionEvidence,
                virusEvidence);
    }

    @NotNull
    private ActionableEvents curateExternalTrials(@NotNull ActionableEvents actionableEvents) {
        return ImmutableActionableEvents.builder()
                .hotspots(curateHotspots(actionableEvents.hotspots()))
                .ranges(curateRanges(actionableEvents.ranges()))
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
        List<T> curated = Lists.newArrayList();
        for (T event : events) {
            String curatedTreatmentName = determineCuratedTreatmentName(event);
            if (!curatedTreatmentName.equals(event.treatment().name())) {
                curated.add(factory.create(event, curatedTreatmentName));
            } else {
                curated.add(event);
            }
        }
        return curated;
    }

    @NotNull
    private String determineCuratedTreatmentName(@NotNull ActionableEvent event) {
        if (event.source() == ActionabilityConstants.EXTERNAL_TRIAL_SOURCE) {
            return externalTrialMapper.map(event.treatment().name());
        }

        return event.treatment().name();
    }

    @NotNull
    private static ActionableEvents filterForSources(@NotNull ActionableEvents actionableEvents,
            @NotNull Set<Knowledgebase> sourcesToInclude) {
        return ImmutableActionableEvents.builder()
                .hotspots(filterActionableForSources(actionableEvents.hotspots(), sourcesToInclude))
                .ranges(filterActionableForSources(actionableEvents.ranges(), sourcesToInclude))
                .genes(filterActionableForSources(actionableEvents.genes(), sourcesToInclude))
                .fusions(filterActionableForSources(actionableEvents.fusions(), sourcesToInclude))
                .characteristics(filterActionableForSources(actionableEvents.characteristics(), sourcesToInclude))
                .hla(filterActionableForSources(actionableEvents.hla(), sourcesToInclude))
                .build();
    }

    @NotNull
    private static <T extends ActionableEvent> Set<T> filterActionableForSources(@NotNull List<T> actionables,
            @NotNull Set<Knowledgebase> sourcesToInclude) {
        Set<T> filtered = Sets.newHashSet();
        for (T actionable : actionables) {
            if (sourcesToInclude.contains(actionable.source())) {
                filtered.add(actionable);
            }
        }
        return filtered;
    }

    @NotNull
    private static ActionableEvents filterForApplicability(@NotNull ActionableEvents actionableEvents) {
        return ImmutableActionableEvents.builder()
                .from(actionableEvents)
                .hotspots(filterHotspotsForApplicability(actionableEvents.hotspots()))
                .ranges(filterRangesForApplicability(actionableEvents.ranges()))
                .genes(filterGenesForApplicability(actionableEvents.genes()))
                .build();
    }

    @NotNull
    private static List<ActionableHotspot> filterHotspotsForApplicability(@NotNull List<ActionableHotspot> hotspots) {
        List<ActionableHotspot> filtered = Lists.newArrayList();
        for (ActionableHotspot actionableHotspot : hotspots) {
            if (ApplicabilityFiltering.isApplicable(actionableHotspot)) {
                filtered.add(actionableHotspot);
            }
        }
        return filtered;
    }

    @NotNull
    private static List<ActionableRange> filterRangesForApplicability(@NotNull List<ActionableRange> ranges) {
        List<ActionableRange> filtered = Lists.newArrayList();
        for (ActionableRange actionableRange : ranges) {
            if (ApplicabilityFiltering.isApplicable(actionableRange)) {
                filtered.add(actionableRange);
            }
        }
        return filtered;
    }

    @NotNull
    private static List<ActionableGene> filterGenesForApplicability(@NotNull List<ActionableGene> genes) {
        List<ActionableGene> filtered = Lists.newArrayList();
        for (ActionableGene actionableGene : genes) {
            if (ApplicabilityFiltering.isApplicable(actionableGene)) {
                filtered.add(actionableGene);
            }
        }
        return filtered;
    }

    private interface ActionableFactory<T extends ActionableEvent> {

        @NotNull
        T create(@NotNull T event, @NotNull String curatedTreatmentName);
    }
}
