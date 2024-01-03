package com.hartwig.actin.doid.serialization

import com.hartwig.actin.util.json.JsonDatamodelChecker

object DatamodelCheckerFactory {
    fun rootObjectChecker(): JsonDatamodelChecker {
        return JsonDatamodelChecker("RootObject", mapOf("graphs" to true))
    }

    fun graphsChecker(): JsonDatamodelChecker {
        val properties = listOf(
            "nodes",
            "edges",
            "id",
            "meta",
            "equivalentNodesSets",
            "logicalDefinitionAxioms",
            "domainRangeAxioms",
            "propertyChainAxioms"
        )
        return datamodelChecker("Graphs", properties)
    }

    fun nodeChecker(): JsonDatamodelChecker {
        return datamodelChecker("Node", listOf("type", "lbl", "id", "meta"))
    }

    fun edgeChecker(): JsonDatamodelChecker {
        return datamodelChecker("Edge", listOf("sub", "pred", "obj"))
    }

    fun graphMetadataChecker(): JsonDatamodelChecker {
        return datamodelChecker("GraphMetadata", listOf("xrefs", "basicPropertyValues", "version", "subsets"))
    }

    fun logicalDefinitionAxiomChecker(): JsonDatamodelChecker {
        return datamodelChecker("LogicalDefinitionAxiom", listOf("definedClassId", "genusIds", "restrictions"))
    }

    fun restrictionChecker(): JsonDatamodelChecker {
        return datamodelChecker("Restriction", listOf("propertyId", "fillerId"))
    }

    fun synonymChecker(): JsonDatamodelChecker {
        return datamodelChecker("Synonym", listOf("pred", "val", "xrefs"))
    }

    fun definitionChecker(): JsonDatamodelChecker {
        return datamodelChecker("Definition", listOf("xrefs", "val"))
    }

    fun basicPropertyValueChecker(): JsonDatamodelChecker {
        return datamodelChecker("BasicPropertyValue", listOf("pred", "val"))
    }

    fun metadataXrefChecker(): JsonDatamodelChecker {
        return datamodelChecker("MetadataXref", listOf("val"))
    }

    fun metadataChecker(): JsonDatamodelChecker {
        return datamodelChecker(
            "Metadata", listOf(
                "xrefs", "synonyms", "basicPropertyValues", "definition", "subsets", "deprecated", "comments"
            )
        )
    }

    private fun datamodelChecker(name: String, properties: List<String>) = JsonDatamodelChecker(name, properties.associateWith { true })
}
