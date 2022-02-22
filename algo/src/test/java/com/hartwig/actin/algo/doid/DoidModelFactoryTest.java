package com.hartwig.actin.algo.doid;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.algo.doid.datamodel.DoidEntry;
import com.hartwig.actin.algo.doid.datamodel.Edge;
import com.hartwig.actin.algo.doid.datamodel.ImmutableDoidEntry;
import com.hartwig.actin.algo.doid.datamodel.ImmutableEdge;
import com.hartwig.actin.algo.doid.datamodel.ImmutableNode;
import com.hartwig.actin.algo.doid.datamodel.Node;
import com.hartwig.actin.algo.doid.datamodel.TestDoidEntryFactory;

import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.Test;

public class DoidModelFactoryTest {

    @Test
    public void canGenerateModelFromDoidEntry() {
        List<Edge> edges = Lists.newArrayList();
        edges.add(createParentChildEdge("200", "300"));
        edges.add(createParentChildEdge("300", "400"));
        edges.add(createContainmentEdge("400", "500"));

        List<Node> nodes = Lists.newArrayList();
        nodes.add(createNode("200", "tumor A"));
        nodes.add(createNode("300", null));

        DoidEntry entry =
                ImmutableDoidEntry.builder().from(TestDoidEntryFactory.createMinimalTestDoidEntry()).edges(edges).nodes(nodes).build();
        DoidModel model = DoidModelFactory.createFromDoidEntry(entry);

        assertEquals(2, model.relationship().size());

        Collection<String> relations299 = model.relationship().get("200");
        assertEquals(1, relations299.size());
        assertTrue(relations299.contains("300"));

        Collection<String> relations305 = model.relationship().get("300");
        assertEquals(1, relations305.size());
        assertTrue(relations305.contains("400"));

        assertEquals(1, model.termsForDoid().size());
        assertEquals("tumor A", model.termsForDoid().get("200"));
        assertNull(model.termsForDoid().get("300"));
    }

    @NotNull
    private static Node createNode(@NotNull String doid, @Nullable String term) {
        return ImmutableNode.builder().doid(doid).url(Strings.EMPTY).term(term).build();
    }

    @NotNull
    private static Edge createParentChildEdge(@NotNull String child, @NotNull String parent) {
        return createEdge(child, "is_a", parent);
    }

    @NotNull
    private static Edge createContainmentEdge(@NotNull String child, @NotNull String parent) {
        return createEdge(child, "has_a", parent);
    }

    @NotNull
    private static Edge createEdge(@NotNull String subjectDoid, @NotNull String pred, @NotNull String objectDoid) {
        return ImmutableEdge.builder()
                .subject(Strings.EMPTY)
                .subjectDoid(subjectDoid)
                .object(Strings.EMPTY)
                .objectDoid(objectDoid)
                .predicate(pred)
                .build();
    }
}