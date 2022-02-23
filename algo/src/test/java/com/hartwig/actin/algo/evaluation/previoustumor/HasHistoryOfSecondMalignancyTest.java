package com.hartwig.actin.algo.evaluation.previoustumor;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

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
        PatientRecord noMalignancyRecord = PreviousTumorTestFactory.withPriorSecondPrimaries(priorSecondPrimaries);

        assertEvaluation(EvaluationResult.FAIL, anyMalignancy.evaluate(noMalignancyRecord));
        assertEvaluation(EvaluationResult.FAIL, specificDoid.evaluate(noMalignancyRecord));
        assertEvaluation(EvaluationResult.FAIL, specificDoidInactive.evaluate(noMalignancyRecord));

        priorSecondPrimaries.add(withDoidAndActive("300", true));
        PatientRecord oneMalignancyRecord = PreviousTumorTestFactory.withPriorSecondPrimaries(priorSecondPrimaries);
        assertEvaluation(EvaluationResult.PASS, anyMalignancy.evaluate(oneMalignancyRecord));
        assertEvaluation(EvaluationResult.FAIL, specificDoid.evaluate(oneMalignancyRecord));
        assertEvaluation(EvaluationResult.FAIL, specificDoidInactive.evaluate(oneMalignancyRecord));

        priorSecondPrimaries.add(withDoidAndActive("200", true));
        PatientRecord twoMalignancyRecords = PreviousTumorTestFactory.withPriorSecondPrimaries(priorSecondPrimaries);
        assertEvaluation(EvaluationResult.PASS, anyMalignancy.evaluate(twoMalignancyRecords));
        assertEvaluation(EvaluationResult.PASS, specificDoid.evaluate(twoMalignancyRecords));
        assertEvaluation(EvaluationResult.FAIL, specificDoidInactive.evaluate(twoMalignancyRecords));

        priorSecondPrimaries.add(withDoidAndActive("200", false));
        PatientRecord threeMalignancyRecords = PreviousTumorTestFactory.withPriorSecondPrimaries(priorSecondPrimaries);
        assertEvaluation(EvaluationResult.PASS, anyMalignancy.evaluate(threeMalignancyRecords));
        assertEvaluation(EvaluationResult.PASS, specificDoid.evaluate(threeMalignancyRecords));
        assertEvaluation(EvaluationResult.PASS, specificDoidInactive.evaluate(threeMalignancyRecords));
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