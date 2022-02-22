package com.hartwig.actin.algo.doid;

import java.util.Collection;
import java.util.Map;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.hartwig.actin.algo.doid.datamodel.DoidEntry;
import com.hartwig.actin.algo.doid.datamodel.Edge;
import com.hartwig.actin.algo.doid.datamodel.Node;

import org.jetbrains.annotations.NotNull;

public final class DoidModelFactory {

    private DoidModelFactory() {
    }

    @NotNull
    public static DoidModel createFromDoidEntry(@NotNull DoidEntry doidEntry) {
        Multimap<String, String> relationship = ArrayListMultimap.create();
        for (Edge edge : doidEntry.edges()) {
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

        Map<String, String> termsPerDoid = Maps.newHashMap();
        for (Node node : doidEntry.nodes()) {
            String term = node.term();
            if (term != null) {
                termsPerDoid.put(node.doid(), term);
            }
        }

        return new DoidModel(relationship, termsPerDoid);
    }
}
