package com.hartwig.actin.doid.serialization

import com.google.common.collect.Lists
import com.google.common.io.Resources
import com.hartwig.actin.doid.datamodel.Edge
import com.hartwig.actin.doid.datamodel.ImmutableXref
import com.hartwig.actin.doid.datamodel.Node
import org.apache.logging.log4j.util.Strings
import org.junit.Assert
import org.junit.Test
import java.io.IOException

class DoidJsonTest {
    @Test
    fun canExtractDoidFromUrl() {
        val url = DoidJson.DOID_URL_PREFIX + "300"
        Assert.assertEquals("300", DoidJson.extractDoid(url))
    }

    @Test
    fun canExtractSnomedConceptID() {
        val xref: Xref = ImmutableXref.builder().`val`("SNOMEDCT_US_2020_03_01:109355002").build()
        Assert.assertEquals("109355002", DoidJson.extractSnomedConceptId(Lists.newArrayList<Xref>(xref)))
        val differentId: Xref = ImmutableXref.builder().`val`("ACTIN:1").build()
        Assert.assertNull(DoidJson.extractSnomedConceptId(Lists.newArrayList<Xref>(differentId)))
        val wrongFormat: Xref = ImmutableXref.builder().`val`("SNOMED found!").build()
        Assert.assertNull(DoidJson.extractSnomedConceptId(Lists.newArrayList<Xref>(wrongFormat)))
    }

    @Test
    @Throws(IOException::class)
    fun canReadDoidJsonFile() {
        val entry: DoidEntry = DoidJson.readDoidOwlEntry(DOID_EXAMPLE_FILE_JSON)
        Assert.assertEquals(DoidJson.ID_TO_READ, entry.id())
        assertNodes(entry.nodes())
        assertEdges(entry.edges())
        Assert.assertTrue(entry.metadata().subsets().isEmpty())
        Assert.assertTrue(entry.metadata().xrefs().isEmpty())
        Assert.assertTrue(entry.metadata().basicPropertyValues().isEmpty())
        Assert.assertTrue(entry.equivalentNodesSets().isEmpty())
        Assert.assertTrue(entry.logicalDefinitionAxioms().isEmpty())
        Assert.assertTrue(entry.domainRangeAxioms().isEmpty())
        Assert.assertTrue(entry.propertyChainAxioms().isEmpty())
    }

    companion object {
        private val DOID_EXAMPLE_FILE_JSON = Resources.getResource("doid/example_doid.json").path
        private fun assertNodes(nodes: List<Node>) {
            Assert.assertEquals(2, nodes.size.toLong())
            assertNode1(nodes[0])
            assertNode2(nodes[1])
        }

        private fun assertNode1(node1: Node) {
            Assert.assertEquals("8718", node1.doid())
            Assert.assertEquals("http://purl.obolibrary.org/obo/DOID_8718", node1.url())
            Assert.assertEquals("obsolete carcinoma in situ of respiratory system", node1.term())
            Assert.assertEquals("CLASS", node1.type())
            val definition = node1.metadata()!!.definition()
            Assert.assertEquals(
                "A carcinoma in situ that is characterized by the spread of cancer in the respiratory "
                        + "system and the lack of invasion of surrounding tissues.", definition!!.`val`()
            )
            Assert.assertEquals(Lists.newArrayList("url:http://en.wikipedia.org/wiki/Carcinoma_in_situ"), definition.xrefs())
            val synonym: Synonym = node1.metadata()!!.synonyms()!![0]
            Assert.assertEquals("hasExactSynonym", synonym.pred())
            Assert.assertEquals("carcinoma in situ of respiratory tract (disorder)", synonym.`val`())
            Assert.assertTrue(synonym.xrefs().isEmpty())
            val basicPropertyValue1: BasicPropertyValue = node1.metadata()!!.basicPropertyValues()!![0]
            Assert.assertEquals("http://www.geneontology.org/formats/oboInOwl#hasAlternativeId", basicPropertyValue1.pred())
            Assert.assertEquals("DOID:8965", basicPropertyValue1.`val`())
            val basicPropertyValue2: BasicPropertyValue = node1.metadata()!!.basicPropertyValues()!![1]
            Assert.assertEquals("http://www.w3.org/2002/07/owl#deprecated", basicPropertyValue2.pred())
            Assert.assertEquals("true", basicPropertyValue2.`val`())
            val basicPropertyValue3: BasicPropertyValue = node1.metadata()!!.basicPropertyValues()!![2]
            Assert.assertEquals("http://www.geneontology.org/formats/oboInOwl#hasOBONamespace", basicPropertyValue3.pred())
            Assert.assertEquals("disease_ontology", basicPropertyValue3.`val`())
        }

        private fun assertNode2(node2: Node) {
            Assert.assertEquals("8717", node2.doid())
            Assert.assertEquals("http://purl.obolibrary.org/obo/DOID_8717", node2.url())
            Assert.assertEquals("decubitus ulcer", node2.term())
            Assert.assertEquals("CLASS", node2.type())
            val definition = node2.metadata()!!.definition()
            Assert.assertEquals(
                "Decubitus ulcer is a chronic ulcer of skin where the ulcer is an ulceration of "
                        + "tissue deprived of adequate blood supply by prolonged pressure.", definition!!.`val`()
            )
            Assert.assertEquals(
                Lists.newArrayList("url:http://www2.merriam-webster.com/cgi-bin/mwmednlm?book=Medical&va=bedsore"),
                definition.xrefs()
            )
            val subset = node2.metadata()!!.subsets()
            Assert.assertEquals(Lists.newArrayList("http://purl.obolibrary.org/obo/doid#NCIthesaurus"), subset)
            val xrefs: List<Xref?>? = node2.metadata()!!.xrefs()
            Assert.assertEquals(ImmutableXref.builder().`val`("NCI:C50706").build(), xrefs!![0])
            Assert.assertEquals(ImmutableXref.builder().`val`("MESH:D003668").build(), xrefs!![1])
            Assert.assertEquals(ImmutableXref.builder().`val`("ICD9CM:707.0").build(), xrefs!![2])
            Assert.assertEquals(ImmutableXref.builder().`val`("UMLS_CUI:C0011127").build(), xrefs!![3])
            Assert.assertEquals(ImmutableXref.builder().`val`("SNOMEDCT_US_2020_03_01:28103007").build(), xrefs!![4])
            Assert.assertEquals(ImmutableXref.builder().`val`("ICD10CM:L89").build(), xrefs!![5])
            val synonym1: Synonym = node2.metadata()!!.synonyms()!![0]
            Assert.assertEquals("hasExactSynonym", synonym1.pred())
            Assert.assertEquals("Decubitus ulcer any site", synonym1.`val`())
            Assert.assertEquals(Lists.newArrayList<Any?>(), synonym1.xrefs())
            val synonym2: Synonym = node2.metadata()!!.synonyms()!![1]
            Assert.assertEquals("hasExactSynonym", synonym2.pred())
            Assert.assertEquals("pressure ulcer", synonym2.`val`())
            Assert.assertTrue(synonym2.xrefs().isEmpty())
            val synonym3: Synonym = node2.metadata()!!.synonyms()!![2]
            Assert.assertEquals("hasExactSynonym", synonym3.pred())
            Assert.assertEquals("pressure sores", synonym3.`val`())
            Assert.assertTrue(synonym3.xrefs().isEmpty())
            val synonym4: Synonym = node2.metadata()!!.synonyms()!![3]
            Assert.assertEquals("hasExactSynonym", synonym4.pred())
            Assert.assertEquals("Decubitus (pressure) ulcer", synonym4.`val`())
            Assert.assertTrue(synonym4.xrefs().isEmpty())
            val synonym5: Synonym = node2.metadata()!!.synonyms()!![4]
            Assert.assertEquals("hasRelatedSynonym", synonym5.pred())
            Assert.assertEquals("bedsore", synonym5.`val`())
            Assert.assertTrue(synonym5.xrefs().isEmpty())
            val basicPropertyValue1: BasicPropertyValue = node2.metadata()!!.basicPropertyValues()!![0]
            Assert.assertEquals("http://www.geneontology.org/formats/oboInOwl#hasAlternativeId", basicPropertyValue1.pred())
            Assert.assertEquals("DOID:8808", basicPropertyValue1.`val`())
            val basicPropertyValue2: BasicPropertyValue = node2.metadata()!!.basicPropertyValues()!![1]
            Assert.assertEquals("http://www.geneontology.org/formats/oboInOwl#hasAlternativeId", basicPropertyValue2.pred())
            Assert.assertEquals("DOID:9129", basicPropertyValue2.`val`())
            val basicPropertyValue3: BasicPropertyValue = node2.metadata()!!.basicPropertyValues()!![2]
            Assert.assertEquals("http://www.geneontology.org/formats/oboInOwl#hasOBONamespace", basicPropertyValue3.pred())
            Assert.assertEquals("disease_ontology", basicPropertyValue3.`val`())
            val basicPropertyValue4: BasicPropertyValue = node2.metadata()!!.basicPropertyValues()!![3]
            Assert.assertEquals("http://www.geneontology.org/formats/oboInOwl#hasAlternativeId", basicPropertyValue4.pred())
            Assert.assertEquals("DOID:9029", basicPropertyValue4.`val`())
            val basicPropertyValue5: BasicPropertyValue = node2.metadata()!!.basicPropertyValues()!![4]
            Assert.assertEquals("http://www.geneontology.org/formats/oboInOwl#hasAlternativeId", basicPropertyValue5.pred())
            Assert.assertEquals("DOID:9002", basicPropertyValue5.`val`())
        }

        private fun assertEdges(edges: List<Edge>) {
            Assert.assertEquals(9, edges.size.toLong())
            val edge1 = edges[0]
            Assert.assertEquals("http://purl.obolibrary.org/obo/DOID_8717", edge1.subject())
            Assert.assertEquals("8717", edge1.subjectDoid())
            Assert.assertEquals("http://purl.obolibrary.org/obo/DOID_8549", edge1.`object`())
            Assert.assertEquals("8549", edge1.objectDoid())
            Assert.assertEquals("is_a", edge1.predicate())
            val edge2 = edges[1]
            Assert.assertEquals("http://purl.obolibrary.org/obo/CHEBI_50906", edge2.subject())
            Assert.assertEquals(Strings.EMPTY, edge2.subjectDoid())
            Assert.assertEquals("is_a", edge2.predicate())
            Assert.assertEquals("http://purl.obolibrary.org/obo/doid#chebi", edge2.`object`())
            Assert.assertEquals(Strings.EMPTY, edge2.objectDoid())
        }
    }
}