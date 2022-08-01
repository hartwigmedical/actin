package com.hartwig.actin.doid;

import java.util.Map;
import java.util.Set;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.hartwig.actin.doid.config.DoidManualConfig;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DoidModel {

    @NotNull
    private final Multimap<String, String> relationship;
    @NotNull
    private final Map<String, String> termPerDoidMap;
    @NotNull
    private final Map<String, String> doidPerLowerCaseTermMap;
    @NotNull
    private final DoidManualConfig doidManualConfig;

    DoidModel(@NotNull final Multimap<String, String> relationship, @NotNull final Map<String, String> termPerDoidMap,
            @NotNull final Map<String, String> doidPerLowerCaseTermMap, @NotNull final DoidManualConfig doidManualConfig) {
        this.relationship = relationship;
        this.termPerDoidMap = termPerDoidMap;
        this.doidPerLowerCaseTermMap = doidPerLowerCaseTermMap;
        this.doidManualConfig = doidManualConfig;
    }

    @NotNull
    @VisibleForTesting
    Multimap<String, String> relationship() {
        return relationship;
    }

    @NotNull
    @VisibleForTesting
    Map<String, String> termForDoidMap() {
        return termPerDoidMap;
    }

    @NotNull
    @VisibleForTesting
    Map<String, String> doidForLowerCaseTermMap() {
        return doidPerLowerCaseTermMap;
    }

    @NotNull
    public Set<String> doidWithParents(@NotNull String doid) {
        Set<String> doids = Sets.newHashSet(doid);
        addParents(doid, doids);
        return doids;
    }

    @NotNull
    public Set<String> mainCancerDoids(@NotNull String doid) {
        Set<String> doids = doidWithParents(doid);
        Set<String> matches = Sets.newHashSet();
        for (String mainCancerDoid : doidManualConfig.mainCancerDoids()) {
            if (doids.contains(mainCancerDoid)) {
                matches.add(mainCancerDoid);
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
    public String resolveTermForDoid(@NotNull String doid) {
        return termPerDoidMap.get(doid);
    }

    @Nullable
    public String resolveDoidForTerm(@NotNull String term) {
        return doidPerLowerCaseTermMap.get(term.toLowerCase());
    }
}
