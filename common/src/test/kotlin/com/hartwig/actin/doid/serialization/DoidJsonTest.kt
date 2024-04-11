package com.hartwig.actin.doid.serialization

import com.hartwig.actin.doid.datamodel.BasicPropertyValue
import com.hartwig.actin.doid.datamodel.Definition
import com.hartwig.actin.doid.datamodel.DoidEntry
import com.hartwig.actin.doid.datamodel.Edge
import com.hartwig.actin.doid.datamodel.Node
import com.hartwig.actin.doid.datamodel.Synonym
import com.hartwig.actin.doid.datamodel.Xref
import com.hartwig.actin.testutil.ResourceLocator.resourceOnClasspath
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class DoidJsonTest {

    private val doidExampleFileJson = resourceOnClasspath("doid/example_doid.json")

    @Test
    fun `Should extract doid from URL`() {
        assertThat(DoidJson.extractDoid(DoidJson.DOID_URL_PREFIX + "300")).isEqualTo("300")
    }

    @Test
    fun `Should extract snomed concept ID`() {
        assertThat(DoidJson.extractSnomedConceptId(listOf(Xref("SNOMEDCT_US_2020_03_01:109355002")))).isEqualTo("109355002")
        assertThat(DoidJson.extractSnomedConceptId(listOf(Xref("ACTIN:1")))).isNull()
        assertThat(DoidJson.extractSnomedConceptId(listOf(Xref("SNOMED found!")))).isNull()
    }

    @Test
    fun `Should read test DOID JSON file`() {
        val entry: DoidEntry = DoidJson.readDoidOwlEntry(doidExampleFileJson)
        assertThat(entry.id).isEqualTo(DoidJson.ID_TO_READ)
        assertNodes(entry.nodes)
        assertEdges(entry.edges)
        assertThat(entry.metadata.subsets).isEmpty()
        assertThat(entry.metadata.xrefs).isEmpty()
        assertThat(entry.metadata.basicPropertyValues).isEmpty()
        assertThat(entry.equivalentNodesSets).isEmpty()
        assertThat(entry.logicalDefinitionAxioms).isEmpty()
        assertThat(entry.domainRangeAxioms).isEmpty()
        assertThat(entry.propertyChainAxioms).isEmpty()
    }

    private fun assertNodes(nodes: List<Node>) {
        assertThat(nodes).hasSize(2)
        assertNode1(nodes[0])
        assertNode2(nodes[1])
    }

    private fun assertNode1(node1: Node) {
        assertThat(node1.doid).isEqualTo("8718")
        assertThat(node1.url).isEqualTo("http://purl.obolibrary.org/obo/DOID_8718")
        assertThat(node1.term).isEqualTo("obsolete carcinoma in situ of respiratory system")
        assertThat(node1.type).isEqualTo("CLASS")

        val metadata = node1.metadata!!
        val definition = metadata.definition
        assertThat(definition!!.`val`).isEqualTo(
            "A carcinoma in situ that is characterized by the spread of cancer in the respiratory "
                    + "system and the lack of invasion of surrounding tissues."
        )
        assertThat(definition.xrefs).containsExactly("url:http://en.wikipedia.org/wiki/Carcinoma_in_situ")

        assertThat(metadata.synonyms).containsExactly(
            Synonym(
                "hasExactSynonym", "carcinoma in situ of respiratory tract (disorder)", emptyList()
            )
        )

        assertThat(metadata.basicPropertyValues).containsExactly(
            BasicPropertyValue("http://www.geneontology.org/formats/oboInOwl#hasAlternativeId", "DOID:8965"),
            BasicPropertyValue("http://www.w3.org/2002/07/owl#deprecated", "true"),
            BasicPropertyValue("http://www.geneontology.org/formats/oboInOwl#hasOBONamespace", "disease_ontology")
        )
    }

    private fun assertNode2(node2: Node) {
        assertThat(node2.doid).isEqualTo("8717")
        assertThat(node2.url).isEqualTo("http://purl.obolibrary.org/obo/DOID_8717")
        assertThat(node2.term).isEqualTo("decubitus ulcer")
        assertThat(node2.type).isEqualTo("CLASS")

        val metadata = node2.metadata!!
        assertThat(metadata.definition).isEqualTo(
            Definition(
                "Decubitus ulcer is a chronic ulcer of skin where the ulcer is an ulceration of "
                        + "tissue deprived of adequate blood supply by prolonged pressure.",
                listOf("url:http://www2.merriam-webster.com/cgi-bin/mwmednlm?book=Medical&va=bedsore")
            )
        )

        assertThat(metadata.subsets).containsExactly("http://purl.obolibrary.org/obo/doid#NCIthesaurus")
        assertThat(metadata.xrefs).containsExactly(
            Xref("NCI:C50706"),
            Xref("MESH:D003668"),
            Xref("ICD9CM:707.0"),
            Xref("UMLS_CUI:C0011127"),
            Xref("SNOMEDCT_US_2020_03_01:28103007"),
            Xref("ICD10CM:L89")
        )

        assertThat(metadata.synonyms).containsExactly(
            Synonym("hasExactSynonym", "Decubitus ulcer any site", emptyList()),
            Synonym("hasExactSynonym", "pressure ulcer", emptyList()),
            Synonym("hasExactSynonym", "pressure sores", emptyList()),
            Synonym("hasExactSynonym", "Decubitus (pressure) ulcer", emptyList()),
            Synonym("hasRelatedSynonym", "bedsore", emptyList())
        )

        assertThat(metadata.basicPropertyValues).containsExactly(
            BasicPropertyValue("http://www.geneontology.org/formats/oboInOwl#hasAlternativeId", "DOID:8808"),
            BasicPropertyValue("http://www.geneontology.org/formats/oboInOwl#hasAlternativeId", "DOID:9129"),
            BasicPropertyValue("http://www.geneontology.org/formats/oboInOwl#hasOBONamespace", "disease_ontology"),
            BasicPropertyValue("http://www.geneontology.org/formats/oboInOwl#hasAlternativeId", "DOID:9029"),
            BasicPropertyValue("http://www.geneontology.org/formats/oboInOwl#hasAlternativeId", "DOID:9002")
        )
    }

    private fun assertEdges(edges: List<Edge>) {
        assertThat(edges).hasSize(9)
        val edge1 = edges[0]
        assertThat(edge1.subject).isEqualTo("http://purl.obolibrary.org/obo/DOID_8717")
        assertThat(edge1.subjectDoid).isEqualTo("8717")
        assertThat(edge1.`object`).isEqualTo("http://purl.obolibrary.org/obo/DOID_8549")
        assertThat(edge1.objectDoid).isEqualTo("8549")
        assertThat(edge1.predicate).isEqualTo("is_a")
        val edge2 = edges[1]
        assertThat(edge2.subject).isEqualTo("http://purl.obolibrary.org/obo/CHEBI_50906")
        assertThat(edge2.subjectDoid).isEqualTo("")
        assertThat(edge2.predicate).isEqualTo("is_a")
        assertThat(edge2.`object`).isEqualTo("http://purl.obolibrary.org/obo/doid#chebi")
        assertThat(edge2.objectDoid).isEqualTo("")
    }
}