package com.hartwig.actin.algo.evaluation.treatment;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.clinical.datamodel.PriorTumorTreatment;

import org.junit.Test;

public class HasHadLimitedSystemicTreatmentsTest {

    @Test
    public void canEvaluate() {
        HasHadLimitedSystemicTreatments function = new HasHadLimitedSystemicTreatments(1);

        // Empty list
        List<PriorTumorTreatment> priorTumorTreatments = Lists.newArrayList();
        assertEvaluation(EvaluationResult.PASS, function.evaluate(TreatmentTestFactory.withPriorTumorTreatments(priorTumorTreatments)));

        // Add one non-systemic
        priorTumorTreatments.add(TreatmentTestFactory.builder().isSystemic(false).build());
        assertEvaluation(EvaluationResult.PASS, function.evaluate(TreatmentTestFactory.withPriorTumorTreatments(priorTumorTreatments)));

        // Add one systemic
        priorTumorTreatments.add(TreatmentTestFactory.builder().isSystemic(true).build());
        assertEvaluation(EvaluationResult.PASS, function.evaluate(TreatmentTestFactory.withPriorTumorTreatments(priorTumorTreatments)));

        // Add one more systemic
        priorTumorTreatments.add(TreatmentTestFactory.builder().isSystemic(true).build());
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TreatmentTestFactory.withPriorTumorTreatments(priorTumorTreatments)));
    }
}