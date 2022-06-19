package com.hartwig.actin.algo.evaluation.molecular;

import java.util.List;

import com.hartwig.actin.ImmutablePatientRecord;
import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.TestDataFactory;
import com.hartwig.actin.clinical.datamodel.ImmutableClinicalRecord;
import com.hartwig.actin.clinical.datamodel.PriorMolecularTest;
import com.hartwig.actin.molecular.datamodel.ImmutableMolecularRecord;
import com.hartwig.actin.molecular.datamodel.MolecularRecord;
import com.hartwig.actin.molecular.datamodel.TestMolecularFactory;
import com.hartwig.actin.molecular.datamodel.characteristics.ImmutableMolecularCharacteristics;
import com.hartwig.actin.molecular.datamodel.evidence.ActinTrialEvidence;
import com.hartwig.actin.molecular.datamodel.evidence.ActinTrialEvidenceTestFactory;
import com.hartwig.actin.molecular.datamodel.evidence.ImmutableMolecularEvidence;
import com.hartwig.actin.molecular.datamodel.evidence.MolecularEventType;
import com.hartwig.actin.molecular.datamodel.evidence.MolecularEvidence;

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
                .from(TestMolecularFactory.createMinimalTestMolecularRecord())
                .evidence(withActinEvidence(createActinEvidence(MolecularEventType.MUTATED_GENE, gene, mutation)))
                .build());
    }

    @NotNull
    public static PatientRecord withActivatedGene(@NotNull String gene) {
        return withMolecularRecord(ImmutableMolecularRecord.builder()
                .from(TestMolecularFactory.createMinimalTestMolecularRecord())
                .evidence(withActinEvidence(createActinEvidence(MolecularEventType.ACTIVATED_GENE, gene, null)))
                .build());
    }

    @NotNull
    public static PatientRecord withInactivatedGene(@NotNull String gene) {
        return withMolecularRecord(ImmutableMolecularRecord.builder()
                .from(TestMolecularFactory.createMinimalTestMolecularRecord())
                .evidence(withActinEvidence(createActinEvidence(MolecularEventType.INACTIVATED_GENE, gene, null)))
                .build());
    }

    @NotNull
    public static PatientRecord withAmplifiedGene(@NotNull String gene) {
        return withMolecularRecord(ImmutableMolecularRecord.builder()
                .from(TestMolecularFactory.createMinimalTestMolecularRecord())
                .evidence(withActinEvidence(createActinEvidence(MolecularEventType.AMPLIFIED_GENE, gene, null)))
                .build());
    }

    @NotNull
    public static PatientRecord withWildTypeGene(@NotNull String gene) {
        return withMolecularRecord(ImmutableMolecularRecord.builder()
                .from(TestMolecularFactory.createMinimalTestMolecularRecord())
                .evidence(withActinEvidence(createActinEvidence(MolecularEventType.WILD_TYPE_GENE, gene, null)))
                .build());
    }

    @NotNull
    public static PatientRecord withFusedGene(@NotNull String gene) {
        return withMolecularRecord(ImmutableMolecularRecord.builder()
                .from(TestMolecularFactory.createMinimalTestMolecularRecord())
                .evidence(withActinEvidence(createActinEvidence(MolecularEventType.FUSED_GENE, gene, null)))
                .build());
    }

    @NotNull
    private static ActinTrialEvidence createActinEvidence(@NotNull MolecularEventType type, @Nullable String gene,
            @Nullable String mutation) {
        return ActinTrialEvidenceTestFactory.builder().type(type).gene(gene).mutation(mutation).build();
    }

    @NotNull
    private static MolecularEvidence withActinEvidence(@NotNull ActinTrialEvidence actinTrialEvidence) {
        return ImmutableMolecularEvidence.builder()
                .from(TestMolecularFactory.createMinimalTestMolecularRecord().evidence())
                .addActinTrials(actinTrialEvidence)
                .build();
    }

    @NotNull
    public static PatientRecord withMicrosatelliteInstability(@Nullable Boolean isMicrosatelliteUnstable) {
        MolecularRecord base = TestMolecularFactory.createMinimalTestMolecularRecord();

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
        MolecularRecord base = TestMolecularFactory.createMinimalTestMolecularRecord();

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
        MolecularRecord base = TestMolecularFactory.createMinimalTestMolecularRecord();

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
        MolecularRecord base = TestMolecularFactory.createMinimalTestMolecularRecord();

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
