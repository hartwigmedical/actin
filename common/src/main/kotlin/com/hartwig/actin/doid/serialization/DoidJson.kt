package com.hartwig.actin.doid.serialization

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.hartwig.actin.doid.datamodel.BasicPropertyValue
import com.hartwig.actin.doid.datamodel.Definition
import com.hartwig.actin.doid.datamodel.DoidEntry
import com.hartwig.actin.doid.datamodel.Edge
import com.hartwig.actin.doid.datamodel.GraphMetadata
import com.hartwig.actin.doid.datamodel.LogicalDefinitionAxioms
import com.hartwig.actin.doid.datamodel.Metadata
import com.hartwig.actin.doid.datamodel.Node
import com.hartwig.actin.doid.datamodel.Restriction
import com.hartwig.actin.doid.datamodel.Synonym
import com.hartwig.actin.doid.datamodel.Xref
import com.hartwig.actin.util.json.Json.array
import com.hartwig.actin.util.json.Json.`object`
import com.hartwig.actin.util.json.Json.optionalArray
import com.hartwig.actin.util.json.Json.optionalBool
import com.hartwig.actin.util.json.Json.optionalObject
import com.hartwig.actin.util.json.Json.optionalString
import com.hartwig.actin.util.json.Json.optionalStringList
import com.hartwig.actin.util.json.Json.string
import com.hartwig.actin.util.json.Json.stringList
import com.hartwig.actin.util.json.JsonDatamodelChecker
import org.apache.logging.log4j.LogManager
import java.io.FileReader

object DoidJson {

    private val LOGGER = LogManager.getLogger(DoidJson::class.java)

    const val ID_TO_READ = "http://purl.obolibrary.org/obo/doid.owl"

    const val DOID_URL_PREFIX = "http://purl.obolibrary.org/obo/DOID_"

    fun readDoidOwlEntry(doidJson: String): DoidEntry {
        val reader = JsonReader(FileReader(doidJson))
        reader.isLenient = true
        val rootObject: JsonObject = JsonParser.parseReader(reader).asJsonObject
        DatamodelCheckerFactory.rootObjectChecker().check(rootObject)

        var entry: DoidEntry? = null
        val graphsChecker: JsonDatamodelChecker = DatamodelCheckerFactory.graphsChecker()
        for (element in array(rootObject, "graphs")) {
            val graph: JsonObject = element.asJsonObject
            graphsChecker.check(graph)
            val id: String = string(graph, "id")
            if (id == ID_TO_READ) {
                LOGGER.debug(" Reading DOID entry with ID '{}'", id)
                entry = DoidEntry(
                    id = id,
                    nodes = extractNodes(array(graph, "nodes")),
                    edges = extractEdges(array(graph, "edges")),
                    metadata = extractGraphMetadata(`object`(graph, "meta")),
                    logicalDefinitionAxioms = extractLogicalDefinitionAxioms(optionalArray(graph, "logicalDefinitionAxioms"))
                )
            }
        }
        if (reader.peek() != JsonToken.END_DOCUMENT) {
            LOGGER.warn("More data found in {} after reading main JSON object!", doidJson)
        }
        checkNotNull(entry) { "Could not read DOID entry with ID '$ID_TO_READ'" }
        return entry
    }

    fun extractDoid(url: String): String {
        return if (url.startsWith(DOID_URL_PREFIX)) url.substring(DOID_URL_PREFIX.length) else ""
    }

    private fun extractNodes(nodeArray: JsonArray): List<Node> {
        val nodeChecker: JsonDatamodelChecker = DatamodelCheckerFactory.nodeChecker()
        return nodeArray.map { nodeElement ->
            val node: JsonObject = nodeElement.asJsonObject
            nodeChecker.check(node)
            val id: String = string(node, "id")
            Node(
                doid = extractDoid(id),
                url = id,
                metadata = extractMetadata(optionalObject(node, "meta")),
                type = optionalString(node, "type"),
                term = optionalString(node, "lbl")
            )
        }
    }

    private fun extractEdges(edgeArray: JsonArray): List<Edge> {
        val edgeChecker: JsonDatamodelChecker = DatamodelCheckerFactory.edgeChecker()
        return edgeArray.map { edgeElement ->
            val edge: JsonObject = edgeElement.asJsonObject
            edgeChecker.check(edge)
            val `object`: String = string(edge, "obj")
            val subject: String = string(edge, "sub")
            Edge(
                subject = subject,
                subjectDoid = extractDoid(subject),
                `object` = `object`,
                objectDoid = extractDoid(`object`),
                predicate = string(edge, "pred")
            )
        }
    }

    private fun extractGraphMetadata(metadata: JsonObject): GraphMetadata {
        DatamodelCheckerFactory.graphMetadataChecker().check(metadata)

        return GraphMetadata(
            basicPropertyValues = extractBasicPropertyValues(optionalArray(metadata, "basicPropertyValues")),
            version = string(metadata, "version")
        )
    }

    private fun extractBasicPropertyValues(basicPropertyValueArray: JsonArray?): List<BasicPropertyValue>? {
        if (basicPropertyValueArray == null) {
            return null
        }
        val basicPropertyValuesChecker: JsonDatamodelChecker = DatamodelCheckerFactory.basicPropertyValueChecker()

        return basicPropertyValueArray.map { basicPropertyElement ->
            val basicProperty: JsonObject = basicPropertyElement.asJsonObject
            basicPropertyValuesChecker.check(basicProperty)
            BasicPropertyValue(
                pred = string(basicProperty, "pred"),
                `val` = string(basicProperty, "val")
            )
        }
    }

    private fun extractLogicalDefinitionAxioms(logicalDefinitionAxiomArray: JsonArray?): List<LogicalDefinitionAxioms>? {
        val logicalDefinitionAxiomsChecker: JsonDatamodelChecker = DatamodelCheckerFactory.logicalDefinitionAxiomChecker()

        return logicalDefinitionAxiomArray?.map { logicalDefinitionAxiomElement ->
            val logicalDefinitionAxiom: JsonObject = logicalDefinitionAxiomElement.asJsonObject
            logicalDefinitionAxiomsChecker.check(logicalDefinitionAxiom)

            LogicalDefinitionAxioms(
                definedClassId = string(logicalDefinitionAxiom, "definedClassId"),
                genusIds = stringList(logicalDefinitionAxiom, "genusIds"),
                restrictions = extractLogicalDefinitionAxiomRestrictions(optionalArray(logicalDefinitionAxiom, "restrictions"))
            )
        }
    }

    private fun extractLogicalDefinitionAxiomRestrictions(restrictionArray: JsonArray?): List<Restriction>? {
        val restrictionChecker: JsonDatamodelChecker = DatamodelCheckerFactory.restrictionChecker()
        return restrictionArray?.map { restrictionElement ->
            val restriction: JsonObject = restrictionElement.asJsonObject
            restrictionChecker.check(restriction)
            Restriction(
                propertyId = string(restriction, "propertyId"),
                fillerId = string(restriction, "fillerId")
            )
        }
    }

    private fun extractMetadata(metadata: JsonObject?): Metadata? {
        if (metadata == null) {
            return null
        }
        DatamodelCheckerFactory.metadataChecker().check(metadata)
        val xrefs = extractDoidXrefValList(optionalArray(metadata, "xrefs"))

        return Metadata(
            synonyms = extractSynonyms(optionalArray(metadata, "synonyms")),
            basicPropertyValues = extractBasicPropertyValues(optionalArray(metadata, "basicPropertyValues")),
            definition = extractDefinition(optionalObject(metadata, "definition")),
            subsets = optionalStringList(metadata, "subsets"),
            xrefs = xrefs,
            snomedConceptId = extractSnomedConceptId(xrefs),
            deprecated = optionalBool(metadata, "deprecated"),
            comments = optionalStringList(metadata, "comments"),
        )
    }

    private fun extractDoidXrefValList(xrefs: JsonArray?): List<Xref>? {
        if (xrefs == null) {
            return null
        }
        val xrefChecker: JsonDatamodelChecker = DatamodelCheckerFactory.metadataXrefChecker()

        return xrefs.map { xrefElement ->
            val xref: JsonObject = xrefElement.asJsonObject
            xrefChecker.check(xref)
            Xref(string(xref, "val"))
        }
    }

    fun extractSnomedConceptId(xrefs: List<Xref>?): String? {
        if (xrefs == null) {
            return null
        }
        for (xref in xrefs) {
            // Format to look for is SNOMEDCT_US_2020_03_01:109355002
            if (xref.`val`.contains("SNOMED")) {
                val parts = xref.`val`.split(":".toRegex()).dropLastWhile { it.isEmpty() }
                if (parts.size == 2 && isLong(parts[1])) {
                    return parts[1]
                } else {
                    LOGGER.warn("Unexpected SNOMED entry found: {}", xref.`val`)
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
        return synonymArray.map { synonymElement ->
            val synonym: JsonObject = synonymElement.asJsonObject
            synonymChecker.check(synonym)
            Synonym(
                pred = string(synonym, "pred"),
                `val` = string(synonym, "val"),
                xrefs = optionalStringList(synonym, "xrefs"),
                synonymType = optionalString(synonym, "synonymType")
            )
        }
    }

    private fun extractDefinition(definition: JsonObject?): Definition? {
        if (definition == null) {
            return null
        }
        DatamodelCheckerFactory.definitionChecker().check(definition)
        return Definition(
            `val` = string(definition, "val"),
            xrefs = stringList(definition, "xrefs")
        )
    }
}
