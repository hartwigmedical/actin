package com.hartwig.actin.molecular.serialization;

import static com.hartwig.actin.json.Json.array;
import static com.hartwig.actin.json.Json.bool;
import static com.hartwig.actin.json.Json.integer;
import static com.hartwig.actin.json.Json.nullableDate;
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
import com.hartwig.actin.molecular.datamodel.ExperimentType;
import com.hartwig.actin.molecular.datamodel.ImmutableMolecularRecord;
import com.hartwig.actin.molecular.datamodel.ImmutableTreatmentEvidence;
import com.hartwig.actin.molecular.datamodel.MolecularRecord;
import com.hartwig.actin.molecular.datamodel.TreatmentEvidence;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class MolecularRecordJson {

    private static final Logger LOGGER = LogManager.getLogger(MolecularRecordJson.class);

    private static final String MICROSATELLITE_STABLE = "MSS";
    private static final String MICROSATELLITE_UNSTABLE = "MSI";

    private static final String HOMOLOGOUS_REPAIR_DEFICIENT = "HR_DEFICIENT";
    private static final String HOMOLOGOUS_REPAIR_PROFICIENT = "HR_PROFICIENT";

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

            // TODO Extract wildtype genes from WGS results.
            // TODO Extract fusion genes & mutations from ACTIN protect results.

            JsonObject purple = object(record, "purple");
            JsonObject chord = object(record, "chord");
            return ImmutableMolecularRecord.builder()
                    .sampleId(string(record, "sampleId"))
                    .date(nullableDate(record, "reportDate"))
                    .hasReliableQuality(bool(purple, "hasReliableQuality"))
                    .type(ExperimentType.WGS)
                    .configuredPrimaryTumorDoids(extractDoids(array(record, "configuredPrimaryTumor")))
                    .mutations(Lists.newArrayList())
                    .fusions(Sets.newHashSet())
                    .wildtypeGenes(Sets.newHashSet())
                    .isMicrosatelliteUnstable(isMSI(string(purple, "microsatelliteStatus")))
                    .isHomologousRepairDeficient(isHRD(string(chord, "hrStatus")))
                    .tumorMutationalBurden(number(purple, "tumorMutationalBurdenPerMb"))
                    .tumorMutationalLoad(integer(purple, "tumorMutationalLoad"))
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

        @Nullable
        private static Boolean isMSI(@NotNull String microsatelliteStatus) {
            if (microsatelliteStatus.equals(MICROSATELLITE_UNSTABLE)) {
                return true;
            } else if (microsatelliteStatus.equals(MICROSATELLITE_STABLE)) {
                return false;
            }

            LOGGER.warn("Cannot interpret microsatellite status '{}'", microsatelliteStatus);
            return null;
        }

        @Nullable
        private static Boolean isHRD(@NotNull String homologousRepairStatus) {
            if (homologousRepairStatus.equals(HOMOLOGOUS_REPAIR_DEFICIENT)) {
                return true;
            } else if (homologousRepairStatus.equals(HOMOLOGOUS_REPAIR_PROFICIENT)) {
                return false;
            }

            LOGGER.warn("Cannot interpret homologous repair status '{}'", homologousRepairStatus);
            return null;
        }

        @NotNull
        private static List<TreatmentEvidence> toEvidences(@NotNull JsonArray protectArray) {
            List<TreatmentEvidence> evidences = Lists.newArrayList();
            for (JsonElement element : protectArray) {
                JsonObject evidence = element.getAsJsonObject();
                boolean reported = bool(evidence, "reported");
                if (reported) {
                    evidences.add(ImmutableTreatmentEvidence.builder()
                            .gene(nullableString(evidence, "gene"))
                            .event(string(evidence, "event"))
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
