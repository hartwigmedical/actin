package com.hartwig.actin.doid.serialization

import com.google.common.collect.Maps
import com.hartwig.actin.util.json.JsonDatamodelChecker

internal object DatamodelCheckerFactory {
    fun rootObjectChecker(): JsonDatamodelChecker {
        val map: MutableMap<String, Boolean> = Maps.newHashMap()
        map["graphs"] = true
        return JsonDatamodelChecker("RootObject", map)
    }

    fun graphsChecker(): JsonDatamodelChecker {
        val map: MutableMap<String, Boolean> = Maps.newHashMap()
        map["nodes"] = true
        map["edges"] = true
        map["id"] = true
        map["meta"] = true
        map["equivalentNodesSets"] = true
        map["logicalDefinitionAxioms"] = true
        map["domainRangeAxioms"] = true
        map["propertyChainAxioms"] = true
        return JsonDatamodelChecker("Graphs", map)
    }

    fun nodeChecker(): JsonDatamodelChecker {
        val map: MutableMap<String, Boolean> = Maps.newHashMap()
        map["type"] = false
        map["lbl"] = false
        map["id"] = true
        map["meta"] = false
        return JsonDatamodelChecker("Node", map)
    }

    fun edgeChecker(): JsonDatamodelChecker {
        val map: MutableMap<String, Boolean> = Maps.newHashMap()
        map["sub"] = true
        map["pred"] = true
        map["obj"] = true
        return JsonDatamodelChecker("Edge", map)
    }

    fun graphMetadataChecker(): JsonDatamodelChecker {
        val map: MutableMap<String, Boolean> = Maps.newHashMap()
        map["xrefs"] = true
        map["basicPropertyValues"] = true
        map["version"] = false
        map["subsets"] = true
        return JsonDatamodelChecker("GraphMetadata", map)
    }

    fun logicalDefinitionAxiomChecker(): JsonDatamodelChecker {
        val map: MutableMap<String, Boolean> = Maps.newHashMap()
        map["definedClassId"] = true
        map["genusIds"] = true
        map["restrictions"] = true
        return JsonDatamodelChecker("LogicalDefinitionAxiom", map)
    }

    fun restrictionChecker(): JsonDatamodelChecker {
        val map: MutableMap<String, Boolean> = Maps.newHashMap()
        map["propertyId"] = true
        map["fillerId"] = true
        return JsonDatamodelChecker("Restriction", map)
    }

    fun synonymChecker(): JsonDatamodelChecker {
        val map: MutableMap<String, Boolean> = Maps.newHashMap()
        map["pred"] = true
        map["val"] = true
        map["xrefs"] = true
        return JsonDatamodelChecker("Synonym", map)
    }

    fun definitionChecker(): JsonDatamodelChecker {
        val map: MutableMap<String, Boolean> = Maps.newHashMap()
        map["xrefs"] = true
        map["val"] = true
        return JsonDatamodelChecker("Definition", map)
    }

    fun basicPropertyValueChecker(): JsonDatamodelChecker {
        val map: MutableMap<String, Boolean> = Maps.newHashMap()
        map["pred"] = true
        map["val"] = true
        return JsonDatamodelChecker("BasicPropertyValue", map)
    }

    fun metadataXrefChecker(): JsonDatamodelChecker {
        val map: MutableMap<String, Boolean> = Maps.newHashMap()
        map["val"] = true
        return JsonDatamodelChecker("MetadataXref", map)
    }

    fun metadataChecker(): JsonDatamodelChecker {
        val map: MutableMap<String, Boolean> = Maps.newHashMap()
        map["xrefs"] = false
        map["synonyms"] = false
        map["basicPropertyValues"] = false
        map["definition"] = false
        map["subsets"] = false
        map["deprecated"] = false
        map["comments"] = false
        return JsonDatamodelChecker("Metadata", map)
    }
}
