package com.hartwig.actin.algo.evaluation.reproduction;

import com.hartwig.actin.ImmutablePatientRecord;
import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.TestDataFactory;
import com.hartwig.actin.clinical.datamodel.ClinicalRecord;
import com.hartwig.actin.clinical.datamodel.Gender;
import com.hartwig.actin.clinical.datamodel.ImmutableClinicalRecord;
import com.hartwig.actin.clinical.datamodel.ImmutablePatientDetails;

import org.jetbrains.annotations.NotNull;

final class ReproductionTestFactory {

    private ReproductionTestFactory() {
    }

    @NotNull
    public static PatientRecord withGender(@NotNull Gender gender) {
        PatientRecord base = TestDataFactory.createMinimalTestPatientRecord();

        ClinicalRecord clinical = ImmutableClinicalRecord.builder()
                .from(base.clinical())
                .patient(ImmutablePatientDetails.builder().from(base.clinical().patient()).gender(gender).build())
                .build();

        return ImmutablePatientRecord.builder().from(base).clinical(clinical).build();
    }
}
