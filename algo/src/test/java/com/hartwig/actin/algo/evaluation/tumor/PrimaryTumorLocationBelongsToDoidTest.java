package com.hartwig.actin.algo.evaluation.tumor;

import static org.junit.Assert.assertEquals;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.doid.DoidModel;
import com.hartwig.actin.algo.doid.TestDoidModelFactory;
import com.hartwig.actin.clinical.datamodel.ImmutableTumorDetails;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.Test;

public class PrimaryTumorLocationBelongsToDoidTest {

    @Test
    public void canMatchWithPrimaryTumorDoid() {
        DoidModel doidModel = TestDoidModelFactory.createWithOneParentChild("100", "200");

        PrimaryTumorLocationBelongsToDoid function100 = new PrimaryTumorLocationBelongsToDoid(doidModel, "100");

        assertEquals(EvaluationResult.PASS, function100.evaluate(patientWithDoids("100")).result());
        assertEquals(EvaluationResult.PASS, function100.evaluate(patientWithDoids("200")).result());
        assertEquals(EvaluationResult.PASS, function100.evaluate(patientWithDoids("10", "100")).result());
        assertEquals(EvaluationResult.FAIL, function100.evaluate(patientWithDoids("50", "250")).result());
        assertEquals(EvaluationResult.UNDETERMINED, function100.evaluate(patientWithDoids((List<String>) null)).result());

        PrimaryTumorLocationBelongsToDoid function200 = new PrimaryTumorLocationBelongsToDoid(doidModel, "200");

        assertEquals(EvaluationResult.FAIL, function200.evaluate(patientWithDoids("100")).result());
        assertEquals(EvaluationResult.PASS, function200.evaluate(patientWithDoids("200")).result());
        assertEquals(EvaluationResult.FAIL, function200.evaluate(patientWithDoids("10", "100")).result());
        assertEquals(EvaluationResult.FAIL, function200.evaluate(patientWithDoids("50", "250")).result());
        assertEquals(EvaluationResult.UNDETERMINED, function200.evaluate(patientWithDoids(Lists.newArrayList())).result());
    }

    @NotNull
    private static PatientRecord patientWithDoids(@NotNull String... doids) {
        return patientWithDoids(Lists.newArrayList(doids));
    }

    @NotNull
    private static PatientRecord patientWithDoids(@Nullable List<String> doids) {
        return TumorEvaluationTestUtil.withTumorDetails(ImmutableTumorDetails.builder().doids(doids).build());
    }
}