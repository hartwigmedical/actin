package com.hartwig.actin.algo.evaluation.molecular;

import java.util.List;

import com.google.common.collect.Sets;
import com.hartwig.actin.ImmutablePatientRecord;
import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.TestDataFactory;
import com.hartwig.actin.clinical.datamodel.ImmutableClinicalRecord;
import com.hartwig.actin.clinical.datamodel.ImmutablePriorMolecularTest;
import com.hartwig.actin.clinical.datamodel.PriorMolecularTest;
import com.hartwig.actin.molecular.datamodel.ImmutableFusionGene;
import com.hartwig.actin.molecular.datamodel.ImmutableGeneMutation;
import com.hartwig.actin.molecular.datamodel.ImmutableInactivatedGene;
import com.hartwig.actin.molecular.datamodel.ImmutableMolecularRecord;
import com.hartwig.actin.molecular.datamodel.MolecularRecord;
import com.hartwig.actin.molecular.datamodel.TestMolecularDataFactory;

import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

final class MolecularTestFactory {

    private MolecularTestFactory() {
    }

    @NotNull
    public static ImmutablePriorMolecularTest.Builder builder() {
        return ImmutablePriorMolecularTest.builder().test(Strings.EMPTY).item(Strings.EMPTY);
    }

    @NotNull
    public static PatientRecord withPriorMolecularTests(@NotNull List<PriorMolecularTest> priorMolecularTests) {
        PatientRecord base = TestDataFactory.createMinimalTestPatientRecord();
        return ImmutablePatientRecord.builder()
                .from(base)
                .clinical(ImmutableClinicalRecord.builder().from(base.clinical()).priorMolecularTests(priorMolecularTests).build())
                .build();
    }

    @NotNull
    public static PatientRecord withGeneMutation(@NotNull String gene, @NotNull String mutation) {
        return withMolecularRecord(ImmutableMolecularRecord.builder()
                .from(TestMolecularDataFactory.createMinimalTestMolecularRecord())
                .mutations(Sets.newHashSet(ImmutableGeneMutation.builder().gene(gene).mutation(mutation).build()))
                .build());
    }

    @NotNull
    public static PatientRecord withActivatedGene(@NotNull String gene) {
        return withMolecularRecord(ImmutableMolecularRecord.builder()
                .from(TestMolecularDataFactory.createMinimalTestMolecularRecord())
                .activatedGenes(Sets.newHashSet(gene))
                .build());
    }

    @NotNull
    public static PatientRecord withInactivatedGene(@NotNull String gene, boolean hasBeenDeleted) {
        return withMolecularRecord(ImmutableMolecularRecord.builder()
                .from(TestMolecularDataFactory.createMinimalTestMolecularRecord())
                .inactivatedGenes(Sets.newHashSet(ImmutableInactivatedGene.builder().gene(gene).hasBeenDeleted(hasBeenDeleted).build()))
                .build());
    }

    @NotNull
    public static PatientRecord withAmplifiedGene(@NotNull String gene) {
        return withMolecularRecord(ImmutableMolecularRecord.builder()
                .from(TestMolecularDataFactory.createMinimalTestMolecularRecord())
                .amplifiedGenes(Sets.newHashSet(gene))
                .build());
    }

    @NotNull
    public static PatientRecord withWildtypeGene(@NotNull String gene) {
        return withMolecularRecord(ImmutableMolecularRecord.builder()
                .from(TestMolecularDataFactory.createMinimalTestMolecularRecord())
                .wildtypeGenes(Sets.newHashSet(gene))
                .build());
    }

    @NotNull
    public static PatientRecord withFusionGene(@NotNull String fiveGene, @NotNull String threeGene) {
        return withMolecularRecord(ImmutableMolecularRecord.builder()
                .from(TestMolecularDataFactory.createMinimalTestMolecularRecord())
                .fusions(Sets.newHashSet(ImmutableFusionGene.builder().fiveGene(fiveGene).threeGene(threeGene).build()))
                .build());
    }

    @NotNull
    public static PatientRecord withMicrosatelliteInstability(@Nullable Boolean isMicrosatelliteUnstable) {
        return withMolecularRecord(ImmutableMolecularRecord.builder()
                .from(TestMolecularDataFactory.createMinimalTestMolecularRecord())
                .isMicrosatelliteUnstable(isMicrosatelliteUnstable)
                .build());
    }

    @NotNull
    public static PatientRecord withHomologousRepairDeficiency(@Nullable Boolean isHomologousRepairDeficient) {
        return withMolecularRecord(ImmutableMolecularRecord.builder()
                .from(TestMolecularDataFactory.createMinimalTestMolecularRecord())
                .isHomologousRepairDeficient(isHomologousRepairDeficient)
                .build());
    }

    @NotNull
    public static PatientRecord withTumorMutationalBurden(@Nullable Double tumorMutationalBurden) {
        return withMolecularRecord(ImmutableMolecularRecord.builder()
                .from(TestMolecularDataFactory.createMinimalTestMolecularRecord())
                .tumorMutationalBurden(tumorMutationalBurden)
                .build());
    }

    @NotNull
    public static PatientRecord withTumorMutationalLoad(@Nullable Integer tumorMutationalLoad) {
        return withMolecularRecord(ImmutableMolecularRecord.builder()
                .from(TestMolecularDataFactory.createMinimalTestMolecularRecord())
                .hasReliableQuality(true)
                .tumorMutationalLoad(tumorMutationalLoad)
                .build());
    }

    @NotNull
    private static PatientRecord withMolecularRecord(@NotNull MolecularRecord molecular) {
        MolecularRecord reliable = ImmutableMolecularRecord.builder().from(molecular).hasReliableQuality(true).build();
        return ImmutablePatientRecord.builder().from(TestDataFactory.createMinimalTestPatientRecord()).molecular(reliable).build();
    }
}
