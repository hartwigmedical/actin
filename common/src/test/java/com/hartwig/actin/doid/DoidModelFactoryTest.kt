package com.hartwig.actin.doid

import com.google.common.collect.Lists
import com.hartwig.actin.doid.datamodel.Edge
import com.hartwig.actin.doid.datamodel.ImmutableDoidEntry
import com.hartwig.actin.doid.datamodel.Node
import org.apache.logging.log4j.util.Strings
import org.junit.Assert
import org.junit.Test

class DoidModelFactoryTest {
    @Test
    fun canGenerateModelFromDoidEntry() {
        val edges: MutableList<Edge> = Lists.newArrayList()
        edges.add(createParentChildEdge("200", "300"))
        edges.add(createParentChildEdge("300", "400"))
        edges.add(createContainmentEdge("400", "500"))
        val nodes: MutableList<Node> = Lists.newArrayList()
        nodes.add(createNode("200", "tumor A"))
        nodes.add(createNode("300", null))
        val entry: DoidEntry =
            ImmutableDoidEntry.builder().from(TestDoidEntryFactory.createMinimalTestDoidEntry()).edges(edges).nodes(nodes).build()
        val model = DoidModelFactory.createFromDoidEntry(entry)
        Assert.assertEquals(2, model.childToParentsMap().size().toLong())
        val relations299 = model.childToParentsMap()["200"]
        Assert.assertEquals(1, relations299.size.toLong())
        Assert.assertTrue(relations299.contains("300"))
        val relations305 = model.childToParentsMap()["300"]
        Assert.assertEquals(1, relations305.size.toLong())
        Assert.assertTrue(relations305.contains("400"))
        Assert.assertEquals(1, model.termForDoidMap().size.toLong())
        Assert.assertEquals("tumor A", model.termForDoidMap()["200"])
        Assert.assertNull(model.termForDoidMap()["300"])
        Assert.assertEquals(1, model.doidForLowerCaseTermMap().size.toLong())
        Assert.assertEquals("200", model.doidForLowerCaseTermMap()["tumor a"])
        Assert.assertNull(model.doidForLowerCaseTermMap()["tumor b"])
    }

    companion object {
        private fun createNode(doid: String, term: String?): Node {
            return ImmutableNode.builder().doid(doid).url(Strings.EMPTY).term(term).build()
        }

        private fun createParentChildEdge(child: String, parent: String): Edge {
            return createEdge(child, "is_a", parent)
        }

        private fun createContainmentEdge(child: String, parent: String): Edge {
            return createEdge(child, "has_a", parent)
        }

        private fun createEdge(subjectDoid: String, pred: String, objectDoid: String): Edge {
            return ImmutableEdge.builder()
                .subject(Strings.EMPTY)
                .subjectDoid(subjectDoid)
                .`object`(Strings.EMPTY)
                .objectDoid(objectDoid)
                .predicate(pred)
                .build()
        }
    }
}