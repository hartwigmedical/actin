package com.hartwig.actin.algo.evaluation.cardiacfunction;

import com.google.common.collect.Lists;
import com.hartwig.actin.ImmutablePatientRecord;
import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.TestDataFactory;
import com.hartwig.actin.clinical.datamodel.ECG;
import com.hartwig.actin.clinical.datamodel.ImmutableClinicalRecord;
import com.hartwig.actin.clinical.datamodel.ImmutableClinicalStatus;
import com.hartwig.actin.clinical.datamodel.ImmutableECG;
import com.hartwig.actin.clinical.datamodel.PriorOtherCondition;
import com.hartwig.actin.clinical.datamodel.TestClinicalFactory;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

final class CardiacFunctionTestFactory {

    private CardiacFunctionTestFactory() {
    }

    @NotNull
    public static ImmutableECG.Builder builder() {
        return ImmutableECG.builder().hasSigAberrationLatestECG(false);
    }

    @NotNull
    public static PatientRecord withHasSignificantECGAberration(boolean hasSignificantECGAberration) {
        return withECG(builder().hasSigAberrationLatestECG(hasSignificantECGAberration).build());
    }

    @NotNull
    public static PatientRecord withHasSignificantECGAberration(boolean hasSignificantECGAberration, @Nullable String description) {
        return withECG(builder().hasSigAberrationLatestECG(hasSignificantECGAberration).aberrationDescription(description).build());
    }

    @NotNull
    public static PatientRecord withLVEF(@Nullable Double lvef) {
        PatientRecord base = TestDataFactory.createMinimalTestPatientRecord();
        return ImmutablePatientRecord.builder()
                .from(base)
                .clinical(ImmutableClinicalRecord.builder()
                        .from(base.clinical())
                        .clinicalStatus(ImmutableClinicalStatus.builder().from(base.clinical().clinicalStatus()).lvef(lvef).build())
                        .build())
                .build();
    }

    @NotNull
    public static PatientRecord withECG(@Nullable ECG ecg) {
        return ImmutablePatientRecord.builder()
                .from(TestDataFactory.createMinimalTestPatientRecord())
                .clinical(ImmutableClinicalRecord.builder()
                        .from(TestClinicalFactory.createMinimalTestClinicalRecord())
                        .clinicalStatus(ImmutableClinicalStatus.builder().ecg(ecg).build())
                        .build())
                .build();
    }

    @NotNull
    public static PatientRecord withPriorOtherCondition(@NotNull PriorOtherCondition priorOtherCondition) {
        return ImmutablePatientRecord.builder()
                .from(TestDataFactory.createMinimalTestPatientRecord())
                .clinical(ImmutableClinicalRecord.builder()
                        .from(TestClinicalFactory.createMinimalTestClinicalRecord())
                        .priorOtherConditions(Lists.newArrayList(priorOtherCondition))
                        .build())
                .build();
    }
}
