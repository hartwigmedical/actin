package com.hartwig.actin.algo.evaluation.bloodtransfusion;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.ImmutablePatientRecord;
import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.TestDataFactory;
import com.hartwig.actin.clinical.datamodel.BloodTransfusion;
import com.hartwig.actin.clinical.datamodel.ImmutableClinicalRecord;
import com.hartwig.actin.clinical.datamodel.TestClinicalFactory;

import org.jetbrains.annotations.NotNull;

final class BloodTransfusionTestFactory {

    private BloodTransfusionTestFactory() {
    }

    @NotNull
    public static PatientRecord withBloodTransfusion(@NotNull BloodTransfusion transfusion) {
        return withBloodTransfusions(Lists.newArrayList(transfusion));
    }

    @NotNull
    public static PatientRecord withBloodTransfusions(@NotNull List<BloodTransfusion> transfusions) {
        return ImmutablePatientRecord.builder()
                .from(TestDataFactory.createMinimalTestPatientRecord())
                .clinical(ImmutableClinicalRecord.builder()
                        .from(TestClinicalFactory.createMinimalTestClinicalRecord())
                        .bloodTransfusions(transfusions)
                        .build())
                .build();
    }
}
