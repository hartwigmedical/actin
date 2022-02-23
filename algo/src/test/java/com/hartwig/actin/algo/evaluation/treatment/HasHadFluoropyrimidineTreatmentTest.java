package com.hartwig.actin.algo.evaluation.treatment;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

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
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TreatmentTestFactory.withPriorTumorTreatments(priorTumorTreatments)));

        // Add a random non-fluoropyrimidine treatment
        priorTumorTreatments.add(TreatmentTestFactory.builder().name("some random treatment").build());
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TreatmentTestFactory.withPriorTumorTreatments(priorTumorTreatments)));

        // Add a random fluoropyrimidine treatment
        String firstValidTreatment = HasHadFluoropyrimidineTreatment.FLUOROPYRIMIDINE_TREATMENTS.iterator().next();
        priorTumorTreatments.add(TreatmentTestFactory.builder().name(firstValidTreatment).build());
        assertEvaluation(EvaluationResult.PASS, function.evaluate(TreatmentTestFactory.withPriorTumorTreatments(priorTumorTreatments)));
    }
}