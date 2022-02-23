package com.hartwig.actin.algo.evaluation.treatment;

import static org.junit.Assert.assertEquals;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.clinical.datamodel.PriorTumorTreatment;

import org.junit.Test;

public class HasHadFluoropyrimidineTreatmentTest {

    @Test
    public void canEvaluate() {
        HasHadFluoropyrimidineTreatment function = new HasHadFluoropyrimidineTreatment();

        // Empty list
        List<PriorTumorTreatment> priorTumorTreatments = Lists.newArrayList();
        assertEquals(EvaluationResult.FAIL,
                function.evaluate(TreatmentTestFactory.withPriorTumorTreatments(priorTumorTreatments)).result());

        // Add a random non-fluoropyrimidine treatment
        priorTumorTreatments.add(TreatmentTestFactory.builder().name("some random treatment").build());
        assertEquals(EvaluationResult.FAIL,
                function.evaluate(TreatmentTestFactory.withPriorTumorTreatments(priorTumorTreatments)).result());

        // Add a random fluoropyrimidine treatment
        String firstValidTreatment = HasHadFluoropyrimidineTreatment.FLUOROPYRIMIDINE_TREATMENTS.iterator().next();
        priorTumorTreatments.add(TreatmentTestFactory.builder().name(firstValidTreatment).build());
        assertEquals(EvaluationResult.PASS,
                function.evaluate(TreatmentTestFactory.withPriorTumorTreatments(priorTumorTreatments)).result());
    }
}