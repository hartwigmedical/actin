package com.hartwig.actin.algo.evaluation.molecular;

import java.util.List;

import com.hartwig.actin.ImmutablePatientRecord;
import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.TestDataFactory;
import com.hartwig.actin.clinical.datamodel.ImmutableClinicalRecord;
import com.hartwig.actin.clinical.datamodel.PriorMolecularTest;
import com.hartwig.actin.molecular.datamodel.ImmutableMolecularRecord;
import com.hartwig.actin.molecular.datamodel.MolecularRecord;
import com.hartwig.actin.molecular.datamodel.TestMolecularDataFactory;
import com.hartwig.actin.molecular.datamodel.characteristics.ImmutableMolecularCharacteristics;
import com.hartwig.actin.molecular.datamodel.evidence.ActinTrialEvidence;
import com.hartwig.actin.molecular.datamodel.evidence.EvidenceEntry;
import com.hartwig.actin.molecular.datamodel.evidence.ImmutableMolecularEvidence;
import com.hartwig.actin.molecular.datamodel.evidence.MolecularEvidence;
import com.hartwig.actin.treatment.datamodel.EligibilityRule;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

final class MolecularTestFactory {

    private static final String EVIDENCE_EVENT_SEPARATOR = ": ";

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
                .evidence(withActinEvidence(createGeneMutationEntry(gene, mutation)))
                .build());
    }

    @NotNull
    private static EvidenceEntry createGeneMutationEntry(@NotNull String gene, @NotNull String mutation) {
        return withSourceEvent(EligibilityRule.MUTATION_IN_GENE_X_OF_TYPE_Y + EVIDENCE_EVENT_SEPARATOR + gene + " " + mutation);
    }

    @NotNull
    public static PatientRecord withActivatedGene(@NotNull String gene) {
        return withMolecularRecord(ImmutableMolecularRecord.builder()
                .from(TestMolecularDataFactory.createMinimalTestMolecularRecord())
                .evidence(withActinEvidence(createActivatedGeneEntry(gene)))
                .build());
    }

    @NotNull
    private static EvidenceEntry createActivatedGeneEntry(@NotNull String gene) {
        return withSourceEvent(EligibilityRule.ACTIVATING_MUTATION_IN_GENE_X + EVIDENCE_EVENT_SEPARATOR + gene);
    }

    @NotNull
    public static PatientRecord withInactivatedGene(@NotNull String gene) {
        return withMolecularRecord(ImmutableMolecularRecord.builder()
                .from(TestMolecularDataFactory.createMinimalTestMolecularRecord())
                .evidence(withActinEvidence(createInactivatedGeneEntry(gene)))
                .build());
    }

    @NotNull
    private static EvidenceEntry createInactivatedGeneEntry(@NotNull String gene) {
        return withSourceEvent(EligibilityRule.INACTIVATION_OF_GENE_X + EVIDENCE_EVENT_SEPARATOR + gene);
    }

    @NotNull
    public static PatientRecord withAmplifiedGene(@NotNull String gene) {
        return withMolecularRecord(ImmutableMolecularRecord.builder()
                .from(TestMolecularDataFactory.createMinimalTestMolecularRecord())
                .evidence(withActinEvidence(createAmplifiedGeneEntry(gene)))
                .build());
    }

    @NotNull
    private static EvidenceEntry createAmplifiedGeneEntry(@NotNull String gene) {
        return withSourceEvent(EligibilityRule.AMPLIFICATION_OF_GENE_X + EVIDENCE_EVENT_SEPARATOR + gene);
    }

    @NotNull
    public static PatientRecord withWildtypeGene(@NotNull String gene) {
        return withMolecularRecord(ImmutableMolecularRecord.builder()
                .from(TestMolecularDataFactory.createMinimalTestMolecularRecord())
                .evidence(withActinEvidence(createWildtypeGeneEntry(gene)))
                .build());
    }

    @NotNull
    private static ActinTrialEvidence createWildtypeGeneEntry(@NotNull String gene) {
        return createActinTrial(EligibilityRule.WILDTYPE_OF_GENE_X + EVIDENCE_EVENT_SEPARATOR + gene);
    }

    @NotNull
    public static PatientRecord withFusedGene(@NotNull String gene) {
        return withMolecularRecord(ImmutableMolecularRecord.builder()
                .from(TestMolecularDataFactory.createMinimalTestMolecularRecord())
                .evidence(withActinEvidence(createFusedGeneEntry(gene)))
                .build());
    }

    @NotNull
    private static ActinTrialEvidence createFusedGeneEntry(@NotNull String gene) {
        return createActinTrial(EligibilityRule.FUSION_IN_GENE_X + EVIDENCE_EVENT_SEPARATOR + gene);
    }

    @NotNull
    private static ActinTrialEvidence createActinTrial(@NotNull String s) {
        return null;
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

    @NotNull
    private static MolecularEvidence withActinEvidence(@NotNull ActinTrialEvidence actinTrialEvidence) {
        return ImmutableMolecularEvidence.builder()
                .from(TestMolecularDataFactory.createMinimalTestMolecularRecord().evidence())
                .addActinTrials(actinTrialEvidence)
                .build();
    }
}
