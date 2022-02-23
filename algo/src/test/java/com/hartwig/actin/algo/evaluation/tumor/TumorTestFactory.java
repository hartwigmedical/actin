package com.hartwig.actin.algo.evaluation.tumor;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.ImmutablePatientRecord;
import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.TestDataFactory;
import com.hartwig.actin.clinical.datamodel.ImmutableClinicalRecord;
import com.hartwig.actin.clinical.datamodel.ImmutableTumorDetails;
import com.hartwig.actin.clinical.datamodel.TestClinicalDataFactory;
import com.hartwig.actin.clinical.datamodel.TumorDetails;
import com.hartwig.actin.clinical.datamodel.TumorStage;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

final class TumorTestFactory {

    private TumorTestFactory() {
    }

    @NotNull
    public static PatientRecord withDoids(@NotNull String... doids) {
        return withDoids(Lists.newArrayList(doids));
    }

    @NotNull
    public static PatientRecord withDoids(@Nullable List<String> doids) {
        return withTumorDetails(ImmutableTumorDetails.builder().doids(doids).build());
    }

    @NotNull
    public static PatientRecord withTumorStage(@Nullable TumorStage stage) {
        return withTumorDetails(ImmutableTumorDetails.builder().stage(stage).build());
    }

    @NotNull
    public static PatientRecord withMeasurableLesionRecist(@Nullable Boolean hasMeasurableLesionRecist) {
        return withTumorDetails(ImmutableTumorDetails.builder().hasMeasurableLesionRecist(hasMeasurableLesionRecist).build());
    }

    @NotNull
    public static PatientRecord withBrainLesions(@Nullable Boolean hasBrainLesions) {
        return withTumorDetails(ImmutableTumorDetails.builder().hasBrainLesions(hasBrainLesions).build());
    }

    @NotNull
    public static PatientRecord withActiveBrainLesions(@Nullable Boolean hasActiveBrainLesions) {
        return withTumorDetails(ImmutableTumorDetails.builder().hasActiveBrainLesions(hasActiveBrainLesions).build());
    }

    @NotNull
    public static PatientRecord withSymptomaticBrainLesions(@Nullable Boolean hasSymptomaticBrainLesions) {
        return withTumorDetails(ImmutableTumorDetails.builder().hasSymptomaticBrainLesions(hasSymptomaticBrainLesions).build());
    }

    @NotNull
    public static PatientRecord withCnsLesions(@Nullable Boolean hasCnsLesions) {
        return withTumorDetails(ImmutableTumorDetails.builder().hasCnsLesions(hasCnsLesions).build());
    }

    @NotNull
    public static PatientRecord withActiveCnsLesions(@Nullable Boolean hasActiveCnsLesions) {
        return withTumorDetails(ImmutableTumorDetails.builder().hasActiveCnsLesions(hasActiveCnsLesions).build());
    }

    @NotNull
    public static PatientRecord withSymptomaticCnsLesions(@Nullable Boolean hasSymptomaticCnsLesions) {
        return withTumorDetails(ImmutableTumorDetails.builder().hasSymptomaticCnsLesions(hasSymptomaticCnsLesions).build());
    }

    @NotNull
    public static PatientRecord withBoneLesions(@Nullable Boolean hasBoneLesions) {
        return withTumorDetails(ImmutableTumorDetails.builder().hasBoneLesions(hasBoneLesions).build());
    }

    @NotNull
    public static PatientRecord withLiverLesions(@Nullable Boolean hasLiverLesions) {
        return withTumorDetails(ImmutableTumorDetails.builder().hasLiverLesions(hasLiverLesions).build());
    }

    @NotNull
    public static PatientRecord withHasOtherLesions(@Nullable Boolean hasOtherLesions) {
        return withTumorDetails(ImmutableTumorDetails.builder().hasOtherLesions(hasOtherLesions).build());
    }

    @NotNull
    public static PatientRecord withOtherLesions(@Nullable List<String> otherLesions) {
        return withTumorDetails(ImmutableTumorDetails.builder().otherLesions(otherLesions).build());
    }

    @NotNull
    public static PatientRecord withTumorDetails(@NotNull TumorDetails tumor) {
        return ImmutablePatientRecord.builder()
                .from(TestDataFactory.createMinimalTestPatientRecord())
                .clinical(ImmutableClinicalRecord.builder()
                        .from(TestClinicalDataFactory.createMinimalTestClinicalRecord())
                        .tumor(tumor)
                        .build())
                .build();
    }
}
