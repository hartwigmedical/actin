package com.hartwig.actin.doid.serialization

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode
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
import com.hartwig.actin.util.json.Json
import com.hartwig.actin.util.json.Json.array
import com.hartwig.actin.util.json.Json.objectNode
import com.hartwig.actin.util.json.Json.optionalArray
import com.hartwig.actin.util.json.Json.optionalBool
import com.hartwig.actin.util.json.Json.optionalObject
import com.hartwig.actin.util.json.Json.optionalString
import com.hartwig.actin.util.json.Json.optionalStringList
import com.hartwig.actin.util.json.Json.string
import com.hartwig.actin.util.json.Json.stringList
import com.hartwig.actin.util.json.JsonDatamodelChecker
import io.github.oshai.kotlinlogging.KotlinLogging

object DoidJson {

    private val logger = KotlinLogging.logger {}
    private val mapper = ObjectMapper()

    const val ID_TO_READ = "http://purl.obolibrary.org/obo/doid.owl"

    const val DOID_URL_PREFIX = "http://purl.obolibrary.org/obo/DOID_"

    fun readDoidOwlEntry(doidJson: String): DoidEntry {
        val rootObject = Json.readSingleObjectFromFile(doidJson, mapper)
        DatamodelCheckerFactory.rootObjectChecker().check(rootObject)

        var entry: DoidEntry? = null
        val graphsChecker = DatamodelCheckerFactory.graphsChecker()
        for (element in array(rootObject, "graphs")) {
            val graph = element as ObjectNode
            graphsChecker.check(graph)
            val id: String = string(graph, "id")
            if (id == ID_TO_READ) {
                logger.debug { "Reading DOID entry with ID '$id'" }
                entry = DoidEntry(
                    id = id,
                    nodes = extractNodes(array(graph, "nodes")),
                    edges = extractEdges(array(graph, "edges")),
                    metadata = extractGraphMetadata(objectNode(graph, "meta")),
                    logicalDefinitionAxioms = extractLogicalDefinitionAxioms(optionalArray(graph, "logicalDefinitionAxioms"))
                )
            }
        }
        checkNotNull(entry) { "Could not read DOID entry with ID '$ID_TO_READ'" }
        return entry
    }

    fun extractDoid(url: String): String {
        return if (url.startsWith(DOID_URL_PREFIX)) url.substring(DOID_URL_PREFIX.length) else ""
    }

    private fun extractNodes(nodeArray: ArrayNode): List<Node> {
        val nodeChecker: JsonDatamodelChecker = DatamodelCheckerFactory.nodeChecker()
        return nodeArray.map { nodeElement ->
            val node = nodeElement as ObjectNode
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

    private fun extractEdges(edgeArray: ArrayNode): List<Edge> {
        val edgeChecker: JsonDatamodelChecker = DatamodelCheckerFactory.edgeChecker()
        return edgeArray.map { edgeElement ->
            val edge = edgeElement as ObjectNode
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

    private fun extractGraphMetadata(metadata: ObjectNode): GraphMetadata {
        DatamodelCheckerFactory.graphMetadataChecker().check(metadata)

        return GraphMetadata(
            basicPropertyValues = extractBasicPropertyValues(optionalArray(metadata, "basicPropertyValues")),
            version = string(metadata, "version")
        )
    }

    private fun extractBasicPropertyValues(basicPropertyValueArray: ArrayNode?): List<BasicPropertyValue>? {
        if (basicPropertyValueArray == null) {
            return null
        }
        val basicPropertyValuesChecker: JsonDatamodelChecker = DatamodelCheckerFactory.basicPropertyValueChecker()

        return basicPropertyValueArray.map { basicPropertyElement ->
            val basicProperty = basicPropertyElement as ObjectNode
            basicPropertyValuesChecker.check(basicProperty)
            BasicPropertyValue(
                pred = string(basicProperty, "pred"),
                `val` = string(basicProperty, "val")
            )
        }
    }

    private fun extractLogicalDefinitionAxioms(logicalDefinitionAxiomArray: ArrayNode?): List<LogicalDefinitionAxioms>? {
        val logicalDefinitionAxiomsChecker: JsonDatamodelChecker = DatamodelCheckerFactory.logicalDefinitionAxiomChecker()

        return logicalDefinitionAxiomArray?.map { logicalDefinitionAxiomElement ->
            val logicalDefinitionAxiom = logicalDefinitionAxiomElement as ObjectNode
            logicalDefinitionAxiomsChecker.check(logicalDefinitionAxiom)

            LogicalDefinitionAxioms(
                definedClassId = string(logicalDefinitionAxiom, "definedClassId"),
                genusIds = stringList(logicalDefinitionAxiom, "genusIds"),
                restrictions = extractLogicalDefinitionAxiomRestrictions(optionalArray(logicalDefinitionAxiom, "restrictions"))
            )
        }
    }

    private fun extractLogicalDefinitionAxiomRestrictions(restrictionArray: ArrayNode?): List<Restriction>? {
        val restrictionChecker: JsonDatamodelChecker = DatamodelCheckerFactory.restrictionChecker()
        return restrictionArray?.map { restrictionElement ->
            val restriction = restrictionElement as ObjectNode
            restrictionChecker.check(restriction)
            Restriction(
                propertyId = string(restriction, "propertyId"),
                fillerId = string(restriction, "fillerId")
            )
        }
    }

    private fun extractMetadata(metadata: ObjectNode?): Metadata? {
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

    private fun extractDoidXrefValList(xrefs: ArrayNode?): List<Xref>? {
        if (xrefs == null) {
            return null
        }
        val xrefChecker: JsonDatamodelChecker = DatamodelCheckerFactory.metadataXrefChecker()

        return xrefs.map { xrefElement ->
            val xref = xrefElement as ObjectNode
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
                    logger.warn { "Unexpected SNOMED entry found: ${xref.`val`}" }
                }
            }
        }
        return null
    }

    private fun isLong(string: String): Boolean {
        return try {
            string.toLong()
            true
        } catch (_: NumberFormatException) {
            false
        }
    }

    private fun extractSynonyms(synonymArray: ArrayNode?): List<Synonym>? {
        if (synonymArray == null) {
            return null
        }
        val synonymChecker: JsonDatamodelChecker = DatamodelCheckerFactory.synonymChecker()
        return synonymArray.map { synonymElement ->
            val synonym = synonymElement as ObjectNode
            synonymChecker.check(synonym)
            Synonym(
                pred = string(synonym, "pred"),
                `val` = string(synonym, "val"),
                xrefs = optionalStringList(synonym, "xrefs"),
                synonymType = optionalString(synonym, "synonymType")
            )
        }
    }

    private fun extractDefinition(definition: ObjectNode?): Definition? {
        if (definition == null) {
            return null
        }
        DatamodelCheckerFactory.definitionChecker().check(definition)
        return Definition(
            `val` = string(definition, "val"),
            xrefs = optionalStringList(definition, "xrefs")
        )
    }
}
