package com.hartwig.actin.doid.serialization

import com.hartwig.actin.util.json.JsonDatamodelChecker

object DatamodelCheckerFactory {

    fun rootObjectChecker(): JsonDatamodelChecker {
        return allRequiredDatamodelChecker("RootObject", listOf("graphs"))
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
        return allRequiredDatamodelChecker("Graphs", properties)
    }

    fun nodeChecker(): JsonDatamodelChecker {
        return JsonDatamodelChecker("Node", mapOf("type" to false, "lbl" to false, "id" to true, "meta" to false))
    }

    fun edgeChecker(): JsonDatamodelChecker {
        return allRequiredDatamodelChecker("Edge", listOf("sub", "pred", "obj"))
    }

    fun graphMetadataChecker(): JsonDatamodelChecker {
        return JsonDatamodelChecker(
            "GraphMetadata", mapOf("xrefs" to true, "basicPropertyValues" to true, "version" to false, "subsets" to true)
        )
    }

    fun logicalDefinitionAxiomChecker(): JsonDatamodelChecker {
        return allRequiredDatamodelChecker("LogicalDefinitionAxiom", listOf("definedClassId", "genusIds", "restrictions"))
    }

    fun restrictionChecker(): JsonDatamodelChecker {
        return allRequiredDatamodelChecker("Restriction", listOf("propertyId", "fillerId"))
    }

    fun synonymChecker(): JsonDatamodelChecker {
        return allRequiredDatamodelChecker("Synonym", listOf("pred", "val", "xrefs"))
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
