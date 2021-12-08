package com.hartwig.actin.algo.evaluation.treatment;

import static org.junit.Assert.assertEquals;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
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
        PatientRecord noMalignancyRecord = TreatmentEvaluationTestUtil.withPriorSecondPrimaries(priorSecondPrimaries);

        assertEquals(Evaluation.FAIL, anyMalignancy.evaluate(noMalignancyRecord));
        assertEquals(Evaluation.FAIL, specificDoid.evaluate(noMalignancyRecord));
        assertEquals(Evaluation.FAIL, specificDoidInactive.evaluate(noMalignancyRecord));

        priorSecondPrimaries.add(withDoidAndActive("300", true));
        PatientRecord oneMalignancyRecord = TreatmentEvaluationTestUtil.withPriorSecondPrimaries(priorSecondPrimaries);
        assertEquals(Evaluation.PASS, anyMalignancy.evaluate(oneMalignancyRecord));
        assertEquals(Evaluation.FAIL, specificDoid.evaluate(oneMalignancyRecord));
        assertEquals(Evaluation.FAIL, specificDoidInactive.evaluate(oneMalignancyRecord));

        priorSecondPrimaries.add(withDoidAndActive("200", true));
        PatientRecord twoMalignancyRecords = TreatmentEvaluationTestUtil.withPriorSecondPrimaries(priorSecondPrimaries);
        assertEquals(Evaluation.PASS, anyMalignancy.evaluate(twoMalignancyRecords));
        assertEquals(Evaluation.PASS, specificDoid.evaluate(twoMalignancyRecords));
        assertEquals(Evaluation.FAIL, specificDoidInactive.evaluate(twoMalignancyRecords));

        priorSecondPrimaries.add(withDoidAndActive("200", false));
        PatientRecord threeMalignancyRecords = TreatmentEvaluationTestUtil.withPriorSecondPrimaries(priorSecondPrimaries);
        assertEquals(Evaluation.PASS, anyMalignancy.evaluate(threeMalignancyRecords));
        assertEquals(Evaluation.PASS, specificDoid.evaluate(threeMalignancyRecords));
        assertEquals(Evaluation.PASS, specificDoidInactive.evaluate(threeMalignancyRecords));
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