package com.hartwig.actin.molecular.orange.serialization;

import static com.hartwig.actin.util.json.Json.array;
import static com.hartwig.actin.util.json.Json.bool;
import static com.hartwig.actin.util.json.Json.date;
import static com.hartwig.actin.util.json.Json.integer;
import static com.hartwig.actin.util.json.Json.nullableArray;
import static com.hartwig.actin.util.json.Json.nullableInteger;
import static com.hartwig.actin.util.json.Json.nullableIntegerList;
import static com.hartwig.actin.util.json.Json.nullableObject;
import static com.hartwig.actin.util.json.Json.nullableString;
import static com.hartwig.actin.util.json.Json.number;
import static com.hartwig.actin.util.json.Json.object;
import static com.hartwig.actin.util.json.Json.string;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

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
import com.hartwig.actin.molecular.orange.datamodel.ImmutableOrangeRecord;
import com.hartwig.actin.molecular.orange.datamodel.OrangeRecord;
import com.hartwig.actin.molecular.orange.datamodel.OrangeRefGenomeVersion;
import com.hartwig.actin.molecular.orange.datamodel.chord.ChordRecord;
import com.hartwig.actin.molecular.orange.datamodel.chord.ChordStatus;
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
import com.hartwig.actin.molecular.orange.datamodel.linx.LinxRecord;
import com.hartwig.actin.molecular.orange.datamodel.linx.LinxRegionType;
import com.hartwig.actin.molecular.orange.datamodel.linx.LinxStructuralVariant;
import com.hartwig.actin.molecular.orange.datamodel.peach.ImmutablePeachEntry;
import com.hartwig.actin.molecular.orange.datamodel.peach.ImmutablePeachRecord;
import com.hartwig.actin.molecular.orange.datamodel.peach.PeachEntry;
import com.hartwig.actin.molecular.orange.datamodel.peach.PeachRecord;
import com.hartwig.actin.molecular.orange.datamodel.purple.ImmutablePurpleCharacteristics;
import com.hartwig.actin.molecular.orange.datamodel.purple.ImmutablePurpleDriver;
import com.hartwig.actin.molecular.orange.datamodel.purple.ImmutablePurpleFit;
import com.hartwig.actin.molecular.orange.datamodel.purple.ImmutablePurpleGainLoss;
import com.hartwig.actin.molecular.orange.datamodel.purple.ImmutablePurpleRecord;
import com.hartwig.actin.molecular.orange.datamodel.purple.ImmutablePurpleTranscriptImpact;
import com.hartwig.actin.molecular.orange.datamodel.purple.ImmutablePurpleVariant;
import com.hartwig.actin.molecular.orange.datamodel.purple.PurpleCharacteristics;
import com.hartwig.actin.molecular.orange.datamodel.purple.PurpleCodingEffect;
import com.hartwig.actin.molecular.orange.datamodel.purple.PurpleDriver;
import com.hartwig.actin.molecular.orange.datamodel.purple.PurpleDriverType;
import com.hartwig.actin.molecular.orange.datamodel.purple.PurpleFit;
import com.hartwig.actin.molecular.orange.datamodel.purple.PurpleGainLoss;
import com.hartwig.actin.molecular.orange.datamodel.purple.PurpleGainLossInterpretation;
import com.hartwig.actin.molecular.orange.datamodel.purple.PurpleHotspotType;
import com.hartwig.actin.molecular.orange.datamodel.purple.PurpleMicrosatelliteStatus;
import com.hartwig.actin.molecular.orange.datamodel.purple.PurpleQCStatus;
import com.hartwig.actin.molecular.orange.datamodel.purple.PurpleRecord;
import com.hartwig.actin.molecular.orange.datamodel.purple.PurpleTranscriptImpact;
import com.hartwig.actin.molecular.orange.datamodel.purple.PurpleTumorMutationalStatus;
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

    private static final String ORANGE_EXPERIMENT_TYPE_PANEL = "TARGETED";
    private static final String ORANGE_EXPERIMENT_TYPE_WGS = "WHOLE_GENOME";

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
                    .experimentType(toExperimentType(string(record, "experimentType")))
                    .refGenomeVersion(OrangeRefGenomeVersion.valueOf(string(record, "refGenomeVersion")))
                    .purple(toPurpleRecord(object(record, "purple")))
                    .linx(toLinxRecord(object(record, "linx")))
                    .peach(toPeachRecord(nullableArray(record, "peach")))
                    .cuppa(toCuppaRecord(nullableObject(record, "cuppa")))
                    .virusInterpreter(toVirusInterpreterRecord(nullableObject(record, "virusInterpreter")))
                    .lilac(toLilacRecord(object(record, "lilac")))
                    .chord(toChordRecord(nullableObject(record, "chord")))
                    .build();
        }

        private static ExperimentType toExperimentType(String experimentType) {
            switch (experimentType) {
                case ORANGE_EXPERIMENT_TYPE_WGS:
                    return ExperimentType.WGS;
                case ORANGE_EXPERIMENT_TYPE_PANEL:
                    return ExperimentType.PANEL;
                default:
                    throw new IllegalStateException("Unable to determine experiment type for value " + experimentType);
            }
        }

        @NotNull
        private static PurpleRecord toPurpleRecord(@NotNull JsonObject purple) {
            Set<PurpleDriver> drivers = Sets.newHashSet();
            drivers.addAll(toPurpleDrivers(array(purple, "somaticDrivers")));
            drivers.addAll(toPurpleDrivers(nullableArray(purple, "germlineDrivers")));

            Set<PurpleVariant> variants = Sets.newHashSet();
            variants.addAll(toPurpleVariants(array(purple, "allSomaticVariants")));
            variants.addAll(toPurpleVariants(nullableArray(purple, "reportableGermlineVariants")));

            return ImmutablePurpleRecord.builder()
                    .fit(toPurpleFit(object(purple, "fit")))
                    .characteristics(toPurpleCharacteristics(object(purple, "characteristics")))
                    .drivers(drivers)
                    .variants(variants)
                    .gainsLosses(toPurpleGainsLosses(array(purple, "allSomaticGainsLosses")))
                    .build();
        }

        @NotNull
        private static PurpleFit toPurpleFit(@NotNull JsonObject fit) {
            return ImmutablePurpleFit.builder()
                    .hasSufficientQuality(bool(fit, "hasSufficientQuality"))
                    .containsTumorCells(bool(fit, "containsTumorCells"))
                    .purity(number(fit, "purity"))
                    .ploidy(number(fit, "ploidy"))
                    .qcStatuses(toPurpleQCStatuses(object(fit, "qc")))
                    .build();
        }

        @NotNull
        private static Set<PurpleQCStatus> toPurpleQCStatuses(@NotNull JsonObject qc) {
            return array(qc, "status").asList()
                    .stream()
                    .map(JsonElement::getAsString)
                    .map(PurpleQCStatus::valueOf)
                    .collect(Collectors.toSet());
        }

        @NotNull
        private static PurpleCharacteristics toPurpleCharacteristics(@NotNull JsonObject characteristics) {
            return ImmutablePurpleCharacteristics.builder()
                    .microsatelliteStatus(PurpleMicrosatelliteStatus.valueOf(string(characteristics, "microsatelliteStatus")))
                    .tumorMutationalBurdenPerMb(number(characteristics, "tumorMutationalBurdenPerMb"))
                    .tumorMutationalBurdenStatus(PurpleTumorMutationalStatus.valueOf(string(characteristics,
                            "tumorMutationalBurdenStatus")))
                    .tumorMutationalLoad(integer(characteristics, "tumorMutationalLoad"))
                    .tumorMutationalLoadStatus(PurpleTumorMutationalStatus.valueOf(string(characteristics, "tumorMutationalLoadStatus")))
                    .build();
        }

        @NotNull
        private static Set<PurpleDriver> toPurpleDrivers(@Nullable JsonArray driverArray) {
            if (driverArray == null) {
                return Sets.newHashSet();
            } else {
                return extractSetFromJson(driverArray,
                        driver -> ImmutablePurpleDriver.builder()
                                .gene(string(driver, "gene"))
                                .transcript(string(driver, "transcript"))
                                .type(PurpleDriverType.valueOf(string(driver, "driver")))
                                .driverLikelihood(number(driver, "driverLikelihood"))
                                .build());
            }
        }

        @NotNull
        private static Set<PurpleVariant> toPurpleVariants(@Nullable JsonArray variantArray) {
            if (variantArray == null) {
                return Sets.newHashSet();
            } else {
                return extractSetFromJson(variantArray,
                        variant -> ImmutablePurpleVariant.builder()
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
        }

        @NotNull
        private static Set<PurpleTranscriptImpact> toPurpleTranscriptImpacts(@NotNull JsonArray impactArray) {
            return extractSetFromJson(impactArray, OrangeRecordCreator::toPurpleTranscriptImpact);
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
            return effectArray.asList()
                    .stream()
                    .map(JsonElement::getAsString)
                    .map(PurpleVariantEffect::valueOf)
                    .collect(Collectors.toSet());
        }

        @NotNull
        private static Set<PurpleGainLoss> toPurpleGainsLosses(@NotNull JsonArray gainLossArray) {
            return extractSetFromJson(gainLossArray,
                    gainLoss -> ImmutablePurpleGainLoss.builder()
                            .gene(string(gainLoss, "gene"))
                            .interpretation(PurpleGainLossInterpretation.valueOf(string(gainLoss, "interpretation")))
                            .minCopies(integer(gainLoss, "minCopies"))
                            .maxCopies(integer(gainLoss, "maxCopies"))
                            .build());
        }

        @NotNull
        private static LinxRecord toLinxRecord(@NotNull JsonObject linx) {
            for (String arrayField : List.of("allGermlineStructuralVariants", "allGermlineBreakends", "germlineHomozygousDisruptions")) {
                throwIfGermlineArrayFieldNonEmpty(linx, arrayField);
            }

            return ImmutableLinxRecord.builder()
                    .structuralVariants(toLinxStructuralVariants(array(linx, "allSomaticStructuralVariants")))
                    .homozygousDisruptions(toLinxHomozygousDisruptions(array(linx, "somaticHomozygousDisruptions")))
                    .breakends(toLinxBreakends(array(linx, "allSomaticBreakends")))
                    .fusions(toLinxFusions(array(linx, "allSomaticFusions")))
                    .build();
        }

        private static void throwIfGermlineArrayFieldNonEmpty(@NotNull JsonObject json, @NotNull String arrayField) {
            if (!Optional.ofNullable(nullableArray(json, arrayField)).map(JsonArray::isEmpty).orElse(true)) {
                throw new RuntimeException(arrayField + " must be null or empty because ACTIN only accepts ORANGE output that has been "
                        + "scrubbed of germline data. Please use the JSON output from the 'orange_no_germline' directory.");
            }
        }

        @NotNull
        private static LinxStructuralVariant linxStructuralVariantFromSvAndCluster(int svId, int clusterId) {
            return ImmutableLinxStructuralVariant.builder().svId(svId).clusterId(clusterId).build();
        }

        @NotNull
        private static Set<LinxStructuralVariant> toLinxStructuralVariants(@NotNull JsonArray structuralVariantArray) {
            return extractSetFromJson(structuralVariantArray,
                    sv -> linxStructuralVariantFromSvAndCluster(integer(sv, "svId"), integer(sv, "clusterId")));
        }

        @NotNull
        private static Set<ImmutableLinxHomozygousDisruption> toLinxHomozygousDisruptions(@NotNull JsonArray homozygousDisruptionArray) {
            return extractSetFromJson(homozygousDisruptionArray,
                    homozygousDisruption -> ImmutableLinxHomozygousDisruption.builder().gene(string(homozygousDisruption, "gene")).build());
        }

        @NotNull
        private static Set<LinxBreakend> toLinxBreakends(@NotNull JsonArray somaticBreakendArray) {
            return extractSetFromJson(somaticBreakendArray,
                    breakend -> ImmutableLinxBreakend.builder()
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

        @NotNull
        private static Set<LinxFusion> toLinxFusions(@NotNull JsonArray fusionArray) {
            return extractSetFromJson(fusionArray,
                    fusion -> ImmutableLinxFusion.builder()
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

        private static Optional<PeachRecord> toPeachRecord(@Nullable JsonArray nullablePeachArray) {
            return Optional.ofNullable(nullablePeachArray).map(peachArray -> {
                Set<PeachEntry> peachEntries = extractSetFromJson(peachArray,
                        peach -> ImmutablePeachEntry.builder()
                                .gene(string(peach, "gene"))
                                .haplotype(string(peach, "haplotype"))
                                .function(string(peach, "function"))
                                .build());
                return ImmutablePeachRecord.builder().entries(peachEntries).build();
            });
        }

        private static Optional<CuppaRecord> toCuppaRecord(@Nullable JsonObject nullableCuppa) {
            return Optional.ofNullable(nullableCuppa).map(cuppa -> {
                Set<CuppaPrediction> predictions = extractSetFromJson(array(cuppa, "predictions"),
                        prediction -> ImmutableCuppaPrediction.builder()
                                .cancerType(string(prediction, "cancerType"))
                                .likelihood(number(prediction, "likelihood"))
                                .build());
                return ImmutableCuppaRecord.builder().predictions(predictions).build();
            });
        }

        private static Optional<VirusInterpreterRecord> toVirusInterpreterRecord(@Nullable JsonObject nullableVirusInterpreter) {
            return Optional.ofNullable(nullableVirusInterpreter)
                    .map(virusInterpreter -> ImmutableVirusInterpreterRecord.builder()
                            .entries(extractSetFromJson(array(virusInterpreter, "allViruses"),
                                    OrangeRecordCreator::toVirusInterpreterEntry))
                            .build());
        }

        @NotNull
        private static VirusInterpreterEntry toVirusInterpreterEntry(@NotNull JsonObject virus) {
            return ImmutableVirusInterpreterEntry.builder()
                    .reported(bool(virus, "reported"))
                    .name(string(virus, "name"))
                    .qcStatus(VirusQCStatus.valueOf(string(virus, "qcStatus")))
                    .interpretation(toVirusInterpretation(nullableString(virus, "interpretation")))
                    .integrations(integer(virus, "integrations"))
                    .driverLikelihood(VirusDriverLikelihood.valueOf(string(virus, "virusDriverLikelihoodType")))
                    .build();
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

        private static Optional<ChordRecord> toChordRecord(@Nullable JsonObject nullableChord) {
            return Optional.ofNullable(nullableChord)
                    .map(chord -> ImmutableChordRecord.builder().hrStatus(ChordStatus.valueOf(string(chord, "hrStatus"))).build());
        }

        @NotNull
        private static <T> Set<T> extractSetFromJson(@NotNull JsonArray jsonArray, Function<JsonObject, T> extractor) {
            return jsonArray.asList().stream().map(JsonElement::getAsJsonObject).map(extractor).collect(Collectors.toSet());
        }
    }
}
