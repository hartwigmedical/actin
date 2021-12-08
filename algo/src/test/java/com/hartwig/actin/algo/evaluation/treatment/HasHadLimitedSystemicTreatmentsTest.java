package com.hartwig.actin.algo.evaluation.treatment;

import static org.junit.Assert.assertEquals;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.clinical.datamodel.PriorTumorTreatment;

import org.junit.Test;

public class HasHadLimitedSystemicTreatmentsTest {

    @Test
    public void canEvaluate() {
        HasHadLimitedSystemicTreatments function = new HasHadLimitedSystemicTreatments(1);

        // Empty list
        List<PriorTumorTreatment> priorTumorTreatments = Lists.newArrayList();
        assertEquals(Evaluation.PASS, function.evaluate(TreatmentEvaluationTestUtil.withPriorTumorTreatments(priorTumorTreatments)));

        // Add one non-systemic
        priorTumorTreatments.add(TreatmentEvaluationTestUtil.builder().isSystemic(false).build());
        assertEquals(Evaluation.PASS, function.evaluate(TreatmentEvaluationTestUtil.withPriorTumorTreatments(priorTumorTreatments)));

        // Add one systemic
        priorTumorTreatments.add(TreatmentEvaluationTestUtil.builder().isSystemic(true).build());
        assertEquals(Evaluation.PASS, function.evaluate(TreatmentEvaluationTestUtil.withPriorTumorTreatments(priorTumorTreatments)));

        // Add one more systemic
        priorTumorTreatments.add(TreatmentEvaluationTestUtil.builder().isSystemic(true).build());
        assertEquals(Evaluation.FAIL, function.evaluate(TreatmentEvaluationTestUtil.withPriorTumorTreatments(priorTumorTreatments)));
    }
}