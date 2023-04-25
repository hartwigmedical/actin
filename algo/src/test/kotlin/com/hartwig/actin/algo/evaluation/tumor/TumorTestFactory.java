package com.hartwig.actin.algo.evaluation.tumor;

import java.util.List;
import java.util.Set;

import com.google.common.collect.Sets;
import com.hartwig.actin.ImmutablePatientRecord;
import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.TestDataFactory;
import com.hartwig.actin.clinical.datamodel.ImmutableClinicalRecord;
import com.hartwig.actin.clinical.datamodel.ImmutableTumorDetails;
import com.hartwig.actin.clinical.datamodel.TestClinicalFactory;
import com.hartwig.actin.clinical.datamodel.TumorDetails;
import com.hartwig.actin.clinical.datamodel.TumorStage;
import com.hartwig.actin.molecular.datamodel.ExperimentType;
import com.hartwig.actin.molecular.datamodel.ImmutableMolecularRecord;
import com.hartwig.actin.molecular.datamodel.characteristics.ImmutableMolecularCharacteristics;
import com.hartwig.actin.molecular.datamodel.driver.CopyNumberType;
import com.hartwig.actin.molecular.datamodel.driver.GeneRole;
import com.hartwig.actin.molecular.datamodel.driver.ImmutableMolecularDrivers;
import com.hartwig.actin.molecular.datamodel.driver.ProteinEffect;
import com.hartwig.actin.molecular.datamodel.driver.TestCopyNumberFactory;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

final class TumorTestFactory {

    private TumorTestFactory() {
    }

    @NotNull
    public static ImmutableTumorDetails.Builder builder() {
        return ImmutableTumorDetails.builder();
    }

    @NotNull
    public static PatientRecord withDoids(@NotNull String... doids) {
        return withDoids(Sets.newHashSet(doids));
    }

    @NotNull
    public static PatientRecord withDoidsAndAmplication(@NotNull Set<String> doids, @NotNull String amplifiedGene) {
        PatientRecord base = TestDataFactory.createMinimalTestPatientRecord();

        return ImmutablePatientRecord.builder()
                .from(base)
                .clinical(ImmutableClinicalRecord.builder().from(base.clinical()).tumor(builder().doids(doids).build()).build())
                .molecular(ImmutableMolecularRecord.builder()
                        .from(base.molecular())
                        .characteristics(ImmutableMolecularCharacteristics.builder()
                                .from(base.molecular().characteristics())
                                .ploidy(2D)
                                .build())
                        .drivers(ImmutableMolecularDrivers.builder()
                                .from(base.molecular().drivers())
                                .addCopyNumbers(TestCopyNumberFactory.builder()
                                        .isReportable(true)
                                        .gene(amplifiedGene)
                                        .geneRole(GeneRole.ONCO)
                                        .proteinEffect(ProteinEffect.GAIN_OF_FUNCTION)
                                        .type(CopyNumberType.FULL_GAIN)
                                        .minCopies(20)
                                        .maxCopies(20)
                                        .build())
                                .build())
                        .build())
                .build();
    }

    @NotNull
    public static PatientRecord withDoidAndSubLocation(@NotNull String doid, @Nullable String primaryTumorSubLocation) {
        return withTumorDetails(builder().addDoids(doid).primaryTumorSubLocation(primaryTumorSubLocation).build());
    }

    @NotNull
    public static PatientRecord withDoidAndDetails(@NotNull String doid, @NotNull String extraDetails) {
        return withTumorDetails(builder().addDoids(doid).primaryTumorExtraDetails(extraDetails).build());
    }

    @NotNull
    public static PatientRecord withDoids(@Nullable Set<String> doids) {
        return withTumorDetails(builder().doids(doids).build());
    }

    @NotNull
    public static PatientRecord withTumorStage(@Nullable TumorStage stage) {
        return withTumorDetails(builder().stage(stage).build());
    }

    @NotNull
    public static PatientRecord withTumorStageAndDoid(@Nullable TumorStage stage, @Nullable String doid) {
        Set<String> doids = null;
        if (doid != null) {
            doids = Sets.newHashSet(doid);
        }
        return withTumorDetails(builder().stage(stage).doids(doids).build());
    }

    @NotNull
    public static PatientRecord withMeasurableDisease(@Nullable Boolean hasMeasurableDisease) {
        return withTumorDetails(builder().hasMeasurableDisease(hasMeasurableDisease).build());
    }

    @NotNull
    public static PatientRecord withMeasurableDiseaseAndDoid(@Nullable Boolean hasMeasurableDisease, @NotNull String doid) {
        return withTumorDetails(builder().hasMeasurableDisease(hasMeasurableDisease).addDoids(doid).build());
    }

    @NotNull
    public static PatientRecord withBrainAndCnsLesions(@Nullable Boolean hasBrainLesions, @Nullable Boolean hasCnsLesions) {
        return withTumorDetails(builder().hasBrainLesions(hasBrainLesions).hasCnsLesions(hasCnsLesions).build());
    }

    @NotNull
    public static PatientRecord withActiveBrainAndCnsLesionStatus(@Nullable Boolean hasBrainLesions,
            @Nullable Boolean hasActiveBrainLesions, @Nullable Boolean hasCnsLesions, @Nullable Boolean hasActiveCnsLesions) {
        return withTumorDetails(builder().hasBrainLesions(hasBrainLesions)
                .hasActiveBrainLesions(hasActiveBrainLesions)
                .hasCnsLesions(hasCnsLesions)
                .hasActiveCnsLesions(hasActiveCnsLesions)
                .build());
    }

    @NotNull
    public static PatientRecord withBrainLesions(@Nullable Boolean hasBrainLesions) {
        return withTumorDetails(builder().hasBrainLesions(hasBrainLesions).build());
    }

    @NotNull
    public static PatientRecord withBrainLesionStatus(@Nullable Boolean hasBrainLesions, @Nullable Boolean hasActiveBrainLesions) {
        return withTumorDetails(builder().hasBrainLesions(hasBrainLesions).hasActiveBrainLesions(hasActiveBrainLesions).build());
    }

    @NotNull
    public static PatientRecord withCnsLesions(@Nullable Boolean hasCnsLesions) {
        return withTumorDetails(builder().hasCnsLesions(hasCnsLesions).build());
    }

    @NotNull
    public static PatientRecord withBoneLesions(@Nullable Boolean hasBoneLesions) {
        return withTumorDetails(builder().hasBoneLesions(hasBoneLesions).build());
    }

    @NotNull
    public static PatientRecord withBoneAndLiverLesions(@Nullable Boolean hasBoneLesions, @Nullable Boolean hasLiverLesions) {
        return withTumorDetails(builder().hasBoneLesions(hasBoneLesions).hasLiverLesions(hasLiverLesions).build());
    }

    @NotNull
    public static PatientRecord withBoneAndOtherLesions(@Nullable Boolean hasBoneLesions, @NotNull List<String> otherLesions) {
        return withTumorDetails(builder().hasBoneLesions(hasBoneLesions).otherLesions(otherLesions).build());
    }

    @NotNull
    public static PatientRecord withLiverLesions(@Nullable Boolean hasLiverLesions) {
        return withTumorDetails(builder().hasLiverLesions(hasLiverLesions).build());
    }

    @NotNull
    public static PatientRecord withLungLesions(@Nullable Boolean hasLungLesions) {
        return withTumorDetails(builder().hasLungLesions(hasLungLesions).build());
    }

    @NotNull
    public static PatientRecord withLymphNodeLesions(@Nullable Boolean hasLymphNodeLesions) {
        return withTumorDetails(builder().hasLymphNodeLesions(hasLymphNodeLesions).build());
    }

    @NotNull
    public static PatientRecord withOtherLesions(@Nullable List<String> otherLesions) {
        return withTumorDetails(builder().otherLesions(otherLesions).build());
    }

    @NotNull
    public static PatientRecord withTumorDetails(@NotNull TumorDetails tumor) {
        return ImmutablePatientRecord.builder()
                .from(TestDataFactory.createMinimalTestPatientRecord())
                .clinical(ImmutableClinicalRecord.builder()
                        .from(TestClinicalFactory.createMinimalTestClinicalRecord())
                        .tumor(tumor)
                        .build())
                .build();
    }

    @NotNull
    public static PatientRecord withMolecularExperimentType(@Nullable ExperimentType type) {
        PatientRecord base = TestDataFactory.createMinimalTestPatientRecord();

        return ImmutablePatientRecord.builder()
                .from(base)
                .molecular(ImmutableMolecularRecord.builder().from(base.molecular()).type(type).build())
                .build();
    }
}
