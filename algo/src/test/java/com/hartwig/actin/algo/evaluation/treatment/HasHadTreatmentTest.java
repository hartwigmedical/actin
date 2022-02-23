package com.hartwig.actin.algo.evaluation.treatment;

import static org.junit.Assert.assertEquals;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.clinical.datamodel.PriorTumorTreatment;

import org.junit.Test;

public class HasHadTreatmentTest {

    @Test
    public void canEvaluate() {
        HasHadTreatment function = new HasHadTreatment("treatment 1");

        // Empty list
        List<PriorTumorTreatment> priorTumorTreatments = Lists.newArrayList();
        assertEquals(EvaluationResult.FAIL,
                function.evaluate(TreatmentTestFactory.withPriorTumorTreatments(priorTumorTreatments)).result());

        // Add wrong treatment
        priorTumorTreatments.add(TreatmentTestFactory.builder().name("treatment 2").build());
        assertEquals(EvaluationResult.FAIL,
                function.evaluate(TreatmentTestFactory.withPriorTumorTreatments(priorTumorTreatments)).result());

        // Add correct treatment
        priorTumorTreatments.add(TreatmentTestFactory.builder().name("treatment 1").build());
        assertEquals(EvaluationResult.PASS,
                function.evaluate(TreatmentTestFactory.withPriorTumorTreatments(priorTumorTreatments)).result());
    }
}