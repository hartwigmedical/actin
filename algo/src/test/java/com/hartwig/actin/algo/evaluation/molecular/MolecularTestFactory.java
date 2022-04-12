package com.hartwig.actin.algo.evaluation.molecular;

import java.util.List;

import com.google.common.collect.Sets;
import com.hartwig.actin.ImmutablePatientRecord;
import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.TestDataFactory;
import com.hartwig.actin.clinical.datamodel.ImmutableClinicalRecord;
import com.hartwig.actin.clinical.datamodel.PriorMolecularTest;
import com.hartwig.actin.molecular.datamodel.ImmutableMolecularRecord;
import com.hartwig.actin.molecular.datamodel.MolecularRecord;
import com.hartwig.actin.molecular.datamodel.TestMolecularDataFactory;
import com.hartwig.actin.molecular.datamodel.characteristics.ImmutableMolecularCharacteristics;
import com.hartwig.actin.molecular.datamodel.mapping.ImmutableFusionGene;
import com.hartwig.actin.molecular.datamodel.mapping.ImmutableGeneMutation;
import com.hartwig.actin.molecular.datamodel.mapping.ImmutableInactivatedGene;
import com.hartwig.actin.molecular.datamodel.mapping.ImmutableMappedActinEvents;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

final class MolecularTestFactory {

    private MolecularTestFactory() {
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
                .mappedEvents(ImmutableMappedActinEvents.builder()
                        .mutations(Sets.newHashSet(ImmutableGeneMutation.builder().gene(gene).mutation(mutation).build()))
                        .build())
                .build());
    }

    @NotNull
    public static PatientRecord withActivatedGene(@NotNull String gene) {
        return withMolecularRecord(ImmutableMolecularRecord.builder()
                .from(TestMolecularDataFactory.createMinimalTestMolecularRecord())
                .mappedEvents(ImmutableMappedActinEvents.builder().activatedGenes(Sets.newHashSet(gene)).build())
                .build());
    }

    @NotNull
    public static PatientRecord withInactivatedGene(@NotNull String gene, boolean hasBeenDeleted) {
        return withMolecularRecord(ImmutableMolecularRecord.builder()
                .from(TestMolecularDataFactory.createMinimalTestMolecularRecord())
                .mappedEvents(ImmutableMappedActinEvents.builder()
                        .inactivatedGenes(Sets.newHashSet(ImmutableInactivatedGene.builder()
                                .gene(gene)
                                .hasBeenDeleted(hasBeenDeleted)
                                .build()))
                        .build())
                .build());
    }

    @NotNull
    public static PatientRecord withAmplifiedGene(@NotNull String gene) {
        return withMolecularRecord(ImmutableMolecularRecord.builder()
                .from(TestMolecularDataFactory.createMinimalTestMolecularRecord())
                .mappedEvents(ImmutableMappedActinEvents.builder().amplifiedGenes(Sets.newHashSet(gene)).build())
                .build());
    }

    @NotNull
    public static PatientRecord withWildtypeGene(@NotNull String gene) {
        return withMolecularRecord(ImmutableMolecularRecord.builder()
                .from(TestMolecularDataFactory.createMinimalTestMolecularRecord())
                .mappedEvents(ImmutableMappedActinEvents.builder().wildtypeGenes(Sets.newHashSet(gene)).build())
                .build());
    }

    @NotNull
    public static PatientRecord withFusionGene(@NotNull String fiveGene, @NotNull String threeGene) {
        return withMolecularRecord(ImmutableMolecularRecord.builder()
                .from(TestMolecularDataFactory.createMinimalTestMolecularRecord())
                .mappedEvents(ImmutableMappedActinEvents.builder()
                        .fusions(Sets.newHashSet(ImmutableFusionGene.builder().fiveGene(fiveGene).threeGene(threeGene).build()))
                        .build())
                .build());
    }

    @NotNull
    public static PatientRecord withMicrosatelliteInstability(@Nullable Boolean isMicrosatelliteUnstable) {
        MolecularRecord base = TestMolecularDataFactory.createMinimalTestMolecularRecord();

        return withMolecularRecord(ImmutableMolecularRecord.builder()
                .from(base)
                .characteristics(ImmutableMolecularCharacteristics.builder()
                        .from(base.characteristics())
                        .isMicrosatelliteUnstable(isMicrosatelliteUnstable)
                        .build())
                .build());
    }

    @NotNull
    public static PatientRecord withHomologousRepairDeficiency(@Nullable Boolean isHomologousRepairDeficient) {
        MolecularRecord base = TestMolecularDataFactory.createMinimalTestMolecularRecord();

        return withMolecularRecord(ImmutableMolecularRecord.builder()
                .from(base)
                .characteristics(ImmutableMolecularCharacteristics.builder()
                        .from(base.characteristics())
                        .isHomologousRepairDeficient(isHomologousRepairDeficient)
                        .build())
                .build());
    }

    @NotNull
    public static PatientRecord withTumorMutationalBurden(@Nullable Double tumorMutationalBurden) {
        MolecularRecord base = TestMolecularDataFactory.createMinimalTestMolecularRecord();

        return withMolecularRecord(ImmutableMolecularRecord.builder()
                .from(base)
                .characteristics(ImmutableMolecularCharacteristics.builder()
                        .from(base.characteristics())
                        .tumorMutationalBurden(tumorMutationalBurden)
                        .build())
                .build());
    }

    @NotNull
    public static PatientRecord withTumorMutationalLoad(@Nullable Integer tumorMutationalLoad) {
        MolecularRecord base = TestMolecularDataFactory.createMinimalTestMolecularRecord();

        return withMolecularRecord(ImmutableMolecularRecord.builder()
                .from(base)
                .characteristics(ImmutableMolecularCharacteristics.builder()
                        .from(base.characteristics())
                        .tumorMutationalLoad(tumorMutationalLoad)
                        .build())
                .build());
    }

    @NotNull
    private static PatientRecord withMolecularRecord(@NotNull MolecularRecord molecular) {
        return ImmutablePatientRecord.builder().from(TestDataFactory.createMinimalTestPatientRecord()).molecular(molecular).build();
    }
}
