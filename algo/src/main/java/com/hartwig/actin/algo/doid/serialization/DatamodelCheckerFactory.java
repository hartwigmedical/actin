package com.hartwig.actin.algo.doid.serialization;

import java.util.Map;

import com.google.common.collect.Maps;
import com.hartwig.actin.json.JsonDatamodelChecker;

import org.jetbrains.annotations.NotNull;

final class DatamodelCheckerFactory {

    private DatamodelCheckerFactory() {
    }

    @NotNull
    static JsonDatamodelChecker rootObjectChecker() {
        Map<String, Boolean> map = Maps.newHashMap();
        map.put("graphs", true);

        return new JsonDatamodelChecker("RootObject", map);
    }

    @NotNull
    static JsonDatamodelChecker graphsChecker() {
        Map<String, Boolean> map = Maps.newHashMap();
        map.put("nodes", true);
        map.put("edges", true);
        map.put("id", true);
        map.put("meta", true);
        map.put("equivalentNodesSets", true);
        map.put("logicalDefinitionAxioms", true);
        map.put("domainRangeAxioms", true);
        map.put("propertyChainAxioms", true);

        return new JsonDatamodelChecker("Graphs", map);
    }

    @NotNull
    static JsonDatamodelChecker nodeChecker() {
        Map<String, Boolean> map = Maps.newHashMap();
        map.put("type", false);
        map.put("lbl", false);
        map.put("id", true);
        map.put("meta", false);

        return new JsonDatamodelChecker("Node", map);
    }

    @NotNull
    static JsonDatamodelChecker edgeChecker() {
        Map<String, Boolean> map = Maps.newHashMap();
        map.put("sub", true);
        map.put("pred", true);
        map.put("obj", true);

        return new JsonDatamodelChecker("Edge", map);
    }

    @NotNull
    static JsonDatamodelChecker graphMetadataChecker() {
        Map<String, Boolean> map = Maps.newHashMap();

        map.put("xrefs", true);
        map.put("basicPropertyValues", true);
        map.put("version", false);
        map.put("subsets", true);

        return new JsonDatamodelChecker("GraphMetadata", map);
    }

    @NotNull
    static JsonDatamodelChecker logicalDefinitionAxiomChecker() {
        Map<String, Boolean> map = Maps.newHashMap();
        map.put("definedClassId", true);
        map.put("genusIds", true);
        map.put("restrictions", true);

        return new JsonDatamodelChecker("LogicalDefinitionAxiom", map);
    }

    @NotNull
    static JsonDatamodelChecker restrictionChecker() {
        Map<String, Boolean> map = Maps.newHashMap();
        map.put("propertyId", true);
        map.put("fillerId", true);

        return new JsonDatamodelChecker("Restriction", map);
    }

    @NotNull
    static JsonDatamodelChecker synonymChecker() {
        Map<String, Boolean> map = Maps.newHashMap();
        map.put("pred", true);
        map.put("val", true);
        map.put("xrefs", true);
        return new JsonDatamodelChecker("Synonym", map);
    }

    @NotNull
    static JsonDatamodelChecker definitionChecker() {
        Map<String, Boolean> map = Maps.newHashMap();
        map.put("xrefs", true);
        map.put("val", true);
        return new JsonDatamodelChecker("Definition", map);
    }

    @NotNull
    static JsonDatamodelChecker basicPropertyValueChecker() {
        Map<String, Boolean> map = Maps.newHashMap();
        map.put("pred", true);
        map.put("val", true);
        return new JsonDatamodelChecker("BasicPropertyValue", map);
    }

    @NotNull
    static JsonDatamodelChecker metadataXrefChecker() {
        Map<String, Boolean> map = Maps.newHashMap();
        map.put("val", true);

        return new JsonDatamodelChecker("MetadataXref", map);
    }

    @NotNull
    static JsonDatamodelChecker metadataChecker() {
        Map<String, Boolean> map = Maps.newHashMap();
        map.put("xrefs", false);
        map.put("synonyms", false);
        map.put("basicPropertyValues", false);
        map.put("definition", false);
        map.put("subsets", false);

        return new JsonDatamodelChecker("Metadata", map);
    }
}
