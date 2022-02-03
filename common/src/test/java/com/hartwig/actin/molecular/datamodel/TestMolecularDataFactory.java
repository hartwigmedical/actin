package com.hartwig.actin.molecular.datamodel;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.hartwig.actin.TestDataFactory;

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
                .hasReliableQuality(false)
                .externalTrialSource(Strings.EMPTY)
                .evidenceSource(Strings.EMPTY)
                .build();
    }

    @NotNull
    public static MolecularRecord createProperTestMolecularRecord() {
        return ImmutableMolecularRecord.builder()
                .from(createMinimalTestMolecularRecord())
                .date(TODAY.minusDays(DAYS_SINCE_MOLECULAR_ANALYSIS))
                .hasReliableQuality(true)
                .mutations(createTestMutations())
                .activatedGenes(Sets.newHashSet("BRAF"))
                .inactivatedGenes(createTestInactivatedGenes())
                .amplifiedGenes(Sets.newHashSet())
                .wildtypeGenes(Sets.newHashSet())
                .fusions(Lists.newArrayList())
                .isMicrosatelliteUnstable(false)
                .isHomologousRepairDeficient(false)
                .tumorMutationalBurden(13.71)
                .tumorMutationalLoad(185)
                .actinTrials(createTestActinTrials())
                .externalTrialSource("CKB")
                .externalTrials(createTestExternalTrials())
                .evidenceSource("CKB")
                .approvedResponsiveEvidence(createTestApprovedResponsiveEvidence())
                .experimentalResponsiveEvidence(createTestExperimentalResponsiveEvidence())
                .otherResponsiveEvidence(createTestOtherResponsiveEvidence())
                .resistanceEvidence(createTestResistanceEvidence())
                .build();
    }

    @NotNull
    public static MolecularRecord createExhaustiveTestMolecularRecord() {
        return ImmutableMolecularRecord.builder()
                .from(createProperTestMolecularRecord())
                .amplifiedGenes(Sets.newHashSet("AMP"))
                .wildtypeGenes(Sets.newHashSet("WILD"))
                .fusions(Lists.newArrayList(ImmutableFusionGene.builder().fiveGene("FIVE").threeGene("THREE").build()))
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
    private static List<MolecularEvidence> createTestActinTrials() {
        List<MolecularEvidence> result = Lists.newArrayList();

        result.add(ImmutableMolecularEvidence.builder().event("BRAF V600E").treatment("Trial 1").build());
        result.add(ImmutableMolecularEvidence.builder().event("High TML").treatment("Trial 1").build());

        return result;
    }

    @NotNull
    private static List<MolecularEvidence> createTestExternalTrials() {
        List<MolecularEvidence> result = Lists.newArrayList();

        result.add(ImmutableMolecularEvidence.builder().event("BRAF V600E").treatment("Trial 1").build());
        result.add(ImmutableMolecularEvidence.builder().event("High TML").treatment("Trial 1").build());

        return result;
    }

    @NotNull
    private static List<MolecularEvidence> createTestApprovedResponsiveEvidence() {
        List<MolecularEvidence> result = Lists.newArrayList();

        result.add(ImmutableMolecularEvidence.builder().event("BRAF V600E").treatment("Vemurafenib").build());
        result.add(ImmutableMolecularEvidence.builder().event("BRAF V600E").treatment("Dabrafenib").build());
        result.add(ImmutableMolecularEvidence.builder().event("High TML").treatment("Nivolumab").build());

        return result;
    }

    @NotNull
    private static List<MolecularEvidence> createTestExperimentalResponsiveEvidence() {
        List<MolecularEvidence> result = Lists.newArrayList();

        result.add(ImmutableMolecularEvidence.builder().event("High TML").treatment("Pembrolizumab").build());

        return result;
    }

    @NotNull
    private static List<? extends MolecularEvidence> createTestOtherResponsiveEvidence() {
        List<MolecularEvidence> result = Lists.newArrayList();

        result.add(ImmutableMolecularEvidence.builder().event("BRAF V600E").treatment("Trametinib").build());

        return result;
    }

    @NotNull
    private static List<? extends MolecularEvidence> createTestResistanceEvidence() {
        List<MolecularEvidence> result = Lists.newArrayList();

        result.add(ImmutableMolecularEvidence.builder().event("BRAF V600E").treatment("Erlotinib").build());

        return result;
    }

}
