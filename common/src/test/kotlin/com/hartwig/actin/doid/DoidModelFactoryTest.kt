package com.hartwig.actin.doid

import com.hartwig.actin.doid.datamodel.Edge
import com.hartwig.actin.doid.datamodel.Node
import com.hartwig.actin.doid.datamodel.TestDoidEntryFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class DoidModelFactoryTest {

    @Test
    fun `Should generate child-to-parents map from doid entry, excluding containment or blacklisted edges`() {
        assertThat(createDoidModel().childToParentsMap).isEqualTo(
            mapOf(
                "200" to listOf("300"),
                "300" to listOf("400", "600")
            )
        )
    }

    @Test
    fun `Should generate mappings between doids and terms, excluding doids with unknown terms`() {
        val model = createDoidModel()
        assertThat(model.termForDoidMap).isEqualTo(mapOf("200" to "tumor A"))
        assertThat(model.doidForLowerCaseTermMap).isEqualTo(mapOf("tumor a" to "200"))
    }

    private fun createDoidModel(): DoidModel {
        val edges = listOf(
            createParentChildEdge("200", "300"),
            createParentChildEdge("300", "400"),
            createParentChildEdge("235", "1475"), // excluded by manual config
            createParentChildEdge("300", "600"),
            createContainmentEdge("400", "500")
        )
        val nodes = listOf(
            createNode("200", "tumor A"),
            createNode("300", null)
        )
        val entry = TestDoidEntryFactory.createMinimalTestDoidEntry().copy(edges = edges, nodes = nodes)
        return DoidModelFactory.createFromDoidEntry(entry)
    }

    private fun createNode(doid: String, term: String?): Node {
        return Node(doid = doid, url = "", term = term, metadata = null, type = null)
    }

    private fun createParentChildEdge(child: String, parent: String): Edge {
        return createEdge(child, "is_a", parent)
    }

    private fun createContainmentEdge(child: String, parent: String): Edge {
        return createEdge(child, "has_a", parent)
    }

    private fun createEdge(subjectDoid: String, pred: String, objectDoid: String): Edge {
        return Edge(
            subject = "",
            subjectDoid = subjectDoid,
            `object` = "",
            objectDoid = objectDoid,
            predicate = pred
        )
    }
}