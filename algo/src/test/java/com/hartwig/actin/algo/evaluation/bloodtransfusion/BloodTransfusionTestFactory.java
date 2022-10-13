package com.hartwig.actin.algo.evaluation.bloodtransfusion;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.ImmutablePatientRecord;
import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.TestDataFactory;
import com.hartwig.actin.clinical.datamodel.BloodTransfusion;
import com.hartwig.actin.clinical.datamodel.ClinicalRecord;
import com.hartwig.actin.clinical.datamodel.ImmutableClinicalRecord;
import com.hartwig.actin.clinical.datamodel.ImmutableMedication;
import com.hartwig.actin.clinical.datamodel.Medication;
import com.hartwig.actin.clinical.datamodel.TestClinicalFactory;

import org.apache.logging.log4j.util.Strings;
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
        return withClinicalRecord(ImmutableClinicalRecord.builder()
                .from(TestClinicalFactory.createMinimalTestClinicalRecord())
                .bloodTransfusions(transfusions)
                .build());
    }

    @NotNull
    public static ImmutableMedication.Builder medicationBuilder() {
        return ImmutableMedication.builder().name(Strings.EMPTY).codeATC(Strings.EMPTY);
    }

    @NotNull
    public static PatientRecord withMedication(@NotNull Medication medication) {
        return withMedications(Lists.newArrayList(medication));
    }

    @NotNull
    public static PatientRecord withMedications(@NotNull List<Medication> medications) {
        return withClinicalRecord(ImmutableClinicalRecord.builder()
                .from(TestClinicalFactory.createMinimalTestClinicalRecord())
                .medications(medications)
                .build());
    }

    @NotNull
    private static PatientRecord withClinicalRecord(@NotNull ClinicalRecord clinical) {
        return ImmutablePatientRecord.builder().from(TestDataFactory.createMinimalTestPatientRecord()).clinical(clinical).build();
    }
}
