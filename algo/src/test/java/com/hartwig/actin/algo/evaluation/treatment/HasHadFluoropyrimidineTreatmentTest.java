package com.hartwig.actin.algo.evaluation.treatment;

import static org.junit.Assert.assertEquals;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.clinical.datamodel.PriorTumorTreatment;

import org.junit.Test;

public class HasHadFluoropyrimidineTreatmentTest {

    @Test
    public void canEvaluate() {
        HasHadFluoropyrimidineTreatment function = new HasHadFluoropyrimidineTreatment();

        // Empty list
        List<PriorTumorTreatment> priorTumorTreatments = Lists.newArrayList();
        assertEquals(Evaluation.FAIL, function.evaluate(TreatmentEvaluationTestUtil.withPriorTumorTreatments(priorTumorTreatments)));

        // Add a random non-fluoropyrimidine treatment
        priorTumorTreatments.add(TreatmentEvaluationTestUtil.builder().name("some random treatment").build());
        assertEquals(Evaluation.FAIL, function.evaluate(TreatmentEvaluationTestUtil.withPriorTumorTreatments(priorTumorTreatments)));

        // Add a random fluoropyrimidine treatment
        String firstValidTreatment = HasHadFluoropyrimidineTreatment.FLUOROPYRIMIDINE_TREATMENTS.iterator().next();
        priorTumorTreatments.add(TreatmentEvaluationTestUtil.builder().name(firstValidTreatment).build());
        assertEquals(Evaluation.PASS, function.evaluate(TreatmentEvaluationTestUtil.withPriorTumorTreatments(priorTumorTreatments)));
    }
}