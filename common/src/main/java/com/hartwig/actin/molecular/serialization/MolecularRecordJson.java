package com.hartwig.actin.molecular.serialization;

import static com.hartwig.actin.util.json.Json.array;
import static com.hartwig.actin.util.json.Json.bool;
import static com.hartwig.actin.util.json.Json.integer;
import static com.hartwig.actin.util.json.Json.nullableBool;
import static com.hartwig.actin.util.json.Json.nullableDate;
import static com.hartwig.actin.util.json.Json.nullableInteger;
import static com.hartwig.actin.util.json.Json.nullableNumber;
import static com.hartwig.actin.util.json.Json.nullableObject;
import static com.hartwig.actin.util.json.Json.number;
import static com.hartwig.actin.util.json.Json.object;
import static com.hartwig.actin.util.json.Json.string;
import static com.hartwig.actin.util.json.Json.stringList;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.util.List;
import java.util.Set;

import com.google.common.annotations.VisibleForTesting;
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
import com.hartwig.actin.molecular.datamodel.ExperimentType;
import com.hartwig.actin.molecular.datamodel.ImmutableMolecularRecord;
import com.hartwig.actin.molecular.datamodel.MolecularRecord;
import com.hartwig.actin.molecular.datamodel.characteristics.ImmutableMolecularCharacteristics;
import com.hartwig.actin.molecular.datamodel.characteristics.ImmutablePredictedTumorOrigin;
import com.hartwig.actin.molecular.datamodel.characteristics.MolecularCharacteristics;
import com.hartwig.actin.molecular.datamodel.characteristics.PredictedTumorOrigin;
import com.hartwig.actin.molecular.datamodel.driver.Amplification;
import com.hartwig.actin.molecular.datamodel.driver.Disruption;
import com.hartwig.actin.molecular.datamodel.driver.Fusion;
import com.hartwig.actin.molecular.datamodel.driver.FusionDriverType;
import com.hartwig.actin.molecular.datamodel.driver.ImmutableAmplification;
import com.hartwig.actin.molecular.datamodel.driver.ImmutableDisruption;
import com.hartwig.actin.molecular.datamodel.driver.ImmutableFusion;
import com.hartwig.actin.molecular.datamodel.driver.ImmutableLoss;
import com.hartwig.actin.molecular.datamodel.driver.ImmutableMolecularDrivers;
import com.hartwig.actin.molecular.datamodel.driver.ImmutableVariant;
import com.hartwig.actin.molecular.datamodel.driver.ImmutableVirus;
import com.hartwig.actin.molecular.datamodel.driver.Loss;
import com.hartwig.actin.molecular.datamodel.driver.MolecularDrivers;
import com.hartwig.actin.molecular.datamodel.driver.Variant;
import com.hartwig.actin.molecular.datamodel.driver.VariantDriverType;
import com.hartwig.actin.molecular.datamodel.driver.Virus;
import com.hartwig.actin.molecular.datamodel.evidence.EvidenceEntry;
import com.hartwig.actin.molecular.datamodel.evidence.ImmutableEvidenceEntry;
import com.hartwig.actin.molecular.datamodel.evidence.ImmutableMolecularEvidence;
import com.hartwig.actin.molecular.datamodel.evidence.MolecularEvidence;
import com.hartwig.actin.molecular.datamodel.mapping.FusionGene;
import com.hartwig.actin.molecular.datamodel.mapping.GeneMutation;
import com.hartwig.actin.molecular.datamodel.mapping.ImmutableFusionGene;
import com.hartwig.actin.molecular.datamodel.mapping.ImmutableGeneMutation;
import com.hartwig.actin.molecular.datamodel.mapping.ImmutableInactivatedGene;
import com.hartwig.actin.molecular.datamodel.mapping.ImmutableMappedActinEvents;
import com.hartwig.actin.molecular.datamodel.mapping.InactivatedGene;
import com.hartwig.actin.molecular.datamodel.mapping.MappedActinEvents;
import com.hartwig.actin.molecular.datamodel.pharmaco.ImmutablePharmacoEntry;
import com.hartwig.actin.molecular.datamodel.pharmaco.PharmacoEntry;
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
                    .qc(string(record, "qc"))
                    .characteristics(toMolecularCharacteristics(object(record, "characteristics")))
                    .drivers(toMolecularDrivers(object(record, "drivers")))
                    .pharmaco(toPharmacoEntries(array(record, "pharmaco")))
                    .evidence(toMolecularEvidence(object(record, "evidence")))
                    .mappedEvents(toMappedActinEvents(object(record, "mappedEvents")))
                    .build();
        }

        @NotNull
        private static MolecularCharacteristics toMolecularCharacteristics(@NotNull JsonObject genome) {
            return ImmutableMolecularCharacteristics.builder()
                    .purity(nullableNumber(genome, "purity"))
                    .predictedTumorOrigin(toPredictedTumorOrigin(nullableObject(genome, "predictedTumorOrigin")))
                    .isMicrosatelliteUnstable(nullableBool(genome, "isMicrosatelliteUnstable"))
                    .isHomologousRepairDeficient(nullableBool(genome, "isHomologousRepairDeficient"))
                    .tumorMutationalBurden(nullableNumber(genome, "tumorMutationalBurden"))
                    .tumorMutationalLoad(nullableInteger(genome, "tumorMutationalLoad"))
                    .build();
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
        private static MolecularDrivers toMolecularDrivers(@NotNull JsonObject drivers) {
            return ImmutableMolecularDrivers.builder()
                    .variants(toVariants(array(drivers, "variants")))
                    .amplifications(toAmplifications(array(drivers, "amplifications")))
                    .losses(toLosses(array(drivers, "losses")))
                    .disruptions(toDisruptions(array(drivers, "disruptions")))
                    .fusions(toFusions(array(drivers, "fusions")))
                    .viruses(toViruses(array(drivers, "viruses")))
                    .build();
        }

        @NotNull
        private static Set<Variant> toVariants(@NotNull JsonArray variantArray) {
            Set<Variant> variants = Sets.newHashSet();
            for (JsonElement element : variantArray) {
                JsonObject variant = element.getAsJsonObject();
                variants.add(ImmutableVariant.builder()
                        .gene(string(variant, "gene"))
                        .impact(string(variant, "impact"))
                        .variantCopyNumber(number(variant, "variantCopyNumber"))
                        .totalCopyNumber(number(variant, "totalCopyNumber"))
                        .driverType(VariantDriverType.valueOf(string(variant, "variantDriverType")))
                        .driverLikelihood(number(variant, "driverLikelihood"))
                        .subclonalLikelihood(number(variant, "subclonalLikelihood"))
                        .build());
            }
            return variants;
        }

        @NotNull
        private static Set<Amplification> toAmplifications(@NotNull JsonArray amplificationArray) {
            Set<Amplification> amplifications = Sets.newHashSet();
            for (JsonElement element : amplificationArray) {
                JsonObject amplification = element.getAsJsonObject();
                amplifications.add(ImmutableAmplification.builder()
                        .gene(string(amplification, "gene"))
                        .copies(integer(amplification, "copies"))
                        .isPartial(bool(amplification, "isPartial"))
                        .build());
            }
            return amplifications;
        }

        @NotNull
        private static Set<Loss> toLosses(@NotNull JsonArray lossArray) {
            Set<Loss> losses = Sets.newHashSet();
            for (JsonElement element : lossArray) {
                JsonObject loss = element.getAsJsonObject();
                losses.add(ImmutableLoss.builder().gene(string(loss, "gene")).isPartial(bool(loss, "isPartial")).build());
            }
            return losses;
        }

        @NotNull
        private static Set<Disruption> toDisruptions(@NotNull JsonArray disruptionArray) {
            Set<Disruption> disruptions = Sets.newHashSet();
            for (JsonElement element : disruptionArray) {
                JsonObject disruption = element.getAsJsonObject();
                disruptions.add(ImmutableDisruption.builder()
                        .gene(string(disruption, "gene"))
                        .isHomozygous(bool(disruption, "isHomozygous"))
                        .details(string(disruption, "details"))
                        .build());
            }
            return disruptions;
        }

        @NotNull
        private static Set<Fusion> toFusions(@NotNull JsonArray fusionArray) {
            Set<Fusion> fusions = Sets.newHashSet();
            for (JsonElement element : fusionArray) {
                JsonObject fusion = element.getAsJsonObject();
                fusions.add(ImmutableFusion.builder()
                        .fiveGene(string(fusion, "fiveGene"))
                        .threeGene(string(fusion, "threeGene"))
                        .details(string(fusion, "details"))
                        .driverType(FusionDriverType.valueOf(string(fusion, "threeGene")))
                        .driverLikelihood(string(fusion, "driverLikelihood"))
                        .build());
            }
            return fusions;
        }

        @NotNull
        private static Set<Virus> toViruses(@NotNull JsonArray virusArray) {
            Set<Virus> viruses = Sets.newHashSet();
            for (JsonElement element : virusArray) {
                JsonObject virus = element.getAsJsonObject();
                viruses.add(ImmutableVirus.builder()
                        .name(string(virus, "name"))
                        .details(string(virus, "details"))
                        .driverLikelihood(string(virus, "driverLikelihood"))
                        .build());
            }
            return viruses;
        }

        @NotNull
        private static List<PharmacoEntry> toPharmacoEntries(@NotNull JsonArray pharmacoArray) {
            List<PharmacoEntry> pharmacoEntries = Lists.newArrayList();
            for (JsonElement element : pharmacoArray) {
                JsonObject pharmaco = element.getAsJsonObject();
                pharmacoEntries.add(ImmutablePharmacoEntry.builder()
                        .gene(string(pharmaco, "gene"))
                        .result(string(pharmaco, "result"))
                        .build());
            }
            return pharmacoEntries;
        }

        @NotNull
        private static MappedActinEvents toMappedActinEvents(@NotNull JsonObject mappedEvents) {
            return ImmutableMappedActinEvents.builder()
                    .mutations(toGeneMutations(array(mappedEvents, "mutations")))
                    .activatedGenes(stringList(mappedEvents, "activatedGenes"))
                    .inactivatedGenes(toInactivatedGenes(array(mappedEvents, "inactivatedGenes")))
                    .amplifiedGenes(stringList(mappedEvents, "amplifiedGenes"))
                    .wildtypeGenes(stringList(mappedEvents, "wildtypeGenes"))
                    .fusions(toFusionGenes(array(mappedEvents, "fusions")))
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

        @NotNull
        private static MolecularEvidence toMolecularEvidence(@NotNull JsonObject evidence) {
            return ImmutableMolecularEvidence.builder()
                    .actinSource(string(evidence, "actinSource"))
                    .actinTrials(toEvidences(array(evidence, "actinTrials")))
                    .externalTrialSource(string(evidence, "externalTrialSource"))
                    .externalTrials(toEvidences(array(evidence, "externalTrials")))
                    .evidenceSource(string(evidence, "evidenceSource"))
                    .approvedResponsiveEvidence(toEvidences(array(evidence, "approvedResponsiveEvidence")))
                    .experimentalResponsiveEvidence(toEvidences(array(evidence, "experimentalResponsiveEvidence")))
                    .otherResponsiveEvidence(toEvidences(array(evidence, "otherResponsiveEvidence")))
                    .resistanceEvidence(toEvidences(array(evidence, "resistanceEvidence")))
                    .build();
        }

        @NotNull
        private static Set<EvidenceEntry> toEvidences(@NotNull JsonArray evidenceArray) {
            Set<EvidenceEntry> evidences = Sets.newHashSet();
            for (JsonElement element : evidenceArray) {
                JsonObject object = element.getAsJsonObject();
                evidences.add(ImmutableEvidenceEntry.builder()
                        .event(string(object, "event"))
                        .treatment(string(object, "treatment"))
                        .build());
            }
            return evidences;
        }
    }
}
