package com.hartwig.actin.algo.evaluation.general;

import static org.junit.Assert.assertEquals;

import com.hartwig.actin.ImmutablePatientRecord;
import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.TestDataFactory;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.clinical.datamodel.ClinicalRecord;
import com.hartwig.actin.clinical.datamodel.ImmutableClinicalRecord;
import com.hartwig.actin.clinical.datamodel.ImmutablePatientDetails;
import com.hartwig.actin.clinical.datamodel.TestClinicalDataFactory;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class HasAtLeastCertainAgeTest {

    @Test
    public void canEvaluate() {
        EvaluationFunction function = new HasAtLeastCertainAge(2020, 18);

        assertEquals(Evaluation.PASS, function.evaluate(patientWithBirthYear(1960)));
        assertEquals(Evaluation.FAIL, function.evaluate(patientWithBirthYear(2014)));
        assertEquals(Evaluation.PASS_BUT_WARN, function.evaluate(patientWithBirthYear(2002)));
    }

    @NotNull
    private static PatientRecord patientWithBirthYear(int birthYear) {
        ClinicalRecord baseRecord = TestClinicalDataFactory.createProperTestClinicalRecord();
        ClinicalRecord recordWithBirthYear = ImmutableClinicalRecord.builder()
                .from(baseRecord)
                .patient(ImmutablePatientDetails.builder().from(baseRecord.patient()).birthYear(birthYear).build())
                .build();

        return ImmutablePatientRecord.builder().from(TestDataFactory.createProperTestPatientRecord()).clinical(recordWithBirthYear).build();
    }
}