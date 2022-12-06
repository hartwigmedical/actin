package com.hartwig.actin.molecular.orange.serialization;

import static com.hartwig.actin.util.json.Json.array;
import static com.hartwig.actin.util.json.Json.bool;
import static com.hartwig.actin.util.json.Json.date;
import static com.hartwig.actin.util.json.Json.integer;
import static com.hartwig.actin.util.json.Json.nullableArray;
import static com.hartwig.actin.util.json.Json.nullableInteger;
import static com.hartwig.actin.util.json.Json.nullableIntegerList;
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
import com.hartwig.actin.molecular.orange.datamodel.OrangeRefGenomeVersion;
import com.hartwig.actin.molecular.orange.datamodel.chord.ChordRecord;
import com.hartwig.actin.molecular.orange.datamodel.chord.ImmutableChordRecord;
import com.hartwig.actin.molecular.orange.datamodel.cuppa.CuppaPrediction;
import com.hartwig.actin.molecular.orange.datamodel.cuppa.CuppaRecord;
import com.hartwig.actin.molecular.orange.datamodel.cuppa.ImmutableCuppaPrediction;
import com.hartwig.actin.molecular.orange.datamodel.cuppa.ImmutableCuppaRecord;
import com.hartwig.actin.molecular.orange.datamodel.lilac.ImmutableLilacHlaAllele;
import com.hartwig.actin.molecular.orange.datamodel.lilac.ImmutableLilacRecord;
import com.hartwig.actin.molecular.orange.datamodel.lilac.LilacHlaAllele;
import com.hartwig.actin.molecular.orange.datamodel.lilac.LilacRecord;
import com.hartwig.actin.molecular.orange.datamodel.linx.ImmutableLinxBreakend;
import com.hartwig.actin.molecular.orange.datamodel.linx.ImmutableLinxFusion;
import com.hartwig.actin.molecular.orange.datamodel.linx.ImmutableLinxHomozygousDisruption;
import com.hartwig.actin.molecular.orange.datamodel.linx.ImmutableLinxRecord;
import com.hartwig.actin.molecular.orange.datamodel.linx.ImmutableLinxStructuralVariant;
import com.hartwig.actin.molecular.orange.datamodel.linx.LinxBreakend;
import com.hartwig.actin.molecular.orange.datamodel.linx.LinxBreakendType;
import com.hartwig.actin.molecular.orange.datamodel.linx.LinxCodingType;
import com.hartwig.actin.molecular.orange.datamodel.linx.LinxFusion;
import com.hartwig.actin.molecular.orange.datamodel.linx.LinxFusionDriverLikelihood;
import com.hartwig.actin.molecular.orange.datamodel.linx.LinxFusionType;
import com.hartwig.actin.molecular.orange.datamodel.linx.LinxHomozygousDisruption;
import com.hartwig.actin.molecular.orange.datamodel.linx.LinxRecord;
import com.hartwig.actin.molecular.orange.datamodel.linx.LinxRegionType;
import com.hartwig.actin.molecular.orange.datamodel.linx.LinxStructuralVariant;
import com.hartwig.actin.molecular.orange.datamodel.peach.ImmutablePeachEntry;
import com.hartwig.actin.molecular.orange.datamodel.peach.ImmutablePeachRecord;
import com.hartwig.actin.molecular.orange.datamodel.peach.PeachEntry;
import com.hartwig.actin.molecular.orange.datamodel.peach.PeachRecord;
import com.hartwig.actin.molecular.orange.datamodel.purple.ImmutablePurpleCharacteristics;
import com.hartwig.actin.molecular.orange.datamodel.purple.ImmutablePurpleCopyNumber;
import com.hartwig.actin.molecular.orange.datamodel.purple.ImmutablePurpleDriver;
import com.hartwig.actin.molecular.orange.datamodel.purple.ImmutablePurpleFit;
import com.hartwig.actin.molecular.orange.datamodel.purple.ImmutablePurpleRecord;
import com.hartwig.actin.molecular.orange.datamodel.purple.ImmutablePurpleTranscriptImpact;
import com.hartwig.actin.molecular.orange.datamodel.purple.ImmutablePurpleVariant;
import com.hartwig.actin.molecular.orange.datamodel.purple.PurpleCharacteristics;
import com.hartwig.actin.molecular.orange.datamodel.purple.PurpleCodingEffect;
import com.hartwig.actin.molecular.orange.datamodel.purple.PurpleCopyNumber;
import com.hartwig.actin.molecular.orange.datamodel.purple.PurpleCopyNumberInterpretation;
import com.hartwig.actin.molecular.orange.datamodel.purple.PurpleDriver;
import com.hartwig.actin.molecular.orange.datamodel.purple.PurpleDriverType;
import com.hartwig.actin.molecular.orange.datamodel.purple.PurpleFit;
import com.hartwig.actin.molecular.orange.datamodel.purple.PurpleHotspotType;
import com.hartwig.actin.molecular.orange.datamodel.purple.PurpleRecord;
import com.hartwig.actin.molecular.orange.datamodel.purple.PurpleTranscriptImpact;
import com.hartwig.actin.molecular.orange.datamodel.purple.PurpleVariant;
import com.hartwig.actin.molecular.orange.datamodel.purple.PurpleVariantEffect;
import com.hartwig.actin.molecular.orange.datamodel.purple.PurpleVariantType;
import com.hartwig.actin.molecular.orange.datamodel.virus.ImmutableVirusInterpreterEntry;
import com.hartwig.actin.molecular.orange.datamodel.virus.ImmutableVirusInterpreterRecord;
import com.hartwig.actin.molecular.orange.datamodel.virus.VirusDriverLikelihood;
import com.hartwig.actin.molecular.orange.datamodel.virus.VirusInterpretation;
import com.hartwig.actin.molecular.orange.datamodel.virus.VirusInterpreterEntry;
import com.hartwig.actin.molecular.orange.datamodel.virus.VirusInterpreterRecord;
import com.hartwig.actin.molecular.orange.datamodel.virus.VirusQCStatus;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
                    .experimentDate(date(record, "experimentDate"))
                    .refGenomeVersion(OrangeRefGenomeVersion.valueOf(string(record, "refGenomeVersion")))
                    .purple(toPurpleRecord(object(record, "purple")))
                    .linx(toLinxRecord(object(record, "linx")))
                    .peach(toPeachRecord(array(record, "peach")))
                    .cuppa(toCuppaRecord(object(record, "cuppa")))
                    .virusInterpreter(toVirusInterpreterRecord(object(record, "virusInterpreter")))
                    .lilac(toLilacRecord(object(record, "lilac")))
                    .chord(toChordRecord(object(record, "chord")))
                    .build();
        }

        @NotNull
        private static PurpleRecord toPurpleRecord(@NotNull JsonObject purple) {
            Set<PurpleDriver> drivers = Sets.newHashSet();
            drivers.addAll(toPurpleDrivers(array(purple, "somaticDrivers")));
            drivers.addAll(toPurpleDrivers(nullableArray(purple, "germlineDrivers")));

            Set<PurpleVariant> variants = Sets.newHashSet();
            variants.addAll(toPurpleVariants(array(purple, "allSomaticVariants")));
            variants.addAll(toPurpleVariants(nullableArray(purple, "allGermlineVariants")));

            return ImmutablePurpleRecord.builder()
                    .fit(toPurpleFit(object(purple, "fit")))
                    .characteristics(toPurpleCharacteristics(object(purple, "characteristics")))
                    .drivers(drivers)
                    .variants(variants)
                    .copyNumbers(toPurpleCopyNumbers(array(purple, "allSomaticGainsLosses")))
                    .build();
        }

        @NotNull
        private static PurpleFit toPurpleFit(@NotNull JsonObject fit) {
            return ImmutablePurpleFit.builder()
                    .hasReliableQuality(bool(fit, "hasReliableQuality"))
                    .hasReliablePurity(bool(fit, "hasReliablePurity"))
                    .purity(number(fit, "purity"))
                    .ploidy(number(fit, "ploidy"))
                    .build();
        }

        @NotNull
        private static PurpleCharacteristics toPurpleCharacteristics(@NotNull JsonObject characteristics) {
            return ImmutablePurpleCharacteristics.builder()
                    .microsatelliteStabilityStatus(string(characteristics, "microsatelliteStatus"))
                    .tumorMutationalBurden(number(characteristics, "tumorMutationalBurdenPerMb"))
                    .tumorMutationalBurdenStatus(string(characteristics, "tumorMutationalBurdenStatus"))
                    .tumorMutationalLoad(integer(characteristics, "tumorMutationalLoad"))
                    .tumorMutationalLoadStatus(string(characteristics, "tumorMutationalLoadStatus"))
                    .build();
        }

        @NotNull
        private static Set<PurpleDriver> toPurpleDrivers(@Nullable JsonArray driverArray) {
            if (driverArray == null) {
                return Sets.newHashSet();
            }

            Set<PurpleDriver> drivers = Sets.newHashSet();
            for (JsonElement element : driverArray) {
                JsonObject driver = element.getAsJsonObject();
                drivers.add(ImmutablePurpleDriver.builder()
                        .gene(string(driver, "gene"))
                        .transcript(string(driver, "transcript"))
                        .type(PurpleDriverType.valueOf(string(driver, "driver")))
                        .driverLikelihood(number(driver, "driverLikelihood"))
                        .build());
            }
            return drivers;
        }

        @NotNull
        private static Set<PurpleVariant> toPurpleVariants(@Nullable JsonArray variantArray) {
            if (variantArray == null) {
                return Sets.newHashSet();
            }

            Set<PurpleVariant> variants = Sets.newHashSet();
            for (JsonElement element : variantArray) {
                JsonObject variant = element.getAsJsonObject();
                variants.add(ImmutablePurpleVariant.builder()
                        .reported(bool(variant, "reported"))
                        .type(PurpleVariantType.valueOf(string(variant, "type")))
                        .gene(string(variant, "gene"))
                        .chromosome(string(variant, "chromosome"))
                        .position(integer(variant, "position"))
                        .ref(string(variant, "ref"))
                        .alt(string(variant, "alt"))
                        .adjustedCopyNumber(number(variant, "adjustedCopyNumber"))
                        .variantCopyNumber(number(variant, "variantCopyNumber"))
                        .hotspot(PurpleHotspotType.valueOf(string(variant, "hotspot")))
                        .subclonalLikelihood(number(variant, "subclonalLikelihood"))
                        .biallelic(bool(variant, "biallelic"))
                        .localPhaseSets(nullableIntegerList(variant, "localPhaseSets"))
                        .canonicalImpact(toPurpleTranscriptImpact(object(variant, "canonicalImpact")))
                        .otherImpacts(toPurpleTranscriptImpacts(array(variant, "otherImpacts")))
                        .build());
            }
            return variants;
        }

        @NotNull
        private static Set<PurpleTranscriptImpact> toPurpleTranscriptImpacts(@NotNull JsonArray impactArray) {
            Set<PurpleTranscriptImpact> impacts = Sets.newHashSet();
            for (JsonElement element : impactArray) {
                impacts.add(toPurpleTranscriptImpact(element.getAsJsonObject()));
            }
            return impacts;
        }

        @NotNull
        private static PurpleTranscriptImpact toPurpleTranscriptImpact(@NotNull JsonObject impact) {
            return ImmutablePurpleTranscriptImpact.builder()
                    .transcript(string(impact, "transcript"))
                    .hgvsCodingImpact(string(impact, "hgvsCodingImpact"))
                    .hgvsProteinImpact(string(impact, "hgvsProteinImpact"))
                    .affectedCodon(nullableInteger(impact, "affectedCodon"))
                    .affectedExon(nullableInteger(impact, "affectedExon"))
                    .spliceRegion(bool(impact, "spliceRegion"))
                    .codingEffect(PurpleCodingEffect.valueOf(string(impact, "codingEffect")))
                    .effects(toPurpleVariantEffects(array(impact, "effects")))
                    .build();
        }

        @NotNull
        private static Set<PurpleVariantEffect> toPurpleVariantEffects(@NotNull JsonArray effectArray) {
            Set<PurpleVariantEffect> effects = Sets.newHashSet();
            for (JsonElement element : effectArray) {
                effects.add(PurpleVariantEffect.valueOf(element.getAsString()));
            }
            return effects;
        }

        @NotNull
        private static Set<PurpleCopyNumber> toPurpleCopyNumbers(@NotNull JsonArray gainLossArray) {
            Set<PurpleCopyNumber> copyNumbers = Sets.newHashSet();
            for (JsonElement element : gainLossArray) {
                JsonObject gainLoss = element.getAsJsonObject();
                copyNumbers.add(ImmutablePurpleCopyNumber.builder()
                        .gene(string(gainLoss, "gene"))
                        .interpretation(PurpleCopyNumberInterpretation.valueOf(string(gainLoss, "interpretation")))
                        .minCopies(integer(gainLoss, "minCopies"))
                        .maxCopies(integer(gainLoss, "maxCopies"))
                        .build());
            }
            return copyNumbers;
        }

        @NotNull
        private static LinxRecord toLinxRecord(@NotNull JsonObject linx) {
            return ImmutableLinxRecord.builder()
                    .structuralVariants(toLinxStructuralVariants(array(linx, "allStructuralVariants")))
                    .homozygousDisruptions(toLinxHomozygousDisruptions(array(linx, "homozygousDisruptions")))
                    .breakends(toLinxBreakends(array(linx, "allBreakends")))
                    .fusions(toLinxFusions(array(linx, "allFusions")))
                    .build();
        }

        @NotNull
        private static Set<LinxStructuralVariant> toLinxStructuralVariants(@NotNull JsonArray structuralVariantArray) {
            Set<LinxStructuralVariant> structuralVariants = Sets.newHashSet();
            for (JsonElement element : structuralVariantArray) {
                JsonObject structuralVariant = element.getAsJsonObject();
                structuralVariants.add(ImmutableLinxStructuralVariant.builder()
                        .svId(integer(structuralVariant, "svId"))
                        .clusterId(integer(structuralVariant, "clusterId"))
                        .build());
            }
            return structuralVariants;
        }

        @NotNull
        private static Set<LinxHomozygousDisruption> toLinxHomozygousDisruptions(@NotNull JsonArray homozygousDisruptionArray) {
            Set<LinxHomozygousDisruption> homozygousDisruptions = Sets.newHashSet();
            for (JsonElement element : homozygousDisruptionArray) {
                JsonObject homozygousDisruption = element.getAsJsonObject();
                homozygousDisruptions.add(ImmutableLinxHomozygousDisruption.builder().gene(string(homozygousDisruption, "gene")).build());
            }
            return homozygousDisruptions;
        }

        @NotNull
        private static Set<LinxBreakend> toLinxBreakends(@NotNull JsonArray breakendArray) {
            Set<LinxBreakend> breakends = Sets.newHashSet();
            for (JsonElement element : breakendArray) {
                JsonObject breakend = element.getAsJsonObject();
                breakends.add(ImmutableLinxBreakend.builder()
                        .reported(bool(breakend, "reportedDisruption"))
                        .svId(integer(breakend, "svId"))
                        .gene(string(breakend, "gene"))
                        .type(LinxBreakendType.valueOf(string(breakend, "type")))
                        .junctionCopyNumber(number(breakend, "junctionCopyNumber"))
                        .undisruptedCopyNumber(number(breakend, "undisruptedCopyNumber"))
                        .regionType(LinxRegionType.valueOf(string(breakend, "regionType")))
                        .codingType(LinxCodingType.valueOf(string(breakend, "codingType")))
                        .build());
            }
            return breakends;
        }

        @NotNull
        private static Set<LinxFusion> toLinxFusions(@NotNull JsonArray fusionArray) {
            Set<LinxFusion> fusions = Sets.newHashSet();
            for (JsonElement element : fusionArray) {
                JsonObject fusion = element.getAsJsonObject();
                fusions.add(ImmutableLinxFusion.builder()
                        .reported(bool(fusion, "reported"))
                        .type(LinxFusionType.valueOf(string(fusion, "reportedType")))
                        .geneStart(string(fusion, "geneStart"))
                        .geneTranscriptStart(string(fusion, "geneTranscriptStart"))
                        .fusedExonUp(integer(fusion, "fusedExonUp"))
                        .geneEnd(string(fusion, "geneEnd"))
                        .geneTranscriptEnd(string(fusion, "geneTranscriptEnd"))
                        .fusedExonDown(integer(fusion, "fusedExonDown"))
                        .driverLikelihood(LinxFusionDriverLikelihood.valueOf(string(fusion, "likelihood")))
                        .build());
            }
            return fusions;
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
            Set<CuppaPrediction> predictions = Sets.newHashSet();
            for (JsonElement element : array(cuppa, "predictions")) {
                JsonObject prediction = element.getAsJsonObject();
                predictions.add(ImmutableCuppaPrediction.builder()
                        .cancerType(string(prediction, "cancerType"))
                        .likelihood(number(prediction, "likelihood"))
                        .build());
            }
            return ImmutableCuppaRecord.builder().predictions(predictions).build();
        }

        @NotNull
        private static VirusInterpreterRecord toVirusInterpreterRecord(@NotNull JsonObject virusInterpreter) {
            Set<VirusInterpreterEntry> entries = Sets.newHashSet();
            for (JsonElement element : array(virusInterpreter, "reportableViruses")) {
                JsonObject virus = element.getAsJsonObject();
                entries.add(ImmutableVirusInterpreterEntry.builder()
                        .reported(true)
                        .name(string(virus, "name"))
                        .qcStatus(VirusQCStatus.valueOf(string(virus, "qcStatus")))
                        .interpretation(toVirusInterpretation(nullableString(virus, "interpretation")))
                        .integrations(integer(virus, "integrations"))
                        .driverLikelihood(VirusDriverLikelihood.valueOf(string(virus, "virusDriverLikelihoodType")))
                        .build());
            }
            return ImmutableVirusInterpreterRecord.builder().entries(entries).build();
        }

        @Nullable
        private static VirusInterpretation toVirusInterpretation(@Nullable String interpretation) {
            return interpretation != null ? VirusInterpretation.valueOf(interpretation) : null;
        }

        @NotNull
        private static LilacRecord toLilacRecord(@NotNull JsonObject lilac) {
            Set<LilacHlaAllele> alleles = Sets.newHashSet();
            for (JsonElement element : array(lilac, "alleles")) {
                JsonObject allele = element.getAsJsonObject();
                alleles.add(ImmutableLilacHlaAllele.builder()
                        .allele(string(allele, "allele"))
                        .tumorCopyNumber(number(allele, "tumorCopyNumber"))
                        .somaticMissense(number(allele, "somaticMissense"))
                        .somaticNonsenseOrFrameshift(number(allele, "somaticNonsenseOrFrameshift"))
                        .somaticSplice(number(allele, "somaticSplice"))
                        .somaticInframeIndel(number(allele, "somaticInframeIndel"))
                        .build());
            }

            return ImmutableLilacRecord.builder().qc(string(lilac, "qc")).alleles(alleles).build();
        }

        @NotNull
        private static ChordRecord toChordRecord(@NotNull JsonObject chord) {
            return ImmutableChordRecord.builder().hrStatus(string(chord, "hrStatus")).build();
        }
    }
}
