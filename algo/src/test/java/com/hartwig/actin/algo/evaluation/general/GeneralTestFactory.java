package com.hartwig.actin.algo.evaluation.general;

import com.hartwig.actin.ImmutablePatientRecord;
import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.TestDataFactory;
import com.hartwig.actin.clinical.datamodel.BodyWeight;
import com.hartwig.actin.clinical.datamodel.ClinicalRecord;
import com.hartwig.actin.clinical.datamodel.ClinicalStatus;
import com.hartwig.actin.clinical.datamodel.Gender;
import com.hartwig.actin.clinical.datamodel.ImmutableClinicalRecord;
import com.hartwig.actin.clinical.datamodel.ImmutableClinicalStatus;
import com.hartwig.actin.clinical.datamodel.ImmutablePatientDetails;
import com.hartwig.actin.clinical.datamodel.PatientDetails;
import com.hartwig.actin.clinical.datamodel.TestClinicalFactory;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

final class GeneralTestFactory {

    private GeneralTestFactory() {
    }

    @NotNull
    public static PatientRecord withBirthYear(int birthYear) {
        PatientDetails patientDetails = ImmutablePatientDetails.builder()
                .from(TestClinicalFactory.createMinimalTestClinicalRecord().patient())
                .birthYear(birthYear)
                .build();

        return withPatientDetails(patientDetails);
    }

    @NotNull
    public static PatientRecord withGender(@NotNull Gender gender) {
        PatientDetails patientDetails = ImmutablePatientDetails.builder()
                .from(TestClinicalFactory.createMinimalTestClinicalRecord().patient())
                .gender(gender)
                .build();

        return withPatientDetails(patientDetails);
    }

    @NotNull
    public static PatientRecord withWHO(@Nullable Integer who) {
        ClinicalStatus clinicalStatus = ImmutableClinicalStatus.builder()
                .from(TestClinicalFactory.createMinimalTestClinicalRecord().clinicalStatus())
                .who(who)
                .build();

        return withClinicalStatus(clinicalStatus);
    }

    @NotNull
    public static PatientRecord withBodyWeights(@NotNull Iterable<BodyWeight> bodyWeights) {
        ClinicalRecord clinical = ImmutableClinicalRecord.builder()
                .from(TestClinicalFactory.createMinimalTestClinicalRecord())
                .bodyWeights(bodyWeights)
                .build();

        return withClinicalRecord(clinical);
    }

    @NotNull
    private static PatientRecord withPatientDetails(@NotNull PatientDetails patientDetails) {
        ClinicalRecord clinical = ImmutableClinicalRecord.builder()
                .from(TestClinicalFactory.createMinimalTestClinicalRecord())
                .patient(patientDetails)
                .build();

        return withClinicalRecord(clinical);
    }

    @NotNull
    private static PatientRecord withClinicalStatus(@NotNull ClinicalStatus clinicalStatus) {
        ClinicalRecord clinical = ImmutableClinicalRecord.builder()
                .from(TestClinicalFactory.createMinimalTestClinicalRecord())
                .clinicalStatus(clinicalStatus)
                .build();

        return withClinicalRecord(clinical);
    }

    @NotNull
    private static PatientRecord withClinicalRecord(@NotNull ClinicalRecord clinical) {
        return ImmutablePatientRecord.builder().from(TestDataFactory.createMinimalTestPatientRecord()).clinical(clinical).build();
    }
}
