package com.hartwig.actin.algo.evaluation.treatment;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.clinical.datamodel.PriorTumorTreatment;
import com.hartwig.actin.clinical.datamodel.TreatmentCategory;

import org.junit.Test;

public class HasHadImmunotherapyTreatmentTest {

    @Test
    public void canEvaluate() {
        HasHadImmunotherapyTreatment function = new HasHadImmunotherapyTreatment();

        // Empty list
        List<PriorTumorTreatment> priorTumorTreatments = Lists.newArrayList();
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TreatmentTestFactory.withPriorTumorTreatments(priorTumorTreatments)));

        // Add one non-immunotherapy
        priorTumorTreatments.add(TreatmentTestFactory.builder().addCategories(TreatmentCategory.RADIOTHERAPY).build());
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TreatmentTestFactory.withPriorTumorTreatments(priorTumorTreatments)));

        // Add one immuno/radiotherapy
        priorTumorTreatments.add(TreatmentTestFactory.builder()
                .addCategories(TreatmentCategory.RADIOTHERAPY, TreatmentCategory.IMMUNOTHERAPY)
                .build());
        assertEvaluation(EvaluationResult.PASS, function.evaluate(TreatmentTestFactory.withPriorTumorTreatments(priorTumorTreatments)));
    }
}