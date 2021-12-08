package com.hartwig.actin.algo.evaluation.treatment;

import static org.junit.Assert.assertEquals;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.clinical.datamodel.PriorTumorTreatment;

import org.junit.Test;

public class HasHadDrugTest {

    @Test
    public void canEvaluate() {
        HasHadDrug function = new HasHadDrug("drug 1");

        // Empty list
        List<PriorTumorTreatment> priorTumorTreatments = Lists.newArrayList();
        assertEquals(Evaluation.FAIL, function.evaluate(TreatmentEvaluationTestUtil.withPriorTumorTreatments(priorTumorTreatments)));

        // Add wrong drug
        priorTumorTreatments.add(TreatmentEvaluationTestUtil.builder().name("drug 2").build());
        assertEquals(Evaluation.FAIL, function.evaluate(TreatmentEvaluationTestUtil.withPriorTumorTreatments(priorTumorTreatments)));

        // Add correct drug
        priorTumorTreatments.add(TreatmentEvaluationTestUtil.builder().name("drug 1").build());
        assertEquals(Evaluation.PASS, function.evaluate(TreatmentEvaluationTestUtil.withPriorTumorTreatments(priorTumorTreatments)));
    }
}