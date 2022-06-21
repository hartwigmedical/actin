package com.hartwig.actin.molecular.datamodel;

import java.time.LocalDate;
import java.util.Set;

import com.google.common.collect.Sets;
import com.hartwig.actin.TestDataFactory;
import com.hartwig.actin.molecular.datamodel.characteristics.ImmutableMolecularCharacteristics;
import com.hartwig.actin.molecular.datamodel.characteristics.ImmutablePredictedTumorOrigin;
import com.hartwig.actin.molecular.datamodel.characteristics.MolecularCharacteristics;
import com.hartwig.actin.molecular.datamodel.driver.DriverLikelihood;
import com.hartwig.actin.molecular.datamodel.driver.FusionDriverType;
import com.hartwig.actin.molecular.datamodel.driver.ImmutableAmplification;
import com.hartwig.actin.molecular.datamodel.driver.ImmutableDisruption;
import com.hartwig.actin.molecular.datamodel.driver.ImmutableFusion;
import com.hartwig.actin.molecular.datamodel.driver.ImmutableHomozygousDisruption;
import com.hartwig.actin.molecular.datamodel.driver.ImmutableLoss;
import com.hartwig.actin.molecular.datamodel.driver.ImmutableMolecularDrivers;
import com.hartwig.actin.molecular.datamodel.driver.ImmutableVariant;
import com.hartwig.actin.molecular.datamodel.driver.ImmutableVirus;
import com.hartwig.actin.molecular.datamodel.driver.MolecularDrivers;
import com.hartwig.actin.molecular.datamodel.driver.VariantDriverType;
import com.hartwig.actin.molecular.datamodel.evidence.ActinTrialEvidence;
import com.hartwig.actin.molecular.datamodel.evidence.ExternalTrialEvidence;
import com.hartwig.actin.molecular.datamodel.evidence.ImmutableActinTrialEvidence;
import com.hartwig.actin.molecular.datamodel.evidence.ImmutableExternalTrialEvidence;
import com.hartwig.actin.molecular.datamodel.evidence.ImmutableMolecularEvidence;
import com.hartwig.actin.molecular.datamodel.evidence.ImmutableTreatmentEvidence;
import com.hartwig.actin.molecular.datamodel.evidence.MolecularEventType;
import com.hartwig.actin.molecular.datamodel.evidence.MolecularEvidence;
import com.hartwig.actin.molecular.datamodel.evidence.TreatmentEvidence;
import com.hartwig.actin.molecular.datamodel.pharmaco.ImmutableHaplotype;
import com.hartwig.actin.molecular.datamodel.pharmaco.ImmutablePharmacoEntry;
import com.hartwig.actin.molecular.datamodel.pharmaco.PharmacoEntry;

import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;

public final class TestMolecularFactory {

    private static final LocalDate TODAY = LocalDate.now();

    private static final int DAYS_SINCE_MOLECULAR_ANALYSIS = 5;

    private TestMolecularFactory() {
    }

    @NotNull
    public static MolecularRecord createMinimalTestMolecularRecord() {
        return ImmutableMolecularRecord.builder()
                .sampleId(TestDataFactory.TEST_SAMPLE)
                .type(ExperimentType.WGS)
                .hasReliableQuality(true)
                .characteristics(ImmutableMolecularCharacteristics.builder().build())
                .drivers(ImmutableMolecularDrivers.builder().build())
                .evidence(createMinimalTestEvidence())
                .build();
    }

    @NotNull
    public static MolecularRecord createProperTestMolecularRecord() {
        return ImmutableMolecularRecord.builder()
                .from(createMinimalTestMolecularRecord())
                .date(TODAY.minusDays(DAYS_SINCE_MOLECULAR_ANALYSIS))
                .characteristics(createProperTestCharacteristics())
                .drivers(createProperTestDrivers())
                .pharmaco(createProperTestPharmaco())
                .wildTypeGenes(createTestWildTypeGenes())
                .evidence(createProperTestEvidence())
                .build();
    }

    @NotNull
    public static MolecularRecord createExhaustiveTestMolecularRecord() {
        return ImmutableMolecularRecord.builder()
                .from(createProperTestMolecularRecord())
                .drivers(createExhaustiveTestDrivers())
                .evidence(createExhaustiveTestEvidence())
                .build();
    }

    @NotNull
    private static MolecularEvidence createMinimalTestEvidence() {
        return ImmutableMolecularEvidence.builder()
                .actinSource(Strings.EMPTY)
                .externalTrialSource(Strings.EMPTY)
                .evidenceSource(Strings.EMPTY)
                .build();
    }

    @NotNull
    private static MolecularCharacteristics createProperTestCharacteristics() {
        return ImmutableMolecularCharacteristics.builder()
                .purity(0.98)
                .hasReliablePurity(true)
                .predictedTumorOrigin(ImmutablePredictedTumorOrigin.builder().tumorType("Melanoma").likelihood(0.996).build())
                .isMicrosatelliteUnstable(false)
                .isHomologousRepairDeficient(false)
                .tumorMutationalBurden(13.71)
                .tumorMutationalLoad(185)
                .build();
    }

    @NotNull
    private static MolecularDrivers createProperTestDrivers() {
        return ImmutableMolecularDrivers.builder()
                .addVariants(ImmutableVariant.builder()
                        .event("BRAF V600E")
                        .driverLikelihood(DriverLikelihood.HIGH)
                        .gene("BRAF")
                        .impact("p.V600E")
                        .variantCopyNumber(4.1)
                        .totalCopyNumber(6.0)
                        .driverType(VariantDriverType.HOTSPOT)
                        .clonalLikelihood(1.0)
                        .build())
                .addLosses(ImmutableLoss.builder()
                        .event("PTEN del")
                        .driverLikelihood(DriverLikelihood.HIGH)
                        .gene("PTEN")
                        .isPartial(true)
                        .build())
                .build();
    }

    @NotNull
    private static Set<PharmacoEntry> createProperTestPharmaco() {
        return Sets.newHashSet(ImmutablePharmacoEntry.builder()
                .gene("DPYD")
                .addHaplotypes(ImmutableHaplotype.builder().name("1* HOM").function("Normal function").build())
                .build());
    }

    @NotNull
    private static Iterable<String> createTestWildTypeGenes() {
        Set<String> wildTypeGenes = Sets.newHashSet();
        wildTypeGenes.add("KRAS");
        return wildTypeGenes;
    }

    @NotNull
    private static MolecularEvidence createProperTestEvidence() {
        return ImmutableMolecularEvidence.builder()
                .actinSource("Local")
                .actinTrials(createTestActinTrials())
                .externalTrialSource("External")
                .externalTrials(createTestExternalTrials())
                .evidenceSource("General")
                .approvedEvidence(createTestApprovedEvidence())
                .onLabelExperimentalEvidence(createTestOnLabelExperimentalEvidence())
                .offLabelExperimentalEvidence(createTestOffLabelExperimentalEvidence())
                .preClinicalEvidence(createTestPreClinicalEvidence())
                .build();
    }

    @NotNull
    private static MolecularEvidence createExhaustiveTestEvidence() {
        return ImmutableMolecularEvidence.builder()
                .from(createProperTestEvidence())
                .knownResistanceEvidence(createTestKnownResistanceEvidence())
                .suspectResistanceEvidence(createTestSuspectResistanceEvidence())
                .build();
    }


    @NotNull
    private static Set<ActinTrialEvidence> createTestActinTrials() {
        Set<ActinTrialEvidence> result = Sets.newHashSet();

        result.add(ImmutableActinTrialEvidence.builder()
                .event("BRAF V600E")
                .trialAcronym("Trial 1")
                .cohortId("A")
                .isInclusionCriterion(true)
                .type(MolecularEventType.MUTATED_GENE)
                .gene("BRAF")
                .mutation("V600E")
                .build());
        result.add(ImmutableActinTrialEvidence.builder()
                .event("High tumor mutational load")
                .trialAcronym("Trial 1")
                .cohortId(null)
                .isInclusionCriterion(false)
                .type(MolecularEventType.SIGNATURE)
                .gene(null)
                .mutation(null)
                .build());

        return result;
    }

    @NotNull
    private static Set<ExternalTrialEvidence> createTestExternalTrials() {
        Set<ExternalTrialEvidence> result = Sets.newHashSet();

        result.add(ImmutableExternalTrialEvidence.builder().event("BRAF V600E").trial("External test trial 1").build());
        result.add(ImmutableExternalTrialEvidence.builder().event("High TML").trial("External test trial 2").build());

        return result;
    }

    @NotNull
    private static Set<TreatmentEvidence> createTestApprovedEvidence() {
        Set<TreatmentEvidence> result = Sets.newHashSet();

        result.add(ImmutableTreatmentEvidence.builder().event("BRAF V600E").treatment("Vemurafenib").build());
        result.add(ImmutableTreatmentEvidence.builder().event("BRAF V600E").treatment("Dabrafenib").build());
        result.add(ImmutableTreatmentEvidence.builder().event("High TML").treatment("Nivolumab").build());

        return result;
    }

    @NotNull
    private static Set<TreatmentEvidence> createTestOnLabelExperimentalEvidence() {
        Set<TreatmentEvidence> result = Sets.newHashSet();

        result.add(ImmutableTreatmentEvidence.builder().event("High TML").treatment("Pembrolizumab").build());

        return result;
    }

    @NotNull
    private static Set<TreatmentEvidence> createTestOffLabelExperimentalEvidence() {
        Set<TreatmentEvidence> result = Sets.newHashSet();

        result.add(ImmutableTreatmentEvidence.builder().event("BRAF V600E").treatment("Trametinib").build());

        return result;
    }

    @NotNull
    private static Set<TreatmentEvidence> createTestPreClinicalEvidence() {
        Set<TreatmentEvidence> result = Sets.newHashSet();

        result.add(ImmutableTreatmentEvidence.builder().event("BRAF V600E").treatment("Pre-clinical treatment").build());

        return result;
    }

    @NotNull
    private static Set<TreatmentEvidence> createTestKnownResistanceEvidence() {
        Set<TreatmentEvidence> result = Sets.newHashSet();

        result.add(ImmutableTreatmentEvidence.builder().event("BRAF V600E").treatment("Erlotinib").build());

        return result;
    }

    @NotNull
    private static Set<TreatmentEvidence> createTestSuspectResistanceEvidence() {
        Set<TreatmentEvidence> result = Sets.newHashSet();

        result.add(ImmutableTreatmentEvidence.builder().event("BRAF V600E").treatment("Some treatment").build());

        return result;
    }

    @NotNull
    private static MolecularDrivers createExhaustiveTestDrivers() {
        return ImmutableMolecularDrivers.builder()
                .from(createProperTestDrivers())
                .addAmplifications(ImmutableAmplification.builder()
                        .event("MYC amp")
                        .driverLikelihood(DriverLikelihood.HIGH)
                        .gene("MYC")
                        .copies(38)
                        .isPartial(false)
                        .build())
                .addHomozygousDisruptions(ImmutableHomozygousDisruption.builder()
                        .event("PTEN disruption")
                        .driverLikelihood(DriverLikelihood.HIGH)
                        .gene("PTEN")
                        .build())
                .addDisruptions(ImmutableDisruption.builder()
                        .event(Strings.EMPTY)
                        .driverLikelihood(DriverLikelihood.LOW)
                        .gene("PTEN")
                        .type("DEL")
                        .junctionCopyNumber(1.1)
                        .undisruptedCopyNumber(1.8)
                        .range("Intron 1 downstream")
                        .build())
                .addFusions(ImmutableFusion.builder()
                        .event("EML4-ALK fusion")
                        .driverLikelihood(DriverLikelihood.HIGH)
                        .fiveGene("EML4")
                        .threeGene("ALK")
                        .details("Exon 2 - Exon 4")
                        .driverType(FusionDriverType.KNOWN)
                        .build())
                .addViruses(ImmutableVirus.builder()
                        .event("HPV positive")
                        .driverLikelihood(DriverLikelihood.HIGH)
                        .name("Human papillomavirus type 16d")
                        .integrations(3)
                        .build())
                .build();
    }
}
