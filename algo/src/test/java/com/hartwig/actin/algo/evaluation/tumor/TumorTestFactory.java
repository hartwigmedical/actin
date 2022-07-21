package com.hartwig.actin.algo.evaluation.tumor;

import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
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
    public static PatientRecord withTumorTypeAndDoids(@Nullable String primaryTumorType, @Nullable String primaryTumorSubType,
            @NotNull String... doids) {
        return withTumorTypeAndDoids(primaryTumorType, primaryTumorSubType, Lists.newArrayList(doids));
    }

    @NotNull
    public static PatientRecord withTumorTypeAndDoids(@Nullable String primaryTumorType, @Nullable String primaryTumorSubType,
            @Nullable List<String> doids) {
        return withTumorDetails(builder().primaryTumorType(primaryTumorType).primaryTumorSubType(primaryTumorSubType).doids(doids).build());
    }

    @NotNull
    public static PatientRecord withDoids(@NotNull String... doids) {
        return withDoids(Sets.newHashSet(doids));
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
    public static PatientRecord withActiveBrainAndCnsLesions(@Nullable Boolean hasActiveBrainLesions,
            @Nullable Boolean hasActiveCnsLesions) {
        return withTumorDetails(builder().hasActiveBrainLesions(hasActiveBrainLesions).hasActiveCnsLesions(hasActiveCnsLesions).build());
    }

    @NotNull
    public static PatientRecord withBrainLesions(@Nullable Boolean hasBrainLesions) {
        return withTumorDetails(builder().hasBrainLesions(hasBrainLesions).build());
    }

    @NotNull
    public static PatientRecord withActiveBrainLesions(@Nullable Boolean hasActiveBrainLesions) {
        return withTumorDetails(builder().hasActiveBrainLesions(hasActiveBrainLesions).build());
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
