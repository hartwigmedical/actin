package com.hartwig.actin.algo.evaluation.tumor;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.doid.DoidModel;
import com.hartwig.actin.algo.doid.TestDoidModelFactory;

import org.junit.Test;

public class PrimaryTumorLocationBelongsToDoidTest {

    @Test
    public void canEvaluateNonExclusiveNonExact() {
        DoidModel doidModel = TestDoidModelFactory.createWithOneParentChild("100", "200");

        PrimaryTumorLocationBelongsToDoid function100 = new PrimaryTumorLocationBelongsToDoid(doidModel, "100", false, false);

        assertEvaluation(EvaluationResult.PASS, function100.evaluate(TumorTestFactory.withDoids("100")));
        assertEvaluation(EvaluationResult.PASS, function100.evaluate(TumorTestFactory.withDoids("200")));
        assertEvaluation(EvaluationResult.PASS, function100.evaluate(TumorTestFactory.withDoids("10", "100")));
        assertEvaluation(EvaluationResult.FAIL, function100.evaluate(TumorTestFactory.withDoids("50", "250")));
        assertEvaluation(EvaluationResult.UNDETERMINED, function100.evaluate(TumorTestFactory.withDoids((List<String>) null)));

        PrimaryTumorLocationBelongsToDoid function200 = new PrimaryTumorLocationBelongsToDoid(doidModel, "200", false, false);

        assertEvaluation(EvaluationResult.FAIL, function200.evaluate(TumorTestFactory.withDoids("100")));
        assertEvaluation(EvaluationResult.PASS, function200.evaluate(TumorTestFactory.withDoids("200")));
        assertEvaluation(EvaluationResult.FAIL, function200.evaluate(TumorTestFactory.withDoids("10", "100")));
        assertEvaluation(EvaluationResult.FAIL, function200.evaluate(TumorTestFactory.withDoids("50", "250")));
        assertEvaluation(EvaluationResult.UNDETERMINED, function200.evaluate(TumorTestFactory.withDoids(Lists.newArrayList())));
    }

    @Test
    public void canEvaluateExclusiveNonExact() {
        DoidModel doidModel = TestDoidModelFactory.createWithOneParentChild("100", "200");

        PrimaryTumorLocationBelongsToDoid function100 = new PrimaryTumorLocationBelongsToDoid(doidModel, "100", true, false);

        assertEvaluation(EvaluationResult.PASS, function100.evaluate(TumorTestFactory.withDoids("100")));
        assertEvaluation(EvaluationResult.PASS, function100.evaluate(TumorTestFactory.withDoids("200")));
        assertEvaluation(EvaluationResult.FAIL, function100.evaluate(TumorTestFactory.withDoids("10", "100")));
        assertEvaluation(EvaluationResult.FAIL, function100.evaluate(TumorTestFactory.withDoids("50", "250")));
        assertEvaluation(EvaluationResult.UNDETERMINED, function100.evaluate(TumorTestFactory.withDoids((List<String>) null)));

        PrimaryTumorLocationBelongsToDoid function200 = new PrimaryTumorLocationBelongsToDoid(doidModel, "200", true, false);

        assertEvaluation(EvaluationResult.FAIL, function200.evaluate(TumorTestFactory.withDoids("100")));
        assertEvaluation(EvaluationResult.PASS, function200.evaluate(TumorTestFactory.withDoids("200")));
        assertEvaluation(EvaluationResult.FAIL, function200.evaluate(TumorTestFactory.withDoids("10", "100")));
        assertEvaluation(EvaluationResult.FAIL, function200.evaluate(TumorTestFactory.withDoids("50", "250")));
        assertEvaluation(EvaluationResult.UNDETERMINED, function200.evaluate(TumorTestFactory.withDoids(Lists.newArrayList())));
    }

    @Test
    public void canEvaluateNonExclusiveExact() {
        DoidModel doidModel = TestDoidModelFactory.createWithOneParentChild("100", "200");

        PrimaryTumorLocationBelongsToDoid function100 = new PrimaryTumorLocationBelongsToDoid(doidModel, "100", false, true);

        assertEvaluation(EvaluationResult.PASS, function100.evaluate(TumorTestFactory.withDoids("100")));
        assertEvaluation(EvaluationResult.FAIL, function100.evaluate(TumorTestFactory.withDoids("200")));
        assertEvaluation(EvaluationResult.PASS, function100.evaluate(TumorTestFactory.withDoids("10", "100")));
        assertEvaluation(EvaluationResult.FAIL, function100.evaluate(TumorTestFactory.withDoids("50", "250")));
        assertEvaluation(EvaluationResult.UNDETERMINED, function100.evaluate(TumorTestFactory.withDoids((List<String>) null)));

        PrimaryTumorLocationBelongsToDoid function200 = new PrimaryTumorLocationBelongsToDoid(doidModel, "200", false, true);

        assertEvaluation(EvaluationResult.FAIL, function200.evaluate(TumorTestFactory.withDoids("100")));
        assertEvaluation(EvaluationResult.PASS, function200.evaluate(TumorTestFactory.withDoids("200")));
        assertEvaluation(EvaluationResult.FAIL, function200.evaluate(TumorTestFactory.withDoids("10", "100")));
        assertEvaluation(EvaluationResult.FAIL, function200.evaluate(TumorTestFactory.withDoids("50", "250")));
        assertEvaluation(EvaluationResult.UNDETERMINED, function200.evaluate(TumorTestFactory.withDoids(Lists.newArrayList())));
    }

    @Test
    public void canEvaluateExclusiveExact() {
        DoidModel doidModel = TestDoidModelFactory.createWithOneParentChild("100", "200");

        PrimaryTumorLocationBelongsToDoid function100 = new PrimaryTumorLocationBelongsToDoid(doidModel, "100", true, true);

        assertEvaluation(EvaluationResult.PASS, function100.evaluate(TumorTestFactory.withDoids("100")));
        assertEvaluation(EvaluationResult.FAIL, function100.evaluate(TumorTestFactory.withDoids("200")));
        assertEvaluation(EvaluationResult.FAIL, function100.evaluate(TumorTestFactory.withDoids("10", "100")));
        assertEvaluation(EvaluationResult.FAIL, function100.evaluate(TumorTestFactory.withDoids("50", "250")));
        assertEvaluation(EvaluationResult.UNDETERMINED, function100.evaluate(TumorTestFactory.withDoids((List<String>) null)));

        PrimaryTumorLocationBelongsToDoid function200 = new PrimaryTumorLocationBelongsToDoid(doidModel, "200", true, true);

        assertEvaluation(EvaluationResult.FAIL, function200.evaluate(TumorTestFactory.withDoids("100")));
        assertEvaluation(EvaluationResult.PASS, function200.evaluate(TumorTestFactory.withDoids("200")));
        assertEvaluation(EvaluationResult.FAIL, function200.evaluate(TumorTestFactory.withDoids("10", "100")));
        assertEvaluation(EvaluationResult.FAIL, function200.evaluate(TumorTestFactory.withDoids("50", "250")));
        assertEvaluation(EvaluationResult.UNDETERMINED, function200.evaluate(TumorTestFactory.withDoids(Lists.newArrayList())));
    }
}