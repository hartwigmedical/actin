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
import com.hartwig.actin.molecular.orange.datamodel.cuppa.CuppaPrediction;
import com.hartwig.actin.molecular.orange.datamodel.cuppa.CuppaRecord;
import com.hartwig.actin.molecular.orange.datamodel.cuppa.ImmutableCuppaPrediction;
import com.hartwig.actin.molecular.orange.datamodel.cuppa.ImmutableCuppaRecord;
import com.hartwig.actin.molecular.orange.datamodel.lilac.ImmutableLilacHlaAllele;
import com.hartwig.actin.molecular.orange.datamodel.lilac.ImmutableLilacRecord;
import com.hartwig.actin.molecular.orange.datamodel.lilac.LilacHlaAllele;
import com.hartwig.actin.molecular.orange.datamodel.lilac.LilacRecord;
import com.hartwig.actin.molecular.orange.datamodel.linx.ImmutableLinxDisruption;
import com.hartwig.actin.molecular.orange.datamodel.linx.ImmutableLinxFusion;
import com.hartwig.actin.molecular.orange.datamodel.linx.ImmutableLinxHomozygousDisruption;
import com.hartwig.actin.molecular.orange.datamodel.linx.ImmutableLinxRecord;
import com.hartwig.actin.molecular.orange.datamodel.linx.LinxCodingType;
import com.hartwig.actin.molecular.orange.datamodel.linx.LinxDisruption;
import com.hartwig.actin.molecular.orange.datamodel.linx.LinxFusion;
import com.hartwig.actin.molecular.orange.datamodel.linx.LinxFusionDriverLikelihood;
import com.hartwig.actin.molecular.orange.datamodel.linx.LinxFusionType;
import com.hartwig.actin.molecular.orange.datamodel.linx.LinxHomozygousDisruption;
import com.hartwig.actin.molecular.orange.datamodel.linx.LinxRecord;
import com.hartwig.actin.molecular.orange.datamodel.linx.LinxRegionType;
import com.hartwig.actin.molecular.orange.datamodel.peach.ImmutablePeachEntry;
import com.hartwig.actin.molecular.orange.datamodel.peach.ImmutablePeachRecord;
import com.hartwig.actin.molecular.orange.datamodel.peach.PeachEntry;
import com.hartwig.actin.molecular.orange.datamodel.peach.PeachRecord;
import com.hartwig.actin.molecular.orange.datamodel.purple.CopyNumberInterpretation;
import com.hartwig.actin.molecular.orange.datamodel.purple.ImmutablePurpleCharacteristics;
import com.hartwig.actin.molecular.orange.datamodel.purple.ImmutablePurpleCopyNumber;
import com.hartwig.actin.molecular.orange.datamodel.purple.ImmutablePurpleRecord;
import com.hartwig.actin.molecular.orange.datamodel.purple.ImmutablePurpleTranscriptImpact;
import com.hartwig.actin.molecular.orange.datamodel.purple.ImmutablePurpleVariant;
import com.hartwig.actin.molecular.orange.datamodel.purple.PurpleCharacteristics;
import com.hartwig.actin.molecular.orange.datamodel.purple.PurpleCodingEffect;
import com.hartwig.actin.molecular.orange.datamodel.purple.PurpleCopyNumber;
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
            Set<PurpleVariant> variants = Sets.newHashSet();
            variants.addAll(toPurpleVariants(array(purple, "reportableSomaticVariants")));
            variants.addAll(toPurpleVariants(array(purple, "reportableGermlineVariants")));

            JsonObject purpleFit = object(purple, "fit");

            return ImmutablePurpleRecord.builder()
                    .hasSufficientQuality(bool(purpleFit, "hasReliableQuality"))
                    .containsTumorCells(bool(purpleFit, "hasReliablePurity"))
                    .characteristics(toPurpleCharacteristics(purpleFit, object(purple, "characteristics")))
                    .variants(variants)
                    .copyNumbers(toPurpleCopyNumbers(array(purple, "reportableSomaticGainsLosses")))
                    .build();
        }

        @NotNull
        private static PurpleCharacteristics toPurpleCharacteristics(@NotNull JsonObject purpleFit,
                @NotNull JsonObject purpleCharacteristics) {
            // TODO Determine tumor mutational burden status in ORANGE
            double tumorMutationalBurden = number(purpleCharacteristics, "tumorMutationalBurdenPerMb");
            String tumorMutationalBurdenStatus = tumorMutationalBurden >= 10 ? "HIGH" : "LOW";

            return ImmutablePurpleCharacteristics.builder()
                    .purity(number(purpleFit, "purity"))
                    .ploidy(number(purpleFit, "ploidy"))
                    .microsatelliteStabilityStatus(string(purpleCharacteristics, "microsatelliteStatus"))
                    .tumorMutationalBurden(number(purpleCharacteristics, "tumorMutationalBurdenPerMb"))
                    .tumorMutationalBurdenStatus(tumorMutationalBurdenStatus)
                    .tumorMutationalLoad(integer(purpleCharacteristics, "tumorMutationalLoad"))
                    .tumorMutationalLoadStatus(string(purpleCharacteristics, "tumorMutationalLoadStatus"))
                    .build();
        }

        @NotNull
        private static Set<PurpleVariant> toPurpleVariants(@NotNull JsonArray reportableVariantArray) {
            Set<PurpleVariant> variants = Sets.newHashSet();
            // TODO Populate other transcript impacts in ORANGE.
            for (JsonElement element : reportableVariantArray) {
                JsonObject variant = element.getAsJsonObject();
                variants.add(ImmutablePurpleVariant.builder()
                        .reported(true)
                        .type(PurpleVariantType.valueOf(string(variant, "type")))
                        .gene(string(variant, "gene"))
                        .chromosome(string(variant, "chromosome"))
                        .position(integer(variant, "position"))
                        .ref(string(variant, "ref"))
                        .alt(string(variant, "alt"))
                        .totalCopyNumber(number(variant, "totalCopyNumber"))
                        .alleleCopyNumber(number(variant, "alleleCopyNumber"))
                        .hotspot(PurpleHotspotType.valueOf(string(variant, "hotspot")))
                        .driverLikelihood(number(variant, "driverLikelihood"))
                        .clonalLikelihood(number(variant, "clonalLikelihood"))
                        .biallelic(bool(variant, "biallelic"))
                        .localPhaseSet(nullableInteger(variant, "localPhaseSet"))
                        .canonicalImpact(toCanonicalTranscriptImpact(variant))
                        .build());
            }
            return variants;
        }

        @NotNull
        private static PurpleTranscriptImpact toCanonicalTranscriptImpact(@NotNull JsonObject variant) {
            // TODO Read splice region directly from purple rather than approximate it.
            // TODO Populate "affected codon" and "affected exon".
            PurpleCodingEffect codingEffect = PurpleCodingEffect.valueOf(string(variant, "canonicalCodingEffect"));

            return ImmutablePurpleTranscriptImpact.builder()
                    .transcriptId(string(variant, "canonicalTranscript"))
                    .hgvsCodingImpact(string(variant, "canonicalHgvsCodingImpact"))
                    .hgvsProteinImpact(string(variant, "canonicalHgvsProteinImpact"))
                    .affectedCodon(null)
                    .affectedExon(null)
                    .spliceRegion(codingEffect == PurpleCodingEffect.SPLICE)
                    .codingEffect(PurpleCodingEffect.valueOf(string(variant, "canonicalCodingEffect")))
                    .effects(PurpleVariantEffect.fromEffectString(string(variant, "canonicalEffect")))
                    .build();
        }

        @NotNull
        private static Set<PurpleCopyNumber> toPurpleCopyNumbers(@NotNull JsonArray reportableGainLossArray) {
            Set<PurpleCopyNumber> copyNumbers = Sets.newHashSet();
            for (JsonElement element : reportableGainLossArray) {
                JsonObject reportableGainLoss = element.getAsJsonObject();
                copyNumbers.add(ImmutablePurpleCopyNumber.builder()
                        .reported(true)
                        .gene(string(reportableGainLoss, "gene"))
                        .interpretation(CopyNumberInterpretation.valueOf(string(reportableGainLoss, "interpretation")))
                        .minCopies(integer(reportableGainLoss, "minCopies"))
                        .maxCopies(integer(reportableGainLoss, "maxCopies"))
                        .build());
            }
            return copyNumbers;
        }

        @NotNull
        private static LinxRecord toLinxRecord(@NotNull JsonObject linx) {
            return ImmutableLinxRecord.builder()
                    .homozygousDisruptions(toLinxHomozygousDisruptions(array(linx, "homozygousDisruptions")))
                    .disruptions(toLinxDisruptions(array(linx, "reportableGeneDisruptions")))
                    .fusions(toLinxFusions(array(linx, "reportableFusions")))
                    .build();
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
        private static Set<LinxDisruption> toLinxDisruptions(@NotNull JsonArray reportableGeneDisruptionArray) {
            Set<LinxDisruption> disruptions = Sets.newHashSet();
            // TODO Read region type and coding type from ORANGE
            for (JsonElement element : reportableGeneDisruptionArray) {
                JsonObject geneDisruption = element.getAsJsonObject();
                disruptions.add(ImmutableLinxDisruption.builder()
                        .reported(true)
                        .gene(string(geneDisruption, "gene"))
                        .type(string(geneDisruption, "type"))
                        .junctionCopyNumber(number(geneDisruption, "junctionCopyNumber"))
                        .undisruptedCopyNumber(number(geneDisruption, "undisruptedCopyNumber"))
                        .regionType(LinxRegionType.UNKNOWN)
                        .codingType(LinxCodingType.UNKNOWN)
                        .clusterId(nullableInteger(geneDisruption, "clusterId"))
                        .build());
            }
            return disruptions;
        }

        @NotNull
        private static Set<LinxFusion> toLinxFusions(@NotNull JsonArray reportableFusionArray) {
            Set<LinxFusion> fusions = Sets.newHashSet();
            for (JsonElement element : reportableFusionArray) {
                JsonObject fusion = element.getAsJsonObject();
                fusions.add(ImmutableLinxFusion.builder()
                        .reported(true)
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
