package com.hartwig.actin.molecular.serialization;

import static com.hartwig.actin.util.Json.array;
import static com.hartwig.actin.util.Json.bool;
import static com.hartwig.actin.util.Json.object;
import static com.hartwig.actin.util.Json.string;

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
import com.hartwig.actin.molecular.datamodel.GenomicTreatmentEvidence;
import com.hartwig.actin.molecular.datamodel.ImmutableGenomicTreatmentEvidence;
import com.hartwig.actin.molecular.datamodel.ImmutableMolecularRecord;
import com.hartwig.actin.molecular.datamodel.MolecularRecord;
import com.hartwig.actin.molecular.util.GenomicEventFormatter;

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
            return ImmutableMolecularRecord.builder()
                    .sampleId(string(record, "sampleId"))
                    .hasReliableQuality(bool(purple, "hasReliableQuality"))
                    .configuredPrimaryTumorDoids(extractDoids(array(record, "configuredPrimaryTumor")))
                    .genomicTreatmentEvidences(toGenomicTreatmentEvidences(array(record, "protect")))
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
        private static List<GenomicTreatmentEvidence> toGenomicTreatmentEvidences(@NotNull JsonArray protectArray) {
            List<GenomicTreatmentEvidence> evidences = Lists.newArrayList();
            for (JsonElement element : protectArray) {
                JsonObject evidence = element.getAsJsonObject();
                boolean reported = bool(evidence, "reported");
                if (reported) {
                    evidences.add(ImmutableGenomicTreatmentEvidence.builder()
                            .genomicEvent(GenomicEventFormatter.format(string(evidence, "genomicEvent")))
                            .treatment(string(evidence, "treatment"))
                            .onLabel(bool(evidence, "onLabel"))
                            .level(EvidenceLevel.valueOf(string(evidence, "level")))
                            .direction(EvidenceDirection.valueOf(string(evidence, "direction")))
                            .build());
                }
            }
            return evidences;
        }
    }
}
