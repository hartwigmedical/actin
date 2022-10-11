package com.hartwig.actin.algo.evaluation.washout;

import java.time.LocalDate;
import java.util.List;

import com.hartwig.actin.ImmutablePatientRecord;
import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.TestDataFactory;
import com.hartwig.actin.algo.medication.MedicationStatusInterpretation;
import com.hartwig.actin.algo.medication.MedicationStatusInterpreter;
import com.hartwig.actin.clinical.datamodel.ImmutableClinicalRecord;
import com.hartwig.actin.clinical.datamodel.ImmutableMedication;
import com.hartwig.actin.clinical.datamodel.Medication;
import com.hartwig.actin.clinical.datamodel.MedicationStatus;
import com.hartwig.actin.clinical.datamodel.TestClinicalFactory;

import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;

final class WashoutTestFactory {

    private WashoutTestFactory() {
    }

    @NotNull
    public static MedicationStatusInterpreter activeFromDate(@NotNull LocalDate referenceDate) {
        return medication -> {
            LocalDate stopDate = medication.stopDate();
            if (stopDate == null || !referenceDate.isAfter(stopDate)) {
                return MedicationStatusInterpretation.ACTIVE;
            } else {
                return MedicationStatusInterpretation.UNKNOWN;
            }
        };
    }

    @NotNull
    public static ImmutableMedication.Builder builder() {
        return ImmutableMedication.builder().name(Strings.EMPTY).codeATC(Strings.EMPTY).status(MedicationStatus.ACTIVE);
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
