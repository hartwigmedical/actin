package com.hartwig.actin.doid;

import java.util.Map;
import java.util.Set;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DoidModel {

    @NotNull
    private final Multimap<String, String> relationship;
    @NotNull
    private final Map<String, String> termsPerDoid;

    DoidModel(@NotNull final Multimap<String, String> relationship, @NotNull final Map<String, String> termsPerDoid) {
        this.relationship = relationship;
        this.termsPerDoid = termsPerDoid;
    }

    @NotNull
    @VisibleForTesting
    Multimap<String, String> relationship() {
        return relationship;
    }

    @NotNull
    @VisibleForTesting
    Map<String, String> termsForDoid() {
        return termsPerDoid;
    }

    @NotNull
    public Set<String> doidWithParents(@NotNull String doid) {
        Set<String> doids = Sets.newHashSet(doid);
        addParents(doid, doids);
        return doids;
    }

    @NotNull
    public Set<String> mainCancerTypes(@NotNull String doid) {
        Set<String> doids = doidWithParents(doid);
        Set<String> matches = Sets.newHashSet();
        for (String mainCancerType : DoidMainCancerTypesConfig.MAIN_CANCER_TYPES) {
            if (doids.contains(mainCancerType)) {
                matches.add(mainCancerType);
            }
        }
        return matches;
    }

    private void addParents(@NotNull String child, @NotNull Set<String> result) {
        if (!relationship.containsKey(child)) {
            return;
        }

        for (String parent : relationship.get(child)) {
            if (result.add(parent)) {
                addParents(parent, result);
            }
        }
    }

    @Nullable
    public String term(@NotNull String doid) {
        return termsForDoid().get(doid);
    }
}
