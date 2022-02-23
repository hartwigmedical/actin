package com.hartwig.actin.algo.evaluation.treatment;

import static org.junit.Assert.assertEquals;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.doid.DoidModel;
import com.hartwig.actin.algo.doid.TestDoidModelFactory;
import com.hartwig.actin.clinical.datamodel.ImmutablePriorSecondPrimary;
import com.hartwig.actin.clinical.datamodel.PriorSecondPrimary;

import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class HasHistoryOfSecondMalignancyTest {

    @Test
    public void canEvaluate() {
        DoidModel doidModel = TestDoidModelFactory.createWithOneParentChild("100", "200");

        HasHistoryOfSecondMalignancy anyMalignancy = new HasHistoryOfSecondMalignancy(doidModel, null, false);
        HasHistoryOfSecondMalignancy specificDoid = new HasHistoryOfSecondMalignancy(doidModel, "100", false);
        HasHistoryOfSecondMalignancy specificDoidInactive = new HasHistoryOfSecondMalignancy(doidModel, "100", true);

        List<PriorSecondPrimary> priorSecondPrimaries = Lists.newArrayList();
        PatientRecord noMalignancyRecord = TreatmentTestFactory.withPriorSecondPrimaries(priorSecondPrimaries);

        assertEquals(EvaluationResult.FAIL, anyMalignancy.evaluate(noMalignancyRecord).result());
        assertEquals(EvaluationResult.FAIL, specificDoid.evaluate(noMalignancyRecord).result());
        assertEquals(EvaluationResult.FAIL, specificDoidInactive.evaluate(noMalignancyRecord).result());

        priorSecondPrimaries.add(withDoidAndActive("300", true));
        PatientRecord oneMalignancyRecord = TreatmentTestFactory.withPriorSecondPrimaries(priorSecondPrimaries);
        assertEquals(EvaluationResult.PASS, anyMalignancy.evaluate(oneMalignancyRecord).result());
        assertEquals(EvaluationResult.FAIL, specificDoid.evaluate(oneMalignancyRecord).result());
        assertEquals(EvaluationResult.FAIL, specificDoidInactive.evaluate(oneMalignancyRecord).result());

        priorSecondPrimaries.add(withDoidAndActive("200", true));
        PatientRecord twoMalignancyRecords = TreatmentTestFactory.withPriorSecondPrimaries(priorSecondPrimaries);
        assertEquals(EvaluationResult.PASS, anyMalignancy.evaluate(twoMalignancyRecords).result());
        assertEquals(EvaluationResult.PASS, specificDoid.evaluate(twoMalignancyRecords).result());
        assertEquals(EvaluationResult.FAIL, specificDoidInactive.evaluate(twoMalignancyRecords).result());

        priorSecondPrimaries.add(withDoidAndActive("200", false));
        PatientRecord threeMalignancyRecords = TreatmentTestFactory.withPriorSecondPrimaries(priorSecondPrimaries);
        assertEquals(EvaluationResult.PASS, anyMalignancy.evaluate(threeMalignancyRecords).result());
        assertEquals(EvaluationResult.PASS, specificDoid.evaluate(threeMalignancyRecords).result());
        assertEquals(EvaluationResult.PASS, specificDoidInactive.evaluate(threeMalignancyRecords).result());
    }

    @NotNull
    private static PriorSecondPrimary withDoidAndActive(@NotNull String doid, boolean isActive) {
        return ImmutablePriorSecondPrimary.builder()
                .tumorLocation(Strings.EMPTY)
                .tumorSubLocation(Strings.EMPTY)
                .tumorType(Strings.EMPTY)
                .tumorSubType(Strings.EMPTY)
                .treatmentHistory(Strings.EMPTY)
                .addDoids(doid)
                .isActive(isActive)
                .build();
    }
}