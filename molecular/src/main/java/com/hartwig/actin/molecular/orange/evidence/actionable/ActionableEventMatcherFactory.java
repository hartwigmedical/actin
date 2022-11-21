package com.hartwig.actin.molecular.orange.evidence.actionable;

import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.hartwig.actin.clinical.datamodel.ClinicalRecord;
import com.hartwig.actin.doid.DoidModel;
import com.hartwig.actin.molecular.orange.curation.ExternalTrialMapping;
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

public class ActionableEventMatcherFactory {

    private static final Set<Knowledgebase> ACTIONABLE_EVENT_SOURCES =
            Sets.newHashSet(ActionabilityConstants.EVIDENCE_SOURCE, ActionabilityConstants.EXTERNAL_TRIAL_SOURCE);

    @NotNull
    private final List<ExternalTrialMapping> externalTrialMappings;
    @NotNull
    private final ClinicalRecord clinical;
    @NotNull
    private final DoidModel doidModel;

    public ActionableEventMatcherFactory(@NotNull final List<ExternalTrialMapping> externalTrialMappings,
            @NotNull final ClinicalRecord clinical, @NotNull final DoidModel doidModel) {
        this.externalTrialMappings = externalTrialMappings;
        this.clinical = clinical;
        this.doidModel = doidModel;
    }

    @NotNull
    public ActionableEventMatcher create(@NotNull ActionableEvents actionableEvents) {
        ActionableEvents filtered = filterForSources(actionableEvents, ACTIONABLE_EVENT_SOURCES);

        ActionableEvents curated = ImmutableActionableEvents.builder()
                .hotspots(curateHotspots(filtered.hotspots()))
                .ranges(curateRanges(filtered.ranges()))
                .genes(curateGenes(filtered.genes()))
                .fusions(curateFusions(filtered.fusions()))
                .characteristics(curateCharacteristics(filtered.characteristics()))
                .hla(curateHla(filtered.hla()))
                .build();

        PersonalizedActionabilityFactory personalizedActionabilityFactory =
                PersonalizedActionabilityFactory.fromClinicalRecord(clinical, doidModel);
        return new ActionableEventMatcher(curated, personalizedActionabilityFactory);
    }

    @NotNull
    private List<ActionableHotspot> curateHotspots(@NotNull List<ActionableHotspot> hotspots) {
        return curateActionableEvents(hotspots,
                (event, curatedTreatmentName) -> ImmutableActionableHotspot.builder()
                        .from(event)
                        .treatment(ImmutableTreatment.builder().from(event.treatment()).name(curatedTreatmentName).build())
                        .build());
    }

    @NotNull
    private List<ActionableRange> curateRanges(@NotNull List<ActionableRange> ranges) {
        return curateActionableEvents(ranges,
                (event, curatedTreatmentName) -> ImmutableActionableRange.builder()
                        .from(event)
                        .treatment(ImmutableTreatment.builder().from(event.treatment()).name(curatedTreatmentName).build())
                        .build());
    }

    @NotNull
    private List<ActionableGene> curateGenes(@NotNull List<ActionableGene> genes) {
        return curateActionableEvents(genes,
                (event, curatedTreatmentName) -> ImmutableActionableGene.builder()
                        .from(event)
                        .treatment(ImmutableTreatment.builder().from(event.treatment()).name(curatedTreatmentName).build())
                        .build());
    }

    @NotNull
    private List<ActionableFusion> curateFusions(@NotNull List<ActionableFusion> fusions) {
        return curateActionableEvents(fusions,
                (event, curatedTreatmentName) -> ImmutableActionableFusion.builder()
                        .from(event)
                        .treatment(ImmutableTreatment.builder().from(event.treatment()).name(curatedTreatmentName).build())
                        .build());
    }

    @NotNull
    private List<ActionableCharacteristic> curateCharacteristics(@NotNull List<ActionableCharacteristic> characteristics) {
        return curateActionableEvents(characteristics,
                (event, curatedTreatmentName) -> ImmutableActionableCharacteristic.builder()
                        .from(event)
                        .treatment(ImmutableTreatment.builder().from(event.treatment()).name(curatedTreatmentName).build())
                        .build());
    }

    @NotNull
    private List<ActionableHLA> curateHla(@NotNull List<ActionableHLA> hlas) {
        return curateActionableEvents(hlas,
                (event, curatedTreatmentName) -> ImmutableActionableHLA.builder()
                        .from(event)
                        .treatment(ImmutableTreatment.builder().from(event.treatment()).name(curatedTreatmentName).build())
                        .build());
    }

    @NotNull
    private String determineCuratedTreatmentName(@NotNull ActionableEvent event) {
        if (event.source() == ActionabilityConstants.EXTERNAL_TRIAL_SOURCE) {
            for (ExternalTrialMapping externalTrialMapping : externalTrialMappings) {
                if (externalTrialMapping.externalTrial().equals(event.treatment().name())) {
                    return externalTrialMapping.actinTrial();
                }
            }
        }
        return event.treatment().name();
    }

    @NotNull
    private <T extends ActionableEvent> List<T> curateActionableEvents(@NotNull List<T> events, @NotNull ActionableFactory<T> factory) {
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
    private static ActionableEvents filterForSources(@NotNull ActionableEvents actionableEvents,
            @NotNull Set<Knowledgebase> sourcesToInclude) {
        return ImmutableActionableEvents.builder()
                .hotspots(filterActionable(actionableEvents.hotspots(), sourcesToInclude))
                .ranges(filterActionable(actionableEvents.ranges(), sourcesToInclude))
                .genes(filterActionable(actionableEvents.genes(), sourcesToInclude))
                .fusions(filterActionable(actionableEvents.fusions(), sourcesToInclude))
                .characteristics(filterActionable(actionableEvents.characteristics(), sourcesToInclude))
                .hla(filterActionable(actionableEvents.hla(), sourcesToInclude))
                .build();
    }

    @NotNull
    private static <T extends ActionableEvent> Set<T> filterActionable(@NotNull List<T> actionables,
            @NotNull Set<Knowledgebase> sourcesToInclude) {
        Set<T> filtered = Sets.newHashSet();
        for (T actionable : actionables) {
            if (sourcesToInclude.contains(actionable.source())) {
                filtered.add(actionable);
            }
        }
        return filtered;
    }

    private interface ActionableFactory<T extends ActionableEvent> {

        @NotNull
        T create(@NotNull T event, @NotNull String curatedTreatmentName);
    }
}
