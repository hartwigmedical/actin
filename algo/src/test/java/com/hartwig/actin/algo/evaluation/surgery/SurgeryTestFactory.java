package com.hartwig.actin.algo.evaluation.surgery;

import java.util.List;

import com.hartwig.actin.ImmutablePatientRecord;
import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.TestDataFactory;
import com.hartwig.actin.clinical.datamodel.ImmutableClinicalRecord;
import com.hartwig.actin.clinical.datamodel.Surgery;
import com.hartwig.actin.clinical.datamodel.TestClinicalDataFactory;

import org.jetbrains.annotations.NotNull;

final class SurgeryTestFactory {

    private SurgeryTestFactory() {
    }

    @NotNull
    public static PatientRecord withSurgeries(@NotNull List<Surgery> surgeries) {
        return ImmutablePatientRecord.builder()
                .from(TestDataFactory.createMinimalTestPatientRecord())
                .clinical(ImmutableClinicalRecord.builder()
                        .from(TestClinicalDataFactory.createMinimalTestClinicalRecord())
                        .surgeries(surgeries)
                        .build())
                .build();
    }
}
