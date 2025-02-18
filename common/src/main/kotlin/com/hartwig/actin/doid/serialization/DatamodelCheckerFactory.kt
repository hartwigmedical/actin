package com.hartwig.actin.doid.serialization

import com.hartwig.actin.util.json.JsonDatamodelChecker

object DatamodelCheckerFactory {

    fun rootObjectChecker(): JsonDatamodelChecker {
        return allRequiredDatamodelChecker("RootObject", listOf("graphs"))
    }

    fun graphsChecker(): JsonDatamodelChecker {
        return JsonDatamodelChecker(
            "Graphs", mapOf("nodes" to true, "edges" to true, "id" to true, "meta" to true, "logicalDefinitionAxioms" to false)
        )
    }

    fun nodeChecker(): JsonDatamodelChecker {
        return JsonDatamodelChecker("Node", mapOf("type" to false, "lbl" to false, "id" to true, "meta" to false))
    }

    fun edgeChecker(): JsonDatamodelChecker {
        return allRequiredDatamodelChecker("Edge", listOf("sub", "pred", "obj"))
    }

    fun graphMetadataChecker(): JsonDatamodelChecker {
        return JsonDatamodelChecker(
            "GraphMetadata", mapOf("basicPropertyValues" to false, "version" to true)
        )
    }

    fun logicalDefinitionAxiomChecker(): JsonDatamodelChecker {
        return JsonDatamodelChecker(
            "LogicalDefinitionAxiom", mapOf("definedClassId" to true, "genusIds" to true, "restrictions" to false)
        )
    }

    fun restrictionChecker(): JsonDatamodelChecker {
        return allRequiredDatamodelChecker("Restriction", listOf("propertyId", "fillerId"))
    }

    fun synonymChecker(): JsonDatamodelChecker {
        return JsonDatamodelChecker(
            "Synonym", mapOf("pred" to true, "val" to true, "xrefs" to false, "synonymType" to false)
        )
    }

    fun definitionChecker(): JsonDatamodelChecker {
        return allRequiredDatamodelChecker("Definition", listOf("xrefs", "val"))
    }

    fun basicPropertyValueChecker(): JsonDatamodelChecker {
        return allRequiredDatamodelChecker("BasicPropertyValue", listOf("pred", "val"))
    }

    fun metadataXrefChecker(): JsonDatamodelChecker {
        return allRequiredDatamodelChecker("MetadataXref", listOf("val"))
    }

    fun metadataChecker(): JsonDatamodelChecker {
        return JsonDatamodelChecker(
            "Metadata",
            listOf("xrefs", "synonyms", "basicPropertyValues", "definition", "subsets", "deprecated", "comments").associateWith { false }
        )
    }

    private fun allRequiredDatamodelChecker(name: String, properties: List<String>) =
        JsonDatamodelChecker(name, properties.associateWith { true })
}
