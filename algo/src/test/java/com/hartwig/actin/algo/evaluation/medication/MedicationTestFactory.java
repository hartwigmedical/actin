package com.hartwig.actin.algo.evaluation.medication;

import java.util.List;

import com.hartwig.actin.ImmutablePatientRecord;
import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.TestDataFactory;
import com.hartwig.actin.algo.medication.MedicationStatusInterpretation;
import com.hartwig.actin.clinical.datamodel.ImmutableClinicalRecord;
import com.hartwig.actin.clinical.datamodel.ImmutableMedication;
import com.hartwig.actin.clinical.datamodel.Medication;
import com.hartwig.actin.clinical.datamodel.TestClinicalFactory;

import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;

final class MedicationTestFactory {

    private MedicationTestFactory() {
    }

    @NotNull
    public static MedicationSelector alwaysActive() {
        return new MedicationSelector(medication -> MedicationStatusInterpretation.ACTIVE);
    }
    @NotNull
    public static MedicationSelector alwaysStopped() {
        return new MedicationSelector(medication -> MedicationStatusInterpretation.STOPPED);
    }

    @NotNull
    public static ImmutableMedication.Builder builder() {
        return ImmutableMedication.builder().name(Strings.EMPTY);
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
