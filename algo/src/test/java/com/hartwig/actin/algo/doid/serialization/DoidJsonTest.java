package com.hartwig.actin.algo.doid.serialization;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.List;

import com.google.common.collect.Lists;
import com.google.common.io.Resources;
import com.hartwig.actin.algo.doid.datamodel.BasicPropertyValue;
import com.hartwig.actin.algo.doid.datamodel.Definition;
import com.hartwig.actin.algo.doid.datamodel.Edge;
import com.hartwig.actin.algo.doid.datamodel.Entry;
import com.hartwig.actin.algo.doid.datamodel.ImmutableXref;
import com.hartwig.actin.algo.doid.datamodel.Node;
import com.hartwig.actin.algo.doid.datamodel.Synonym;
import com.hartwig.actin.algo.doid.datamodel.Xref;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class DoidJsonTest {

    private static final String DOID_EXAMPLE_FILE_JSON = Resources.getResource("doid/example_doid.json").getPath();

    @Test
    public void canExtractDoidFromUrl() {
        String url = "http://purl.obolibrary.org/obo/DOID_345";
        assertEquals("345", DoidJson.extractDoid(url));
    }

    @Test
    public void canExtractSnomedConceptID() {
        Xref xref = ImmutableXref.builder().val("SNOMEDCT_US_2020_03_01:109355002").build();
        assertEquals("109355002", DoidJson.extractSnomedConceptId(Lists.newArrayList(xref)));

        Xref differentId = ImmutableXref.builder().val("ACTIN:1").build();
        assertNull(DoidJson.extractSnomedConceptId(Lists.newArrayList(differentId)));

        Xref wrongFormat = ImmutableXref.builder().val("SNOMED found!").build();
        assertNull(DoidJson.extractSnomedConceptId(Lists.newArrayList(wrongFormat)));
    }

    @Test
    public void canReadDoidJsonFile() throws IOException {
        Entry entry = DoidJson.readDoidOwlEntryFromDoidJson(DOID_EXAMPLE_FILE_JSON);

        assertEquals(DoidJson.ID_TO_READ, entry.id());

        assertNodes(entry.nodes());
        assertEdges(entry.edges());

        assertTrue(entry.metadata().subsets().isEmpty());
        assertTrue(entry.metadata().xrefs().isEmpty());
        assertTrue(entry.metadata().basicPropertyValues().isEmpty());
        assertTrue(entry.equivalentNodesSets().isEmpty());
        assertTrue(entry.logicalDefinitionAxioms().isEmpty());
        assertTrue(entry.domainRangeAxioms().isEmpty());
        assertTrue(entry.propertyChainAxioms().isEmpty());
    }

    private static void assertNodes(@NotNull List<Node> nodes) {
        assertEquals(2, nodes.size());

        assertNode1(nodes.get(0));
        assertNode2(nodes.get(1));
    }

    private static void assertNode1(@NotNull Node node1) {
        assertEquals("8718", node1.doid());
        assertEquals("http://purl.obolibrary.org/obo/DOID_8718", node1.url());
        assertEquals("obsolete carcinoma in situ of respiratory system", node1.term());
        assertEquals("CLASS", node1.type());

        Definition definition = node1.metadata().definition();
        assertEquals("A carcinoma in situ that is characterized by the spread of cancer in the respiratory "
                + "system and the lack of invasion of surrounding tissues.", definition.definitionVal());
        assertEquals(Lists.newArrayList("url:http://en.wikipedia.org/wiki/Carcinoma_in_situ"), definition.definitionXrefs());

        Synonym synonym = node1.metadata().synonyms().get(0);
        assertEquals("hasExactSynonym", synonym.pred());
        assertEquals("carcinoma in situ of respiratory tract (disorder)", synonym.val());
        assertTrue(synonym.xrefs().isEmpty());

        BasicPropertyValue basicPropertyValue1 = node1.metadata().basicPropertyValues().get(0);
        assertEquals("http://www.geneontology.org/formats/oboInOwl#hasAlternativeId", basicPropertyValue1.pred());
        assertEquals("DOID:8965", basicPropertyValue1.val());

        BasicPropertyValue basicPropertyValue2 = node1.metadata().basicPropertyValues().get(1);
        assertEquals("http://www.w3.org/2002/07/owl#deprecated", basicPropertyValue2.pred());
        assertEquals("true", basicPropertyValue2.val());

        BasicPropertyValue basicPropertyValue3 = node1.metadata().basicPropertyValues().get(2);
        assertEquals("http://www.geneontology.org/formats/oboInOwl#hasOBONamespace", basicPropertyValue3.pred());
        assertEquals("disease_ontology", basicPropertyValue3.val());
    }

    private static void assertNode2(@NotNull Node node2) {
        assertEquals("8717", node2.doid());
        assertEquals("http://purl.obolibrary.org/obo/DOID_8717", node2.url());
        assertEquals("decubitus ulcer", node2.term());
        assertEquals("CLASS", node2.type());

        Definition definition = node2.metadata().definition();
        assertEquals("Decubitus ulcer is a chronic ulcer of skin where the ulcer is an ulceration of "
                + "tissue deprived of adequate blood supply by prolonged pressure.", definition.definitionVal());
        assertEquals(Lists.newArrayList("url:http://www2.merriam-webster.com/cgi-bin/mwmednlm?book=Medical&va=bedsore"),
                definition.definitionXrefs());

        List<String> subset = node2.metadata().subsets();
        assertEquals(Lists.newArrayList("http://purl.obolibrary.org/obo/doid#NCIthesaurus"), subset);

        List<Xref> xrefs = node2.metadata().xrefs();
        assertEquals(ImmutableXref.builder().val("NCI:C50706").build(), xrefs.get(0));
        assertEquals(ImmutableXref.builder().val("MESH:D003668").build(), xrefs.get(1));
        assertEquals(ImmutableXref.builder().val("ICD9CM:707.0").build(), xrefs.get(2));
        assertEquals(ImmutableXref.builder().val("UMLS_CUI:C0011127").build(), xrefs.get(3));
        assertEquals(ImmutableXref.builder().val("SNOMEDCT_US_2020_03_01:28103007").build(), xrefs.get(4));
        assertEquals(ImmutableXref.builder().val("ICD10CM:L89").build(), xrefs.get(5));

        Synonym synonym1 = node2.metadata().synonyms().get(0);
        assertEquals("hasExactSynonym", synonym1.pred());
        assertEquals("Decubitus ulcer any site", synonym1.val());
        assertEquals(Lists.newArrayList(), synonym1.xrefs());

        Synonym synonym2 = node2.metadata().synonyms().get(1);
        assertEquals("hasExactSynonym", synonym2.pred());
        assertEquals("pressure ulcer", synonym2.val());
        assertTrue(synonym2.xrefs().isEmpty());

        Synonym synonym3 = node2.metadata().synonyms().get(2);
        assertEquals("hasExactSynonym", synonym3.pred());
        assertEquals("pressure sores", synonym3.val());
        assertTrue(synonym3.xrefs().isEmpty());

        Synonym synonym4 = node2.metadata().synonyms().get(3);
        assertEquals("hasExactSynonym", synonym4.pred());
        assertEquals("Decubitus (pressure) ulcer", synonym4.val());
        assertTrue(synonym4.xrefs().isEmpty());

        Synonym synonym5 = node2.metadata().synonyms().get(4);
        assertEquals("hasRelatedSynonym", synonym5.pred());
        assertEquals("bedsore", synonym5.val());
        assertTrue(synonym5.xrefs().isEmpty());

        BasicPropertyValue basicPropertyValue1 = node2.metadata().basicPropertyValues().get(0);
        assertEquals("http://www.geneontology.org/formats/oboInOwl#hasAlternativeId", basicPropertyValue1.pred());
        assertEquals("DOID:8808", basicPropertyValue1.val());

        BasicPropertyValue basicPropertyValue2 = node2.metadata().basicPropertyValues().get(1);
        assertEquals("http://www.geneontology.org/formats/oboInOwl#hasAlternativeId", basicPropertyValue2.pred());
        assertEquals("DOID:9129", basicPropertyValue2.val());

        BasicPropertyValue basicPropertyValue3 = node2.metadata().basicPropertyValues().get(2);
        assertEquals("http://www.geneontology.org/formats/oboInOwl#hasOBONamespace", basicPropertyValue3.pred());
        assertEquals("disease_ontology", basicPropertyValue3.val());

        BasicPropertyValue basicPropertyValue4 = node2.metadata().basicPropertyValues().get(3);
        assertEquals("http://www.geneontology.org/formats/oboInOwl#hasAlternativeId", basicPropertyValue4.pred());
        assertEquals("DOID:9029", basicPropertyValue4.val());

        BasicPropertyValue basicPropertyValue5 = node2.metadata().basicPropertyValues().get(4);
        assertEquals("http://www.geneontology.org/formats/oboInOwl#hasAlternativeId", basicPropertyValue5.pred());
        assertEquals("DOID:9002", basicPropertyValue5.val());
    }

    private static void assertEdges(@NotNull List<Edge> edges) {
        assertEquals(9, edges.size());

        Edge edge1 = edges.get(0);
        assertEquals("http://purl.obolibrary.org/obo/DOID_8717", edge1.subject());
        assertEquals("is_a", edge1.predicate());
        assertEquals("http://purl.obolibrary.org/obo/DOID_8549", edge1.object());

        Edge edge2 = edges.get(1);
        assertEquals("http://purl.obolibrary.org/obo/CHEBI_50906", edge2.subject());
        assertEquals("is_a", edge2.predicate());
        assertEquals("http://purl.obolibrary.org/obo/doid#chebi", edge2.object());
    }
}