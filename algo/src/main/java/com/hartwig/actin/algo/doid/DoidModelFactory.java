package com.hartwig.actin.algo.doid;

import java.util.Collection;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.hartwig.actin.algo.doid.datamodel.DoidEntry;
import com.hartwig.actin.algo.doid.datamodel.Edge;

import org.jetbrains.annotations.NotNull;

public final class DoidModelFactory {

    private DoidModelFactory() {
    }

    @NotNull
    public static DoidModel createFromEntry(@NotNull DoidEntry entry) {
        Multimap<String, String> relationship = ArrayListMultimap.create();
        for (Edge edge : entry.edges()) {
            if (edge.predicate().equals("is_a")) {
                String child = edge.subjectDoid();
                String parent = edge.objectDoid();

                if (relationship.containsKey(child)) {
                    Collection<String> parents = relationship.get(child);
                    if (!parents.contains(parent)) {
                        parents.add(parent);
                    }
                } else {
                    relationship.put(child, parent);
                }
            }
        }

        return new DoidModel(relationship);
    }
}
