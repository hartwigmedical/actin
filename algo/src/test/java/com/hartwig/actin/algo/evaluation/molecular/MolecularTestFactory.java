package com.hartwig.actin.algo.evaluation.molecular;

import com.google.common.collect.Lists;
import com.hartwig.actin.ImmutablePatientRecord;
import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.TestDataFactory;
import com.hartwig.actin.molecular.datamodel.ImmutableFusionGene;
import com.hartwig.actin.molecular.datamodel.ImmutableGeneMutation;
import com.hartwig.actin.molecular.datamodel.ImmutableInactivatedGene;
import com.hartwig.actin.molecular.datamodel.ImmutableMolecularRecord;
import com.hartwig.actin.molecular.datamodel.MolecularRecord;
import com.hartwig.actin.molecular.datamodel.TestMolecularDataFactory;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

final class MolecularTestFactory {

    private MolecularTestFactory() {
    }

    @NotNull
    public static PatientRecord withGeneMutation(@NotNull String gene, @NotNull String mutation) {
        return withMolecularRecord(ImmutableMolecularRecord.builder()
                .from(TestMolecularDataFactory.createMinimalTestMolecularRecord())
                .mutations(Lists.newArrayList(ImmutableGeneMutation.builder().gene(gene).mutation(mutation).build()))
                .build());
    }

    @NotNull
    public static PatientRecord withActivatedGene(@NotNull String gene) {
        return withMolecularRecord(ImmutableMolecularRecord.builder()
                .from(TestMolecularDataFactory.createMinimalTestMolecularRecord())
                .activatedGenes(Lists.newArrayList(gene))
                .build());
    }

    @NotNull
    public static PatientRecord withInactivatedGene(@NotNull String gene, boolean hasBeenDeleted) {
        return withMolecularRecord(ImmutableMolecularRecord.builder()
                .from(TestMolecularDataFactory.createMinimalTestMolecularRecord())
                .inactivatedGenes(Lists.newArrayList(ImmutableInactivatedGene.builder().gene(gene).hasBeenDeleted(hasBeenDeleted).build()))
                .build());
    }

    @NotNull
    public static PatientRecord withAmplifiedGene(@NotNull String gene) {
        return withMolecularRecord(ImmutableMolecularRecord.builder()
                .from(TestMolecularDataFactory.createMinimalTestMolecularRecord())
                .amplifiedGenes(Lists.newArrayList(gene))
                .build());
    }

    @NotNull
    public static PatientRecord withWildtypeGene(@NotNull String gene) {
        return withMolecularRecord(ImmutableMolecularRecord.builder()
                .from(TestMolecularDataFactory.createMinimalTestMolecularRecord())
                .wildtypeGenes(Lists.newArrayList(gene))
                .build());
    }

    @NotNull
    public static PatientRecord withFusionGene(@NotNull String fiveGene, @NotNull String threeGene) {
        return withMolecularRecord(ImmutableMolecularRecord.builder()
                .from(TestMolecularDataFactory.createMinimalTestMolecularRecord())
                .fusions(Lists.newArrayList(ImmutableFusionGene.builder().fiveGene(fiveGene).threeGene(threeGene).build()))
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
    public static PatientRecord withTumorMutationalBurden(double tumorMutationalBurden) {
        return withMolecularRecord(ImmutableMolecularRecord.builder()
                .from(TestMolecularDataFactory.createMinimalTestMolecularRecord())
                .tumorMutationalBurden(tumorMutationalBurden)
                .build());
    }

    @NotNull
    public static PatientRecord withTumorMutationalLoad(int tumorMutationalLoad) {
        return withMolecularRecord(ImmutableMolecularRecord.builder()
                .from(TestMolecularDataFactory.createMinimalTestMolecularRecord())
                .tumorMutationalLoad(tumorMutationalLoad)
                .build());
    }

    @NotNull
    private static PatientRecord withMolecularRecord(@NotNull MolecularRecord molecular) {
        return ImmutablePatientRecord.builder().from(TestDataFactory.createMinimalTestPatientRecord()).molecular(molecular).build();
    }
}
