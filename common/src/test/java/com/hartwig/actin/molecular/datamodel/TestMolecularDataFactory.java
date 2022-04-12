package com.hartwig.actin.molecular.datamodel;

import java.time.LocalDate;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.hartwig.actin.TestDataFactory;
import com.hartwig.actin.molecular.datamodel.driver.ImmutableMolecularDrivers;

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
                .qc(Strings.EMPTY)
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
                .evidence(createTestEvidence())
                .mappedEvents(createTestMappedEvents())
                .build();
    }

    @NotNull
    public static MolecularRecord createExhaustiveTestMolecularRecord() {
        return ImmutableMolecularRecord.builder()
                .from(createProperTestMolecularRecord())
                .mappedEvents(createExhaustiveTestEvents())
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
                .predictedTumorOrigin(createTestPredictedTumorOrigin())
                .isMicrosatelliteUnstable(false)
                .isHomologousRepairDeficient(false)
                .tumorMutationalBurden(13.71)
                .tumorMutationalLoad(185)
                .build();
    }

    @NotNull
    private static PredictedTumorOrigin createTestPredictedTumorOrigin() {
        return ImmutablePredictedTumorOrigin.builder().tumorType("Melanoma").likelihood(0.996).build();
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
    private static MolecularEvidence createTestEvidence() {
        return ImmutableMolecularEvidence.builder()
                .actinSource("local")
                .actinTrials(createTestActinTrials())
                .externalTrialSource("external")
                .externalTrials(createTestExternalTrials())
                .evidenceSource("general")
                .approvedResponsiveEvidence(createTestApprovedResponsiveEvidence())
                .experimentalResponsiveEvidence(createTestExperimentalResponsiveEvidence())
                .otherResponsiveEvidence(createTestOtherResponsiveEvidence())
                .resistanceEvidence(createTestResistanceEvidence())
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
    private static Set<EvidenceEntry> createTestApprovedResponsiveEvidence() {
        Set<EvidenceEntry> result = Sets.newHashSet();

        result.add(ImmutableEvidenceEntry.builder().event("BRAF V600E").treatment("Vemurafenib").build());
        result.add(ImmutableEvidenceEntry.builder().event("BRAF V600E").treatment("Dabrafenib").build());
        result.add(ImmutableEvidenceEntry.builder().event("High TML").treatment("Nivolumab").build());

        return result;
    }

    @NotNull
    private static Set<EvidenceEntry> createTestExperimentalResponsiveEvidence() {
        Set<EvidenceEntry> result = Sets.newHashSet();

        result.add(ImmutableEvidenceEntry.builder().event("High TML").treatment("Pembrolizumab").build());

        return result;
    }

    @NotNull
    private static Set<EvidenceEntry> createTestOtherResponsiveEvidence() {
        Set<EvidenceEntry> result = Sets.newHashSet();

        result.add(ImmutableEvidenceEntry.builder().event("BRAF V600E").treatment("Trametinib").build());

        return result;
    }

    @NotNull
    private static Set<EvidenceEntry> createTestResistanceEvidence() {
        Set<EvidenceEntry> result = Sets.newHashSet();

        result.add(ImmutableEvidenceEntry.builder().event("BRAF V600E").treatment("Erlotinib").build());

        return result;
    }

    @NotNull
    private static MappedActinEvents createExhaustiveTestEvents() {
        return ImmutableMappedActinEvents.builder()
                .from(createTestMappedEvents())
                .amplifiedGenes(Sets.newHashSet("AMP"))
                .wildtypeGenes(Sets.newHashSet("WILD"))
                .fusions(Lists.newArrayList(ImmutableFusionGene.builder().fiveGene("FIVE").threeGene("THREE").build()))
                .build();
    }
}
