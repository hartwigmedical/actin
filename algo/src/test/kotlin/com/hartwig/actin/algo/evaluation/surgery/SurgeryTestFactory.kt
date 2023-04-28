package com.hartwig.actin.algo.evaluation.surgery;

import java.time.LocalDate;
import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.ImmutablePatientRecord;
import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.TestDataFactory;
import com.hartwig.actin.clinical.datamodel.ImmutableClinicalRecord;
import com.hartwig.actin.clinical.datamodel.ImmutableSurgery;
import com.hartwig.actin.clinical.datamodel.PriorTumorTreatment;
import com.hartwig.actin.clinical.datamodel.Surgery;
import com.hartwig.actin.clinical.datamodel.SurgeryStatus;
import com.hartwig.actin.clinical.datamodel.TestClinicalFactory;

import org.jetbrains.annotations.NotNull;

final class SurgeryTestFactory {

    private SurgeryTestFactory() {
    }

    @NotNull
    public static ImmutableSurgery.Builder builder() {
        return ImmutableSurgery.builder().endDate(LocalDate.of(2020, 4, 5)).status(SurgeryStatus.UNKNOWN);
    }

    @NotNull
    public static PatientRecord withSurgery(@NotNull Surgery surgery) {
        return withSurgeries(Lists.newArrayList(surgery));
    }

    @NotNull
    public static PatientRecord withSurgeries(@NotNull List<Surgery> surgeries) {
        return ImmutablePatientRecord.builder()
                .from(TestDataFactory.createMinimalTestPatientRecord())
                .clinical(ImmutableClinicalRecord.builder()
                        .from(TestClinicalFactory.createMinimalTestClinicalRecord())
                        .surgeries(surgeries)
                        .build())
                .build();
    }

    @NotNull
    public static PatientRecord withPriorTumorTreatments(@NotNull List<PriorTumorTreatment> treatments) {
        return ImmutablePatientRecord.builder()
                .from(TestDataFactory.createMinimalTestPatientRecord())
                .clinical(ImmutableClinicalRecord.builder()
                        .from(TestClinicalFactory.createMinimalTestClinicalRecord())
                        .priorTumorTreatments(treatments)
                        .build())
                .build();
    }
}
