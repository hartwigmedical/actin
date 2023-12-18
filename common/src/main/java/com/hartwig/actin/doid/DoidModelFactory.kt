package com.hartwig.actin.doid;

import java.util.Collection;
import java.util.Map;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.hartwig.actin.doid.config.DoidManualConfigFactory;
import com.hartwig.actin.doid.datamodel.DoidEntry;
import com.hartwig.actin.doid.datamodel.Edge;
import com.hartwig.actin.doid.datamodel.Node;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

public final class DoidModelFactory {

    private static final Logger LOGGER = LogManager.getLogger(DoidModelFactory.class);

    private DoidModelFactory() {
    }

    @NotNull
    public static DoidModel createFromDoidEntry(@NotNull DoidEntry doidEntry) {
        Multimap<String, String> childToParentsMap = ArrayListMultimap.create();
        for (Edge edge : doidEntry.edges()) {
            if (edge.predicate().equals("is_a")) {
                String child = edge.subjectDoid();
                String parent = edge.objectDoid();

                if (childToParentsMap.containsKey(child)) {
                    Collection<String> parents = childToParentsMap.get(child);
                    if (!parents.contains(parent)) {
                        parents.add(parent);
                    }
                } else {
                    childToParentsMap.put(child, parent);
                }
            }
        }

        // Assume both doid and term are unique.
        Map<String, String> termPerDoidMap = Maps.newHashMap();
        Map<String, String> doidPerLowerCaseTermMap = Maps.newHashMap();
        for (Node node : doidEntry.nodes()) {
            String term = node.term();
            if (term != null) {
                termPerDoidMap.put(node.doid(), term);
                String lowerCaseTerm = term.toLowerCase();
                if (doidPerLowerCaseTermMap.containsKey(lowerCaseTerm)) {
                    LOGGER.warn("DOID term (in lower-case) is not unique: '{}'", term);
                } else {
                    doidPerLowerCaseTermMap.put(lowerCaseTerm, node.doid());
                }
            }
        }

        return new DoidModel(childToParentsMap, termPerDoidMap, doidPerLowerCaseTermMap, DoidManualConfigFactory.create());
    }
}
