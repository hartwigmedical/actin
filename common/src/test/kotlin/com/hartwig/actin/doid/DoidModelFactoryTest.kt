package com.hartwig.actin.doid

import com.hartwig.actin.doid.datamodel.Edge
import com.hartwig.actin.doid.datamodel.Node
import com.hartwig.actin.doid.datamodel.TestDoidEntryFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class DoidModelFactoryTest {

    @Test
    fun `Should generate model from doid entry`() {
        val edges = listOf(
            createParentChildEdge("200", "300"),
            createParentChildEdge("300", "400"),
            createContainmentEdge("400", "500")
        )
        val nodes = listOf(
            createNode("200", "tumor A"),
            createNode("300", null)
        )
        val entry = TestDoidEntryFactory.createMinimalTestDoidEntry().copy(edges = edges, nodes = nodes)
        val model = DoidModelFactory.createFromDoidEntry(entry)

        assertThat(model.childToParentsMap).hasSize(2)
        val relations299 = model.childToParentsMap["200"]
        assertThat(relations299).hasSize(1)
        assertThat(relations299).contains("300")
        val relations305 = model.childToParentsMap["300"]
        assertThat(relations305).hasSize(1)
        assertThat(relations305).contains("400")

        assertThat(model.termForDoidMap).hasSize(1)
        assertThat(model.termForDoidMap["200"]).isEqualTo("tumor A")
        assertThat(model.termForDoidMap["300"]).isNull()

        assertThat(model.doidForLowerCaseTermMap).hasSize(1)
        assertThat(model.doidForLowerCaseTermMap["tumor a"]).isEqualTo("200")
        assertThat(model.doidForLowerCaseTermMap["tumor b"]).isNull()
    }

    private fun createNode(doid: String, term: String?): Node {
        return Node(
            doid = doid,
            url = "",
            term = term,
            metadata = null,
            type = null
        )
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