package com.hartwig.actin.molecular.serialization;

import static com.hartwig.actin.util.json.Json.array;
import static com.hartwig.actin.util.json.Json.bool;
import static com.hartwig.actin.util.json.Json.integer;
import static com.hartwig.actin.util.json.Json.nullableBool;
import static com.hartwig.actin.util.json.Json.nullableDate;
import static com.hartwig.actin.util.json.Json.nullableInteger;
import static com.hartwig.actin.util.json.Json.nullableNumber;
import static com.hartwig.actin.util.json.Json.nullableObject;
import static com.hartwig.actin.util.json.Json.nullableString;
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
import com.hartwig.actin.molecular.datamodel.driver.CodingContext;
import com.hartwig.actin.molecular.datamodel.driver.CodingEffect;
import com.hartwig.actin.molecular.datamodel.driver.Disruption;
import com.hartwig.actin.molecular.datamodel.driver.Driver;
import com.hartwig.actin.molecular.datamodel.driver.DriverLikelihood;
import com.hartwig.actin.molecular.datamodel.driver.Fusion;
import com.hartwig.actin.molecular.datamodel.driver.FusionDriverType;
import com.hartwig.actin.molecular.datamodel.driver.GeneRole;
import com.hartwig.actin.molecular.datamodel.driver.HomozygousDisruption;
import com.hartwig.actin.molecular.datamodel.driver.ImmutableAmplification;
import com.hartwig.actin.molecular.datamodel.driver.ImmutableDisruption;
import com.hartwig.actin.molecular.datamodel.driver.ImmutableFusion;
import com.hartwig.actin.molecular.datamodel.driver.ImmutableHomozygousDisruption;
import com.hartwig.actin.molecular.datamodel.driver.ImmutableLoss;
import com.hartwig.actin.molecular.datamodel.driver.ImmutableMolecularDrivers;
import com.hartwig.actin.molecular.datamodel.driver.ImmutableTranscriptImpact;
import com.hartwig.actin.molecular.datamodel.driver.ImmutableVariant;
import com.hartwig.actin.molecular.datamodel.driver.ImmutableVirus;
import com.hartwig.actin.molecular.datamodel.driver.Loss;
import com.hartwig.actin.molecular.datamodel.driver.MolecularDrivers;
import com.hartwig.actin.molecular.datamodel.driver.ProteinEffect;
import com.hartwig.actin.molecular.datamodel.driver.RegionType;
import com.hartwig.actin.molecular.datamodel.driver.TranscriptEffect;
import com.hartwig.actin.molecular.datamodel.driver.TranscriptImpact;
import com.hartwig.actin.molecular.datamodel.driver.Variant;
import com.hartwig.actin.molecular.datamodel.driver.VariantType;
import com.hartwig.actin.molecular.datamodel.driver.Virus;
import com.hartwig.actin.molecular.datamodel.evidence.ActionableEvidence;
import com.hartwig.actin.molecular.datamodel.evidence.ImmutableActionableEvidence;
import com.hartwig.actin.molecular.datamodel.immunology.HlaAllele;
import com.hartwig.actin.molecular.datamodel.immunology.ImmutableHlaAllele;
import com.hartwig.actin.molecular.datamodel.immunology.ImmutableMolecularImmunology;
import com.hartwig.actin.molecular.datamodel.immunology.MolecularImmunology;
import com.hartwig.actin.molecular.datamodel.pharmaco.Haplotype;
import com.hartwig.actin.molecular.datamodel.pharmaco.ImmutableHaplotype;
import com.hartwig.actin.molecular.datamodel.pharmaco.ImmutablePharmacoEntry;
import com.hartwig.actin.molecular.datamodel.pharmaco.PharmacoEntry;
import com.hartwig.actin.molecular.sort.driver.CopyNumberComparator;
import com.hartwig.actin.molecular.sort.driver.DisruptionComparator;
import com.hartwig.actin.molecular.sort.driver.FusionComparator;
import com.hartwig.actin.molecular.sort.driver.HomozygousDisruptionComparator;
import com.hartwig.actin.molecular.sort.driver.VariantComparator;
import com.hartwig.actin.molecular.sort.driver.VirusComparator;
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
                    .patientId(string(record, "patientId"))
                    .sampleId(string(record, "sampleId"))
                    .type(ExperimentType.valueOf(string(record, "type")))
                    .date(nullableDate(record, "date"))
                    .evidenceSource(string(record, "evidenceSource"))
                    .externalTrialSource(string(record, "externalTrialSource"))
                    .containsTumorCells(bool(record, "containsTumorCells"))
                    .hasSufficientQuality(bool(record, "hasSufficientQuality"))
                    .characteristics(toMolecularCharacteristics(object(record, "characteristics")))
                    .drivers(toMolecularDrivers(object(record, "drivers")))
                    .immunology(toMolecularImmunology(object(record, "immunology")))
                    .pharmaco(toPharmacoEntries(array(record, "pharmaco")))
                    .build();
        }

        @NotNull
        private static MolecularCharacteristics toMolecularCharacteristics(@NotNull JsonObject characteristics) {
            return ImmutableMolecularCharacteristics.builder()
                    .purity(nullableNumber(characteristics, "purity"))
                    .ploidy(nullableNumber(characteristics, "ploidy"))
                    .predictedTumorOrigin(toPredictedTumorOrigin(nullableObject(characteristics, "predictedTumorOrigin")))
                    .isMicrosatelliteUnstable(nullableBool(characteristics, "isMicrosatelliteUnstable"))
                    .microsatelliteEvidence(toNullableActionableEvidence(nullableObject(characteristics, "microsatelliteEvidence")))
                    .isHomologousRepairDeficient(nullableBool(characteristics, "isHomologousRepairDeficient"))
                    .homologousRepairDeficiencyEvidence(toNullableActionableEvidence(nullableObject(characteristics,
                            "homologousRepairDeficiencyEvidence")))
                    .tumorMutationalBurden(nullableNumber(characteristics, "tumorMutationalBurden"))
                    .hasHighTumorMutationalBurden(nullableBool(characteristics, "hasHighTumorMutationalBurden"))
                    .tumorMutationalBurdenEvidence(toNullableActionableEvidence(nullableObject(characteristics,
                            "tumorMutationalBurdenEvidence")))
                    .tumorMutationalLoad(nullableInteger(characteristics, "tumorMutationalLoad"))
                    .hasHighTumorMutationalLoad(nullableBool(characteristics, "hasHighTumorMutationalLoad"))
                    .tumorMutationalLoadEvidence(toNullableActionableEvidence(nullableObject(characteristics,
                            "tumorMutationalLoadEvidence")))
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

        @Nullable
        private static ActionableEvidence toNullableActionableEvidence(@Nullable JsonObject evidence) {
            if (evidence == null) {
                return null;
            }
            return toActionableEvidence(evidence);
        }

        @NotNull
        private static MolecularDrivers toMolecularDrivers(@NotNull JsonObject drivers) {
            return ImmutableMolecularDrivers.builder()
                    .variants(toVariants(array(drivers, "variants")))
                    .amplifications(toAmplifications(array(drivers, "amplifications")))
                    .losses(toLosses(array(drivers, "losses")))
                    .homozygousDisruptions(toHomozygousDisruptions(array(drivers, "homozygousDisruptions")))
                    .disruptions(toDisruptions(array(drivers, "disruptions")))
                    .fusions(toFusions(array(drivers, "fusions")))
                    .viruses(toViruses(array(drivers, "viruses")))
                    .build();
        }

        @NotNull
        private static Set<Variant> toVariants(@NotNull JsonArray variantArray) {
            Set<Variant> variants = Sets.newTreeSet(new VariantComparator());
            for (JsonElement element : variantArray) {
                JsonObject variant = element.getAsJsonObject();
                variants.add(ImmutableVariant.builder()
                        .from(toDriver(variant))
                        .gene(string(variant, "gene"))
                        .geneRole(GeneRole.valueOf(string(variant, "geneRole")))
                        .proteinEffect(ProteinEffect.valueOf(string(variant, "proteinEffect")))
                        .isAssociatedWithDrugResistance(nullableBool(variant, "isAssociatedWithDrugResistance"))
                        .type(VariantType.valueOf(string(variant, "type")))
                        .variantCopyNumber(number(variant, "variantCopyNumber"))
                        .totalCopyNumber(number(variant, "totalCopyNumber"))
                        .isBiallelic(bool(variant, "isBiallelic"))
                        .isHotspot(bool(variant, "isHotspot"))
                        .clonalLikelihood(number(variant, "clonalLikelihood"))
                        .phaseGroup(nullableInteger(variant, "phaseGroup"))
                        .canonicalImpact(toTranscriptImpact(object(variant, "canonicalImpact")))
                        .otherImpacts(toTranscriptImpacts(array(variant, "otherImpacts")))
                        .build());
            }
            return variants;
        }

        @NotNull
        private static Set<TranscriptImpact> toTranscriptImpacts(@NotNull JsonArray impactArray) {
            Set<TranscriptImpact> impacts = Sets.newHashSet();
            for (JsonElement element : impactArray) {
                impacts.add(toTranscriptImpact(element.getAsJsonObject()));
            }
            return impacts;
        }

        @NotNull
        private static TranscriptImpact toTranscriptImpact(@NotNull JsonObject impact) {
            return ImmutableTranscriptImpact.builder()
                    .transcriptId(string(impact, "transcriptId"))
                    .hgvsCodingImpact(string(impact, "hgvsCodingImpact"))
                    .hgvsProteinImpact(string(impact, "hgvsProteinImpact"))
                    .affectedCodon(nullableInteger(impact, "affectedCodon"))
                    .affectedExon(nullableInteger(impact, "affectedExon"))
                    .isSpliceRegion(bool(impact, "isSpliceRegion"))
                    .effects(toTranscriptEffects(stringList(impact, "effects")))
                    .codingEffect(toCodingEffect(nullableString(impact, "codingEffect")))
                    .build();
        }

        @NotNull
        private static Set<TranscriptEffect> toTranscriptEffects(@NotNull List<String> effectStrings) {
            Set<TranscriptEffect> effects = Sets.newHashSet();
            for (String effect : effectStrings) {
                effects.add(TranscriptEffect.valueOf(effect));
            }
            return effects;
        }

        @Nullable
        private static CodingEffect toCodingEffect(@Nullable String codingEffectString) {
            return codingEffectString != null ? CodingEffect.valueOf(codingEffectString) : null;
        }

        @NotNull
        private static Set<Amplification> toAmplifications(@NotNull JsonArray amplificationArray) {
            Set<Amplification> amplifications = Sets.newTreeSet(new CopyNumberComparator());
            for (JsonElement element : amplificationArray) {
                JsonObject amplification = element.getAsJsonObject();
                amplifications.add(ImmutableAmplification.builder()
                        .from(toDriver(amplification))
                        .gene(string(amplification, "gene"))
                        .geneRole(GeneRole.valueOf(string(amplification, "geneRole")))
                        .proteinEffect(ProteinEffect.valueOf(string(amplification, "proteinEffect")))
                        .isAssociatedWithDrugResistance(nullableBool(amplification, "isAssociatedWithDrugResistance"))
                        .minCopies(integer(amplification, "minCopies"))
                        .maxCopies(integer(amplification, "maxCopies"))
                        .build());
            }
            return amplifications;
        }

        @NotNull
        private static Set<Loss> toLosses(@NotNull JsonArray lossArray) {
            Set<Loss> losses = Sets.newTreeSet(new CopyNumberComparator());
            for (JsonElement element : lossArray) {
                JsonObject loss = element.getAsJsonObject();
                losses.add(ImmutableLoss.builder()
                        .from(toDriver(loss))
                        .gene(string(loss, "gene"))
                        .geneRole(GeneRole.valueOf(string(loss, "geneRole")))
                        .proteinEffect(ProteinEffect.valueOf(string(loss, "proteinEffect")))
                        .isAssociatedWithDrugResistance(nullableBool(loss, "isAssociatedWithDrugResistance"))
                        .minCopies(integer(loss, "minCopies"))
                        .maxCopies(integer(loss, "maxCopies"))
                        .build());
            }
            return losses;
        }

        @NotNull
        private static Set<HomozygousDisruption> toHomozygousDisruptions(@NotNull JsonArray homozygousDisruptionArray) {
            Set<HomozygousDisruption> homozygousDisruptions = Sets.newTreeSet(new HomozygousDisruptionComparator());
            for (JsonElement element : homozygousDisruptionArray) {
                JsonObject homozygousDisruption = element.getAsJsonObject();
                homozygousDisruptions.add(ImmutableHomozygousDisruption.builder()
                        .from(toDriver(homozygousDisruption))
                        .gene(string(homozygousDisruption, "gene"))
                        .geneRole(GeneRole.valueOf(string(homozygousDisruption, "geneRole")))
                        .proteinEffect(ProteinEffect.valueOf(string(homozygousDisruption, "proteinEffect")))
                        .isAssociatedWithDrugResistance(nullableBool(homozygousDisruption, "isAssociatedWithDrugResistance"))
                        .build());
            }
            return homozygousDisruptions;
        }

        @NotNull
        private static Set<Disruption> toDisruptions(@NotNull JsonArray disruptionArray) {
            Set<Disruption> disruptions = Sets.newTreeSet(new DisruptionComparator());
            for (JsonElement element : disruptionArray) {
                JsonObject disruption = element.getAsJsonObject();
                disruptions.add(ImmutableDisruption.builder()
                        .from(toDriver(disruption))
                        .gene(string(disruption, "gene"))
                        .geneRole(GeneRole.valueOf(string(disruption, "geneRole")))
                        .proteinEffect(ProteinEffect.valueOf(string(disruption, "proteinEffect")))
                        .isAssociatedWithDrugResistance(nullableBool(disruption, "isAssociatedWithDrugResistance"))
                        .type(string(disruption, "type"))
                        .junctionCopyNumber(number(disruption, "junctionCopyNumber"))
                        .undisruptedCopyNumber(number(disruption, "undisruptedCopyNumber"))
                        .regionType(RegionType.valueOf(string(disruption, "regionType")))
                        .codingContext(CodingContext.valueOf(string(disruption, "codingContext")))
                        .clusterGroup(nullableInteger(disruption, "clusterGroup"))
                        .range(string(disruption, "range"))
                        .build());
            }
            return disruptions;
        }

        @NotNull
        private static Set<Fusion> toFusions(@NotNull JsonArray fusionArray) {
            Set<Fusion> fusions = Sets.newTreeSet(new FusionComparator());
            for (JsonElement element : fusionArray) {
                JsonObject fusion = element.getAsJsonObject();
                fusions.add(ImmutableFusion.builder()
                        .from(toDriver(fusion))
                        .geneStart(string(fusion, "geneStart"))
                        .geneTranscriptStart(string(fusion, "geneTranscriptStart"))
                        .geneContextStart(string(fusion, "geneContextStart"))
                        .fusedExonUp(integer(fusion, "fusedExonUp"))
                        .geneEnd(string(fusion, "geneEnd"))
                        .geneTranscriptEnd(string(fusion, "geneTranscriptEnd"))
                        .geneContextEnd(string(fusion, "geneContextEnd"))
                        .fusedExonDown(integer(fusion, "fusedExonDown"))
                        .proteinEffect(ProteinEffect.valueOf(string(fusion, "proteinEffect")))
                        .isAssociatedWithDrugResistance(nullableBool(fusion, "isAssociatedWithDrugResistance"))
                        .driverType(FusionDriverType.valueOf(string(fusion, "driverType")))
                        .build());
            }
            return fusions;
        }

        @NotNull
        private static Set<Virus> toViruses(@NotNull JsonArray virusArray) {
            Set<Virus> viruses = Sets.newTreeSet(new VirusComparator());
            for (JsonElement element : virusArray) {
                JsonObject virus = element.getAsJsonObject();
                viruses.add(ImmutableVirus.builder()
                        .from(toDriver(virus))
                        .name(string(virus, "name"))
                        .interpretation(nullableString(virus, "interpretation"))
                        .integrations(integer(virus, "integrations"))
                        .build());
            }
            return viruses;
        }

        @NotNull
        private static Driver toDriver(@NotNull JsonObject object) {
            return new Driver() {
                @Override
                public boolean isReportable() {
                    return bool(object, "isReportable");
                }

                @Nullable
                @Override
                public DriverLikelihood driverLikelihood() {
                    return toDriverLikelihood(nullableString(object, "driverLikelihood"));
                }

                @NotNull
                @Override
                public ActionableEvidence evidence() {
                    return toActionableEvidence(object(object, "evidence"));
                }
            };
        }

        @Nullable
        private static DriverLikelihood toDriverLikelihood(@Nullable String driverLikelihoodString) {
            return driverLikelihoodString != null ? DriverLikelihood.valueOf(driverLikelihoodString) : null;
        }

        @NotNull
        private static ActionableEvidence toActionableEvidence(@NotNull JsonObject evidence) {
            return ImmutableActionableEvidence.builder()
                    .approvedTreatments(stringList(evidence, "approvedTreatments"))
                    .externalEligibleTrials(stringList(evidence, "externalEligibleTrials"))
                    .onLabelExperimentalTreatments(stringList(evidence, "onLabelExperimentalTreatments"))
                    .offLabelExperimentalTreatments(stringList(evidence, "offLabelExperimentalTreatments"))
                    .preClinicalTreatments(stringList(evidence, "preClinicalTreatments"))
                    .knownResistantTreatments(stringList(evidence, "knownResistantTreatments"))
                    .suspectResistantTreatments(stringList(evidence, "suspectResistantTreatments"))
                    .build();
        }

        @NotNull
        private static MolecularImmunology toMolecularImmunology(@NotNull JsonObject immunology) {
            return ImmutableMolecularImmunology.builder()
                    .isReliable(bool(immunology, "isReliable"))
                    .hlaAlleles(toHlaAlleles(array(immunology, "hlaAlleles")))
                    .build();
        }

        @NotNull
        private static Set<HlaAllele> toHlaAlleles(@NotNull JsonArray hlaAlleleArray) {
            Set<HlaAllele> hlaAlleles = Sets.newHashSet();
            for (JsonElement element : hlaAlleleArray) {
                JsonObject hlaAllele = element.getAsJsonObject();
                hlaAlleles.add(ImmutableHlaAllele.builder()
                        .name(string(hlaAllele, "name"))
                        .tumorCopyNumber(number(hlaAllele, "tumorCopyNumber"))
                        .hasSomaticMutations(bool(hlaAllele, "hasSomaticMutations"))
                        .build());
            }
            return hlaAlleles;
        }

        @NotNull
        private static Set<PharmacoEntry> toPharmacoEntries(@NotNull JsonArray pharmacoArray) {
            Set<PharmacoEntry> pharmacoEntries = Sets.newHashSet();
            for (JsonElement element : pharmacoArray) {
                JsonObject pharmaco = element.getAsJsonObject();
                pharmacoEntries.add(ImmutablePharmacoEntry.builder()
                        .gene(string(pharmaco, "gene"))
                        .haplotypes(toHaplotypes(array(pharmaco, "haplotypes")))
                        .build());
            }
            return pharmacoEntries;
        }

        @NotNull
        private static Set<Haplotype> toHaplotypes(@NotNull JsonArray haplotypeArray) {
            Set<Haplotype> haplotypes = Sets.newHashSet();
            for (JsonElement element : haplotypeArray) {
                JsonObject haplotype = element.getAsJsonObject();
                haplotypes.add(ImmutableHaplotype.builder()
                        .name(string(haplotype, "name"))
                        .function(string(haplotype, "function"))
                        .build());
            }
            return haplotypes;
        }
    }
}
