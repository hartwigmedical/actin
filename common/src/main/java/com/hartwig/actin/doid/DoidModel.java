package com.hartwig.actin.doid;

import java.util.Map;
import java.util.Set;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.hartwig.actin.doid.config.AdenoSquamousMapping;
import com.hartwig.actin.doid.config.DoidManualConfig;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DoidModel {

    @NotNull
    private final Multimap<String, String> childToParentsMap;
    @NotNull
    private final Map<String, String> termPerDoidMap;
    @NotNull
    private final Map<String, String> doidPerLowerCaseTermMap;
    @NotNull
    private final DoidManualConfig doidManualConfig;

    DoidModel(@NotNull final Multimap<String, String> childToParentsMap, @NotNull final Map<String, String> termPerDoidMap,
            @NotNull final Map<String, String> doidPerLowerCaseTermMap, @NotNull final DoidManualConfig doidManualConfig) {
        this.childToParentsMap = childToParentsMap;
        this.termPerDoidMap = termPerDoidMap;
        this.doidPerLowerCaseTermMap = doidPerLowerCaseTermMap;
        this.doidManualConfig = doidManualConfig;
    }

    @NotNull
    @VisibleForTesting
    Multimap<String, String> childToParentsMap() {
        return childToParentsMap;
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
        Set<String> expandedDoids = Sets.newHashSet();
        for (String expandedDoid : expandedWithAllParents(doid)) {
            expandedDoids.add(expandedDoid);
            String additionalDoid = doidManualConfig.additionalDoidsPerDoid().get(expandedDoid);
            if (additionalDoid != null) {
                expandedDoids.addAll(expandedWithAllParents(additionalDoid));
            }
        }

        return expandedDoids;
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

    @NotNull
    public Set<AdenoSquamousMapping> adenoSquamousMappingsForDoid(@NotNull String doidToFind) {
        Set<AdenoSquamousMapping> mappings = Sets.newHashSet();

        for (String doid : doidWithParents(doidToFind)) {
            for (AdenoSquamousMapping mapping : doidManualConfig.adenoSquamousMappings()) {
                if (mapping.adenoDoid().equals(doid) || mapping.squamousDoid().equals(doid)) {
                    mappings.add(mapping);
                }
            }
        }

        return mappings;
    }

    @Nullable
    public String resolveTermForDoid(@NotNull String doid) {
        return termPerDoidMap.get(doid);
    }

    @Nullable
    public String resolveDoidForTerm(@NotNull String term) {
        return doidPerLowerCaseTermMap.get(term.toLowerCase());
    }

    @NotNull
    private Set<String> expandedWithAllParents(@NotNull String doid) {
        Set<String> doids = Sets.newHashSet(doid);
        addParents(doid, doids);
        return doids;
    }

    private void addParents(@NotNull String child, @NotNull Set<String> result) {
        if (!childToParentsMap.containsKey(child)) {
            return;
        }

        for (String parent : childToParentsMap.get(child)) {
            if (result.add(parent)) {
                addParents(parent, result);
            }
        }
    }
}
