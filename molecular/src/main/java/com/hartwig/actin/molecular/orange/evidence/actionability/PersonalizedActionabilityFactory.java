package com.hartwig.actin.molecular.orange.evidence.actionability;

import java.util.List;
import java.util.Set;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Sets;
import com.hartwig.actin.clinical.datamodel.ClinicalRecord;
import com.hartwig.actin.doid.DoidModel;
import com.hartwig.serve.datamodel.ActionableEvent;
import com.hartwig.serve.datamodel.CancerType;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

class PersonalizedActionabilityFactory {

    @NotNull
    private final DoidModel doidModel;
    @NotNull
    private final Set<String> applicableDoids;

    @NotNull
    public static PersonalizedActionabilityFactory fromClinicalRecord(@NotNull ClinicalRecord clinical, @NotNull DoidModel doidModel) {
        return new PersonalizedActionabilityFactory(doidModel, expandDoids(doidModel, clinical.tumor().doids()));
    }

    @VisibleForTesting
    PersonalizedActionabilityFactory(@NotNull final DoidModel doidModel, @NotNull final Set<String> applicableDoids) {
        this.doidModel = doidModel;
        this.applicableDoids = applicableDoids;
    }

    @NotNull
    public ActionabilityMatch create(@NotNull List<ActionableEvent> matches) {
        Set<String> expandedTumorDoids = expandDoids(doidModel, applicableDoids);

        ImmutableActionabilityMatch.Builder builder = ImmutableActionabilityMatch.builder();
        for (ActionableEvent match : matches) {
            if (isOnLabel(match, expandedTumorDoids)) {
                builder.addOnLabelEvents(match);
            } else {
                builder.addOffLabelEvents(match);
            }
        }
        return builder.build();
    }

    private boolean isOnLabel(@NotNull ActionableEvent event, @NotNull Set<String> applicableDoids) {
        if (!applicableDoids.contains(event.applicableCancerType().doid())) {
            return false;
        }

        for (CancerType blacklist : event.blacklistCancerTypes()) {
            if (applicableDoids.contains(blacklist.doid())) {
                return false;
            }
        }

        return true;
    }

    @NotNull
    private static Set<String> expandDoids(@NotNull DoidModel doidModel, @Nullable Set<String> doids) {
        if (doids == null) {
            return Sets.newHashSet();
        }

        Set<String> expanded = Sets.newHashSet();
        for (String doid : doids) {
            expanded.addAll(doidModel.doidWithParents(doid));
        }
        return expanded;
    }
}
