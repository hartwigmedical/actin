package com.hartwig.actin.molecular.orange.serialization;

import static com.hartwig.actin.json.Json.array;
import static com.hartwig.actin.json.Json.bool;
import static com.hartwig.actin.json.Json.integer;
import static com.hartwig.actin.json.Json.nullableDate;
import static com.hartwig.actin.json.Json.nullableInteger;
import static com.hartwig.actin.json.Json.nullableString;
import static com.hartwig.actin.json.Json.number;
import static com.hartwig.actin.json.Json.object;
import static com.hartwig.actin.json.Json.string;
import static com.hartwig.actin.json.Json.stringList;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.util.List;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.hartwig.actin.molecular.orange.datamodel.EvidenceDirection;
import com.hartwig.actin.molecular.orange.datamodel.EvidenceLevel;
import com.hartwig.actin.molecular.orange.datamodel.EvidenceType;
import com.hartwig.actin.molecular.orange.datamodel.ImmutableOrangeRecord;
import com.hartwig.actin.molecular.orange.datamodel.ImmutableTreatmentEvidence;
import com.hartwig.actin.molecular.orange.datamodel.OrangeRecord;
import com.hartwig.actin.molecular.orange.datamodel.TreatmentEvidence;

import org.jetbrains.annotations.NotNull;

public final class OrangeJson {

    private OrangeJson() {
    }

    @NotNull
    public static OrangeRecord read(@NotNull String orangeJson) throws IOException {
        Gson gson = new GsonBuilder().registerTypeAdapter(OrangeRecord.class, new OrangeRecordCreator()).create();

        String json = Files.readString(new File(orangeJson).toPath());
        return gson.fromJson(json, OrangeRecord.class);
    }

    private static class OrangeRecordCreator implements JsonDeserializer<OrangeRecord> {

        @Override
        public OrangeRecord deserialize(@NotNull JsonElement jsonElement, @NotNull Type type,
                @NotNull JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            JsonObject record = jsonElement.getAsJsonObject();

            JsonObject purple = object(record, "purple");
            JsonObject chord = object(record, "chord");
            return ImmutableOrangeRecord.builder()
                    .sampleId(string(record, "sampleId"))
                    .date(nullableDate(record, "reportDate"))
                    .hasReliableQuality(bool(purple, "hasReliableQuality"))
                    .microsatelliteStabilityStatus(string(purple, "microsatelliteStatus"))
                    .homologousRepairStatus(string(chord, "hrStatus"))
                    .tumorMutationalBurden(number(purple, "tumorMutationalBurdenPerMb"))
                    .tumorMutationalLoad(integer(purple, "tumorMutationalLoad"))
                    .evidences(toEvidences(array(record, "protect")))
                    .build();
        }

        @NotNull
        private static List<TreatmentEvidence> toEvidences(@NotNull JsonArray protectArray) {
            List<TreatmentEvidence> evidences = Lists.newArrayList();
            for (JsonElement element : protectArray) {
                JsonObject evidence = element.getAsJsonObject();
                evidences.add(ImmutableTreatmentEvidence.builder()
                        .reported(bool(evidence, "reported"))
                        .gene(nullableString(evidence, "gene"))
                        .event(string(evidence, "event"))
                        .rangeRank(nullableInteger(evidence, "rangeRank"))
                        .treatment(string(evidence, "treatment"))
                        .onLabel(bool(evidence, "onLabel"))
                        .type(EvidenceType.valueOf(string(evidence, "evidenceType")))
                        .level(EvidenceLevel.valueOf(string(evidence, "level")))
                        .direction(EvidenceDirection.valueOf(string(evidence, "direction")))
                        .sources(stringList(evidence, "sources"))
                        .build());
            }
            return evidences;
        }
    }
}
