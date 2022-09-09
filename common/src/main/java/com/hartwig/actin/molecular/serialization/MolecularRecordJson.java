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
import static com.hartwig.actin.util.json.Json.nullableStringList;
import static com.hartwig.actin.util.json.Json.number;
import static com.hartwig.actin.util.json.Json.object;
import static com.hartwig.actin.util.json.Json.string;

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
import com.hartwig.actin.molecular.datamodel.ImmutableMolecularRecord;
import com.hartwig.actin.molecular.datamodel.MolecularRecord;
import com.hartwig.actin.molecular.datamodel.characteristics.ImmutableMolecularCharacteristics;
import com.hartwig.actin.molecular.datamodel.characteristics.ImmutablePredictedTumorOrigin;
import com.hartwig.actin.molecular.datamodel.characteristics.MolecularCharacteristics;
import com.hartwig.actin.molecular.datamodel.characteristics.PredictedTumorOrigin;
import com.hartwig.actin.molecular.datamodel.driver.Amplification;
import com.hartwig.actin.molecular.datamodel.driver.Disruption;
import com.hartwig.actin.molecular.datamodel.driver.DriverLikelihood;
import com.hartwig.actin.molecular.datamodel.driver.Fusion;
import com.hartwig.actin.molecular.datamodel.driver.FusionDriverType;
import com.hartwig.actin.molecular.datamodel.driver.HomozygousDisruption;
import com.hartwig.actin.molecular.datamodel.driver.ImmutableAmplification;
import com.hartwig.actin.molecular.datamodel.driver.ImmutableDisruption;
import com.hartwig.actin.molecular.datamodel.driver.ImmutableFusion;
import com.hartwig.actin.molecular.datamodel.driver.ImmutableHomozygousDisruption;
import com.hartwig.actin.molecular.datamodel.driver.ImmutableLoss;
import com.hartwig.actin.molecular.datamodel.driver.ImmutableMolecularDrivers;
import com.hartwig.actin.molecular.datamodel.driver.ImmutableVariant;
import com.hartwig.actin.molecular.datamodel.driver.ImmutableVirus;
import com.hartwig.actin.molecular.datamodel.driver.Loss;
import com.hartwig.actin.molecular.datamodel.driver.MolecularDrivers;
import com.hartwig.actin.molecular.datamodel.driver.Variant;
import com.hartwig.actin.molecular.datamodel.driver.VariantDriverType;
import com.hartwig.actin.molecular.datamodel.driver.Virus;
import com.hartwig.actin.molecular.datamodel.evidence.ActinTrialEvidence;
import com.hartwig.actin.molecular.datamodel.evidence.ExternalTrialEvidence;
import com.hartwig.actin.molecular.datamodel.evidence.ImmutableActinTrialEvidence;
import com.hartwig.actin.molecular.datamodel.evidence.ImmutableExternalTrialEvidence;
import com.hartwig.actin.molecular.datamodel.evidence.ImmutableMolecularEvidence;
import com.hartwig.actin.molecular.datamodel.evidence.ImmutableTreatmentEvidence;
import com.hartwig.actin.molecular.datamodel.evidence.MolecularEventType;
import com.hartwig.actin.molecular.datamodel.evidence.MolecularEvidence;
import com.hartwig.actin.molecular.datamodel.evidence.TreatmentEvidence;
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
import com.hartwig.actin.molecular.sort.evidence.ActinTrialEvidenceComparator;
import com.hartwig.actin.molecular.sort.evidence.ExternalTrialEvidenceComparator;
import com.hartwig.actin.molecular.sort.evidence.TreatmentEvidenceComparator;
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
                    .containsTumorCells(bool(record, "containsTumorCells"))
                    .hasSufficientQuality(bool(record, "hasSufficientQuality"))
                    .characteristics(toMolecularCharacteristics(object(record, "characteristics")))
                    .drivers(toMolecularDrivers(object(record, "drivers")))
                    .immunology(toMolecularImmunology(object(record, "immunology")))
                    .pharmaco(toPharmacoEntries(array(record, "pharmaco")))
                    .wildTypeGenes(nullableStringList(record, "wildTypeGenes"))
                    .evidence(toMolecularEvidence(object(record, "evidence")))
                    .build();
        }

        @NotNull
        private static MolecularCharacteristics toMolecularCharacteristics(@NotNull JsonObject characteristics) {
            return ImmutableMolecularCharacteristics.builder()
                    .purity(nullableNumber(characteristics, "purity"))
                    .predictedTumorOrigin(toPredictedTumorOrigin(nullableObject(characteristics, "predictedTumorOrigin")))
                    .isMicrosatelliteUnstable(nullableBool(characteristics, "isMicrosatelliteUnstable"))
                    .isHomologousRepairDeficient(nullableBool(characteristics, "isHomologousRepairDeficient"))
                    .tumorMutationalBurden(nullableNumber(characteristics, "tumorMutationalBurden"))
                    .tumorMutationalLoad(nullableInteger(characteristics, "tumorMutationalLoad"))
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
                        .event(string(variant, "event"))
                        .driverLikelihood(DriverLikelihood.valueOf(string(variant, "driverLikelihood")))
                        .gene(string(variant, "gene"))
                        .impact(string(variant, "impact"))
                        .variantCopyNumber(number(variant, "variantCopyNumber"))
                        .totalCopyNumber(number(variant, "totalCopyNumber"))
                        .driverType(VariantDriverType.valueOf(string(variant, "driverType")))
                        .clonalLikelihood(number(variant, "clonalLikelihood"))
                        .build());
            }
            return variants;
        }

        @NotNull
        private static Set<Amplification> toAmplifications(@NotNull JsonArray amplificationArray) {
            Set<Amplification> amplifications = Sets.newTreeSet(new CopyNumberComparator());
            for (JsonElement element : amplificationArray) {
                JsonObject amplification = element.getAsJsonObject();
                amplifications.add(ImmutableAmplification.builder()
                        .event(string(amplification, "event"))
                        .driverLikelihood(DriverLikelihood.valueOf(string(amplification, "driverLikelihood")))
                        .gene(string(amplification, "gene"))
                        .copies(integer(amplification, "copies"))
                        .isPartial(bool(amplification, "isPartial"))
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
                        .event(string(loss, "event"))
                        .driverLikelihood(DriverLikelihood.valueOf(string(loss, "driverLikelihood")))
                        .gene(string(loss, "gene"))
                        .isPartial(bool(loss, "isPartial"))
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
                        .event(string(homozygousDisruption, "event"))
                        .driverLikelihood(DriverLikelihood.valueOf(string(homozygousDisruption, "driverLikelihood")))
                        .gene(string(homozygousDisruption, "gene"))
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
                        .event(string(disruption, "event"))
                        .driverLikelihood(DriverLikelihood.valueOf(string(disruption, "driverLikelihood")))
                        .gene(string(disruption, "gene"))
                        .type(string(disruption, "type"))
                        .junctionCopyNumber(number(disruption, "junctionCopyNumber"))
                        .undisruptedCopyNumber(number(disruption, "undisruptedCopyNumber"))
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
                        .event(string(fusion, "event"))
                        .driverLikelihood(DriverLikelihood.valueOf(string(fusion, "driverLikelihood")))
                        .fiveGene(string(fusion, "fiveGene"))
                        .threeGene(string(fusion, "threeGene"))
                        .details(string(fusion, "details"))
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
                        .event(string(virus, "event"))
                        .driverLikelihood(DriverLikelihood.valueOf(string(virus, "driverLikelihood")))
                        .name(string(virus, "name"))
                        .integrations(integer(virus, "integrations"))
                        .build());
            }
            return viruses;
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

        @NotNull
        private static MolecularEvidence toMolecularEvidence(@NotNull JsonObject evidence) {
            return ImmutableMolecularEvidence.builder()
                    .actinSource(string(evidence, "actinSource"))
                    .actinTrials(toActinTrialEvidences(array(evidence, "actinTrials")))
                    .externalTrialSource(string(evidence, "externalTrialSource"))
                    .externalTrials(toExternalTrialEvidences(array(evidence, "externalTrials")))
                    .evidenceSource(string(evidence, "evidenceSource"))
                    .approvedEvidence(toTreatmentEvidences(array(evidence, "approvedEvidence")))
                    .onLabelExperimentalEvidence(toTreatmentEvidences(array(evidence, "onLabelExperimentalEvidence")))
                    .offLabelExperimentalEvidence(toTreatmentEvidences(array(evidence, "offLabelExperimentalEvidence")))
                    .preClinicalEvidence(toTreatmentEvidences(array(evidence, "preClinicalEvidence")))
                    .knownResistanceEvidence(toTreatmentEvidences(array(evidence, "knownResistanceEvidence")))
                    .suspectResistanceEvidence(toTreatmentEvidences(array(evidence, "suspectResistanceEvidence")))
                    .build();
        }

        @NotNull
        private static Set<ActinTrialEvidence> toActinTrialEvidences(@NotNull JsonArray actinTrialArray) {
            Set<ActinTrialEvidence> actinTrialEvidences = Sets.newTreeSet(new ActinTrialEvidenceComparator());
            for (JsonElement element : actinTrialArray) {
                JsonObject actinTrialEvidence = element.getAsJsonObject();
                actinTrialEvidences.add(ImmutableActinTrialEvidence.builder()
                        .event(string(actinTrialEvidence, "event"))
                        .trialAcronym(string(actinTrialEvidence, "trialAcronym"))
                        .cohortId(nullableString(actinTrialEvidence, "cohortId"))
                        .isInclusionCriterion(bool(actinTrialEvidence, "isInclusionCriterion"))
                        .type(MolecularEventType.valueOf(string(actinTrialEvidence, "type")))
                        .gene(nullableString(actinTrialEvidence, "gene"))
                        .mutation(nullableString(actinTrialEvidence, "mutation"))
                        .build());
            }
            return actinTrialEvidences;
        }

        @NotNull
        private static Set<ExternalTrialEvidence> toExternalTrialEvidences(@NotNull JsonArray externalTrialArray) {
            Set<ExternalTrialEvidence> evidences = Sets.newTreeSet(new ExternalTrialEvidenceComparator());
            for (JsonElement element : externalTrialArray) {
                JsonObject externalTrialEvidence = element.getAsJsonObject();
                evidences.add(ImmutableExternalTrialEvidence.builder()
                        .event(string(externalTrialEvidence, "event"))
                        .trial(string(externalTrialEvidence, "trial"))
                        .build());
            }
            return evidences;
        }

        @NotNull
        private static Set<TreatmentEvidence> toTreatmentEvidences(@NotNull JsonArray evidenceArray) {
            Set<TreatmentEvidence> evidences = Sets.newTreeSet(new TreatmentEvidenceComparator());
            for (JsonElement element : evidenceArray) {
                JsonObject treatmentEvidence = element.getAsJsonObject();
                evidences.add(ImmutableTreatmentEvidence.builder()
                        .event(string(treatmentEvidence, "event"))
                        .treatment(string(treatmentEvidence, "treatment"))
                        .build());
            }
            return evidences;
        }
    }
}
