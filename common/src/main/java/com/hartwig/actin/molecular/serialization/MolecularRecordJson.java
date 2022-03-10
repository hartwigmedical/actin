package com.hartwig.actin.molecular.serialization;

import static com.hartwig.actin.util.json.Json.array;
import static com.hartwig.actin.util.json.Json.bool;
import static com.hartwig.actin.util.json.Json.nullableBool;
import static com.hartwig.actin.util.json.Json.nullableDate;
import static com.hartwig.actin.util.json.Json.nullableInteger;
import static com.hartwig.actin.util.json.Json.nullableNumber;
import static com.hartwig.actin.util.json.Json.nullableObject;
import static com.hartwig.actin.util.json.Json.number;
import static com.hartwig.actin.util.json.Json.string;
import static com.hartwig.actin.util.json.Json.stringList;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.util.Set;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.hartwig.actin.molecular.datamodel.ExperimentType;
import com.hartwig.actin.molecular.datamodel.FusionGene;
import com.hartwig.actin.molecular.datamodel.GeneMutation;
import com.hartwig.actin.molecular.datamodel.ImmutableFusionGene;
import com.hartwig.actin.molecular.datamodel.ImmutableGeneMutation;
import com.hartwig.actin.molecular.datamodel.ImmutableInactivatedGene;
import com.hartwig.actin.molecular.datamodel.ImmutableMolecularEvidence;
import com.hartwig.actin.molecular.datamodel.ImmutableMolecularRecord;
import com.hartwig.actin.molecular.datamodel.ImmutablePredictedTumorOrigin;
import com.hartwig.actin.molecular.datamodel.InactivatedGene;
import com.hartwig.actin.molecular.datamodel.MolecularEvidence;
import com.hartwig.actin.molecular.datamodel.MolecularRecord;
import com.hartwig.actin.molecular.datamodel.PredictedTumorOrigin;
import com.hartwig.actin.util.Paths;
import com.hartwig.actin.util.json.GsonSerializer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MolecularRecordJson {

    private static final Logger LOGGER = LogManager.getLogger(MolecularRecordJson.class);

    private static final String MOLECULAR_JSON_EXTENSION = ".molecular.json";

    private MolecularRecordJson() {
    }

    public static void write(@NotNull MolecularRecord record, @NotNull String directory) throws IOException {
        String path = Paths.forceTrailingFileSeparator(directory);
        String jsonFile = path + record.sampleId() + MOLECULAR_JSON_EXTENSION;

        LOGGER.info("Writing molecular record to {}", jsonFile);
        BufferedWriter writer = new BufferedWriter(new FileWriter(jsonFile));
        writer.write(toJson(record));
        writer.close();
    }

    @NotNull
    public static MolecularRecord read(@NotNull String molecularJson) throws IOException {
        return fromJson(Files.readString(new File(molecularJson).toPath()));
    }

    @VisibleForTesting
    @NotNull
    static String toJson(@NotNull MolecularRecord record) {
        return GsonSerializer.create().toJson(record);
    }

    @VisibleForTesting
    @NotNull
    static MolecularRecord fromJson(@NotNull String json) {
        Gson gson = new GsonBuilder().registerTypeAdapter(MolecularRecord.class, new MolecularRecordCreator()).create();
        return gson.fromJson(json, MolecularRecord.class);
    }

    private static class MolecularRecordCreator implements JsonDeserializer<MolecularRecord> {

        @Override
        public MolecularRecord deserialize(@NotNull JsonElement jsonElement, @NotNull Type type,
                @NotNull JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            JsonObject record = jsonElement.getAsJsonObject();

            return ImmutableMolecularRecord.builder()
                    .sampleId(string(record, "sampleId"))
                    .type(ExperimentType.valueOf(string(record, "type")))
                    .date(nullableDate(record, "date"))
                    .hasReliableQuality(bool(record, "hasReliableQuality"))
                    .mutations(toGeneMutations(array(record, "mutations")))
                    .activatedGenes(stringList(record, "activatedGenes"))
                    .inactivatedGenes(toInactivatedGenes(array(record, "inactivatedGenes")))
                    .amplifiedGenes(stringList(record, "amplifiedGenes"))
                    .wildtypeGenes(stringList(record, "wildtypeGenes"))
                    .fusions(toFusionGenes(array(record, "fusions")))
                    .predictedTumorOrigin(toPredictedTumorOrigin(nullableObject(record, "predictedTumorOrigin")))
                    .isMicrosatelliteUnstable(nullableBool(record, "isMicrosatelliteUnstable"))
                    .isHomologousRepairDeficient(nullableBool(record, "isHomologousRepairDeficient"))
                    .tumorMutationalBurden(nullableNumber(record, "tumorMutationalBurden"))
                    .tumorMutationalLoad(nullableInteger(record, "tumorMutationalLoad"))
                    .actinSource(string(record, "actinSource"))
                    .actinTrials(toEvidences(array(record, "actinTrials")))
                    .externalTrialSource(string(record, "externalTrialSource"))
                    .externalTrials(toEvidences(array(record, "externalTrials")))
                    .evidenceSource(string(record, "evidenceSource"))
                    .approvedResponsiveEvidence(toEvidences(array(record, "approvedResponsiveEvidence")))
                    .experimentalResponsiveEvidence(toEvidences(array(record, "experimentalResponsiveEvidence")))
                    .otherResponsiveEvidence(toEvidences(array(record, "otherResponsiveEvidence")))
                    .resistanceEvidence(toEvidences(array(record, "resistanceEvidence")))
                    .build();
        }

        @NotNull
        private static Set<GeneMutation> toGeneMutations(@NotNull JsonArray mutations) {
            Set<GeneMutation> geneMutationSet = Sets.newHashSet();
            for (JsonElement element : mutations) {
                JsonObject object = element.getAsJsonObject();
                geneMutationSet.add(ImmutableGeneMutation.builder()
                        .gene(string(object, "gene"))
                        .mutation(string(object, "mutation"))
                        .build());
            }
            return geneMutationSet;
        }

        @NotNull
        private static Set<InactivatedGene> toInactivatedGenes(@NotNull JsonArray inactivatedGenes) {
            Set<InactivatedGene> inactivatedGeneSet = Sets.newHashSet();
            for (JsonElement element : inactivatedGenes) {
                JsonObject object = element.getAsJsonObject();
                inactivatedGeneSet.add(ImmutableInactivatedGene.builder()
                        .gene(string(object, "gene"))
                        .hasBeenDeleted(bool(object, "hasBeenDeleted"))
                        .build());
            }
            return inactivatedGeneSet;
        }

        @NotNull
        private static Set<FusionGene> toFusionGenes(@NotNull JsonArray fusions) {
            Set<FusionGene> fusionGeneSet = Sets.newHashSet();
            for (JsonElement element : fusions) {
                JsonObject object = element.getAsJsonObject();
                fusionGeneSet.add(ImmutableFusionGene.builder()
                        .fiveGene(string(object, "fiveGene"))
                        .threeGene(string(object, "threeGene"))
                        .build());
            }
            return fusionGeneSet;
        }

        @Nullable
        private static PredictedTumorOrigin toPredictedTumorOrigin(@Nullable JsonObject predictedTumorOrigin) {
            if (predictedTumorOrigin == null) {
                return null;
            }

            return ImmutablePredictedTumorOrigin.builder()
                    .tumorType(string(predictedTumorOrigin, "tumorType"))
                    .likelihood(number(predictedTumorOrigin, "likelihood"))
                    .build();
        }

        @NotNull
        private static Set<MolecularEvidence> toEvidences(@NotNull JsonArray evidenceArray) {
            Set<MolecularEvidence> evidences = Sets.newHashSet();
            for (JsonElement element : evidenceArray) {
                JsonObject object = element.getAsJsonObject();
                evidences.add(ImmutableMolecularEvidence.builder()
                        .event(string(object, "event"))
                        .treatment(string(object, "treatment"))
                        .build());
            }
            return evidences;
        }
    }
}
