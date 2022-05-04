package com.hartwig.actin.molecular.orange.serialization;

import static com.hartwig.actin.util.json.Json.array;
import static com.hartwig.actin.util.json.Json.bool;
import static com.hartwig.actin.util.json.Json.date;
import static com.hartwig.actin.util.json.Json.integer;
import static com.hartwig.actin.util.json.Json.nullableInteger;
import static com.hartwig.actin.util.json.Json.nullableString;
import static com.hartwig.actin.util.json.Json.number;
import static com.hartwig.actin.util.json.Json.object;
import static com.hartwig.actin.util.json.Json.string;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.util.Set;

import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.hartwig.actin.molecular.orange.datamodel.ImmutableOrangeRecord;
import com.hartwig.actin.molecular.orange.datamodel.OrangeRecord;
import com.hartwig.actin.molecular.orange.datamodel.chord.ChordRecord;
import com.hartwig.actin.molecular.orange.datamodel.chord.ImmutableChordRecord;
import com.hartwig.actin.molecular.orange.datamodel.cuppa.CuppaRecord;
import com.hartwig.actin.molecular.orange.datamodel.cuppa.ImmutableCuppaRecord;
import com.hartwig.actin.molecular.orange.datamodel.linx.FusionDriverLikelihood;
import com.hartwig.actin.molecular.orange.datamodel.linx.FusionType;
import com.hartwig.actin.molecular.orange.datamodel.linx.ImmutableLinxDisruption;
import com.hartwig.actin.molecular.orange.datamodel.linx.ImmutableLinxFusion;
import com.hartwig.actin.molecular.orange.datamodel.linx.ImmutableLinxRecord;
import com.hartwig.actin.molecular.orange.datamodel.linx.LinxDisruption;
import com.hartwig.actin.molecular.orange.datamodel.linx.LinxFusion;
import com.hartwig.actin.molecular.orange.datamodel.linx.LinxRecord;
import com.hartwig.actin.molecular.orange.datamodel.peach.ImmutablePeachEntry;
import com.hartwig.actin.molecular.orange.datamodel.peach.ImmutablePeachRecord;
import com.hartwig.actin.molecular.orange.datamodel.peach.PeachEntry;
import com.hartwig.actin.molecular.orange.datamodel.peach.PeachRecord;
import com.hartwig.actin.molecular.orange.datamodel.protect.EvidenceDirection;
import com.hartwig.actin.molecular.orange.datamodel.protect.EvidenceLevel;
import com.hartwig.actin.molecular.orange.datamodel.protect.EvidenceType;
import com.hartwig.actin.molecular.orange.datamodel.protect.ImmutableProtectEvidence;
import com.hartwig.actin.molecular.orange.datamodel.protect.ImmutableProtectRecord;
import com.hartwig.actin.molecular.orange.datamodel.protect.ImmutableProtectSource;
import com.hartwig.actin.molecular.orange.datamodel.protect.ProtectEvidence;
import com.hartwig.actin.molecular.orange.datamodel.protect.ProtectRecord;
import com.hartwig.actin.molecular.orange.datamodel.protect.ProtectSource;
import com.hartwig.actin.molecular.orange.datamodel.purple.GainLossInterpretation;
import com.hartwig.actin.molecular.orange.datamodel.purple.ImmutablePurpleGainLoss;
import com.hartwig.actin.molecular.orange.datamodel.purple.ImmutablePurpleRecord;
import com.hartwig.actin.molecular.orange.datamodel.purple.ImmutablePurpleVariant;
import com.hartwig.actin.molecular.orange.datamodel.purple.PurpleGainLoss;
import com.hartwig.actin.molecular.orange.datamodel.purple.PurpleRecord;
import com.hartwig.actin.molecular.orange.datamodel.purple.PurpleVariant;
import com.hartwig.actin.molecular.orange.datamodel.purple.VariantHotspot;
import com.hartwig.actin.molecular.orange.datamodel.virus.ImmutableVirusInterpreterEntry;
import com.hartwig.actin.molecular.orange.datamodel.virus.ImmutableVirusInterpreterRecord;
import com.hartwig.actin.molecular.orange.datamodel.virus.VirusDriverLikelihood;
import com.hartwig.actin.molecular.orange.datamodel.virus.VirusInterpreterEntry;
import com.hartwig.actin.molecular.orange.datamodel.virus.VirusInterpreterRecord;

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

            return ImmutableOrangeRecord.builder()
                    .sampleId(string(record, "sampleId"))
                    .reportDate(date(record, "reportDate"))
                    .purple(toPurpleRecord(object(record, "purple")))
                    .linx(toLinxRecord(object(record, "linx")))
                    .peach(toPeachRecord(array(record, "peach")))
                    .cuppa(toCuppaRecord(object(record, "cuppa")))
                    .virusInterpreter(toVirusInterpreterRecord(object(record, "virusInterpreter")))
                    .chord(toChordRecord(object(record, "chord")))
                    .protect(toProtectRecord(array(record, "protect")))
                    .build();
        }

        @NotNull
        private static PurpleRecord toPurpleRecord(@NotNull JsonObject purple) {
            Set<PurpleVariant> variants = Sets.newHashSet();
            variants.addAll(toPurpleVariants(array(purple, "reportableSomaticVariants")));
            variants.addAll(toPurpleVariants(array(purple, "reportableGermlineVariants")));

            return ImmutablePurpleRecord.builder()
                    .hasReliableQuality(bool(purple, "hasReliableQuality"))
                    .purity(number(purple, "purity"))
                    .hasReliablePurity(bool(purple, "hasReliablePurity"))
                    .microsatelliteStabilityStatus(string(purple, "microsatelliteStatus"))
                    .tumorMutationalBurden(number(purple, "tumorMutationalBurdenPerMb"))
                    .tumorMutationalLoad(integer(purple, "tumorMutationalLoad"))
                    .variants(variants)
                    .gainsLosses(toPurpleGainsLosses(array(purple, "reportableGainsLosses")))
                    .build();
        }

        @NotNull
        private static Set<PurpleGainLoss> toPurpleGainsLosses(@NotNull JsonArray reportableGainLossArray) {
            Set<PurpleGainLoss> reportableGainsLosses = Sets.newHashSet();
            for (JsonElement element : reportableGainLossArray) {
                JsonObject reportableGainLoss = element.getAsJsonObject();
                reportableGainsLosses.add(ImmutablePurpleGainLoss.builder()
                        .gene(string(reportableGainLoss, "gene"))
                        .interpretation(GainLossInterpretation.valueOf(string(reportableGainLoss, "interpretation")))
                        .minCopies(integer(reportableGainLoss, "minCopies"))
                        .build());
            }
            return reportableGainsLosses;
        }

        @NotNull
        private static Set<PurpleVariant> toPurpleVariants(@NotNull JsonArray variantArray) {
            Set<PurpleVariant> variants = Sets.newHashSet();
            for (JsonElement element : variantArray) {
                JsonObject variant = element.getAsJsonObject();
                variants.add(ImmutablePurpleVariant.builder()
                        .gene(string(variant, "gene"))
                        .hgvsProteinImpact(string(variant, "canonicalHgvsProteinImpact"))
                        .hgvsCodingImpact(string(variant, "canonicalHgvsCodingImpact"))
                        .effect(string(variant, "canonicalEffect"))
                        .alleleCopyNumber(number(variant, "alleleCopyNumber"))
                        .totalCopyNumber(number(variant, "totalCopyNumber"))
                        .hotspot(VariantHotspot.valueOf(string(variant, "hotspot")))
                        .biallelic(bool(variant, "biallelic"))
                        .driverLikelihood(number(variant, "driverLikelihood"))
                        .clonalLikelihood(number(variant, "clonalLikelihood"))
                        .build());
            }
            return variants;
        }

        @NotNull
        private static LinxRecord toLinxRecord(@NotNull JsonObject linx) {
            return ImmutableLinxRecord.builder()
                    .fusions(toLinxFusions(array(linx, "reportableFusions")))
                    .homozygousDisruptedGenes(toHomozygousDisruptedGenes(array(linx, "homozygousDisruptions")))
                    .disruptions(toLinxDisruptions(array(linx, "geneDisruptions")))
                    .build();
        }

        @NotNull
        private static Set<LinxFusion> toLinxFusions(@NotNull JsonArray reportableFusionArray) {
            Set<LinxFusion> fusions = Sets.newHashSet();
            for (JsonElement element : reportableFusionArray) {
                JsonObject fusion = element.getAsJsonObject();
                fusions.add(ImmutableLinxFusion.builder()
                        .type(FusionType.valueOf(string(fusion, "reportedType")))
                        .geneStart(string(fusion, "geneStart"))
                        .geneContextStart(string(fusion, "geneContextStart"))
                        .geneEnd(string(fusion, "geneEnd"))
                        .geneContextEnd(string(fusion, "geneContextEnd"))
                        .driverLikelihood(FusionDriverLikelihood.valueOf(string(fusion, "likelihood")))
                        .build());
            }
            return fusions;
        }

        @NotNull
        private static Set<String> toHomozygousDisruptedGenes(@NotNull JsonArray homozygousDisruptionArray) {
            Set<String> homozygousDisruptedGenes = Sets.newHashSet();
            for (JsonElement element : homozygousDisruptionArray) {
                JsonObject homozygousDisruption = element.getAsJsonObject();
                homozygousDisruptedGenes.add(string(homozygousDisruption, "gene"));
            }
            return homozygousDisruptedGenes;

        }

        @NotNull
        private static Set<LinxDisruption> toLinxDisruptions(@NotNull JsonArray geneDisruptionArray) {
            Set<LinxDisruption> disruptions = Sets.newHashSet();
            for (JsonElement element : geneDisruptionArray) {
                JsonObject geneDisruption = element.getAsJsonObject();
                disruptions.add(ImmutableLinxDisruption.builder()
                        .gene(string(geneDisruption, "gene"))
                        .type(string(geneDisruption, "type"))
                        .junctionCopyNumber(number(geneDisruption, "junctionCopyNumber"))
                        .undisruptedCopyNumber(number(geneDisruption, "undisruptedCopyNumber"))
                        .range(string(geneDisruption, "range"))
                        .build());
            }
            return disruptions;
        }

        @NotNull
        private static PeachRecord toPeachRecord(@NotNull JsonArray peachArray) {
            Set<PeachEntry> entries = Sets.newHashSet();
            for (JsonElement element : peachArray) {
                JsonObject peach = element.getAsJsonObject();
                entries.add(ImmutablePeachEntry.builder()
                        .gene(string(peach, "gene"))
                        .haplotype(string(peach, "haplotype"))
                        .function(string(peach, "function"))
                        .build());
            }
            return ImmutablePeachRecord.builder().entries(entries).build();
        }

        @NotNull
        private static CuppaRecord toCuppaRecord(@NotNull JsonObject cuppa) {
            return ImmutableCuppaRecord.builder()
                    .predictedCancerType(string(cuppa, "predictedCancerType"))
                    .bestPredictionLikelihood(number(cuppa, "bestPredictionLikelihood"))
                    .build();
        }

        @NotNull
        private static VirusInterpreterRecord toVirusInterpreterRecord(@NotNull JsonObject virusInterpreter) {
            Set<VirusInterpreterEntry> entries = Sets.newHashSet();
            for (JsonElement element : array(virusInterpreter, "reportableViruses")) {
                JsonObject virus = element.getAsJsonObject();
                entries.add(ImmutableVirusInterpreterEntry.builder()
                        .name(string(virus, "name"))
                        .interpretation(nullableString(virus, "interpretation"))
                        .integrations(integer(virus, "integrations"))
                        .driverLikelihood(VirusDriverLikelihood.valueOf(string(virus, "virusDriverLikelihoodType")))
                        .build());
            }
            return ImmutableVirusInterpreterRecord.builder().entries(entries).build();
        }

        @NotNull
        private static ChordRecord toChordRecord(@NotNull JsonObject chord) {
            return ImmutableChordRecord.builder().hrStatus(string(chord, "hrStatus")).build();
        }

        @NotNull
        private static ProtectRecord toProtectRecord(@NotNull JsonArray protectArray) {
            Set<ProtectEvidence> evidences = Sets.newHashSet();
            for (JsonElement element : protectArray) {
                JsonObject evidence = element.getAsJsonObject();

                evidences.add(ImmutableProtectEvidence.builder()
                        .reported(bool(evidence, "reported"))
                        .gene(nullableString(evidence, "gene"))
                        .event(string(evidence, "event"))
                        .treatment(string(evidence, "treatment"))
                        .onLabel(bool(evidence, "onLabel"))
                        .level(EvidenceLevel.valueOf(string(evidence, "level")))
                        .direction(EvidenceDirection.valueOf(string(evidence, "direction")))
                        .sources(toSources(array(evidence, "protectSources")))
                        .build());
            }
            return ImmutableProtectRecord.builder().evidences(evidences).build();
        }

        @NotNull
        private static Set<ProtectSource> toSources(@NotNull JsonArray sourceArray) {
            Set<ProtectSource> sources = Sets.newHashSet();
            for (JsonElement element : sourceArray) {
                JsonObject source = element.getAsJsonObject();

                sources.add(ImmutableProtectSource.builder()
                        .name(string(source, "source"))
                        .event(string(source, "sourceEvent"))
                        .type(EvidenceType.valueOf(string(source, "evidenceType")))
                        .rangeRank(nullableInteger(source, "rangeRank"))
                        .build());
            }
            return sources;
        }
    }
}
