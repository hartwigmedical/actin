package com.hartwig.actin.algo.evaluation;

import static org.junit.Assert.assertEquals;

import com.hartwig.actin.ImmutablePatientRecord;
import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.TestDataFactory;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.clinical.datamodel.ClinicalRecord;
import com.hartwig.actin.clinical.datamodel.ImmutableClinicalRecord;
import com.hartwig.actin.clinical.datamodel.ImmutablePatientDetails;
import com.hartwig.actin.clinical.datamodel.TestClinicalDataFactory;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class IsAtLeastEighteenYearsOldTest {

    @Test
    public void canDetermineWhetherPatientIs18() {
        EvaluationFunction function = new IsAtLeastEighteenYearsOld(2020);

        assertEquals(Evaluation.PASS, function.evaluate(patientWithBirthYear(1960)));
        assertEquals(Evaluation.FAIL, function.evaluate(patientWithBirthYear(2014)));
        assertEquals(Evaluation.UNDETERMINED, function.evaluate(patientWithBirthYear(2002)));
    }

    @NotNull
    private static PatientRecord patientWithBirthYear(int birthYear) {
        ClinicalRecord baseRecord = TestClinicalDataFactory.createProperTestClinicalRecord();
        ClinicalRecord recordWithBirthYear = ImmutableClinicalRecord.builder()
                .from(baseRecord)
                .patient(ImmutablePatientDetails.builder().from(baseRecord.patient()).birthYear(birthYear).build())
                .build();

        return ImmutablePatientRecord.builder().from(TestDataFactory.createTestPatientRecord()).clinical(recordWithBirthYear).build();
    }

}