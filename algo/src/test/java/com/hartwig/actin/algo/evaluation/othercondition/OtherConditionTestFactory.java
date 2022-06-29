package com.hartwig.actin.algo.evaluation.othercondition;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.ImmutablePatientRecord;
import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.TestDataFactory;
import com.hartwig.actin.clinical.datamodel.Complication;
import com.hartwig.actin.clinical.datamodel.ImmutableClinicalRecord;
import com.hartwig.actin.clinical.datamodel.ImmutablePriorOtherCondition;
import com.hartwig.actin.clinical.datamodel.Intolerance;
import com.hartwig.actin.clinical.datamodel.Medication;
import com.hartwig.actin.clinical.datamodel.PriorOtherCondition;
import com.hartwig.actin.clinical.datamodel.TestClinicalFactory;
import com.hartwig.actin.clinical.datamodel.Toxicity;

import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;

final class OtherConditionTestFactory {

    private OtherConditionTestFactory() {
    }

    @NotNull
    public static PatientRecord withPriorOtherCondition(@NotNull PriorOtherCondition condition) {
        return withPriorOtherConditions(Lists.newArrayList(condition));
    }

    @NotNull
    public static PatientRecord withPriorOtherConditions(@NotNull List<PriorOtherCondition> conditions) {
        return ImmutablePatientRecord.builder()
                .from(TestDataFactory.createMinimalTestPatientRecord())
                .clinical(ImmutableClinicalRecord.builder()
                        .from(TestClinicalFactory.createMinimalTestClinicalRecord())
                        .priorOtherConditions(conditions)
                        .build())
                .build();
    }

    @NotNull
    public static ImmutablePriorOtherCondition.Builder builder() {
        return ImmutablePriorOtherCondition.builder().name(Strings.EMPTY).category(Strings.EMPTY).isContraindicationForTherapy(true);
    }

    @NotNull
    public static PatientRecord withComplications(@NotNull List<Complication> complications) {
        return ImmutablePatientRecord.builder()
                .from(TestDataFactory.createMinimalTestPatientRecord())
                .clinical(ImmutableClinicalRecord.builder()
                        .from(TestClinicalFactory.createMinimalTestClinicalRecord())
                        .complications(complications)
                        .build())
                .build();
    }

    @NotNull
    public static PatientRecord withToxicities(@NotNull List<Toxicity> toxicities) {
        return ImmutablePatientRecord.builder()
                .from(TestDataFactory.createMinimalTestPatientRecord())
                .clinical(ImmutableClinicalRecord.builder()
                        .from(TestClinicalFactory.createMinimalTestClinicalRecord())
                        .toxicities(toxicities)
                        .build())
                .build();
    }

    @NotNull
    public static PatientRecord withIntolerances(@NotNull List<Intolerance> intolerances) {
        return ImmutablePatientRecord.builder()
                .from(TestDataFactory.createMinimalTestPatientRecord())
                .clinical(ImmutableClinicalRecord.builder()
                        .from(TestClinicalFactory.createMinimalTestClinicalRecord())
                        .intolerances(intolerances)
                        .build())
                .build();
    }

    @NotNull
    public static PatientRecord withMedications(@NotNull List<Medication> medications) {
        return ImmutablePatientRecord.builder()
                .from(TestDataFactory.createMinimalTestPatientRecord())
                .clinical(ImmutableClinicalRecord.builder()
                        .from(TestClinicalFactory.createMinimalTestClinicalRecord())
                        .medications(medications)
                        .build())
                .build();
    }
}
