package com.hartwig.actin.algo.evaluation.pathology;

import static org.junit.Assert.assertEquals;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.ImmutablePatientRecord;
import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.TestDataFactory;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.doid.DoidModel;
import com.hartwig.actin.algo.doid.TestDoidModelFactory;
import com.hartwig.actin.clinical.datamodel.ImmutableClinicalRecord;
import com.hartwig.actin.clinical.datamodel.ImmutableTumorDetails;
import com.hartwig.actin.clinical.datamodel.TestClinicalDataFactory;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class PrimaryTumorLocationBelongsToDoidTest {

    @Test
    public void canMatchWithPrimaryTumorDoid() {
        DoidModel doidModel = TestDoidModelFactory.createWithOneParentChild("100", "200");

        PrimaryTumorLocationBelongsToDoid function100 = new PrimaryTumorLocationBelongsToDoid(doidModel, "100");

        assertEquals(Evaluation.PASS, function100.evaluate(patientWithDoids("100")));
        assertEquals(Evaluation.PASS, function100.evaluate(patientWithDoids("200")));
        assertEquals(Evaluation.PASS, function100.evaluate(patientWithDoids("10", "100")));
        assertEquals(Evaluation.FAIL, function100.evaluate(patientWithDoids("50", "250")));
        assertEquals(Evaluation.UNDETERMINED, function100.evaluate(patientWithDoids(Lists.newArrayList())));

        PrimaryTumorLocationBelongsToDoid function200 = new PrimaryTumorLocationBelongsToDoid(doidModel, "200");

        assertEquals(Evaluation.FAIL, function200.evaluate(patientWithDoids("100")));
        assertEquals(Evaluation.PASS, function200.evaluate(patientWithDoids("200")));
        assertEquals(Evaluation.FAIL, function200.evaluate(patientWithDoids("10", "100")));
        assertEquals(Evaluation.FAIL, function200.evaluate(patientWithDoids("50", "250")));
        assertEquals(Evaluation.UNDETERMINED, function200.evaluate(patientWithDoids(Lists.newArrayList())));
    }

    @NotNull
    private static PatientRecord patientWithDoids(@NotNull String... doids) {
        return patientWithDoids(Lists.newArrayList(doids));
    }

    @NotNull
    private static PatientRecord patientWithDoids(@NotNull List<String> doids) {
        return ImmutablePatientRecord.builder()
                .from(TestDataFactory.createMinimalTestPatientRecord())
                .clinical(ImmutableClinicalRecord.builder()
                        .from(TestClinicalDataFactory.createMinimalTestClinicalRecord())
                        .tumor(ImmutableTumorDetails.builder().doids(doids).build())
                        .build())
                .build();
    }
}