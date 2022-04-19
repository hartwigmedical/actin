package com.hartwig.actin.molecular.datamodel;

import java.time.LocalDate;
import java.util.Set;

import com.google.common.collect.Lists;
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
import com.hartwig.actin.molecular.datamodel.driver.ImmutableLoss;
import com.hartwig.actin.molecular.datamodel.driver.ImmutableMolecularDrivers;
import com.hartwig.actin.molecular.datamodel.driver.ImmutableVariant;
import com.hartwig.actin.molecular.datamodel.driver.ImmutableVirus;
import com.hartwig.actin.molecular.datamodel.driver.MolecularDrivers;
import com.hartwig.actin.molecular.datamodel.driver.VariantDriverType;
import com.hartwig.actin.molecular.datamodel.evidence.EvidenceEntry;
import com.hartwig.actin.molecular.datamodel.evidence.ImmutableEvidenceEntry;
import com.hartwig.actin.molecular.datamodel.evidence.ImmutableMolecularEvidence;
import com.hartwig.actin.molecular.datamodel.evidence.MolecularEvidence;
import com.hartwig.actin.molecular.datamodel.mapping.GeneMutation;
import com.hartwig.actin.molecular.datamodel.mapping.ImmutableFusionGene;
import com.hartwig.actin.molecular.datamodel.mapping.ImmutableGeneMutation;
import com.hartwig.actin.molecular.datamodel.mapping.ImmutableInactivatedGene;
import com.hartwig.actin.molecular.datamodel.mapping.ImmutableMappedActinEvents;
import com.hartwig.actin.molecular.datamodel.mapping.InactivatedGene;
import com.hartwig.actin.molecular.datamodel.mapping.MappedActinEvents;
import com.hartwig.actin.molecular.datamodel.pharmaco.ImmutablePharmacoEntry;
import com.hartwig.actin.molecular.datamodel.pharmaco.PharmacoEntry;

import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;

public final class TestMolecularDataFactory {

    private static final LocalDate TODAY = LocalDate.now();

    private static final int DAYS_SINCE_MOLECULAR_ANALYSIS = 5;

    private TestMolecularDataFactory() {
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
                .mappedEvents(ImmutableMappedActinEvents.builder().build())
                .build();
    }

    @NotNull
    public static MolecularRecord createProperTestMolecularRecord() {
        return ImmutableMolecularRecord.builder()
                .from(createMinimalTestMolecularRecord())
                .date(TODAY.minusDays(DAYS_SINCE_MOLECULAR_ANALYSIS))
                .characteristics(createTestCharacteristics())
                .drivers(createTestDrivers())
                .pharmaco(createTestPharmaco())
                .evidence(createTestEvidence())
                .mappedEvents(createTestMappedEvents())
                .build();
    }

    @NotNull
    public static MolecularRecord createExhaustiveTestMolecularRecord() {
        return ImmutableMolecularRecord.builder()
                .from(createProperTestMolecularRecord())
                .drivers(createExhaustiveTestDrivers())
                .mappedEvents(createExhaustiveTestMappedEvents())
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
    private static MolecularCharacteristics createTestCharacteristics() {
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
    private static MolecularDrivers createTestDrivers() {
        return ImmutableMolecularDrivers.builder()
                .addVariants(ImmutableVariant.builder()
                        .event("BRAF V600E")
                        .gene("BRAF")
                        .impact("p.V600E")
                        .variantCopyNumber(4.1)
                        .totalCopyNumber(6.0)
                        .driverType(VariantDriverType.HOTSPOT)
                        .driverLikelihood(1.0)
                        .clonalLikelihood(1.0)
                        .build())
                .addLosses(ImmutableLoss.builder().event("PTEN del").gene("PTEN").isPartial(true).build())
                .build();
    }

    @NotNull
    private static Set<PharmacoEntry> createTestPharmaco() {
        return Sets.newHashSet(ImmutablePharmacoEntry.builder().gene("DPYD").haplotype("1* HOM").build());
    }

    @NotNull
    private static MolecularEvidence createTestEvidence() {
        return ImmutableMolecularEvidence.builder()
                .actinSource("local")
                .actinTrials(createTestActinTrials())
                .externalTrialSource("external")
                .externalTrials(createTestExternalTrials())
                .evidenceSource("general")
                .approvedEvidence(createTestApprovedEvidence())
                .onLabelExperimentalEvidence(createTestOnLabelExperimentalEvidence())
                .offLabelExperimentalEvidence(createTestOffLabelExperimentalEvidence())
                .preClinicalEvidence(createTestPreClinicalEvidence())
                .knownResistanceEvidence(createTestKnownResistanceEvidence())
                .suspectResistanceEvidence(createTestSuspectResistanceEvidence())
                .build();
    }

    @NotNull
    private static Set<EvidenceEntry> createTestActinTrials() {
        Set<EvidenceEntry> result = Sets.newHashSet();

        result.add(ImmutableEvidenceEntry.builder().event("BRAF V600E").treatment("Trial 1").build());
        result.add(ImmutableEvidenceEntry.builder().event("High TML").treatment("Trial 1").build());

        return result;
    }

    @NotNull
    private static Set<EvidenceEntry> createTestExternalTrials() {
        Set<EvidenceEntry> result = Sets.newHashSet();

        result.add(ImmutableEvidenceEntry.builder().event("BRAF V600E").treatment("Trial 1").build());
        result.add(ImmutableEvidenceEntry.builder().event("High TML").treatment("Trial 1").build());

        return result;
    }

    @NotNull
    private static Set<EvidenceEntry> createTestApprovedEvidence() {
        Set<EvidenceEntry> result = Sets.newHashSet();

        result.add(ImmutableEvidenceEntry.builder().event("BRAF V600E").treatment("Vemurafenib").build());
        result.add(ImmutableEvidenceEntry.builder().event("BRAF V600E").treatment("Dabrafenib").build());
        result.add(ImmutableEvidenceEntry.builder().event("High TML").treatment("Nivolumab").build());

        return result;
    }

    @NotNull
    private static Set<EvidenceEntry> createTestOnLabelExperimentalEvidence() {
        Set<EvidenceEntry> result = Sets.newHashSet();

        result.add(ImmutableEvidenceEntry.builder().event("High TML").treatment("Pembrolizumab").build());

        return result;
    }

    @NotNull
    private static Set<EvidenceEntry> createTestOffLabelExperimentalEvidence() {
        Set<EvidenceEntry> result = Sets.newHashSet();

        result.add(ImmutableEvidenceEntry.builder().event("BRAF V600E").treatment("Trametinib").build());

        return result;
    }

    @NotNull
    private static Set<EvidenceEntry> createTestPreClinicalEvidence() {
        Set<EvidenceEntry> result = Sets.newHashSet();

        result.add(ImmutableEvidenceEntry.builder().event("BRAF V600E").treatment("Pre-clinical treatment").build());

        return result;
    }

    @NotNull
    private static Set<EvidenceEntry> createTestKnownResistanceEvidence() {
        Set<EvidenceEntry> result = Sets.newHashSet();

        result.add(ImmutableEvidenceEntry.builder().event("BRAF V600E").treatment("Erlotinib").build());

        return result;
    }

    @NotNull
    private static Set<EvidenceEntry> createTestSuspectResistanceEvidence() {
        Set<EvidenceEntry> result = Sets.newHashSet();

        result.add(ImmutableEvidenceEntry.builder().event("BRAF V600E").treatment("Some treatment").build());

        return result;
    }

    @NotNull
    private static MappedActinEvents createTestMappedEvents() {
        return ImmutableMappedActinEvents.builder()
                .mutations(createTestMutations())
                .activatedGenes(Sets.newHashSet("BRAF"))
                .inactivatedGenes(createTestInactivatedGenes())
                .amplifiedGenes(Sets.newHashSet())
                .wildtypeGenes(Sets.newHashSet())
                .fusions(Lists.newArrayList())
                .build();
    }

    @NotNull
    private static Set<GeneMutation> createTestMutations() {
        Set<GeneMutation> mutations = Sets.newHashSet();

        mutations.add(ImmutableGeneMutation.builder().gene("BRAF").mutation("V600E").build());

        return mutations;
    }

    @NotNull
    private static Set<InactivatedGene> createTestInactivatedGenes() {
        Set<InactivatedGene> inactivatedGenes = Sets.newHashSet();

        inactivatedGenes.add(ImmutableInactivatedGene.builder().gene("PTEN").hasBeenDeleted(false).build());
        inactivatedGenes.add(ImmutableInactivatedGene.builder().gene("CDKN2A").hasBeenDeleted(false).build());

        return inactivatedGenes;
    }

    @NotNull
    private static MolecularDrivers createExhaustiveTestDrivers() {
        return ImmutableMolecularDrivers.builder()
                .from(createTestDrivers())
                .addAmplifications(ImmutableAmplification.builder().event("MYC amp").gene("MYC").copies(38).isPartial(false).build())
                .addDisruptions(ImmutableDisruption.builder()
                        .event(Strings.EMPTY)
                        .gene("PTEN")
                        .details("Intron 1 downstream")
                        .isHomozygous(false)
                        .build())
                .addFusions(ImmutableFusion.builder()
                        .event("EML4-ALK fusion")
                        .fiveGene("EML4")
                        .threeGene("ALK")
                        .details("Exon 2 - Exon 4")
                        .driverType(FusionDriverType.KNOWN)
                        .driverLikelihood(DriverLikelihood.HIGH)
                        .build())
                .addViruses(ImmutableVirus.builder()
                        .event("HPV positive")
                        .name("Human papillomavirus type 16d")
                        .details("3 integrations detected")
                        .driverLikelihood(DriverLikelihood.HIGH)
                        .build())
                .build();
    }

    @NotNull
    private static MappedActinEvents createExhaustiveTestMappedEvents() {
        return ImmutableMappedActinEvents.builder()
                .from(createTestMappedEvents())
                .amplifiedGenes(Sets.newHashSet("AMP"))
                .wildtypeGenes(Sets.newHashSet("WILD"))
                .fusions(Lists.newArrayList(ImmutableFusionGene.builder().fiveGene("FIVE").threeGene("THREE").build()))
                .build();
    }
}
