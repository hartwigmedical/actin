package com.hartwig.actin.doid.serialization

import com.google.common.annotations.VisibleForTesting
import com.google.common.collect.Lists
import com.google.gson.JsonArray
import com.google.gson.JsonParser
import com.hartwig.actin.doid.datamodel.Definition
import com.hartwig.actin.doid.datamodel.Edge
import com.hartwig.actin.doid.datamodel.ImmutableBasicPropertyValue
import com.hartwig.actin.doid.datamodel.Metadata
import com.hartwig.actin.doid.datamodel.Node
import com.hartwig.actin.doid.datamodel.Restriction
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.util.Strings
import java.io.IOException

object DoidJson {
    private val LOGGER = LogManager.getLogger(DoidJson::class.java)

    @JvmField
    @VisibleForTesting
    val ID_TO_READ = "http://purl.obolibrary.org/obo/doid.owl"

    @JvmField
    @VisibleForTesting
    val DOID_URL_PREFIX = "http://purl.obolibrary.org/obo/DOID_"

    @JvmStatic
    @Throws(IOException::class)
    fun readDoidOwlEntry(doidJson: String): DoidEntry {
        val reader = JsonReader(FileReader(doidJson))
        reader.setLenient(true)
        val rootObject: JsonObject = JsonParser.parseReader(reader).getAsJsonObject()
        DatamodelCheckerFactory.rootObjectChecker().check(rootObject)
        var entry: DoidEntry? = null
        val graphsChecker: JsonDatamodelChecker = DatamodelCheckerFactory.graphsChecker()
        for (element in array(rootObject, "graphs")) {
            val graph: JsonObject = element.getAsJsonObject()
            graphsChecker.check(graph)
            val id: String = string(graph, "id")
            if (id == ID_TO_READ) {
                LOGGER.debug(" Reading DOID entry with ID '{}'", id)
                entry = ImmutableDoidEntry.builder()
                    .id(id)
                    .nodes(extractNodes(array(graph, "nodes")))
                    .edges(extractEdges(array(graph, "edges")))
                    .metadata(extractGraphMetadata(`object`(graph, "meta")))
                    .logicalDefinitionAxioms(extractLogicalDefinitionAxioms(array(graph, "logicalDefinitionAxioms")))
                    .equivalentNodesSets(optionalStringList(graph, "equivalentNodesSets"))
                    .domainRangeAxioms(optionalStringList(graph, "domainRangeAxioms"))
                    .propertyChainAxioms(optionalStringList(graph, "propertyChainAxioms"))
                    .build()
            }
        }
        if (reader.peek() != JsonToken.END_DOCUMENT) {
            LOGGER.warn("More data found in {} after reading main JSON object!", doidJson)
        }
        checkNotNull(entry) { "Could not read DOID entry with ID '$ID_TO_READ'" }
        return entry
    }

    @JvmStatic
    @VisibleForTesting
    fun extractDoid(url: String): String {
        return if (url.startsWith(DOID_URL_PREFIX)) url.substring(DOID_URL_PREFIX.length) else Strings.EMPTY
    }

    private fun extractNodes(nodeArray: JsonArray): List<Node> {
        val nodes: MutableList<Node> = Lists.newArrayList()
        val nodeChecker: JsonDatamodelChecker = DatamodelCheckerFactory.nodeChecker()
        for (nodeElement in nodeArray) {
            val node: JsonObject = nodeElement.asJsonObject
            nodeChecker.check(node)
            val id: String = string(node, "id")
            nodes.add(
                ImmutableNode.builder()
                    .doid(extractDoid(id))
                    .url(id)
                    .metadata(extractMetadata(optionalObject(node, "meta")))
                    .type(optionalString(node, "type"))
                    .term(optionalString(node, "lbl"))
                    .build()
            )
        }
        return nodes
    }

    private fun extractEdges(edgeArray: JsonArray): List<Edge> {
        val edges: MutableList<Edge> = Lists.newArrayList()
        val edgeChecker: JsonDatamodelChecker = DatamodelCheckerFactory.edgeChecker()
        for (edgeElement in edgeArray) {
            val edge: JsonObject = edgeElement.asJsonObject
            edgeChecker.check(edge)
            val `object`: String = string(edge, "obj")
            val subject: String = string(edge, "sub")
            edges.add(
                ImmutableEdge.builder()
                    .subject(subject)
                    .subjectDoid(extractDoid(subject))
                    .`object`(`object`)
                    .objectDoid(extractDoid(`object`))
                    .predicate(string(edge, "pred"))
                    .build()
            )
        }
        return edges
    }

    private fun extractGraphMetadata(metadata: JsonObject?): GraphMetadata {
        DatamodelCheckerFactory.graphMetadataChecker().check(metadata)
        val xrefArray: JsonArray = optionalArray(metadata, "xrefs")
        val xrefs: MutableList<Xref> = Lists.newArrayList<Xref>()
        if (xrefArray != null) {
            val xrefChecker: JsonDatamodelChecker = DatamodelCheckerFactory.metadataXrefChecker()
            for (xrefElement in xrefArray) {
                val xref: JsonObject = xrefElement.asJsonObject
                xrefChecker.check(xref)
                xrefs.add(ImmutableXref.builder().`val`(string(xref, "val")).build())
            }
        }
        return ImmutableGraphMetadata.builder()
            .basicPropertyValues(extractBasicPropertyValues(optionalArray(metadata, "basicPropertyValues")))
            .subsets(optionalStringList(metadata, "subsets"))
            .xrefs(xrefs)
            .version(optionalString(metadata, "version"))
            .build()
    }

    private fun extractBasicPropertyValues(basicPropertyValueArray: JsonArray?): List<BasicPropertyValue>? {
        if (basicPropertyValueArray == null) {
            return null
        }
        val basicPropertyValues: MutableList<BasicPropertyValue> = Lists.newArrayList<BasicPropertyValue>()
        val basicPropertyValuesChecker: JsonDatamodelChecker = DatamodelCheckerFactory.basicPropertyValueChecker()
        for (basicPropertyElement in basicPropertyValueArray) {
            val basicProperty: JsonObject = basicPropertyElement.asJsonObject
            basicPropertyValuesChecker.check(basicProperty)
            basicPropertyValues.add(
                ImmutableBasicPropertyValue.builder()
                    .pred(string(basicProperty, "pred"))
                    .`val`(string(basicProperty, "val"))
                    .build()
            )
        }
        return basicPropertyValues
    }

    private fun extractLogicalDefinitionAxioms(logicalDefinitionAxiomArray: JsonArray?): List<LogicalDefinitionAxioms>? {
        if (logicalDefinitionAxiomArray == null) {
            return null
        }
        val logicalDefinitionAxioms: MutableList<LogicalDefinitionAxioms> = Lists.newArrayList<LogicalDefinitionAxioms>()
        val logicalDefinitionAxiomsChecker: JsonDatamodelChecker = DatamodelCheckerFactory.logicalDefinitionAxiomChecker()
        for (logicalDefinitionAxiomElement in logicalDefinitionAxiomArray) {
            val logicalDefinitionAxiom: JsonObject = logicalDefinitionAxiomElement.asJsonObject
            logicalDefinitionAxiomsChecker.check(logicalDefinitionAxiom)
            val restrictionChecker: JsonDatamodelChecker = DatamodelCheckerFactory.restrictionChecker()
            val restrictions: MutableList<Restriction> = Lists.newArrayList()
            for (restrictionElement in array(logicalDefinitionAxiom, "restrictions")) {
                if (restrictionElement.isJsonObject()) {
                    val restriction: JsonObject = restrictionElement.getAsJsonObject()
                    restrictionChecker.check(restriction)
                    restrictions.add(
                        ImmutableRestriction.builder()
                            .propertyId(string(restriction, "propertyId"))
                            .fillerId(string(restriction, "fillerId"))
                            .build()
                    )
                }
            }
            logicalDefinitionAxioms.add(
                ImmutableLogicalDefinitionAxioms.builder()
                    .definedClassId(string(logicalDefinitionAxiom, "definedClassId"))
                    .genusIds(stringList(logicalDefinitionAxiom, "genusIds"))
                    .restrictions(restrictions)
                    .build()
            )
        }
        return logicalDefinitionAxioms
    }

    private fun extractMetadata(metadata: JsonObject?): Metadata? {
        if (metadata == null) {
            return null
        }
        DatamodelCheckerFactory.metadataChecker().check(metadata)
        val xrefArray: JsonArray = optionalArray(metadata, "xrefs")
        val xrefs: MutableList<Xref> = Lists.newArrayList<Xref>()
        if (xrefArray != null) {
            val xrefChecker: JsonDatamodelChecker = DatamodelCheckerFactory.metadataXrefChecker()
            for (xrefElement in xrefArray) {
                val xref: JsonObject = xrefElement.asJsonObject
                xrefChecker.check(xref)
                xrefs.add(ImmutableXref.builder().`val`(string(xref, "val")).build())
            }
        }
        return ImmutableMetadata.builder()
            .synonyms(extractSynonyms(optionalArray(metadata, "synonyms")))
            .basicPropertyValues(extractBasicPropertyValues(optionalArray(metadata, "basicPropertyValues")))
            .definition(extractDefinition(optionalObject(metadata, "definition")))
            .subsets(optionalStringList(metadata, "subsets"))
            .xrefs(xrefs)
            .snomedConceptId(extractSnomedConceptId(xrefs))
            .deprecated(optionalBool(metadata, "deprecated"))
            .comments(optionalStringList(metadata, "comments"))
            .build()
    }

    @VisibleForTesting
    fun extractSnomedConceptId(xrefs: List<Xref?>?): String? {
        if (xrefs == null) {
            return null
        }
        for (xref in xrefs) {
            // Format to look for is SNOMEDCT_US_2020_03_01:109355002
            if (xref.`val`().contains("SNOMED")) {
                val parts: Array<String> = xref.`val`().split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                if (parts.size == 2 && isLong(parts[1])) {
                    return parts[1]
                } else {
                    LOGGER.warn("Unexpected SNOMED entry found: {}", xref.`val`())
                }
            }
        }
        return null
    }

    private fun isLong(string: String): Boolean {
        return try {
            string.toLong()
            true
        } catch (e: NumberFormatException) {
            false
        }
    }

    private fun extractSynonyms(synonymArray: JsonArray?): List<Synonym>? {
        if (synonymArray == null) {
            return null
        }
        val synonymChecker: JsonDatamodelChecker = DatamodelCheckerFactory.synonymChecker()
        val synonyms: MutableList<Synonym> = Lists.newArrayList<Synonym>()
        for (synonymElement in synonymArray) {
            val synonym: JsonObject = synonymElement.asJsonObject
            synonymChecker.check(synonym)
            synonyms.add(
                ImmutableSynonym.builder()
                    .pred(string(synonym, "pred"))
                    .`val`(string(synonym, "val"))
                    .xrefs(stringList(synonym, "xrefs"))
                    .build()
            )
        }
        return synonyms
    }

    private fun extractDefinition(definition: JsonObject?): Definition? {
        if (definition == null) {
            return null
        }
        DatamodelCheckerFactory.definitionChecker().check(definition)
        return ImmutableDefinition.builder().`val`(string(definition, "val")).xrefs(stringList(definition, "xrefs")).build()
    }
}
