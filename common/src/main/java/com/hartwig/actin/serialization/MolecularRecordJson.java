package com.hartwig.actin.serialization;

import static com.hartwig.actin.serialization.JsonFunctions.array;
import static com.hartwig.actin.serialization.JsonFunctions.bool;
import static com.hartwig.actin.serialization.JsonFunctions.object;
import static com.hartwig.actin.serialization.JsonFunctions.string;

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
import com.hartwig.actin.datamodel.molecular.EvidenceDirection;
import com.hartwig.actin.datamodel.molecular.EvidenceLevel;
import com.hartwig.actin.datamodel.molecular.GenomicTreatmentEvidence;
import com.hartwig.actin.datamodel.molecular.ImmutableGenomicTreatmentEvidence;
import com.hartwig.actin.datamodel.molecular.ImmutableMolecularRecord;
import com.hartwig.actin.datamodel.molecular.MolecularRecord;

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
                    .hasReliablePurity(bool(purple, "hasReliablePurity"))
                    .genomicTreatmentEvidences(toGenomicTreatmentEvidences(array(record, "protect")))
                    .build();
        }

        @NotNull
        private static List<GenomicTreatmentEvidence> toGenomicTreatmentEvidences(@NotNull JsonArray protect) {
            List<GenomicTreatmentEvidence> evidences = Lists.newArrayList();
            for (JsonElement element : protect) {
                JsonObject evidence = element.getAsJsonObject();
                boolean reported = bool(evidence, "reported");
                if (reported) {
                    evidences.add(ImmutableGenomicTreatmentEvidence.builder()
                            .genomicEvent(string(evidence, "genomicEvent"))
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
