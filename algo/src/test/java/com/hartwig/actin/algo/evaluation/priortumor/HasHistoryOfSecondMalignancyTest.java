package com.hartwig.actin.algo.evaluation.priortumor;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.doid.DoidModel;
import com.hartwig.actin.algo.doid.TestDoidModelFactory;
import com.hartwig.actin.clinical.datamodel.PriorSecondPrimary;

import org.junit.Test;

public class HasHistoryOfSecondMalignancyTest {

    @Test
    public void canEvaluate() {
        DoidModel doidModel = TestDoidModelFactory.createWithOneParentChild("100", "200");

        HasHistoryOfSecondMalignancy anyMalignancy = new HasHistoryOfSecondMalignancy(doidModel, null, false);
        HasHistoryOfSecondMalignancy specificDoid = new HasHistoryOfSecondMalignancy(doidModel, "100", false);
        HasHistoryOfSecondMalignancy specificDoidInactive = new HasHistoryOfSecondMalignancy(doidModel, "100", true);

        // No prior second primaries.
        List<PriorSecondPrimary> priorSecondPrimaries = Lists.newArrayList();
        PatientRecord noMalignancyRecord = PriorTumorTestFactory.withPriorSecondPrimaries(priorSecondPrimaries);
        assertEvaluation(EvaluationResult.FAIL, anyMalignancy.evaluate(noMalignancyRecord));
        assertEvaluation(EvaluationResult.FAIL, specificDoid.evaluate(noMalignancyRecord));
        assertEvaluation(EvaluationResult.FAIL, specificDoidInactive.evaluate(noMalignancyRecord));

        // One second primary with no relevant doid.
        priorSecondPrimaries.add(PriorTumorTestFactory.builder().addDoids("300").isActive(true).build());
        PatientRecord oneMalignancyRecord = PriorTumorTestFactory.withPriorSecondPrimaries(priorSecondPrimaries);
        assertEvaluation(EvaluationResult.PASS, anyMalignancy.evaluate(oneMalignancyRecord));
        assertEvaluation(EvaluationResult.FAIL, specificDoid.evaluate(oneMalignancyRecord));
        assertEvaluation(EvaluationResult.FAIL, specificDoidInactive.evaluate(oneMalignancyRecord));

        // Add one active second primary with child doid
        priorSecondPrimaries.add(PriorTumorTestFactory.builder().addDoids("200").isActive(true).build());
        PatientRecord twoMalignancyRecords = PriorTumorTestFactory.withPriorSecondPrimaries(priorSecondPrimaries);
        assertEvaluation(EvaluationResult.PASS, anyMalignancy.evaluate(twoMalignancyRecords));
        assertEvaluation(EvaluationResult.PASS, specificDoid.evaluate(twoMalignancyRecords));
        assertEvaluation(EvaluationResult.FAIL, specificDoidInactive.evaluate(twoMalignancyRecords));

        // Add one inactive second primary with child doid
        priorSecondPrimaries.add(PriorTumorTestFactory.builder().addDoids("200").isActive(false).build());
        PatientRecord threeMalignancyRecords = PriorTumorTestFactory.withPriorSecondPrimaries(priorSecondPrimaries);
        assertEvaluation(EvaluationResult.PASS, anyMalignancy.evaluate(threeMalignancyRecords));
        assertEvaluation(EvaluationResult.PASS, specificDoid.evaluate(threeMalignancyRecords));
        assertEvaluation(EvaluationResult.PASS, specificDoidInactive.evaluate(threeMalignancyRecords));
    }
}