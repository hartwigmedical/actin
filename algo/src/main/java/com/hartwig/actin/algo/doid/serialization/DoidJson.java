package com.hartwig.actin.algo.doid.serialization;

import static com.hartwig.actin.json.Json.array;
import static com.hartwig.actin.json.Json.object;
import static com.hartwig.actin.json.Json.optionalArray;
import static com.hartwig.actin.json.Json.optionalObject;
import static com.hartwig.actin.json.Json.optionalString;
import static com.hartwig.actin.json.Json.optionalStringList;
import static com.hartwig.actin.json.Json.string;
import static com.hartwig.actin.json.Json.stringList;

import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.hartwig.actin.algo.doid.datamodel.BasicPropertyValue;
import com.hartwig.actin.algo.doid.datamodel.Definition;
import com.hartwig.actin.algo.doid.datamodel.Edge;
import com.hartwig.actin.algo.doid.datamodel.Entry;
import com.hartwig.actin.algo.doid.datamodel.GraphMetadata;
import com.hartwig.actin.algo.doid.datamodel.ImmutableBasicPropertyValue;
import com.hartwig.actin.algo.doid.datamodel.ImmutableDefinition;
import com.hartwig.actin.algo.doid.datamodel.ImmutableEdge;
import com.hartwig.actin.algo.doid.datamodel.ImmutableEntry;
import com.hartwig.actin.algo.doid.datamodel.ImmutableGraphMetadata;
import com.hartwig.actin.algo.doid.datamodel.ImmutableLogicalDefinitionAxioms;
import com.hartwig.actin.algo.doid.datamodel.ImmutableMetadata;
import com.hartwig.actin.algo.doid.datamodel.ImmutableNode;
import com.hartwig.actin.algo.doid.datamodel.ImmutableRestriction;
import com.hartwig.actin.algo.doid.datamodel.ImmutableSynonym;
import com.hartwig.actin.algo.doid.datamodel.ImmutableXref;
import com.hartwig.actin.algo.doid.datamodel.LogicalDefinitionAxioms;
import com.hartwig.actin.algo.doid.datamodel.Metadata;
import com.hartwig.actin.algo.doid.datamodel.Node;
import com.hartwig.actin.algo.doid.datamodel.Restriction;
import com.hartwig.actin.algo.doid.datamodel.Synonym;
import com.hartwig.actin.algo.doid.datamodel.Xref;
import com.hartwig.actin.json.JsonDatamodelChecker;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class DoidJson {

    private static final Logger LOGGER = LogManager.getLogger(DoidJson.class);

    @VisibleForTesting
    static final String ID_TO_READ = "http://purl.obolibrary.org/obo/doid.owl";

    private DoidJson() {
    }

    @NotNull
    public static Entry readDoidOwlEntryFromDoidJson(@NotNull String doidJsonFile) throws IOException {
        JsonReader reader = new JsonReader(new FileReader(doidJsonFile));
        reader.setLenient(true);

        JsonObject rootObject = JsonParser.parseReader(reader).getAsJsonObject();
        DatamodelCheckerFactory.rootObjectChecker().check(rootObject);

        Entry entry = null;
        JsonDatamodelChecker graphsChecker = DatamodelCheckerFactory.graphsChecker();
        for (JsonElement element : array(rootObject, "graphs")) {
            JsonObject graph = element.getAsJsonObject();
            graphsChecker.check(graph);

            String id = string(graph, "id");
            if (id.equals(ID_TO_READ)) {
                LOGGER.debug(" Reading DOID entry with ID '{}'", id);

                entry = ImmutableEntry.builder()
                        .id(id)
                        .nodes(extractNodes(array(graph, "nodes")))
                        .edges(extractEdges(array(graph, "edges")))
                        .metadata(extractGraphMetaNode(object(graph, "meta")))
                        .logicalDefinitionAxioms(extractLogicalDefinitionAxioms(array(graph, "logicalDefinitionAxioms")))
                        .equivalentNodesSets(optionalStringList(graph, "equivalentNodesSets"))
                        .domainRangeAxioms(optionalStringList(graph, "domainRangeAxioms"))
                        .propertyChainAxioms(optionalStringList(graph, ("propertyChainAxioms")))
                        .build();
            }
        }

        if (reader.peek() != JsonToken.END_DOCUMENT) {
            LOGGER.warn("More data found in {} after reading main JSON object!", doidJsonFile);
        }

        if (entry == null) {
            throw new IllegalStateException("Could not read DOID entry with ID '" + ID_TO_READ + "'");
        }

        return entry;
    }

    @NotNull
    public static String extractDoid(@NotNull String url) {
        return url.replace("http://purl.obolibrary.org/obo/DOID_", "");
    }

    @NotNull
    private static List<Node> extractNodes(@NotNull JsonArray nodeArray) {
        List<Node> nodes = Lists.newArrayList();

        JsonDatamodelChecker nodeChecker = DatamodelCheckerFactory.nodeChecker();
        for (JsonElement nodeElement : nodeArray) {
            JsonObject node = nodeElement.getAsJsonObject();
            nodeChecker.check(node);

            String id = string(node, "id");
            nodes.add(ImmutableNode.builder()
                    .doid(extractDoid(id))
                    .url(id)
                    .metadata(extractMetadata(optionalObject(node, "meta")))
                    .type(optionalString(node, "type"))
                    .term(optionalString(node, "lbl"))
                    .build());
        }

        return nodes;
    }

    @NotNull
    private static List<Edge> extractEdges(@NotNull JsonArray edgeArray) {
        List<Edge> edges = Lists.newArrayList();

        JsonDatamodelChecker edgeChecker = DatamodelCheckerFactory.edgeChecker();
        for (JsonElement edgeElement : edgeArray) {
            JsonObject edge = edgeElement.getAsJsonObject();
            edgeChecker.check(edge);

            String predicate = string(edge, "pred");
            String object = string(edge, "obj");
            String subject = string(edge, "sub");

            edges.add(ImmutableEdge.builder().predicate(predicate).subject(subject).object(object).build());
        }

        return edges;
    }

    @NotNull
    private static GraphMetadata extractGraphMetaNode(@Nullable JsonObject metaObject) {
        DatamodelCheckerFactory.graphMetadataChecker().check(metaObject);

        JsonArray xrefArray = metaObject.getAsJsonArray("xrefs");
        List<Xref> xrefValList = Lists.newArrayList();
        if (xrefArray != null) {
            JsonDatamodelChecker xrefChecker = DatamodelCheckerFactory.metadataXrefChecker();

            for (JsonElement xrefElement : xrefArray) {
                JsonObject xref = xrefElement.getAsJsonObject();

                xrefChecker.check(xref);
                xrefValList.add(ImmutableXref.builder().val(string(xref, "val")).build());
            }
        }

        return ImmutableGraphMetadata.builder()
                .basicPropertyValues(extractBasicPropertyValues(optionalArray(metaObject, "basicPropertyValues")))
                .subsets(optionalStringList(metaObject, "subsets"))
                .xrefs(xrefValList)
                .version(optionalString(metaObject, "version"))
                .build();
    }

    @Nullable
    private static List<BasicPropertyValue> extractBasicPropertyValues(@Nullable JsonArray basicPropertyValueArray) {
        if (basicPropertyValueArray == null) {
            return null;
        }

        List<BasicPropertyValue> basicPropertyValueList = Lists.newArrayList();

        JsonDatamodelChecker basicPropertyValuesChecker = DatamodelCheckerFactory.basicPropertyValueChecker();
        for (JsonElement basicPropertyElement : basicPropertyValueArray) {
            JsonObject basicProperty = basicPropertyElement.getAsJsonObject();
            basicPropertyValuesChecker.check(basicProperty);

            basicPropertyValueList.add(ImmutableBasicPropertyValue.builder()
                    .pred(string(basicProperty, "pred"))
                    .val(string(basicProperty, "val"))
                    .build());
        }

        return basicPropertyValueList;
    }

    @Nullable
    private static List<LogicalDefinitionAxioms> extractLogicalDefinitionAxioms(@Nullable JsonArray logicalDefinitionAxiomArray) {
        if (logicalDefinitionAxiomArray == null) {
            return null;
        }

        List<LogicalDefinitionAxioms> logicalDefinitionAxioms = Lists.newArrayList();

        JsonDatamodelChecker logicalDefinitionAxiomsChecker = DatamodelCheckerFactory.logicalDefinitionAxiomChecker();
        for (JsonElement logicalDefinitionAxiomElement : logicalDefinitionAxiomArray) {
            JsonObject logicalDefinitionAxiom = logicalDefinitionAxiomElement.getAsJsonObject();
            logicalDefinitionAxiomsChecker.check(logicalDefinitionAxiom);

            JsonDatamodelChecker restrictionChecker = DatamodelCheckerFactory.restrictionChecker();
            List<Restriction> restrictionList = Lists.newArrayList();
            for (JsonElement restrictionElement : array(logicalDefinitionAxiom, "restrictions")) {
                if (restrictionElement.isJsonObject()) {
                    JsonObject restriction = restrictionElement.getAsJsonObject();
                    restrictionChecker.check(restriction);

                    restrictionList.add(ImmutableRestriction.builder()
                            .propertyId(string(restriction, "propertyId"))
                            .fillerId(string(restriction, "fillerId"))
                            .build());
                }
            }

            List<String> genusIdList = Lists.newArrayList();
            for (JsonElement genusIdElement : array(logicalDefinitionAxiom, "genusIds")) {
                genusIdList.add(genusIdElement.getAsString());
            }

            logicalDefinitionAxioms.add(ImmutableLogicalDefinitionAxioms.builder()
                    .definedClassId(string(logicalDefinitionAxiom, "definedClassId"))
                    .genusIds(genusIdList)
                    .restrictions(restrictionList)
                    .build());
        }

        return logicalDefinitionAxioms;
    }

    @Nullable
    private static Metadata extractMetadata(@Nullable JsonObject metadataObject) {
        if (metadataObject == null) {
            return null;
        }

        DatamodelCheckerFactory.metadataChecker().check(metadataObject);

        JsonArray xrefArray = metadataObject.getAsJsonArray("xrefs");
        List<Xref> xrefs = Lists.newArrayList();
        if (xrefArray != null) {
            JsonDatamodelChecker xrefChecker = DatamodelCheckerFactory.metadataXrefChecker();
            for (JsonElement xrefElement : xrefArray) {
                JsonObject xref = xrefElement.getAsJsonObject();
                xrefChecker.check(xref);

                xrefs.add(ImmutableXref.builder().val(string(xref, "val")).build());
            }
        }

        return ImmutableMetadata.builder()
                .synonyms(extractSynonyms(optionalArray(metadataObject, "synonyms")))
                .basicPropertyValues(extractBasicPropertyValues(optionalArray(metadataObject, "basicPropertyValues")))
                .definition(extractDefinition(optionalObject(metadataObject, "definition")))
                .subsets(optionalStringList(metadataObject, "subsets"))
                .xrefs(xrefs)
                .snomedConceptId(extractSnomedConceptId(xrefs))
                .build();
    }

    @Nullable
    @VisibleForTesting
    static String extractSnomedConceptId(@Nullable List<Xref> xrefs) {
        if (xrefs == null) {
            return null;
        }

        for (Xref xref : xrefs) {
            // Format to look for is SNOMEDCT_US_2020_03_01:109355002
            if (xref.val().contains("SNOMED")) {
                String[] parts = xref.val().split(":");
                if (parts.length == 2 && isLong(parts[1])) {
                    return parts[1];
                } else {
                    LOGGER.warn("Unexpected SNOMED entry found: {}", xref.val());
                }
            }
        }
        return null;
    }

    private static boolean isLong(@NotNull String string) {
        try {
            Long.parseLong(string);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    @Nullable
    private static List<Synonym> extractSynonyms(@Nullable JsonArray synonymArray) {
        if (synonymArray == null) {
            return null;
        }

        JsonDatamodelChecker synonymChecker = DatamodelCheckerFactory.synonymChecker();
        List<Synonym> synonymList = Lists.newArrayList();
        for (JsonElement synonymElement : synonymArray) {
            JsonObject synonym = synonymElement.getAsJsonObject();
            synonymChecker.check(synonym);

            synonymList.add(ImmutableSynonym.builder()
                    .pred(string(synonym, "pred"))
                    .val(string(synonym, "val"))
                    .xrefs(stringList(synonym, "xrefs"))
                    .build());
        }

        return synonymList;
    }

    @Nullable
    private static Definition extractDefinition(@Nullable JsonObject definitionObject) {
        if (definitionObject == null) {
            return null;
        }

        DatamodelCheckerFactory.definitionChecker().check(definitionObject);

        return ImmutableDefinition.builder()
                .definitionVal(string(definitionObject, "val"))
                .definitionXrefs(stringList(definitionObject, "xrefs"))
                .build();
    }
}
