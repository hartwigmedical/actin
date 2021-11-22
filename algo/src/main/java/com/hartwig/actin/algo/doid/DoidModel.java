package com.hartwig.actin.algo.doid;

import java.util.Set;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

import org.jetbrains.annotations.NotNull;

public class DoidModel {

    @NotNull
    private final Multimap<String, String> relationship;

    DoidModel(@NotNull final Multimap<String, String> relationship) {
        this.relationship = relationship;
    }

    @NotNull
    @VisibleForTesting
    Multimap<String, String> relationship() {
        return relationship;
    }

    @NotNull
    public Set<String> doidWithParents(@NotNull String doid) {
        Set<String> doids = Sets.newHashSet(doid);
        addParents(doid, doids);
        return doids;
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
}
