package com.hartwig.actin.algo.evaluation.laboratory;

import java.time.LocalDate;
import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.ImmutablePatientRecord;
import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.TestDataFactory;
import com.hartwig.actin.clinical.datamodel.ImmutableClinicalRecord;
import com.hartwig.actin.clinical.datamodel.ImmutableLabValue;
import com.hartwig.actin.clinical.datamodel.LabValue;
import com.hartwig.actin.clinical.datamodel.TestClinicalDataFactory;
import com.hartwig.actin.clinical.interpretation.LabMeasurement;

import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;

final class LaboratoryTestUtil {

    private LaboratoryTestUtil() {
    }

    @NotNull
    public static PatientRecord withLabValue(@NotNull LabValue labValue) {
        return create(Lists.newArrayList(labValue));
    }

    @NotNull
    public static PatientRecord withLabValues(@NotNull LabValue... labValues) {
        return ImmutablePatientRecord.builder()
                .from(TestDataFactory.createMinimalTestPatientRecord())
                .clinical(ImmutableClinicalRecord.builder()
                        .from(TestClinicalDataFactory.createMinimalTestClinicalRecord())
                        .labValues(Lists.newArrayList(labValues))
                        .build())
                .build();
    }

    @NotNull
    private static PatientRecord create(@NotNull List<LabValue> labValues) {
        return ImmutablePatientRecord.builder()
                .from(TestDataFactory.createMinimalTestPatientRecord())
                .clinical(ImmutableClinicalRecord.builder()
                        .from(TestClinicalDataFactory.createMinimalTestClinicalRecord())
                        .labValues(labValues)
                        .build())
                .build();
    }

    @NotNull
    public static ImmutableLabValue.Builder forMeasurement(@NotNull LabMeasurement measurement) {
        return builder().code(measurement.code()).unit(measurement.expectedUnit());
    }

    @NotNull
    public static ImmutableLabValue.Builder builder() {
        return ImmutableLabValue.builder()
                .date(LocalDate.of(2020, 1, 1))
                .name(Strings.EMPTY)
                .code(Strings.EMPTY)
                .comparator(Strings.EMPTY)
                .value(0D)
                .unit(Strings.EMPTY);
    }
}
