package com.hartwig.actin.molecular.serialization;

import static com.hartwig.actin.json.Json.array;
import static com.hartwig.actin.json.Json.bool;
import static com.hartwig.actin.json.Json.nullableDate;
import static com.hartwig.actin.json.Json.object;
import static com.hartwig.actin.json.Json.string;
import static com.hartwig.actin.json.Json.stringList;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.hartwig.actin.molecular.datamodel.EvidenceDirection;
import com.hartwig.actin.molecular.datamodel.EvidenceLevel;
import com.hartwig.actin.molecular.datamodel.ImmutableMolecularRecord;
import com.hartwig.actin.molecular.datamodel.ImmutableMolecularTreatmentEvidence;
import com.hartwig.actin.molecular.datamodel.MolecularExperimentType;
import com.hartwig.actin.molecular.datamodel.MolecularRecord;
import com.hartwig.actin.molecular.datamodel.MolecularTreatmentEvidence;

import org.jetbrains.annotations.NotNull;

public final class MolecularRecordJson {

    private MolecularRecordJson() {
    }

    @NotNull
    public static MolecularRecord read(@NotNull String molecularJson) throws IOException {
        Gson gson = new GsonBuilder().registerTypeAdapter(MolecularRecord.class, new MolecularRecordCreator()).create();

        String json = Files.readString(new File(molecularJson).toPath());
        return gson.fromJson(json, MolecularRecord.class);
    }

    private static class MolecularRecordCreator implements JsonDeserializer<MolecularRecord> {

        @Override
        public MolecularRecord deserialize(@NotNull JsonElement jsonElement, @NotNull Type type,
                @NotNull JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            JsonObject record = jsonElement.getAsJsonObject();

            JsonObject purple = object(record, "purple");
            JsonObject purpleQc = object(purple, "qc");
            return ImmutableMolecularRecord.builder()
                    .sampleId(string(record, "sampleId"))
                    .date(nullableDate(record, "reportDate"))
                    .hasReliableQuality(bool(purple, "hasReliableQuality"))
                    .type(MolecularExperimentType.WGS)
                    .configuredPrimaryTumorDoids(extractDoids(array(record, "configuredPrimaryTumor")))
                    .evidences(toEvidences(array(record, "protect")))
                    .build();
        }

        @NotNull
        private static Set<String> extractDoids(@NotNull JsonArray configuredPrimaryTumorArray) {
            Set<String> doids = Sets.newHashSet();
            for (JsonElement element : configuredPrimaryTumorArray) {
                JsonObject doidNode = element.getAsJsonObject();
                doids.add(string(doidNode, "doid"));
            }
            return doids;
        }

        @NotNull
        private static List<MolecularTreatmentEvidence> toEvidences(@NotNull JsonArray protectArray) {
            List<MolecularTreatmentEvidence> evidences = Lists.newArrayList();
            for (JsonElement element : protectArray) {
                JsonObject evidence = element.getAsJsonObject();
                boolean reported = bool(evidence, "reported");
                if (reported) {
                    evidences.add(ImmutableMolecularTreatmentEvidence.builder()
                            .genomicEvent(string(evidence, "genomicEvent"))
                            .treatment(string(evidence, "treatment"))
                            .onLabel(bool(evidence, "onLabel"))
                            .level(EvidenceLevel.valueOf(string(evidence, "level")))
                            .direction(EvidenceDirection.valueOf(string(evidence, "direction")))
                            .sources(stringList(evidence, "sources"))
                            .build());
                }
            }
            return evidences;
        }
    }
}
